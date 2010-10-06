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
 ** Advanced fragment shader demonstration implementing a GPGPU raymarcher to render a pseudo 3D
 ** Mandelbrot fractal. All matrix calculations, object setup and lighting is done solely in the
 ** fragment shader. Demonstrates the use of a 1D texture as LUT (using TextureUtils) and also makes
 ** use of ShaderUtils to ease the handling of vertex&pixel shaders. For an impression how this routine
 ** looks like see here: http://www.youtube.com/watch?v=JVnEtCj-oLs
 **
 **/

import framework.util.*;
import java.awt.image.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_Raymarcher_Mandelbrot3D extends GL2_FBO_FullscreenQuad_Base {

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/mandelbrot3d.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor.getWidth(), (float)mBaseFrameBufferObjectRendererExecutor.getHeight()});
        //create BufferedImage to be used as LUT ...
        BufferedImage tLUT = TextureUtils.createARGBBufferedImage(5,1);
        /*
        //"Mars" from kuler.adobe.com
        tLUT.setRGB(0,0,0x003D182B);
        tLUT.setRGB(1,0,0x00AD0626);
        tLUT.setRGB(2,0,0x00FF005A);
        tLUT.setRGB(3,0,0x00FF9800);
        tLUT.setRGB(4,0,0x00FFCE00);
        */
        /*
        //"Earth and Wind" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00452F27);
        tLUT.setRGB(1,0,0x005E504A);
        tLUT.setRGB(2,0,0x006B6865);
        tLUT.setRGB(3,0,0x009BBAB2);
        tLUT.setRGB(4,0,0x00B0FFED);
        */
        /*
        //"Almost Happy" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00FF4845);
        tLUT.setRGB(1,0,0x00FFA154);
        tLUT.setRGB(2,0,0x00FFD154);
        tLUT.setRGB(3,0,0x00EFFF63);
        tLUT.setRGB(4,0,0x00C6FCA9);
        */
        /*
        //"Extra Grape" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00FFF9C0);
        tLUT.setRGB(1,0,0x00FF6E6E);
        tLUT.setRGB(2,0,0x00B24376);
        tLUT.setRGB(3,0,0x00570E66);
        tLUT.setRGB(4,0,0x00261823);
        */
        /*
        //"Thicket" from kuler.adobe.com
        tLUT.setRGB(0,0,0x0069590E);
        tLUT.setRGB(1,0,0x00859600);
        tLUT.setRGB(2,0,0x0073E800);
        tLUT.setRGB(3,0,0x0068FF70);
        tLUT.setRGB(4,0,0x009DFFC0);
        */
        //"Colorful" from kuler.adobe.com
        tLUT.setRGB(0,0,0x00282C61);
        tLUT.setRGB(1,0,0x00005EBC);
        tLUT.setRGB(2,0,0x0027ADC5);
        tLUT.setRGB(3,0,0x006ACC9B);
        tLUT.setRGB(4,0,0x00C5F275);
        mLUTTextureID = TextureUtils.generateTexture1DFromBufferedImage(inGL,tLUT,GL_CLAMP);
    }

}
