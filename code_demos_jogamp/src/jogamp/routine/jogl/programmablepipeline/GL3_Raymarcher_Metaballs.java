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
 ** Advanced fragment shader demonstration implementing a GPGPU raymarcher to render a 3D isosurface.
 ** (a couple of metaballs to be exact). All matrix calculations, object setup and lighting is done 
 ** solely in the fragment shader. Demonstrates the use of a 1D texture as LUT (using TextureUtils)
 ** and also makes use of ShaderUtils to ease the handling of vertex&pixel shaders. For an impression
 ** how this routine looks like see here: http://www.youtube.com/watch?v=bIDaE2PtmPs
 **
 **/

import framework.util.*;
import java.awt.image.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_Raymarcher_Metaballs extends GL2_FBO_FullscreenQuad_Base {

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/metaballs.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor.getWidth(), (float)mBaseFrameBufferObjectRendererExecutor.getHeight()});
        //create BufferedImage to be used as LUT ...
        BufferedImage tLUT = TextureUtils.createARGBBufferedImage(5,1);
        //"Bicycles" from kuler.adobe.com
        tLUT.setRGB(0,0,0x0065356B);
        tLUT.setRGB(1,0,0x00AB434F);
        tLUT.setRGB(2,0,0x00C76347);
        tLUT.setRGB(3,0,0x00FFA24C);
        tLUT.setRGB(4,0,0x00519183);
        mLUTTextureID = TextureUtils.generateTexture1DFromBufferedImage(inGL,tLUT,GL_CLAMP);
    }
    
}
