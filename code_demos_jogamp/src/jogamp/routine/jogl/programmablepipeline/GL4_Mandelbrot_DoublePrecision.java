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
 ** and also makes use of ShaderUtils to ease the handling of vertex&pixel shaders. This version
 ** uses double precision (FP64) floats to zoom in even further (if ur GPU is up2date enough to 
 ** supports the GL_ARB_gpu_shader_fp64 extension). For an impression how this routine looks like
 ** see here: http://youtu.be/FjK9a6b47A8
 **
 **/

import framework.base.*;
import framework.util.*;
import java.awt.image.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL4_Mandelbrot_DoublePrecision extends GL2_FBO_FullscreenQuad_Base implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/fractalshaders/mandelbrot_doubleprecision.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor.getWidth(), (float)mBaseFrameBufferObjectRendererExecutor.getHeight()});
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

}
