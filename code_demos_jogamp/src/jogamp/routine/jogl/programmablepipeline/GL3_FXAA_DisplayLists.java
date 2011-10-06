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
 ** Evaluation routine demonstrating the combination of post-processing anti-aliasing using FXAA and
 ** variable FSAA (super sampling) on a simple "geometry heavy" example scene. For an impression how
 ** this routine looks like see here:
 ** http://copypastaresearch.tumblr.com/post/11101649574/continuing-with-my-experiments-regarding-anti
 **
 **/

import framework.base.*;
import framework.util.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_FXAA_DisplayLists extends BaseRoutineAdapter implements BaseRoutineInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_DisplayLists;
    protected GL3_InternalFBO_DisplayLists_RAW mGL3_InternalFBO_DisplayLists_RAW;
    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_FXAA;
    protected GL3_InternalFBO_FXAA mGL3_InternalFBO_FXAA;
    protected BaseSuperSamplingFBOWrapper mBaseSuperSamplingFBOWrapper;
    protected GL3_InternalFBO_SuperSampling mGL3_InternalFBO_SuperSampling;
    protected float mInternalSuperSamplingFactor = 2.0f;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mGL3_InternalFBO_DisplayLists_RAW = new GL3_InternalFBO_DisplayLists_RAW();
        mGL3_InternalFBO_FXAA = new GL3_InternalFBO_FXAA();
        mGL3_InternalFBO_SuperSampling = new GL3_InternalFBO_SuperSampling();
        mBaseSuperSamplingFBOWrapper = new BaseSuperSamplingFBOWrapper(mInternalSuperSamplingFactor,mGL3_InternalFBO_SuperSampling);
        mBaseSuperSamplingFBOWrapper.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_FXAA = new BaseFrameBufferObjectRendererExecutor(
                (int)(BaseGlobalEnvironment.getInstance().getScreenWidth()*mInternalSuperSamplingFactor),
                (int)(BaseGlobalEnvironment.getInstance().getScreenHeight()*mInternalSuperSamplingFactor),
                mGL3_InternalFBO_FXAA
        );
        mBaseFrameBufferObjectRendererExecutor_FXAA.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_DisplayLists = new BaseFrameBufferObjectRendererExecutor(
                (int)(BaseGlobalEnvironment.getInstance().getScreenWidth()*mInternalSuperSamplingFactor),
                (int)(BaseGlobalEnvironment.getInstance().getScreenHeight()*mInternalSuperSamplingFactor),
                mGL3_InternalFBO_DisplayLists_RAW
        );
        mBaseFrameBufferObjectRendererExecutor_DisplayLists.init(inGL,inGLU,inGLUT);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor_DisplayLists.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor_DisplayLists.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_FXAA.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor_FXAA.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper.executeToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor_DisplayLists.cleanup(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_FXAA.cleanup(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected class GL3_InternalFBO_SuperSampling implements BaseFrameBufferObjectRendererInterface {

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        }

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            inGL.glDisable(GL_LIGHTING);
            inGL.glDisable(GL_CULL_FACE);
            inGL.glDisable(GL_DEPTH_TEST);
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glOrtho(0, mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(), mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight(), 0, -1, 1);
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor_FXAA.getColorTextureID());
            //inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor_DisplayLists.getColorTextureID());
            inGL.glActiveTexture(GL_TEXTURE0);
            inGL.glEnable(GL_TEXTURE_2D);
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex2f(0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(), 0.0f);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(), mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight());
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex2f(0.0f, mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight()); 
            inGL.glEnd();            
            inGL.glBindTexture(GL_TEXTURE_2D, 0);
            inGL.glDisable(GL_TEXTURE_2D);
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        }

    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected class GL3_InternalFBO_DisplayLists_RAW implements BaseFrameBufferObjectRendererInterface {

        private int mDisplayListStartID;
        private int mDisplayListSize;

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
            //initialize the display lists ...
            mDisplayListSize = 9;
            mDisplayListStartID = inGL.glGenLists(mDisplayListSize);
            inGL.glNewList(mDisplayListStartID+0,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,0.5f,0.5f}));
                inGLUT.glutSolidSphere(1.0f, 16, 16);
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+1,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,0.75f,0.5f}));
                inGLUT.glutSolidCube(1.0f);
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+2,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,0.5f}));
                inGLUT.glutSolidOctahedron();
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+3,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.75f,1.0f,0.5f}));
                inGL.glDisable(GL_CULL_FACE);
                inGLUT.glutSolidCone(1.0f,1.5f,16,16);
                inGL.glEnable(GL_CULL_FACE);
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+4,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,1.0f,0.5f}));
                inGL.glDisable(GL_CULL_FACE);
                inGLUT.glutSolidCylinder(1.0f,1.0f,16,16);
                inGL.glEnable(GL_CULL_FACE);
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+5,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,1.0f,0.75f}));
                inGLUT.glutSolidTorus(0.5f,1.0f,16,16);
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+6,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,1.0f,1.0f}));
                inGLUT.glutSolidIcosahedron();
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+7,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,0.75f,1.0f}));
                inGLUT.glutSolidTetrahedron();
            inGL.glEndList();
            inGL.glNewList(mDisplayListStartID+8,GL_COMPILE);
                inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,0.5f,1.0f}));
                inGLUT.glutSolidRhombicDodecahedron();
            inGL.glEndList();
            //setup lighting, materials, shading and culling ...
            inGL.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.1f,0.1f,0.1f,1.0f}));
            inGL.glLightfv(GL_LIGHT0, GL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.3f,0.3f,0.3f,1.0f}));
            inGL.glLightfv(GL_LIGHT0, GL_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,1.0f,1.0f}));
            inGL.glLightfv(GL_LIGHT0, GL_POSITION, DirectBufferUtils.createDirectFloatBuffer(new float[]{-50.0f,50.0f,100.0f,1.0f}));
            inGL.glEnable(GL_LIGHT0);
            inGL.glMaterialfv(GL_FRONT, GL_SPECULAR, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,1.0f,1.0f}));
            inGL.glMateriali(GL_FRONT, GL_SHININESS, 64);
        }

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            //clear screen and z-buffer ...
            inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            inGL.glShadeModel(GL_SMOOTH);
            inGL.glEnable(GL_LIGHTING);
            inGL.glFrontFace(GL_CCW);
            inGL.glEnable(GL_CULL_FACE);
            inGL.glEnable(GL_DEPTH_TEST);
            inGL.glColor3f(1.0f, 1.0f, 1.0f);
            //could ofcourse be compiled into one big display list for better performance ... \|=//
            int tYLength = 8;
            int tXLength = 8;
            int tZLength = 8;
            int tDisplayListIDCounter = mDisplayListStartID;
            float tSpacing = 2.50f;
            for (int z=-7; z<tZLength; z++) {
                for (int y=-7; y<tYLength; y++) {
                    for (int x=-7; x<tXLength; x++) {
                        inGL.glPushMatrix();
                            inGL.glRotatef((inFrameNumber/2.0f)%360,1.0f,1.0f,1.0f);
                            inGL.glTranslatef(x*tSpacing,y*tSpacing,z*tSpacing);
                            if (tDisplayListIDCounter>(mDisplayListStartID+8)) {
                                tDisplayListIDCounter = mDisplayListStartID;
                            }
                            inGL.glCallList(tDisplayListIDCounter);
                        inGL.glPopMatrix();
                        tDisplayListIDCounter++;
                    }
                }
            }
           
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
            inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
            inGL.glFlush();
        }

    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected class GL3_InternalFBO_FXAA implements BaseFrameBufferObjectRendererInterface {

        protected int mLinkedShader;
        protected FloatBuffer mScreenDimensionUniform2fv;

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
            int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/fxaa.fs");
            mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
            mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(
                    new float[] {
                            (float)mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(),
                            (float)mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight()
                    }
            );
        }

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            //display texture billboard
            inGL.glDisable(GL_LIGHTING);
            inGL.glDisable(GL_CULL_FACE);
            inGL.glDisable(GL_DEPTH_TEST);
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glOrtho(0, mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(), mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight(), 0, -1, 1);
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            inGL.glUseProgram(mLinkedShader);
            ShaderUtils.setUniform2fv(inGL,mLinkedShader,"resolution",mScreenDimensionUniform2fv);
            inGL.glActiveTexture(GL_TEXTURE0);
            inGL.glEnable(GL_TEXTURE_2D);
            inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor_DisplayLists.getColorTextureID());
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",0);
            inGL.glValidateProgram(mLinkedShader);
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex2f(0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(), 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor_FXAA.getWidth(), mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight());
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex2f(0.0f, mBaseFrameBufferObjectRendererExecutor_FXAA.getHeight());
            inGL.glEnd();
            inGL.glUseProgram(0);
            inGL.glBindTexture(GL_TEXTURE_2D, 0);
            inGL.glDisable(GL_TEXTURE_2D);
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
            inGL.glDeleteShader(mLinkedShader);
            inGL.glFlush();
        }

    }

}