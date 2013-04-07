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
 ** Expanding on the fractal-bitmap-orbit-trapping used in my JOGL2 port of Elektronenmultiplizierer
 ** this routine shows a wide variety of bitmap orbit traps for the julia- and mandelbrot-fractals.
 ** As most of the work is done in the fragment shader, the setup code is quite simple and allows
 ** to conveniently select the bitmap for the orbit-trap and some other IFS-related parameters e.g.
 ** number of iterations, choose between julia- and mandelbrot set, zoomin, julia-power and so on.
 ** Also a simple bloom is added afterwards as post-processing effect.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 ** Geometric orbit traps: http://www.iquilezles.org/www/articles/ftrapsgeometric/ftrapsgeometric.htm
 ** Bitmap orbit traps: http://www.iquilezles.org/www/articles/ftrapsbitmap/ftrapsbitmap.htm
 **
 ** For an impression how this routine looks like see here: 
 ** http://youtu.be/G79KtKOBr9Y - http://youtu.be/U_RI_hdWgTQ - http://youtu.be/xN1qlO8PW0c 
 ** http://youtu.be/ZwdX1HFuhmQ - http://youtu.be/z0wocD_BbwM - http://youtu.be/03OD_e9cX3M 
 ** http://youtu.be/odFATcfuDPU - http://youtu.be/FZTEmy8tec0 - http://youtu.be/jQ0UdJS8Tn0
 ** http://youtu.be/LI3J1U_eqQA - http://youtu.be/pF66_6D5Dtc - http://youtu.be/JOlbjDOomDA
 ** http://youtu.be/7Hf9wJ9Aa1E - http://youtu.be/0n5FbP2vMXg - http://youtu.be/tCLC6AK3Hnw
 ** http://youtu.be/gBez1ztuPIk - http://youtu.be/UwOHe5WmGic - http://youtu.be/QL_yiqRSnJk
 ** http://youtu.be/Zbz1JrL6quU - http://youtu.be/t-v06ZjvCdM - http://youtu.be/aky-XM0050Y
 ** http://youtu.be/kdidwlf0wxA - http://youtu.be/d9lYwOmRNQA - http://youtu.be/7saLlbYFF0w
 ** http://youtu.be/ksGLZkQ5CqY - http://youtu.be/nvWY94AgPxY
 **
 **/

