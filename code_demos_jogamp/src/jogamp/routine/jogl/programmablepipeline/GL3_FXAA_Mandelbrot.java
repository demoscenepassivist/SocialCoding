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
 ** variable FSAA (super sampling) on a fullscreen billboard using an "aliasing heavy" Mandelbrot
 ** fractal zoom fragment shader. For an impression how this routine looks like see here: 
 ** http://copypastaresearch.tumblr.com/post/11101649574/continuing-with-my-experiments-regarding-anti
 **
 **/

import framework.base.*;
import framework.util.*;
import java.awt.image.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_FXAA_Mandelbrot extends BaseRoutineAdapter implements BaseRoutineInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_Mandelbrot;
    protected GL3_InternalFBO_Mandelbrot_RAW mGL3_InternalFBO_Mandelbrot_RAW;
    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_FXAA;
    protected GL3_InternalFBO_FXAA mGL3_InternalFBO_FXAA;
    protected BaseSuperSamplingFBOWrapper mBaseSuperSamplingFBOWrapper;
    protected GL3_InternalFBO_SuperSampling mGL3_InternalFBO_SuperSampling;
    protected float mInternalSuperSamplingFactor = 2.0f;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mGL3_InternalFBO_Mandelbrot_RAW = new GL3_InternalFBO_Mandelbrot_RAW();
        mGL3_InternalFBO_FXAA = new GL3_InternalFBO_FXAA();
        mGL3_InternalFBO_SuperSampling = new GL3_InternalFBO_SuperSampling();
        mBaseSuperSamplingFBOWrapper = new BaseSuperSamplingFBOWrapper(mInternalSuperSamplingFactor,mGL3_InternalFBO_SuperSampling);
        mBaseSuperSamplingFBOWrapper.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_Mandelbrot = new BaseFrameBufferObjectRendererExecutor(
                (int)(BaseGlobalEnvironment.getInstance().getScreenWidth()*mInternalSuperSamplingFactor),
                (int)(BaseGlobalEnvironment.getInstance().getScreenHeight()*mInternalSuperSamplingFactor),
                mGL3_InternalFBO_Mandelbrot_RAW
        );
        mBaseFrameBufferObjectRendererExecutor_Mandelbrot.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_FXAA = new BaseFrameBufferObjectRendererExecutor(
                (int)(BaseGlobalEnvironment.getInstance().getScreenWidth()*mInternalSuperSamplingFactor),
                (int)(BaseGlobalEnvironment.getInstance().getScreenHeight()*mInternalSuperSamplingFactor),
                mGL3_InternalFBO_FXAA
        );
        mBaseFrameBufferObjectRendererExecutor_FXAA.init(inGL,inGLU,inGLUT);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor_Mandelbrot.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor_Mandelbrot.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_FXAA.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor_FXAA.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper.executeToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor_Mandelbrot.cleanup(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_FXAA.cleanup(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected class GL3_InternalFBO_SuperSampling implements BaseFrameBufferObjectRendererInterface {

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
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
            inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor_FXAA.getColorTextureID());
            //inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getColorTextureID());
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

    protected class GL3_InternalFBO_Mandelbrot_RAW implements BaseFrameBufferObjectRendererInterface {

        protected int mLinkedShader;
        protected FloatBuffer mScreenDimensionUniform2fv;
        protected int mLUTTextureID;

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
            int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/fractalshaders/mandelbrot.fs");
            mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
            mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(
                    new float[] {
                            (float)mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getWidth(), 
                            (float)mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getHeight()
                    }
            );
            //create BufferedImage to be used as LUT ...
            BufferedImage tLUT = TextureUtils.createARGBBufferedImage(5,1);
            //"Blue Sky Mine" from kuler.adobe.com
            tLUT.setRGB(0,0,0x00FDFF98);
            tLUT.setRGB(1,0,0x00A7DB9E);
            tLUT.setRGB(2,0,0x005EA692);
            tLUT.setRGB(3,0,0x00524B70);
            tLUT.setRGB(4,0,0x0023263A);
            mLUTTextureID = TextureUtils.generateTexture1DFromBufferedImage(inGL,tLUT,GL_CLAMP);
        }

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            //display texture billboard
            inGL.glDisable(GL_LIGHTING);
            inGL.glDisable(GL_CULL_FACE);
            inGL.glDisable(GL_DEPTH_TEST);
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glOrtho(0, mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getWidth(), mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getHeight(), 0, -1, 1);
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            inGL.glUseProgram(mLinkedShader);
            ShaderUtils.setUniform1f(inGL,mLinkedShader,"time",inFrameNumber/100.0f);
            ShaderUtils.setUniform2fv(inGL,mLinkedShader,"resolution",mScreenDimensionUniform2fv);
            inGL.glActiveTexture(GL_TEXTURE0);
            inGL.glEnable(GL_TEXTURE_1D);
            inGL.glBindTexture(GL_TEXTURE_1D, mLUTTextureID);
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",0);
            inGL.glValidateProgram(mLinkedShader);
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex2f(0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getWidth(), 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getWidth(), mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getHeight());
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex2f(0.0f, mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getHeight());
            inGL.glEnd();
            inGL.glUseProgram(0);
            inGL.glBindTexture(GL_TEXTURE_2D, 0);
            inGL.glDisable(GL_TEXTURE_2D);
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
            TextureUtils.deleteTextureID(inGL,mLUTTextureID);
            inGL.glDeleteShader(mLinkedShader);
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
            inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor_Mandelbrot.getColorTextureID());
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