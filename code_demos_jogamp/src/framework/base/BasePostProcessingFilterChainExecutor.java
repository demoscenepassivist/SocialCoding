package framework.base;

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
 ** Utility class wich helps with setup and handling of BasePostProcessingFilterChainShaderInterface
 ** instances. Simply add the filter chain and call executeFilterChain(). The uniform, texture unit
 ** and framebuffer setup is handled automatically. Also provides simple iteration support to simplify
 ** convolution-filter handling. 
 **
 **/

import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import framework.jogl.postprocessingblenders.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class BasePostProcessingFilterChainExecutor {

    private ArrayList<BasePostProcessingFilterChainShaderInterface> mFilterList;
    private int mScreenSizeDivisionFactor;
    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_Primary;
    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_Secondary;
    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor_Original;
    private BaseFrameBufferObjectRendererInterface mPrimaryFBORenderer;
    private BaseFrameBufferObjectRendererInterface mSecondaryFBORenderer;
    private BaseFrameBufferObjectRendererInterface mOriginalFBORenderer;
    private BasePostProcessingFilterChainShaderInterface mBasePostProcessingFilterChainShaderInterface_Primary;
    private BasePostProcessingFilterChainShaderInterface mBasePostProcessingFilterChainShaderInterface_Secondary;
    private BasePostProcessingFilterChainShaderInterface mBasePostProcessingFilterChainShaderInterface_Original;
    private BaseFrameBufferObjectRendererExecutor mOriginalFBO;
    private BaseFrameBufferObjectRendererExecutor mCurrent_PrimaryFBO;
    private BaseFrameBufferObjectRendererExecutor mCurrent_SecondaryFBO;
    private BaseFrameBufferObjectRendererExecutor mCurrent_OriginalFBO;
    private enum ENDRESULT_BUFFER {PRIMARY,SECONDARY,ORIGINAL}
    private int mFilterChainResultColorTexture;
    private BaseFrameBufferObjectRendererExecutor mFilterChainResultFrameBufferObjectRendererExecutor;
    private boolean mFilterChainLogging;
    
    public BasePostProcessingFilterChainExecutor(int inScreenSizeDivisionFactor) {
        mFilterList = new ArrayList<BasePostProcessingFilterChainShaderInterface>();
        mScreenSizeDivisionFactor = inScreenSizeDivisionFactor;
    }

    public void init(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mPrimaryFBORenderer = new PrimaryFBORenderer();
        mSecondaryFBORenderer = new SecondaryFBORenderer();
        mOriginalFBORenderer = new OriginalFBORenderer();
        mBaseFrameBufferObjectRendererExecutor_Primary = new BaseFrameBufferObjectRendererExecutor(
                BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor,
                BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor,
                mPrimaryFBORenderer
        );
        mBaseFrameBufferObjectRendererExecutor_Primary.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_Secondary = new BaseFrameBufferObjectRendererExecutor(
                BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor,
                BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor,
                mSecondaryFBORenderer
        );
        mBaseFrameBufferObjectRendererExecutor_Secondary.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_Original = new BaseFrameBufferObjectRendererExecutor(
                BaseGlobalEnvironment.getInstance().getScreenWidth()/1,
                BaseGlobalEnvironment.getInstance().getScreenHeight()/1,
                mOriginalFBORenderer
        );
        mBaseFrameBufferObjectRendererExecutor_Original.init(inGL,inGLU,inGLUT);
    }

    //quite a lot of redundant code here ... anyway who cares X-)
    private class PrimaryFBORenderer implements BaseFrameBufferObjectRendererInterface {

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            if (mFilterChainLogging) { BaseLogging.getInstance().info("RENDER SECONDARY->PRIMARY"); }
            BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT,BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor);
            inGL.glShadeModel(GL_SMOOTH);
            inGL.glDisable(GL_LIGHTING);
            inGL.glFrontFace(GL_CCW);
            inGL.glDisable(GL_CULL_FACE);
            inGL.glDisable(GL_DEPTH_TEST);
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor, 0, -1, 1);
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            //bind old fullscreen texture to texture unit 1
            inGL.glActiveTexture(GL_TEXTURE1);
            inGL.glBindTexture(GL_TEXTURE_2D, mOriginalFBO.getColorTextureID());
            inGL.glActiveTexture(GL_TEXTURE0);
            mCurrent_PrimaryFBO.prepareForColouredRendering(inGL,GL_TEXTURE0);
            mBasePostProcessingFilterChainShaderInterface_Primary.prepareForProgramUse(inGL);
            renderInternalBillboardWithAutomaticFlipping(inGL,inGLU,inGLUT,false);
            mBasePostProcessingFilterChainShaderInterface_Primary.stopProgramUse(inGL);
            mCurrent_PrimaryFBO.stopColouredRendering(inGL);
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}

    }

    //quite a lot of redundant code here ... anyway who cares X-)
    private class SecondaryFBORenderer implements BaseFrameBufferObjectRendererInterface {

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            if (mFilterChainLogging) { BaseLogging.getInstance().info("RENDER PRIMARY->SECONDARY"); }
            BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT,BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor);
            inGL.glShadeModel(GL_SMOOTH);
            inGL.glDisable(GL_LIGHTING);
            inGL.glFrontFace(GL_CCW);
            inGL.glDisable(GL_CULL_FACE);
            inGL.glDisable(GL_DEPTH_TEST);          
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor, 0, -1, 1);
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            //bind old fullscreen texture to texture unit 1
            inGL.glActiveTexture(GL_TEXTURE1);
            inGL.glBindTexture(GL_TEXTURE_2D, mOriginalFBO.getColorTextureID());
            inGL.glActiveTexture(GL_TEXTURE0);
            mCurrent_SecondaryFBO.prepareForColouredRendering(inGL,GL_TEXTURE0);
            mBasePostProcessingFilterChainShaderInterface_Secondary.prepareForProgramUse(inGL);
            renderInternalBillboardWithAutomaticFlipping(inGL,inGLU,inGLUT,false);
            mBasePostProcessingFilterChainShaderInterface_Secondary.stopProgramUse(inGL);
            mCurrent_SecondaryFBO.stopColouredRendering(inGL);
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}

    }

    //quite a lot of redundant code here ... anyway who cares X-)
    private class OriginalFBORenderer implements BaseFrameBufferObjectRendererInterface {

        public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}

        public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
            if (mFilterChainLogging) { BaseLogging.getInstance().info("RENDER PRIMARY/SECONDARY->ORIGINAL"); }
            BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT,BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glShadeModel(GL_SMOOTH);
            inGL.glDisable(GL_LIGHTING);
            inGL.glFrontFace(GL_CCW);
            inGL.glDisable(GL_CULL_FACE);
            inGL.glDisable(GL_DEPTH_TEST);
            inGL.glMatrixMode(GL_PROJECTION);
            inGL.glLoadIdentity();
            inGL.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, -1, 1);
            inGL.glMatrixMode(GL_MODELVIEW);
            inGL.glLoadIdentity();
            //bind old fullscreen texture to texture unit 1
            inGL.glActiveTexture(GL_TEXTURE1);
            inGL.glBindTexture(GL_TEXTURE_2D, mOriginalFBO.getColorTextureID());
            inGL.glActiveTexture(GL_TEXTURE0);
            mCurrent_OriginalFBO.prepareForColouredRendering(inGL,GL_TEXTURE0);
            mBasePostProcessingFilterChainShaderInterface_Original.prepareForProgramUse(inGL);
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex2f(0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glEnd();
            mBasePostProcessingFilterChainShaderInterface_Original.stopProgramUse(inGL);
            mCurrent_OriginalFBO.stopColouredRendering(inGL);
        }

        public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}

    }

    public void addFilter(BasePostProcessingFilterChainShaderInterface inBasePostProcessingFilterChainShaderInterface) {
        mFilterList.add(inBasePostProcessingFilterChainShaderInterface);
    }

    public void removeFilter(BasePostProcessingFilterChainShaderInterface inBasePostProcessingFilterChainShaderInterface) {
        mFilterList.remove(inBasePostProcessingFilterChainShaderInterface);
    }

    public void removeAllFilters() {
        mFilterList.clear();
    }
    
    public int getFilterChainResultColorTexture() {
        return mFilterChainResultColorTexture;
    }
    
    public BaseFrameBufferObjectRendererExecutor getFilterChainResultFBOExecutor() {
        return mFilterChainResultFrameBufferObjectRendererExecutor;
    }
    
    public void executeFilterChain(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT,BaseFrameBufferObjectRendererExecutor inOriginalFBO,boolean inDrawToFrameBuffer) {
        executeFilterChain(inFrameNumber,inGL,inGLU,inGLUT,inOriginalFBO,inDrawToFrameBuffer,false);
    }
    
    public void setFilterChainLogging(boolean inFilterChainLogging) {
        mFilterChainLogging = inFilterChainLogging;
    }

    public void executeFilterChain(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT,BaseFrameBufferObjectRendererExecutor inOriginalFBO,boolean inDrawToFrameBuffer,boolean inFlipped) {
        if (mFilterChainLogging) { BaseLogging.getInstance().info("-!PROCESSING executeFilterChain() on "+this+" !-"); }
        mOriginalFBO = inOriginalFBO;
        ENDRESULT_BUFFER tEndresultBuffer = ENDRESULT_BUFFER.PRIMARY;
        boolean tUsePrimary = false;
        for (int i=0; i<mFilterList.size(); i++) {
            if (mFilterList.get(i) instanceof PostProcessingFilter_Blender_Base) {
                mBasePostProcessingFilterChainShaderInterface_Original = mFilterList.get(i);
                if (mFilterChainLogging) { BaseLogging.getInstance().info("USING BLENDER FILTER HANDLING ON FILTER NUMBER="+i); }
                if (tEndresultBuffer==ENDRESULT_BUFFER.PRIMARY) {
                    if (mFilterChainLogging) { BaseLogging.getInstance().info("BLENDER ENDRESULT_BUFFER.PRIMARY"); }
                    mCurrent_OriginalFBO = mBaseFrameBufferObjectRendererExecutor_Primary;
                    mBaseFrameBufferObjectRendererExecutor_Original.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
                    tEndresultBuffer = ENDRESULT_BUFFER.ORIGINAL;
                } else {
                    if (mFilterChainLogging) { BaseLogging.getInstance().info("BLENDER ENDRESULT_BUFFER.SECONDARY"); }
                    mCurrent_OriginalFBO = mBaseFrameBufferObjectRendererExecutor_Secondary;
                    mBaseFrameBufferObjectRendererExecutor_Original.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
                    tEndresultBuffer = ENDRESULT_BUFFER.ORIGINAL;
                }
            } else {
                if (mFilterChainLogging) { BaseLogging.getInstance().info("NEXT FILTER ------- FILTER NUMBER="+i); }
                mBasePostProcessingFilterChainShaderInterface_Primary = mFilterList.get(i);
                mBasePostProcessingFilterChainShaderInterface_Secondary = mFilterList.get(i);
                //hardcoded initial iteration ... :-X
                if (i==0) {
                    if (mFilterChainLogging) { BaseLogging.getInstance().info("RENDER ORIGINAL->PRIMARY"); }
                    mCurrent_PrimaryFBO = mOriginalFBO;
                    mBaseFrameBufferObjectRendererExecutor_Primary.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
                    tEndresultBuffer = ENDRESULT_BUFFER.PRIMARY;
                }
                mCurrent_PrimaryFBO = mBaseFrameBufferObjectRendererExecutor_Secondary;
                mCurrent_SecondaryFBO = mBaseFrameBufferObjectRendererExecutor_Primary;
                int tNumberOfIterations;
                if (i==0) {
                    tNumberOfIterations = mFilterList.get(i).getNumberOfIterations()-1;
                } else {
                    tNumberOfIterations = mFilterList.get(i).getNumberOfIterations();
                }
                for (int j=0; j<tNumberOfIterations; j++) {
                    if (mFilterChainLogging) { BaseLogging.getInstance().info("ITERATION LOOP NUMBER="+j); }
                    if (tUsePrimary) {
                        mBaseFrameBufferObjectRendererExecutor_Primary.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
                        tEndresultBuffer = ENDRESULT_BUFFER.PRIMARY;
                    } else {
                        mBaseFrameBufferObjectRendererExecutor_Secondary.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
                        tEndresultBuffer = ENDRESULT_BUFFER.SECONDARY;
                    }
                    tUsePrimary = tUsePrimary^true;
                }
            }
        }
        BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT);
        if (tEndresultBuffer==ENDRESULT_BUFFER.PRIMARY) {
            if (mFilterChainLogging) { BaseLogging.getInstance().info("ENDRESULT_BUFFER.PRIMARY"); }
            mFilterChainResultColorTexture = mBaseFrameBufferObjectRendererExecutor_Primary.getColorTextureID();
            mFilterChainResultFrameBufferObjectRendererExecutor = mBaseFrameBufferObjectRendererExecutor_Primary;
            if (inDrawToFrameBuffer) {
                mBaseFrameBufferObjectRendererExecutor_Primary.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT,inFlipped);
            }
        } else if (tEndresultBuffer==ENDRESULT_BUFFER.SECONDARY) {
            if (mFilterChainLogging) { BaseLogging.getInstance().info("ENDRESULT_BUFFER.SECONDARY"); }
            mFilterChainResultColorTexture = mBaseFrameBufferObjectRendererExecutor_Secondary.getColorTextureID();
            mFilterChainResultFrameBufferObjectRendererExecutor = mBaseFrameBufferObjectRendererExecutor_Secondary;
            if (inDrawToFrameBuffer) {
                mBaseFrameBufferObjectRendererExecutor_Secondary.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT,inFlipped);
            }
        } else {
            if (mFilterChainLogging) { BaseLogging.getInstance().info("ENDRESULT_BUFFER.ORIGINAL"); }
            mFilterChainResultColorTexture = mBaseFrameBufferObjectRendererExecutor_Original.getColorTextureID();
            mFilterChainResultFrameBufferObjectRendererExecutor = mBaseFrameBufferObjectRendererExecutor_Original;
            if (inDrawToFrameBuffer) {
                if (inFlipped) {
                    mBaseFrameBufferObjectRendererExecutor_Original.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
                } else {
                    mBaseFrameBufferObjectRendererExecutor_Original.renderFBOAsFullscreenBillboard_FLIPPED(inGL,inGLU,inGLUT);
                }
            }
        }
        if (mFilterChainLogging) { BaseLogging.getInstance().info("------- NEXT FRAME -------"); }
    }

    private void renderInternalBillboardWithAutomaticFlipping(GL2 inGL,GLU inGLU,GLUT inGLUT,boolean inFlipped) {
        if (inFlipped) {
            //flipped billboard
            inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor);
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor);
            inGL.glEnd();
        } else {
            inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth()/mScreenSizeDivisionFactor, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor);
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight()/mScreenSizeDivisionFactor);
            inGL.glEnd();
        }
    }

    public void cleanup(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor_Primary.cleanup(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_Secondary.cleanup(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor_Original.cleanup(inGL,inGLU,inGLUT);
    }

}
