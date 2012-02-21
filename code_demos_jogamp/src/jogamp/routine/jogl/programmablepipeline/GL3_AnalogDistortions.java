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
 ** Simply renders a fullscreen quad filled with a 2D texture to a FBO wich is afterwards used
 ** as texture for a pre-/post-/blender-filter. In this routine a fragment shader (pre-filter)
 ** implementing various "analog distortion" effects like contrast, vigneting, color separation,
 ** color shift, tv-lines and tv-flicker is used on the FBO. For an impression how this
 ** routine looks like see here: http://www.youtube.com/watch?v=YVnbtD__IUo
 **
 **/

import framework.base.*;
import framework.util.*;
import framework.jogl.postprocessingfilters.*;
import java.nio.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL3_AnalogDistortions extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    protected BasePostProcessingFilterChainExecutor mBasePostProcessingFilterChainExecutor;
    protected ArrayList<BasePostProcessingFilterChainShaderInterface> mPreFilters;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor = new BasePostProcessingFilterChainExecutor(1);
        mBasePostProcessingFilterChainExecutor.init(inGL,inGLU,inGLUT);
        mPreFilters = PostProcessingUtils.generatePreFilterArrayList(inGL,inGLU,inGLUT);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);        
        mBasePostProcessingFilterChainExecutor.removeAllFilters();
        mBasePostProcessingFilterChainExecutor.addFilter(mPreFilters.get(PostProcessingUtils.POSTPROCESSINGFILTER_PREFILTER_ANALOGDISTORTION));        
        PostProcessingFilter_AnalogDistortion tPostProcessingFilter_AnalogDistortion = (PostProcessingFilter_AnalogDistortion)mPreFilters.get(PostProcessingUtils.POSTPROCESSINGFILTER_PREFILTER_ANALOGDISTORTION);
        tPostProcessingFilter_AnalogDistortion.setTime(inFrameNumber/50.0f);
        mBasePostProcessingFilterChainExecutor.executeFilterChain(inFrameNumber,inGL,inGLU,inGLUT,mBaseFrameBufferObjectRendererExecutor,true);      
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected int mLinkedShader;
    protected FloatBuffer mScreenDimensionUniform2fv;
    protected int mLUTTextureID;
    protected Texture mTexture_Diffuse;
    
    //override at least this method ... -=:-)
    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mTexture_Diffuse = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/Wallpaper_JOGAMP_MyJapanHoliday_03_1920pel.png");
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
        inGL.glActiveTexture(GL_TEXTURE0);
        mTexture_Diffuse.enable(inGL);
        mTexture_Diffuse.bind(inGL);
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
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mTexture_Diffuse.destroy(inGL);
        inGL.glFlush();
    }

}
