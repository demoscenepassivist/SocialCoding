package framework.jogl.postprocessingfilters;

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
 ** Postprocessing filter implementing various analog distortions. See the corresponding 
 ** fragment shader for implementation details.
 **
 **/

import java.nio.*;
import javax.media.opengl.*;
import framework.base.*;
import framework.util.*;

public class PostProcessingFilter_AnalogDistortion extends PostProcessingFilter_Base implements BasePostProcessingFilterChainShaderInterface {

    private int mFragmentShader;
    private int mLinkedShader;
    private float mTime;
    private FloatBuffer mScreenDimensionUniform2fv;

    public void initFilter(GL2 inGL) {
        mFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/postprocessingfilters/PostProcessingFilter_AnalogDistortion.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,mFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth()/getScreenSizeDivisionFactor(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()/getScreenSizeDivisionFactor()});
    }

    public void cleanupFilter(GL2 inGL) {
        inGL.glDeleteShader(mFragmentShader);
    }

    public void prepareForProgramUse(GL2 inGL) {
        inGL.glUseProgram(mLinkedShader);
        //backbuffer texture is implicitly bound to texture unit 0 ...
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",0);
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"time",mTime);
        ShaderUtils.setUniform2fv(inGL,mLinkedShader,"resolution",mScreenDimensionUniform2fv);
        inGL.glValidateProgram(mLinkedShader);
    }
    
    public void setScreenSizeDivisionFactor(int inDivisionFactor) {
        mScreenSizeDivisionFactor = inDivisionFactor;
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth()/getScreenSizeDivisionFactor(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()/getScreenSizeDivisionFactor()});
    }

    public void stopProgramUse(GL2 inGL) {
        inGL.glUseProgram(0);
    }

    public void setTime(float inValue) {
        mTime = inValue;
    }

    public float getTime() {
        return mTime;
    }

}
