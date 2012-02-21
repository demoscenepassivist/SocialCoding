package jogamp.routine.jogl.programmablepipeline;

/**
 **   __ __|_  ___________________________________________________________________________  ___|__ __
 **  //    /\                                           _                                  /\    \\  
 ** //____/  \__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \____\\ 
 **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /  
 **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/   
 **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\     
 ** /  \____\                       http://jogamp.org  |_|                              /____/  \    
 ** \  /   "' _________________________________________________________________________ `"   \  /    
 **  \/____.                                                                             .____\/     
 **
 ** GLSL based depth texture soft shadow mapping using "Percentage Closer Filtering". The 
 ** routine is quite foreward. It takes several samples from a filter kernel and averages them.
 ** The result is some kind of "penumbra zone" wich is an attenuation of the aliasing. The bigger
 ** the kernel, the bigger the penumbra, the softer the shadows. The code is largely inspired
 ** by Fabien Sanglard's "Soft shadows with PCF" blogpost/tutorial wich can be found here: 
 ** "http://www.fabiensanglard.net/shadowmappingPCF/index.php". For an impression how this routine
 ** looks like see here: http://www.youtube.com/watch?v=yLqXZcD8FKg
 **
 ** Due to some nasty bug on my ATI Mobility Radeon HD 4500/5100 I had to dynamically generate
 ** the vertex/fragment shaders and do mostly constants stuff in the fragment shader ... +<||:-)
 **
 **/

