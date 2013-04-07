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
 ** Advanced fragment shader demonstration implementing a GPGPU raymarcher to render a high order
 ** Mandelbulb Julia fractal. All matrix calculations, object setup and lighting is done solely in the
 ** fragment shader. Demonstrates the use of a 1D texture as LUT (using TextureUtils) and also makes
 ** use of ShaderUtils to ease the handling of vertex&pixel shaders. For an impression how this routine
 ** looks like see here: http://www.youtube.com/watch?v=arliqWDlRTk
 **
 ** This is a simple adaption to generate a stereoscopic image pair. It used kinda Toe-in (incorrect)
 ** method for stereo. See here for more information on calculating stereoscopic image pairs: 
 ** http://paulbourke.net/stereographics/stereorender/   
 **
 **/

import java.awt.image.*;
import framework.base.BaseRoutineRuntime;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class S3D_GL3_Raymarcher_Mandelbulb extends GL2_FBO_FullscreenQuad_Base {

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/mandelbulb_s3d.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor.getWidth(), (float)mBaseFrameBufferObjectRendererExecutor.getHeight()});
        //create BufferedImage to be used as LUT ...
        BufferedImage tLUT = TextureUtils.createARGBBufferedImage(5,1);
        /*
        //"Almost Happy" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00FF4845);
        tLUT.setRGB(1,0,0x00FFA154);
        tLUT.setRGB(2,0,0x00FFD154);
        tLUT.setRGB(3,0,0x00EFFF63);
        tLUT.setRGB(4,0,0x00C6FCA9);
        */
        /*
        //"Thicket" from kuler.adobe.com
        tLUT.setRGB(0,0,0x0069590E);
        tLUT.setRGB(1,0,0x00859600);
        tLUT.setRGB(2,0,0x0073E800);
        tLUT.setRGB(3,0,0x0068FF70);
        tLUT.setRGB(4,0,0x009DFFC0);
        */
        /*
        //"vooooooo" from kuler.adobe.com
        tLUT.setRGB(0,0,0x003B3330);
        tLUT.setRGB(1,0,0x00506655);
        tLUT.setRGB(2,0,0x00779C75);
        tLUT.setRGB(3,0,0x00A0BF8C);
        tLUT.setRGB(4,0,0x00D7E3AC);
        */
        /*
        //"Photon" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00E8001E);
        tLUT.setRGB(1,0,0x002B3635);
        tLUT.setRGB(2,0,0x002C4242);
        tLUT.setRGB(3,0,0x00244C4A);
        tLUT.setRGB(4,0,0x0053FFD8);
        */
        
        //"morning side of the mountain" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00FFBB7E);
        tLUT.setRGB(1,0,0x00CA885D);
        tLUT.setRGB(2,0,0x008F6554);
        tLUT.setRGB(3,0,0x004E3F49);
        tLUT.setRGB(4,0,0x00002342);
        
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
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"eyeoffset",BaseRoutineRuntime.getInstance().getCurrentStereoscopicEyeSeparation());
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

}