import framework.base.*;
import framework.util.*;
import java.nio.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL3_FractalBitmapOrbitTrapping extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    private BasePostProcessingFilterChainExecutor mBasePostProcessingFilterChainExecutor;
    private ArrayList<BasePostProcessingFilterChainShaderInterface> mConvolutions;
    private ArrayList<BasePostProcessingFilterChainShaderInterface> mBlenders;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor = new BasePostProcessingFilterChainExecutor(4);
        mBasePostProcessingFilterChainExecutor.init(inGL,inGLU,inGLUT);
        mConvolutions = PostProcessingUtils.generatePostProcessingFilterArrayList(inGL,inGLU,inGLUT);
        mBlenders = PostProcessingUtils.generateBlenderFilterArrayList(inGL,inGLU,inGLUT); 
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.removeAllFilters();
        mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR).setNumberOfIterations(26+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()));
        mBasePostProcessingFilterChainExecutor.addFilter(mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR));
        mBasePostProcessingFilterChainExecutor.addFilter(mBlenders.get(PostProcessingUtils.POSTPROCESSINGFILTER_BLENDER_SCREEN));
        //mBasePostProcessingFilterChainExecutor.addFilter(mBlenders.get(PostProcessingUtils.POSTPROCESSINGFILTER_BLENDER_ADD));
        mBasePostProcessingFilterChainExecutor.executeFilterChain(inFrameNumber,inGL,inGLU,inGLUT,mBaseFrameBufferObjectRendererExecutor,true);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected int mLinkedShader;
    protected FloatBuffer mScreenDimensionUniform2fv;
    protected Texture mBitmapOrbitTrap[];

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        String tFragmentShaderString = ShaderUtils.loadShaderSourceFileAsString("/shaders/fractalshaders/fractalbitmaporbittrapping.fs");
        //tFragmentShaderString = "#define supersamplingfactor 0.25"+"\n"+tFragmentShaderString;
        //tFragmentShaderString = "#define singleiteration"+"\n"+tFragmentShaderString;
        int tFragmentShader = ShaderUtils.generateFragmentShader(inGL,tFragmentShaderString);
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()});
        mBitmapOrbitTrap = new Texture[39];
        mBitmapOrbitTrap[0] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_001.png");
        mBitmapOrbitTrap[1] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_002.png");
        mBitmapOrbitTrap[2] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_003.png");
        mBitmapOrbitTrap[3] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_004.png");
        mBitmapOrbitTrap[4] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_005.png");
        mBitmapOrbitTrap[5] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_006.png");
        mBitmapOrbitTrap[6] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_007.png");
        mBitmapOrbitTrap[7] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_008.png");
        mBitmapOrbitTrap[8] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_009.png");
        mBitmapOrbitTrap[9] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_010.png");
        mBitmapOrbitTrap[10] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_011.png");
        mBitmapOrbitTrap[11] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_012.png");
        mBitmapOrbitTrap[12] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_013.png");
        mBitmapOrbitTrap[13] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_014.png");
        mBitmapOrbitTrap[14] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_015.png");
        mBitmapOrbitTrap[15] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_016.png");
        mBitmapOrbitTrap[16] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_017.png");
        mBitmapOrbitTrap[17] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_018.png");
        mBitmapOrbitTrap[18] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_019.png");
        mBitmapOrbitTrap[19] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_020.png");
        mBitmapOrbitTrap[20] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_021.png");
        mBitmapOrbitTrap[21] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_022.png");
        mBitmapOrbitTrap[22] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_023.png");
        mBitmapOrbitTrap[23] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_024.png");
        mBitmapOrbitTrap[24] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_025.png");
        mBitmapOrbitTrap[25] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_026.png");
        mBitmapOrbitTrap[26] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_027.png");
        mBitmapOrbitTrap[27] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_028.png");
        mBitmapOrbitTrap[28] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_029.png");
        mBitmapOrbitTrap[29] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_030.png");
        mBitmapOrbitTrap[30] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_031.png");
        mBitmapOrbitTrap[31] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_032.png");
        mBitmapOrbitTrap[32] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_033.png");
        mBitmapOrbitTrap[33] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_034.png");
        mBitmapOrbitTrap[34] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_035.png");
        mBitmapOrbitTrap[35] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_036.png");
        mBitmapOrbitTrap[36] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_037.png");
        mBitmapOrbitTrap[37] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_038.png");
        mBitmapOrbitTrap[38] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_039.png");
        for (int i=0; i<mBitmapOrbitTrap.length; i++) {
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
            //mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_REPEAT);
            //mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_REPEAT);
        }
    }

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
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
        int zoominvalue = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_34())%2;
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"zoomin",zoominvalue);
        if (zoominvalue==0) {
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",40+BaseGlobalEnvironment.getInstance().getParameterKey_INT_90());
        } else {
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",(256+BaseGlobalEnvironment.getInstance().getParameterKey_INT_90()%256));
        }
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliamode",Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_56())%2);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliapower",2+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_78()));
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"orbittrapscale",3.5f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS());
        //manually overwrite parameters ...
        //ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliamode",1);
        //ShaderUtils.setUniform1i(inGL,mLinkedShader,"zoomin",1);
        //ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",256);
        inGL.glActiveTexture(GL_TEXTURE0);
        int tCurrentTexture = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()%mBitmapOrbitTrap.length);
        //tCurrentTexture = inFrameNumber/600;//%mBitmapOrbitTrap.length;
        //tCurrentTexture = 36;
        mBitmapOrbitTrap[tCurrentTexture].enable(inGL);
        mBitmapOrbitTrap[tCurrentTexture].bind(inGL);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"texture",0);
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
        inGL.glActiveTexture(GL_TEXTURE0);
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mLinkedShader);
        inGL.glFlush();
    }

}
