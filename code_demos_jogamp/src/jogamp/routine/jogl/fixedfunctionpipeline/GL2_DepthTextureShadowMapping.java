package jogamp.routine.jogl.fixedfunctionpipeline;

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
 ** Simple GL2-Profile routine demonstrating shadow mapping as proposed in the paper by 
 ** Lance Williams in 1978 in “Casting curved shadows on curved surfaces”. Instead of
 ** using a FBO this routine uses glCopyTexImage2D() to obtain a depth-texture from the
 ** rendered framebuffer (the oldskool way). For an impression how this routine looks
 ** like see here: http://www.youtube.com/watch?v=288_W7qFzY4
 **
 **/

import java.nio.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL2_DepthTextureShadowMapping extends BaseRoutineAdapter implements BaseRoutineInterface {

    //nice shadowmapping tutorial:
    //http://dalab.se.sjtu.edu.cn/~jietan/shadowMappingTutorial.html
    
    private boolean mShowShadowMap = false;
    private int mShadowTextureID;
    private FloatBuffer mAmbientLight = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.2f, 0.2f, 0.2f, 1.0f});
    private FloatBuffer mDiffuseLight = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.7f, 0.7f, 0.7f, 1.0f});  
    private FloatBuffer mLowAmbient = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.1f, 0.1f, 0.1f, 1.0f});
    private FloatBuffer mLowDiffuse = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.25f, 0.25f, 0.25f, 1.0f});
    private float mLightPos[] = {50.0f, 100.0f, 50.0f, 1.0f};
    private FloatBuffer mLightPosFloatBuffer = DirectBufferUtils.createDirectFloatBuffer(mLightPos);
    private float mCameraPos[] = {100.0f, 150.0f, 150.0f, 1.0f};
    private double mCameraZoom = 0.4;
    private FloatBuffer mLightModelview = DirectBufferUtils.createDirectFloatBuffer(16);
    private FloatBuffer mLightProjection = DirectBufferUtils.createDirectFloatBuffer(16);
    private FloatBuffer mMatrixBuffer = DirectBufferUtils.createDirectFloatBuffer(16); 
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mShadowTextureID = TextureUtils.generateTextureID(inGL);
    }
    
    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //if (inFrameNumber%250==0) {
        //  mShowShadowMap = mShowShadowMap^true;
        //}
        inGL.glClearColor(0.0f, 0.0f, 0.0f, 1.0f );
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glDepthFunc(GL_LEQUAL);
        inGL.glPolygonOffset(4.0f, 0.0f);
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glEnable(GL_LIGHTING);
        inGL.glEnable(GL_COLOR_MATERIAL);
        inGL.glEnable(GL_NORMALIZE);
        inGL.glEnable(GL_LIGHT0);
        inGL.glBindTexture(GL_TEXTURE_2D, mShadowTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);
        inGL.glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        inGL.glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        inGL.glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        inGL.glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        regenerateShadowMap(inFrameNumber,inGL,inGLU,inGLUT);
        renderScene(inFrameNumber,inGL,inGLU,inGLUT);
    }
    
    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        TextureUtils.deleteTextureID(inGL,mShadowTextureID);
    }

    private void regenerateShadowMap(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        float sceneBoundingRadius = 95.0f;
        float lightToSceneDistance = (float)Math.sqrt(mLightPos[0] * mLightPos[0] + mLightPos[1] * mLightPos[1] + mLightPos[2] * mLightPos[2]);
        float nearPlane = lightToSceneDistance - sceneBoundingRadius;
        float fieldOfView = (float)Math.toDegrees(2.0f * Math.atan(sceneBoundingRadius / lightToSceneDistance));
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGLU.gluPerspective(fieldOfView, 1.0f, nearPlane, nearPlane + (2.0f * sceneBoundingRadius));
        inGL.glGetFloatv(GL_PROJECTION_MATRIX, mLightProjection);
        //switch to light's point of view
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(
                mLightPos[0], mLightPos[1], mLightPos[2], 
                0.0f, 0.0f, 0.0f, 
                0.0f, 1.0f, 0.0f
        );
        inGL.glGetFloatv(GL_MODELVIEW_MATRIX, mLightModelview);
        inGL.glViewport(0, 0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
        //clear the depth buffer only
        inGL.glClear(GL_DEPTH_BUFFER_BIT);
        //diable unneccesary rendering - only depth values ...
        inGL.glShadeModel(GL_FLAT);
        inGL.glDisable(GL_LIGHTING);
        inGL.glDisable(GL_COLOR_MATERIAL);
        inGL.glDisable(GL_NORMALIZE);
        inGL.glColorMask(false, false, false, false);
        inGL.glEnable(GL_POLYGON_OFFSET_FILL);
        DrawModels(inFrameNumber,inGL,inGLU,inGLUT,false);
        //copy depth values into depth texture
        inGL.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0);
        //restore normal drawing state
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glEnable(GL_LIGHTING);
        inGL.glEnable(GL_COLOR_MATERIAL);
        inGL.glEnable(GL_NORMALIZE);
        inGL.glColorMask(true, true, true, true);
        inGL.glDisable(GL_POLYGON_OFFSET_FILL);
    }

    private void renderScene(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //track camera angle
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        if (BaseGlobalEnvironment.getInstance().getScreenWidth() > BaseGlobalEnvironment.getInstance().getScreenHeight()) {
            double ar = (double)BaseGlobalEnvironment.getInstance().getScreenWidth() / (double)BaseGlobalEnvironment.getInstance().getScreenHeight();
            inGL.glFrustum(-ar * mCameraZoom, ar * mCameraZoom, -mCameraZoom, mCameraZoom, 1.0, 1000.0);
        } else {
            double ar = (double)BaseGlobalEnvironment.getInstance().getScreenHeight() / (double)BaseGlobalEnvironment.getInstance().getScreenWidth();
            inGL.glFrustum(-mCameraZoom, mCameraZoom, -ar * mCameraZoom, ar * mCameraZoom, 1.0, 1000.0);
        }
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(
                  mCameraPos[0], mCameraPos[1], mCameraPos[2], 
                  0.0f, 0.0f, 0.0f, 
                  0.0f, 1.0f, 0.0f
        );
        inGL.glViewport(0, 0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
        //track light position
        inGL.glLightfv(GL_LIGHT0, GL_POSITION, mLightPosFloatBuffer);
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (mShowShadowMap) {
            //display shadow map for debug purposes
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            inGL.glMatrixMode(GL_TEXTURE);
            inGL.glPushMatrix();
            inGL.glLoadIdentity();
            inGL.glEnable(GL_TEXTURE_2D);
            inGL.glDisable(GL_LIGHTING);
            inGL.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
            inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
            //show the shadowMap at its actual size relative to window
            inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(-1.0f, -1.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(((float)BaseGlobalEnvironment.getInstance().getScreenWidth()/(float)BaseGlobalEnvironment.getInstance().getScreenWidth())*2.0f-1.0f, -1.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(((float)BaseGlobalEnvironment.getInstance().getScreenWidth()/(float)BaseGlobalEnvironment.getInstance().getScreenWidth())*2.0f-1.0f, ((float)BaseGlobalEnvironment.getInstance().getScreenHeight()/(float)BaseGlobalEnvironment.getInstance().getScreenHeight())*2.0f-1.0f);
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(-1.0f, ((float)BaseGlobalEnvironment.getInstance().getScreenHeight()/(float)BaseGlobalEnvironment.getInstance().getScreenHeight())*2.0f-1.0f);
            inGL.glEnd();
            inGL.glDisable(GL_TEXTURE_2D);
            inGL.glEnable(GL_LIGHTING);
            inGL.glPopMatrix();
            inGL.glMatrixMode(GL_PROJECTION);
            inGLU.gluPerspective(45.0f, 1.0f, 1.0f, 1000.0f);
            inGL.glMatrixMode(GL_MODELVIEW);
        } else {
            //ambient pass ...
            inGL.glLightfv(GL_LIGHT0, GL_AMBIENT, mLowAmbient);
            inGL.glLightfv(GL_LIGHT0, GL_DIFFUSE, mLowDiffuse);
            //draw objects in the scene, including base plane
            DrawModels(inFrameNumber,inGL,inGLU,inGLUT,true);
            //enable alpha test so that shadowed fragments are discarded
            inGL.glAlphaFunc(GL_GREATER, 0.9f);
            inGL.glEnable(GL_ALPHA_TEST);
            inGL.glLightfv(GL_LIGHT0, GL_AMBIENT, mAmbientLight);
            inGL.glLightfv(GL_LIGHT0, GL_DIFFUSE, mDiffuseLight);
            //set up shadow comparison
            inGL.glEnable(GL_TEXTURE_2D);
            inGL.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
            inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
            //set up the eye plane for projecting the shadow map on the scene
            inGL.glEnable(GL_TEXTURE_GEN_S);
            inGL.glEnable(GL_TEXTURE_GEN_T);
            inGL.glEnable(GL_TEXTURE_GEN_R);
            inGL.glEnable(GL_TEXTURE_GEN_Q);
            generateTextureMatrix(inGL,inGLU,inGLUT);
            //draw objects in the scene, including base plane
            DrawModels(inFrameNumber,inGL,inGLU,inGLUT,true);
            inGL.glDisable(GL_ALPHA_TEST);
            inGL.glDisable(GL_TEXTURE_2D);
            inGL.glDisable(GL_TEXTURE_GEN_S);
            inGL.glDisable(GL_TEXTURE_GEN_T);
            inGL.glDisable(GL_TEXTURE_GEN_R);
            inGL.glDisable(GL_TEXTURE_GEN_Q);
        }
    }

    private void DrawModels(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT,boolean drawBasePlane) {
        if (drawBasePlane) {
            //draw plane that the objects rest on
            inGL.glColor3f(0.0f, 0.0f, 0.90f);
            inGL.glNormal3f(0.0f, 1.0f, 0.0f);
            inGL.glBegin(GL_QUADS);
            inGL.glVertex3f(-100.0f, -25.0f, -100.0f);
            inGL.glVertex3f(-100.0f, -25.0f, 100.0f);
            inGL.glVertex3f(100.0f,  -25.0f, 100.0f);
            inGL.glVertex3f(100.0f,  -25.0f, -100.0f);
            inGL.glEnd();
        }
        //red cube
        inGL.glColor3f(1.0f, 0.0f, 0.0f);
        inGL.glPushMatrix();
            inGL.glTranslatef(0.0f, 20.0f, 0.0f);
            inGL.glRotatef(inFrameNumber%360, 1.0f, 1.0f, 0.0f);
            inGLUT.glutSolidCube(48.0f);
        inGL.glPopMatrix();
        //green sphere
        inGL.glColor3f(0.0f, 1.0f, 0.0f);
        inGL.glPushMatrix();
            inGL.glTranslatef(-60.0f, 0.0f, 0.0f);
            inGLUT.glutSolidSphere(25.0f, 50, 50);
        inGL.glPopMatrix();
        //yellow cone
        inGL.glColor3f(1.0f, 1.0f, 0.0f);
        inGL.glPushMatrix();
            inGL.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
            inGL.glTranslatef(60.0f, 0.0f, -24.0f);
            inGLUT.glutSolidCone(25.0f, 50.0f, 50, 50);
        inGL.glPopMatrix();
        //magenta torus
        inGL.glColor3f(1.0f, 0.0f, 1.0f);
        inGL.glPushMatrix();
            inGL.glTranslatef(0.0f, 0.0f, 60.0f);
            inGL.glRotatef(inFrameNumber%360, 1.0f, 0.5f, 0.0f);
            inGLUT.glutSolidTorus(8.0f, 16.0f, 50, 50);
        inGL.glPopMatrix();
        //cyan octahedron
        inGL.glColor3f(0.0f, 1.0f, 1.0f);
        inGL.glPushMatrix();
            inGL.glTranslatef(0.0f, 0.0f, -60.0f);
            inGL.glScalef(25.0f, 25.0f, 25.0f);
            inGL.glRotatef(inFrameNumber%360, 1.0f, 1.0f, 0.0f);
            inGLUT.glutSolidOctahedron();
        inGL.glPopMatrix();
    }

    private void generateTextureMatrix(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //set up projective texture matrix. Use the GL_MODELVIEW matrix
        //stack and OpenGL matrix commands to make the matrix.
        float tSceneBoundingRadius = 95.0f;
        //save the depth precision for where it's useful
        float tLightToSceneDistance = (float)Math.sqrt(mLightPos[0] * mLightPos[0] + mLightPos[1] * mLightPos[1] + mLightPos[2] * mLightPos[2]);
        float tNearPlane = tLightToSceneDistance - tSceneBoundingRadius;
        //keep the scene filling the depth texture
        float tFieldOfView = (float)Math.toDegrees(2.0f * Math.atan(tSceneBoundingRadius / tLightToSceneDistance));
        inGL.glPushMatrix();
        inGL.glLoadIdentity();
        inGL.glTranslatef(0.5f, 0.5f, 0.5f);
        inGL.glScalef(0.5f, 0.5f, 0.5f);
        inGLU.gluPerspective(tFieldOfView, 1.0f, tNearPlane, tNearPlane + (2.0f * tSceneBoundingRadius));
        inGLU.gluLookAt(
                mLightPos[0], mLightPos[1], mLightPos[2], 
                0.0f, 0.0f, 0.0f, 
                0.0f, 1.0f, 0.0f
        );
        inGL.glGetFloatv(GL_MODELVIEW_MATRIX, mMatrixBuffer);
        inGL.glPopMatrix();
        transpose(mMatrixBuffer);           
        inGL.glTexGenfv(GL_S, GL_EYE_PLANE, mMatrixBuffer);
        mMatrixBuffer.position(4);
        inGL.glTexGenfv(GL_T, GL_EYE_PLANE, mMatrixBuffer);
        mMatrixBuffer.position(8);
        inGL.glTexGenfv(GL_R, GL_EYE_PLANE, mMatrixBuffer);
        mMatrixBuffer.position(12);
        inGL.glTexGenfv(GL_Q, GL_EYE_PLANE, mMatrixBuffer);
        mMatrixBuffer.rewind();
    }

    private void transpose(FloatBuffer inMatrix) {
        float tTempValue = inMatrix.get(1);
        inMatrix.put(1, inMatrix.get(4));
        inMatrix.put(4, tTempValue);
        tTempValue = inMatrix.get(2);
        inMatrix.put(2, inMatrix.get(8));
        inMatrix.put(8, tTempValue);
        tTempValue = inMatrix.get(3 );
        inMatrix.put(3, inMatrix.get(12));
        inMatrix.put(12, tTempValue);    
        tTempValue = inMatrix.get(6);
        inMatrix.put(6, inMatrix.get(9));
        inMatrix.put(9, tTempValue);    
        tTempValue = inMatrix.get(7);
        inMatrix.put(7, inMatrix.get(13));
        inMatrix.put(13, tTempValue);    
        tTempValue = inMatrix.get(11);
        inMatrix.put(11, inMatrix.get(14));
        inMatrix.put(14, tTempValue);
        inMatrix.rewind();
    }

}
