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
 ** Postprocessing filter implementing simple grayscale conversion. See the corresponding fragment
 ** shader for implementation details.
 **
 **/

import javax.media.opengl.*;
import framework.base.*;
import framework.util.*;

public class PostProcessingFilter_GrayScale extends PostProcessingFilter_Base implements BasePostProcessingFilterChainShaderInterface {

    private int mFragmentShader;
    private int mLinkedShader;

    public void initFilter(GL2 inGL) {
        mFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/postprocessingfilters/PostProcessingFilter_GrayScale.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,mFragmentShader);
   }

    public void cleanupFilter(GL2 inGL) {
        inGL.glDeleteShader(mFragmentShader);
    }

    public void prepareForProgramUse(GL2 inGL) {
        inGL.glUseProgram(mLinkedShader);
        //backbuffer texture is implicitly bound to texture unit 0 ...
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",0);
        inGL.glValidateProgram(mLinkedShader);
    }

    public void stopProgramUse(GL2 inGL) {
        inGL.glUseProgram(0);
    }

}
