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
 ** a Julia set fractal animation. Demonstrates the use of a 1D texture as LUT (using TextureUtils) 
 ** and also makes use of ShaderUtils to ease the handling of vertex&pixel shaders. For an 
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=_0Qt4C0Zd4I
 **
 **/

import java.awt.image.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_JuliaSet extends GL3_Mandelbrot {

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/fractalshaders/juliaset.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor.getWidth(), (float)mBaseFrameBufferObjectRendererExecutor.getHeight()});
        //create BufferedImage to be used as LUT ...
        BufferedImage tLUT = TextureUtils.createARGBBufferedImage(5,1);
        //"Mars" from kuler.com
        tLUT.setRGB(0,0,0x00FFCE00);
        tLUT.setRGB(1,0,0x00FF9800);
        tLUT.setRGB(2,0,0x00FF005A);
        tLUT.setRGB(3,0,0x00AD0626);
        tLUT.setRGB(4,0,0x003D182B);
        mLUTTextureID = TextureUtils.generateTexture1DFromBufferedImage(inGL,tLUT,GL_CLAMP);
    }

}
