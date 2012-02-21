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
 ** Originally planned for a 1K PC intro, these shaders proceduarally generate an animated texture
 ** and use it as a base for one or multiple nested plane deformations. All shaders are rendered 
 ** to a fullscreen billboard and use super sampling to avoid aliasing artefacts. For an impression
 ** how these shaders look like see here: 
 **
 ** rastergraphomat_series_01.fs: http://www.youtube.com/watch?v=7zAUxvRsFmg
 ** rastergraphomat_series_02.fs: http://www.youtube.com/watch?v=nFuC6_JCdW0
 ** rastergraphomat_series_03.fs: http://www.youtube.com/watch?v=FfBiFy4-kl4
 ** rastergraphomat_series_04.fs: http://www.youtube.com/watch?v=DM4EpnaCsAo
 ** linegraphomat_series_01.fs: http://www.youtube.com/watch?v=X5bXGAWX5bg
 ** linegraphomat_series_02.fs: http://www.youtube.com/watch?v=axPHOjaT6K0
 ** linegraphomat_series_03.fs: http://www.youtube.com/watch?v=NRCixGrpD4o
 ** linegraphomat_series_04.fs: http://www.youtube.com/watch?v=BBIU1x9aUv4
 **/

import framework.base.*;
import framework.util.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_MonkeyMathica extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    private BaseSuperSamplingFBOWrapper mBaseSuperSamplingFBOWrapper;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper = new BaseSuperSamplingFBOWrapper(2.0f,this);
        mBaseSuperSamplingFBOWrapper.init(inGL,inGLU,inGLUT);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseSuperSamplingFBOWrapper.executeToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected int[] mLinkedShaders;
    protected int mLinkedShader;
    protected FloatBuffer mScreenDimensionUniform2fv;

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int[] tFragmentShaders = new int[8];
        tFragmentShaders[0] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/rastergraphomat_series_01.fs");
        tFragmentShaders[1] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/rastergraphomat_series_02.fs");
        tFragmentShaders[2] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/rastergraphomat_series_03.fs");
        tFragmentShaders[3] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/rastergraphomat_series_04.fs");
        tFragmentShaders[4] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/linegraphomat_series_01.fs");
        tFragmentShaders[5] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/linegraphomat_series_02.fs");
        tFragmentShaders[6] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/linegraphomat_series_03.fs");
        tFragmentShaders[7] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/monkeymathicashaders/linegraphomat_series_04.fs");
        mLinkedShaders = new int[tFragmentShaders.length];
        for (int i=0; i<tFragmentShaders.length; i++) {
            mLinkedShaders[i] = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShaders[i]);
        }
        mLinkedShader = mLinkedShaders[0];
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()});
    }

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mLinkedShader = mLinkedShaders[Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()%mLinkedShaders.length)];
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
        inGL.glDeleteShader(mLinkedShader);
        inGL.glFlush();
    }

}

