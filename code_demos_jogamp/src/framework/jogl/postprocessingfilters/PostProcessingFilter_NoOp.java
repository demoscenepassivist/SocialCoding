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
 ** Postprocessing filter implementing nothing :) ... 
 **
 **/

import javax.media.opengl.GL2;
import framework.base.*;

public class PostProcessingFilter_NoOp extends PostProcessingFilter_Base implements BasePostProcessingFilterChainShaderInterface {

    public void initFilter(GL2 inGL) {
        mNumberOfIterations = 1;
    }

    public void cleanupFilter(GL2 inGL) {
    }

    public void prepareForProgramUse(GL2 inGL) {
    }

    public void stopProgramUse(GL2 inGL) {
    }

    public void setNumberOfIterations(int inNumberOfIterations) {
        //ignore number of iterations ...
    }

}
