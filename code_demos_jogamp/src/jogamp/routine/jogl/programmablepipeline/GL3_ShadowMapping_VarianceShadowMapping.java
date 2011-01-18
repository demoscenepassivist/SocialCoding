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
 ** GLSL based depth texture soft shadow mapping with "Variance Shadow Mapping", using chebyshev
 ** probabilist prediction (mean and variance) to approximate a shadow penumbra. The intention was
 ** to work around the massive performance hit of "Percentage Closer Filtering", but maintain or even
 ** surpass PCF quality wise. I'm not quite happy with the result of this routine. The generated
 ** softshadows have a lot of really "strange" artifacts, though I can't find any serious flaw 
 ** in the implementation. The code is largely inspired by Fabien Sanglard's "Soft shadows with VSM"
 ** blogpost/tutorial wich can be found here: "http://www.fabiensanglard.net/shadowmappingVSM/index.php".
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=0pI2Qq5_uto
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

public class GL3_ShadowMapping_VarianceShadowMapping extends BaseRoutineAdapter implements BaseRoutineInterface {

    private float SHADOWMAP_RATIO = 1.00f;
    private float BLUR_RATIO = 0.5f;
    private int mVariancePassShaderID;
    private int mDepthStorePassShaderID;
    private int mBlurPassShaderID;
    private int mDepthTextureID;
    private int mColorTextureID;
    private int mFrameBufferID;
    private int mBlurFrameBufferID;
    private int mBlurFrameBufferColorTextureID;
    private float mCameraPosition[] = {100.0f, 150.0f, 150.0f, 1.0f};
    private float mCameraLookAt[] = {0.0f,0.0f,0.0f};
    private float mLightPosition[] = {150.0f, 100.0f, 150.0f, 1.0f};
    private float mLightLookAt[] = {0.0f,0.0f,0.0f};    
    private float mLightMovementCircleRadius = 275.0f;    
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

