package framework.jogl.postprocessingblenders;

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
 ** Postprocessing filter inheritance root implementing no specific blending mode but providing
 ** a common implementation of all interface methods suitable for most filter implementations.
 **
 **/

import javax.media.opengl.*;
import framework.base.*;
import framework.jogl.postprocessingfilters.*;
import framework.util.*;

public class PostProcessingFilter_Blender_Base extends PostProcessingFilter_Base implements BasePostProcessingFilterChainShaderInterface {

    protected int mFragmentShader;
    protected int mLinkedShader;
    protected int mPrimaryTextureUnitNumber;
    protected int mSecondaryTextureUnitNumber;
    protected float mOpacity;
    protected String mFragmentShaderFileName;

    public void initFilter(GL2 inGL) {
        mFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,mFragmentShaderFileName);
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,mFragmentShader);
        //backbuffer texture is implicitly bound to texture unit 0 ...
        //fullscreenbuffer texture is implicitly bound to texture unit 1 ...
        mPrimaryTextureUnitNumber = 0;
        mSecondaryTextureUnitNumber = 1;
        mOpacity = 1.0f;
        setNumberOfIterations(1);
        setScreenSizeDivisionFactor(1);
    }

    public void cleanupFilter(GL2 inGL) {
        inGL.glDeleteShader(mFragmentShader);
    }

    public void prepareForProgramUse(GL2 inGL) {
        inGL.glUseProgram(mLinkedShader);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",mPrimaryTextureUnitNumber);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler1",mSecondaryTextureUnitNumber);
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"opacity",mOpacity);
        inGL.glValidateProgram(mLinkedShader);
    }

    public void stopProgramUse(GL2 inGL) {
        inGL.glUseProgram(0);
    }

    public void setSamplerTextureUnit_Primary(int inTextureUnitNumber) {
        mPrimaryTextureUnitNumber = inTextureUnitNumber;
    }

    public void setSamplerTextureUnit_Secondary(int inTextureUnitNumber) {
        mSecondaryTextureUnitNumber = inTextureUnitNumber;
    }

    public void setOpacity(float inValue) {
        mOpacity = inValue;
    }

    public float getOpacity() {
        return mOpacity;
    }

}
