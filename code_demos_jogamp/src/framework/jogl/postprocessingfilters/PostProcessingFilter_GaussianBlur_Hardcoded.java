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
 ** Postprocessing filter implementing a 'GAUSSIAN BLUR' linear convolution. See the corresponding 
 ** fragment shader for implementation details.
 **
 **/

import javax.media.opengl.GL2;
import framework.base.*;

public class PostProcessingFilter_GaussianBlur_Hardcoded extends PostProcessingFilter_Base_Convolution implements BasePostProcessingFilterChainShaderInterface {

    public void initFilter(GL2 inGL) {
        mFragmentShaderFileName = "/shaders/postprocessingfilters/PostProcessingFilter_GaussianBlur_Hardcoded.fs";
        super.initFilter(inGL);
    }

}
