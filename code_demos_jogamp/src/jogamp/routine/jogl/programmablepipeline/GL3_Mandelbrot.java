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
 ** Advanced fragment shader demonstration implementing some kind of GPGPU calculation to render
 ** a Mandelbrot fractal zoom. Demonstrates the use of a 1D texture as LUT (using TextureUtils) 
 ** and also makes use of ShaderUtils to ease the handling of vertex&pixel shaders. For an 
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=MgEpH70oZN4
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

public class GL3_Mandelbrot extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);        
        mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected int mLinkedShader;
    protected FloatBuffer mScreenDimensionUniform2fv;
    protected int mLUTTextureID;

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/fractalshaders/mandelbrot.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor.getWidth(), (float)mBaseFrameBufferObjectRendererExecutor.getHeight()});
        //create BufferedImage to be used as LUT ...
        BufferedImage tLUT = TextureUtils.createARGBBufferedImage(5,1);
        //"Blue Sky Mine" from kuler.com
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
        inGL.glOrtho(0, mBaseFrameBufferObjectRendererExecutor.getWidth(), mBaseFrameBufferObjectRendererExecutor.getHeight(), 0, -1, 1);
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
            inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor.getWidth(), 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor.getWidth(), mBaseFrameBufferObjectRendererExecutor.getHeight());
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, mBaseFrameBufferObjectRendererExecutor.getHeight());
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