import java.nio.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_ShadowMapping_PercentageCloserFiltering extends BaseRoutineAdapter implements BaseRoutineInterface {

    private static final float SHADOW_MAP_RATIO = 0.5f;
    private float mCameraPosition[] = {100.0f, 150.0f, 150.0f, 1.0f};
    private float mCameraLookAt[] = {0.0f,0.0f,0.0f};
    private float mLightPosition[] = {150.0f, 100.0f, 150.0f, 1.0f};
    private float mLightLookAt[] = {0.0f,0.0f,0.0f};    
    private float mLightMovementCircleRadius = 275.0f;
    private int mShadowFrameBufferID;
    private int mDepthTextureID;
    private int mLinkedShaderID;    
    private int mDisplayListStartID;
    private int mDisplayListSize;
    private DoubleBuffer mModelViewMatrix = DirectBufferUtils.createDirectDoubleBuffer(16); 
    private DoubleBuffer mProjectionMatrix = DirectBufferUtils.createDirectDoubleBuffer(16);
    //this is matrix transform every coordinate x,y,z ...
    //x = x* 0.5 + 0.5 
    //y = y* 0.5 + 0.5 
    //z = z* 0.5 + 0.5 
    //... moving from unit cube [-1,1] to [0,1]  
    private DoubleBuffer mUniCubeBiasMatrix = DirectBufferUtils.createDirectDoubleBuffer(new double[]{
        0.5, 0.0, 0.0, 0.0, 
        0.0, 0.5, 0.0, 0.0,
        0.0, 0.0, 0.5, 0.0,
        0.5, 0.5, 0.5, 1.0
    });

    private void generateShadowFBO(GL2 inGL) {
        int shadowMapWidth = (int)((float)BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOW_MAP_RATIO);
        int shadowMapHeight = (int)((float)BaseGlobalEnvironment.getInstance().getScreenHeight() * SHADOW_MAP_RATIO);
        //try to use a texture depth component
        mDepthTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glBindTexture(GL_TEXTURE_2D, mDepthTextureID);
        //use GL_LINEAR for really soft shadows ...
        //inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);    
        //remove artefact on the edges of the shadowmap
        inGL.glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP );
        inGL.glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP );
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);
        //no need to force GL_DEPTH_COMPONENT24, drivers usually give you the max precision if available 
        inGL.glTexImage2D( GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, shadowMapWidth, shadowMapHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, null);
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        //create a framebuffer object
        mShadowFrameBufferID = FrameBufferObjectUtils.generateFrameBufferObjectID(inGL);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mShadowFrameBufferID);
        //don't bind a color texture with the currently binded FBO
        inGL.glDrawBuffer(GL_NONE);
        inGL.glReadBuffer(GL_NONE);
        //attach the texture to FBO depth attachment point
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D, mDepthTextureID, 0);
        //check FBO status
        FrameBufferObjectUtils.isFrameBufferObjectComplete(inGL);
        //switch back to window-system-provided framebuffer
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void setupMatrices(GL2 inGL,GLU inGLU,float inPosX,float inPosY,float inPosZ,float inLookAtX,float inLookAtY,float inLookAtZ) {
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGLU.gluPerspective(45.0f,(((float)BaseGlobalEnvironment.getInstance().getScreenWidth())/((float)BaseGlobalEnvironment.getInstance().getScreenHeight())),10.0f,40000.0f);
        //inGLU.gluPerspective(45.0f,(((float)BaseGlobalEnvironment.getInstance().getScreenWidth())/((float)BaseGlobalEnvironment.getInstance().getScreenHeight())),2.0, 500.0);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(inPosX,inPosY,inPosZ,inLookAtX,inLookAtY,inLookAtZ,0,1,0);
    }

    private void setTextureMatrix(GL2 inGL) {
        //grab modelview and transformation matrices ...
        inGL.glGetDoublev(GL_MODELVIEW_MATRIX, mModelViewMatrix);
        inGL.glGetDoublev(GL_PROJECTION_MATRIX, mProjectionMatrix);
        inGL.glMatrixMode(GL_TEXTURE);
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glLoadIdentity();   
        inGL.glLoadMatrixd(mUniCubeBiasMatrix);
        //concatinating all matrice into one ...
        inGL.glMultMatrixd(mProjectionMatrix);
        inGL.glMultMatrixd(mModelViewMatrix);
        //go back to normal matrix mode ...
        inGL.glMatrixMode(GL_MODELVIEW);
    }

    private void drawObjects(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //during tranformation, we also have to maintain the GL_TEXTURE7, used in the shadow shader
        //to determine if a vertex is in the shadow.
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glEnable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glEnable(GL_DEPTH_TEST);
        //draw plane that the objects rest on
        inGL.glCallList(mDisplayListStartID+0); 
        //red cube
        inGL.glPushMatrix();
        inGL.glTranslatef(0.0f, 20.0f, 0.0f);
        inGL.glRotatef(inFrameNumber%360, 1.0f, 1.0f, 0.0f);
        inGL.glMatrixMode(GL_TEXTURE);
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glPushMatrix();
        inGL.glTranslatef(0.0f, 20.0f, 0.0f);
        inGL.glRotatef(inFrameNumber%360, 1.0f, 1.0f, 0.0f);
        inGL.glCallList(mDisplayListStartID+1); 
        inGL.glPopMatrix();
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glPopMatrix();
        //green sphere
        inGL.glPushMatrix();
        inGL.glTranslatef(-60.0f, 0.0f, 0.0f);
        inGL.glMatrixMode(GL_TEXTURE);
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glPushMatrix();
        inGL.glTranslatef(-60.0f, 0.0f, 0.0f);
        inGL.glCallList(mDisplayListStartID+2); 
        inGL.glPopMatrix();
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glPopMatrix();
        //yellow cone
        inGL.glPushMatrix();
        inGL.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        inGL.glTranslatef(60.0f, 0.0f, -24.0f);
        inGL.glMatrixMode(GL_TEXTURE);
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glPushMatrix();
        inGL.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        inGL.glTranslatef(60.0f, 0.0f, -24.0f);
        inGL.glCallList(mDisplayListStartID+3); 
        inGL.glPopMatrix();
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glPopMatrix();
        //magenta torus
        inGL.glPushMatrix();
        inGL.glTranslatef(0.0f, 0.0f, 60.0f);
        inGL.glRotatef(inFrameNumber%360, 1.0f, 0.5f, 0.0f);
        inGL.glMatrixMode(GL_TEXTURE);
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glPushMatrix();
        inGL.glTranslatef(0.0f, 0.0f, 60.0f);
        inGL.glRotatef(inFrameNumber%360, 1.0f, 0.5f, 0.0f);
        inGL.glCallList(mDisplayListStartID+4); 
        inGL.glPopMatrix();
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glPopMatrix();
        //cyan octahedron
        inGL.glPushMatrix();
        inGL.glTranslatef(0.0f, 0.0f, -60.0f);
        inGL.glScalef(25.0f, 25.0f, 25.0f);
        inGL.glRotatef(inFrameNumber%360, 1.0f, 1.0f, 0.0f);
        inGL.glMatrixMode(GL_TEXTURE);
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glPushMatrix();
        inGL.glTranslatef(0.0f, 0.0f, -60.0f);
        inGL.glScalef(25.0f, 25.0f, 25.0f);
        inGL.glRotatef(inFrameNumber%360, 1.0f, 1.0f, 0.0f);
        inGL.glCallList(mDisplayListStartID+5); 
        inGL.glPopMatrix();
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glPopMatrix();
     }

     private void renderScene(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
         mLightPosition[0] = mLightMovementCircleRadius * (float)Math.cos(inFrameNumber/100.0f);
         mLightPosition[2] = mLightMovementCircleRadius * (float)Math.sin(inFrameNumber/100.0f); 
         //render from the light POV to a FBO depth values only ...
         inGL.glBindFramebuffer(GL_FRAMEBUFFER,mShadowFrameBufferID);
         //using the fixed pipeline to render to the depthbuffer
         inGL.glUseProgram(0);
         //adjust viewport to match shadowmap tresolution ...
         inGL.glViewport(0,0,(int)((float)BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOW_MAP_RATIO),(int)((float)BaseGlobalEnvironment.getInstance().getScreenHeight() * SHADOW_MAP_RATIO));
         //clear depthbuffer ...
         inGL.glClear(GL_DEPTH_BUFFER_BIT);
         //disable color rendering, only zbuffer values ...
         inGL.glColorMask(false, false, false, false); 
         setupMatrices(inGL,inGLU,mLightPosition[0],mLightPosition[1],mLightPosition[2],mLightLookAt[0],mLightLookAt[1],mLightLookAt[2]);
         //culling switching, rendering only backfaces to avoid self-shadowing
         inGL.glCullFace(GL_FRONT);
         drawObjects(inFrameNumber,inGL,inGLU,inGLUT);
         //save modelview/projection matrice into texture7 and add bias ...
         setTextureMatrix(inGL);

         //--- Normal color render to framebuffer, camera POV ...
         inGL.glBindFramebuffer(GL_FRAMEBUFFER,0);
         inGL.glViewport(0,0,BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight());
         //reenabling color write ...
         inGL.glColorMask(true, true, true, true); 
         //clear color framebuffer
         inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         inGL.glValidateProgram(mLinkedShaderID);
         //apply shadowshader ...
         inGL.glUseProgram(mLinkedShaderID);
         ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"shadowmap",7);
         //ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"shadowoffset",0.05f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS()/1000.0f));
         ShaderUtils.setUniform3fv(inGL,mLinkedShaderID,"lightposition",FloatBuffer.wrap(mLightPosition));
         ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"shadowintensity",0.2f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_DF());
         ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"specularexponent",128.0f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_GH()*10.0f));         
         inGL.glActiveTexture(GL_TEXTURE7);
         inGL.glBindTexture(GL_TEXTURE_2D,mDepthTextureID);
         setupMatrices(inGL,inGLU,mCameraPosition[0],mCameraPosition[1],mCameraPosition[2],mCameraLookAt[0],mCameraLookAt[1],mCameraLookAt[2]);
         inGL.glCullFace(GL_BACK);
         drawObjects(inFrameNumber,inGL,inGLU,inGLUT);
         inGL.glUseProgram(0);
     }

     public String generatePCFVertexShader() {
         BaseLogging.getInstance().info("GENERATING PCF VERTEX SHADER CODE ...");
         StringBuilder tStringBuilder = new StringBuilder();
         tStringBuilder.append(
                 "varying vec4 shadowcoord;\n"+
                 "uniform vec3 lightposition;\n"+
                 "varying vec3 N, L;\n"+
                 "void main() {\n"+
                     //shadow matrix transformation    
                 "   shadowcoord = gl_TextureMatrix[7] * gl_Vertex;\n"+
                     //vertex MVP transform
                 "   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n"+
                 "   vec4 V = gl_ModelViewMatrix * gl_Vertex;\n"+
                     //eye-space normal
                 "   N = gl_NormalMatrix * gl_Normal;\n"+
                 "   L = lightposition - V.xyz;\n"+
                 "}\n"
         );
         BaseLogging.getInstance().info("GENERATED VERTEX SHADER CODE:\n"+tStringBuilder.toString());
         return tStringBuilder.toString();
     }
  
     public String generatePCFFragmentShader() {
         BaseLogging.getInstance().info("GENERATING PCF FRAGMENT SHADER CODE ...");
         StringBuilder tStringBuilder = new StringBuilder();
         tStringBuilder.append(       
                 "uniform sampler2DShadow shadowmap;\n"+
                 "uniform float specularexponent;\n"+
                 "uniform float shadowintensity;\n"+
                 "const float shadowoffset = 0.05;\n"+
                 "const vec3 lightcolor = vec3(1.0, 1.0, 1.0);\n"+
                 "varying vec3 N, L;\n"+
                 "varying vec4 shadowcoord;\n"+
                 "const float xpixeloffset = "+(1.0f/((float)BaseGlobalEnvironment.getInstance().getScreenWidth()*SHADOW_MAP_RATIO))+";\n"+
                 "const float ypixeloffset = "+(1.0f/((float)BaseGlobalEnvironment.getInstance().getScreenHeight()*SHADOW_MAP_RATIO))+";\n"+
                 "void main() {\n"+
                 "   float shadow = 0.0;\n"+               
                     //avoid counter shadow
                 "   if (shadowcoord.w > 1.0) {\n"
                 
                 );
                 float tPixelSumDivider = 0.0f;
                 for (float y = -3.5f ; y <=3.5f ; y+=1.0f) {
                     for (float x = -3.5f ; x <=3.5f ; x+=1.0f) {                        
                                                      //values are multiplied by ShadowCoord.w because shadow2DProj does a W division for us.
                         //tStringBuilder.append("      shadow += shadow2DProj(shadowmap, shadowcoord + vec4("+x+" * xpixeloffset * shadowcoord.w, "+y+" * ypixeloffset * shadowcoord.w, shadowoffset, 0.0) ).w;\n");
                         //FIX for strange ATI Radeon 6990M shadow2DProj issue: http://forums.amd.com/forum/messageview.cfm?catid=392&threadid=149243 
                         tStringBuilder.append("      shadow += textureProj(shadowmap, shadowcoord + vec4("+x+" * xpixeloffset * shadowcoord.w, "+y+" * ypixeloffset * shadowcoord.w, shadowoffset, 0.0) );\n");
                         tPixelSumDivider++;
                     }
                 }
          tStringBuilder.append("      shadow /= "+tPixelSumDivider+";\n"+
                 "   }\n"+
                 "   shadow += shadowintensity;\n"+
                     //---
                 "   vec3 NN = normalize(N);\n"+
                 "   vec4 fragmentcolor = vec4(0.0,0.0,0.0,1.0);\n"+
                 "   vec3 NL = normalize(L);\n"+
                 "   vec3 NH = normalize(NL + vec3(0.0, 0.0, 1.0));\n"+
                 "   float NdotL = max(0.0, dot(NN, NL));\n"+
                     //accumulate the diffuse contributions
                 "   fragmentcolor.rgb += gl_FrontMaterial.diffuse.rgb * lightcolor * NdotL;\n"+
                     //accumulate the specular contributions
                 "   if (NdotL > 0.0) {\n"+
                        //use lightcolor for specular ...
                        //fragmentcolor.rgb += lightcolor * pow(max(0.0, dot(NN, NH)), specularexponent);
                        //ignore lightcolor and use white lightcolor for specular ...
                 "      fragmentcolor.rgb += pow(max(0.0, dot(NN, NH)), specularexponent);\n"+
                 "   }\n"+
                 "   fragmentcolor.rgb *= shadow;\n"+
                 "   gl_FragColor = fragmentcolor;\n"+
                 "}\n"
         );
         BaseLogging.getInstance().info("GENERATED FRAGMENT SHADER CODE:\n"+tStringBuilder.toString());
         return tStringBuilder.toString();
     }
     
     public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
         generateShadowFBO(inGL);
         //generate the vertex and fragment shader to avoid some really stinky uniform bugs on ATI hardware ... :-(
         int tVertexShader = ShaderUtils.generateVertexShader(inGL,generatePCFVertexShader());         
         int tFragmentShader = ShaderUtils.generateFragmentShader(inGL,generatePCFFragmentShader());         
         mLinkedShaderID = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);
         //initialize the display lists ...
         mDisplayListSize = 6;
         mDisplayListStartID = inGL.glGenLists(mDisplayListSize);
         inGL.glNewList(mDisplayListStartID+0,GL_COMPILE);
             inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.90f}));
             inGL.glNormal3f(0.0f, 1.0f, 0.0f);
             inGL.glBegin(GL_QUADS);
             inGL.glVertex3f(-100.0f, -25.0f, -100.0f);
             inGL.glVertex3f(-100.0f, -25.0f, 100.0f);
             inGL.glVertex3f(100.0f,  -25.0f, 100.0f);
             inGL.glVertex3f(100.0f,  -25.0f, -100.0f);
             inGL.glEnd();
         inGL.glEndList();
         inGL.glNewList(mDisplayListStartID+1,GL_COMPILE);
             inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f, 0.0f, 0.0f}));
             inGLUT.glutSolidCube(48.0f);
         inGL.glEndList();
         inGL.glNewList(mDisplayListStartID+2,GL_COMPILE);
             inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 1.0f, 0.0f}));
             inGLUT.glutSolidSphere(25.0f, 50, 50);
         inGL.glEndList();
         inGL.glNewList(mDisplayListStartID+3,GL_COMPILE);
             inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f, 1.0f, 0.0f}));
             inGLUT.glutSolidCone(25.0f, 50.0f, 50, 50);
         inGL.glEndList();
         inGL.glNewList(mDisplayListStartID+4,GL_COMPILE);
             inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f, 0.0f, 1.0f}));
             inGLUT.glutSolidTorus(8.0f, 16.0f, 50, 50);
         inGL.glEndList();
         inGL.glNewList(mDisplayListStartID+5,GL_COMPILE);
             inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 1.0f, 1.0f}));
             inGLUT.glutSolidOctahedron();
         inGL.glEndList();
     }

     public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
         inGL.glPushAttrib(GL_ALL_ATTRIB_BITS);
         //needed to populate the FBO's depthbuffer ...
         inGL.glEnable(GL_DEPTH_TEST);
         inGL.glClearColor(0.0f,0.0f,0.0f,1.0f);
         inGL.glEnable(GL_CULL_FACE);
         inGL.glHint(GL_PERSPECTIVE_CORRECTION_HINT,GL_NICEST);
         renderScene(inFrameNumber,inGL,inGLU,inGLUT);
         inGL.glPopAttrib();
     }

     public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
         inGL.glDeleteShader(mLinkedShaderID);
         inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
         inGL.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mShadowFrameBufferID));
         inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mDepthTextureID));
     }

}