    private void loadShadowShader(GL2 inGL) {
        int tVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/shadowshaders/varianceshadowmapping_variancepass.vs");
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/shadowshaders/varianceshadowmapping_variancepass.fs");
        mVariancePassShaderID = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);
        tVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/shadowshaders/varianceshadowmapping_depthstorepass.vs");
        tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/shadowshaders/varianceshadowmapping_depthstorepass.fs");
        mDepthStorePassShaderID = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);
        tVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/shadowshaders/varianceshadowmapping_blurpass.vs");
        tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/shadowshaders/varianceshadowmapping_blurpass.fs");
        mBlurPassShaderID = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);
    }

    private void generateShadowFBO(GL2 inGL) {
        int tShadowMapWidth = (int)(BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOWMAP_RATIO);
        int tShadowMapHeight = (int)(BaseGlobalEnvironment.getInstance().getScreenHeight() * SHADOWMAP_RATIO);
        //setup depth FBO
        mDepthTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glBindTexture(GL_TEXTURE_2D, mDepthTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //remove artefact on the edges of the shadowmap ...
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        //better force GL_DEPTH_COMPONENT32 to improve numerical stability ... 
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, tShadowMapWidth, tShadowMapHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, null);
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        mColorTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glBindTexture(GL_TEXTURE_2D, mColorTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
        //remove artefact on the edges of the shadowmap ...
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, tShadowMapWidth, tShadowMapHeight, 0, GL_RGB, GL_FLOAT, null);
        inGL.glGenerateMipmap(GL_TEXTURE_2D);
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        //create a framebuffer object
        mFrameBufferID = FrameBufferObjectUtils.generateFrameBufferObjectID(inGL);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferID);
        //attach texture to FBO depth attachment point ...
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT ,GL_TEXTURE_2D, mDepthTextureID, 0);
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, mColorTextureID, 0);
        //check FBO status
        FrameBufferObjectUtils.isFrameBufferObjectComplete(inGL);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //setup blur FBO
        mBlurFrameBufferID = FrameBufferObjectUtils.generateFrameBufferObjectID(inGL);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mBlurFrameBufferID);
        mBlurFrameBufferColorTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glBindTexture(GL_TEXTURE_2D, mBlurFrameBufferColorTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        inGL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, (int)(tShadowMapWidth*BLUR_RATIO), (int)(tShadowMapHeight*BLUR_RATIO), 0, GL_RGB, GL_FLOAT, null);
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, mBlurFrameBufferColorTextureID, 0);
        //check FBO status
        FrameBufferObjectUtils.isFrameBufferObjectComplete(inGL);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void setupMatrices(GL2 inGL,GLU inGLU,float inPosX,float inPosY,float inPosZ,float inLookAtX,float inLookAtY,float inLookAtZ) {
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        //inGLU.gluPerspective(45.0f,(((float)BaseGlobalEnvironment.getInstance().getScreenWidth())/((float)BaseGlobalEnvironment.getInstance().getScreenHeight())),10.0f,2500.0f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS()*100.0f));
        inGLU.gluPerspective(45.0f,(((float)BaseGlobalEnvironment.getInstance().getScreenWidth())/((float)BaseGlobalEnvironment.getInstance().getScreenHeight())),10.0f, 500.0f);
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

    private void generateBlurShadowMap(GL2 inGL) {
        //bluring the shadow map horinzontally ...
        inGL.glBindFramebuffer(GL_FRAMEBUFFER,mBlurFrameBufferID);
        inGL.glViewport(0,0,(int)(BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOWMAP_RATIO *BLUR_RATIO) ,(int)(BaseGlobalEnvironment.getInstance().getScreenHeight()* SHADOWMAP_RATIO*BLUR_RATIO));
        inGL.glUseProgram(mBlurPassShaderID);
        ShaderUtils.setUniform2fv(inGL,mBlurPassShaderID,"scale",FloatBuffer.wrap(new float[] {1.0f/ (BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOWMAP_RATIO * BLUR_RATIO),0.0f}));
        ShaderUtils.setUniform1i(inGL,mBlurPassShaderID,"sampler",0);
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glBindTexture(GL_TEXTURE_2D,mColorTextureID);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(-BaseGlobalEnvironment.getInstance().getScreenWidth()/2,BaseGlobalEnvironment.getInstance().getScreenWidth()/2,-BaseGlobalEnvironment.getInstance().getScreenHeight()/2,BaseGlobalEnvironment.getInstance().getScreenHeight()/2,1,20);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glTranslated(0,0,-5);
        //draw horizontal blur billboard ...
        inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2d(0,0); inGL.glVertex3f(-BaseGlobalEnvironment.getInstance().getScreenWidth()/2,-BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
            inGL.glTexCoord2d(1,0); inGL.glVertex3f(BaseGlobalEnvironment.getInstance().getScreenWidth()/2,-BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
            inGL.glTexCoord2d(1,1); inGL.glVertex3f(BaseGlobalEnvironment.getInstance().getScreenWidth()/2,BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
            inGL.glTexCoord2d(0,1); inGL.glVertex3f(-BaseGlobalEnvironment.getInstance().getScreenWidth()/2,BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
        inGL.glEnd();
        //draw vertical blur billboard ...
        inGL.glBindFramebuffer(GL_FRAMEBUFFER,mFrameBufferID); 
        inGL.glViewport(0,0,(int)(BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOWMAP_RATIO) ,(int)(BaseGlobalEnvironment.getInstance().getScreenHeight()* SHADOWMAP_RATIO));
        ShaderUtils.setUniform2fv(inGL,mBlurPassShaderID,"scale",FloatBuffer.wrap(new float[] {0.0f, 1.0f/ (BaseGlobalEnvironment.getInstance().getScreenHeight() * SHADOWMAP_RATIO)}));
        inGL.glBindTexture(GL_TEXTURE_2D,mBlurFrameBufferColorTextureID);
        inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2d(0,0); inGL.glVertex3f(-BaseGlobalEnvironment.getInstance().getScreenWidth()/2,-BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
            inGL.glTexCoord2d(1,0); inGL.glVertex3f(BaseGlobalEnvironment.getInstance().getScreenWidth()/2,-BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
            inGL.glTexCoord2d(1,1); inGL.glVertex3f(BaseGlobalEnvironment.getInstance().getScreenWidth()/2,BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
            inGL.glTexCoord2d(0,1); inGL.glVertex3f(-BaseGlobalEnvironment.getInstance().getScreenWidth()/2,BaseGlobalEnvironment.getInstance().getScreenHeight()/2,0);
        inGL.glEnd();
    }

    private void renderScene(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mLightPosition[0] = mLightMovementCircleRadius * (float)Math.cos(inFrameNumber/100.0f);
        mLightPosition[2] = mLightMovementCircleRadius * (float)Math.sin(inFrameNumber/100.0f); 
        //render from the light POV to a FBO, store depth
        //and ^2 the depth values using a floatingpoint rendertarget
        inGL.glBindFramebuffer(GL_FRAMEBUFFER,mFrameBufferID);
        inGL.glUseProgram(mDepthStorePassShaderID);      
        //modify the viewport to adjust to shadowmap resolution ...
        inGL.glViewport(0,0,(int)(BaseGlobalEnvironment.getInstance().getScreenWidth() * SHADOWMAP_RATIO),(int)(BaseGlobalEnvironment.getInstance().getScreenHeight()* SHADOWMAP_RATIO));
        inGL.glClear( GL_COLOR_BUFFER_BIT |  GL_DEPTH_BUFFER_BIT);
        setupMatrices(inGL,inGLU,mLightPosition[0],mLightPosition[1],mLightPosition[2],mLightLookAt[0],mLightLookAt[1],mLightLookAt[2]);
        //culling switching, rendering only backface, this is done to avoid self-shadowing
        inGL.glCullFace(GL_FRONT);
        drawObjects(inFrameNumber,inGL,inGLU,inGLUT);
        inGL.glGenerateMipmap(GL_TEXTURE_2D);
        //save modelview/projection matrice into texture7 and add a biais
        setTextureMatrix(inGL);
        generateBlurShadowMap(inGL);
        //---
        //render camera POV, use generated FBO for shadowmapping
        inGL.glBindFramebuffer(GL_FRAMEBUFFER,0);
        inGL.glViewport(0,0,(int)BaseGlobalEnvironment.getInstance().getScreenWidth(),(int)BaseGlobalEnvironment.getInstance().getScreenHeight());
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        inGL.glUseProgram(mVariancePassShaderID);      
        ShaderUtils.setUniform1i(inGL,mVariancePassShaderID,"sampler",7);
        ShaderUtils.setUniform1f(inGL,mVariancePassShaderID,"varianceoffset",0.00002f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_DF()/10000.0f);
        ShaderUtils.setUniform3fv(inGL,mVariancePassShaderID,"lightposition",FloatBuffer.wrap(mLightPosition));
        ShaderUtils.setUniform1f(inGL,mVariancePassShaderID,"specularexponent",128.0f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_GH()*10.0f));         
        inGL.glActiveTexture(GL_TEXTURE7);
        inGL.glBindTexture(GL_TEXTURE_2D,mColorTextureID);
        setupMatrices(inGL,inGLU,mCameraPosition[0],mCameraPosition[1],mCameraPosition[2],mCameraLookAt[0],mCameraLookAt[1],mCameraLookAt[2]);
        inGL.glLightfv(GL_LIGHT0, GL_POSITION, FloatBuffer.wrap(mLightPosition));
        inGL.glCullFace(GL_BACK);
        drawObjects(inFrameNumber,inGL,inGLU,inGLUT);
    }

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        generateShadowFBO(inGL);
        //generate the vertex and fragment shader to avoid some really stinky uniform bugs on ATI hardware ... :-(
        loadShadowShader(inGL);
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
        //inGL.glPushAttrib(GL_ALL_ATTRIB_BITS);
        //needed to populate the FBO's depthbuffer ...
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glClearColor(0.0f,0.0f,0.0f,1.0f);
        //inGL.glEnable(GL_CULL_FACE);
        //inGL.glHint(GL_PERSPECTIVE_CORRECTION_HINT,GL_NICEST);
        renderScene(inFrameNumber,inGL,inGLU,inGLUT);
        //inGL.glPopAttrib();
        inGL.glUseProgram(0);
        inGL.glActiveTexture(GL_TEXTURE0);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mVariancePassShaderID);
        inGL.glDeleteShader(mDepthStorePassShaderID);
        inGL.glDeleteShader(mBlurPassShaderID);
        inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
        inGL.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mFrameBufferID));
        inGL.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mBlurFrameBufferID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mDepthTextureID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mColorTextureID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mBlurFrameBufferColorTextureID));
    }

}
