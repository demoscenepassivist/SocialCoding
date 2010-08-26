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
 ** Postprocessing filter inheritance root implementing no specific filter but providing
 ** a common implementation of all interface methods suitable for most filter implementations.
 **
 **/

import javax.media.opengl.GL2;
import framework.base.*;

public abstract class PostProcessingFilter_Base implements BasePostProcessingFilterChainShaderInterface {

    protected int mScreenSizeDivisionFactor;
    protected int mNumberOfIterations;

    public abstract void initFilter(GL2 inGL);

    public abstract void cleanupFilter(GL2 inGL);

    public abstract void prepareForProgramUse(GL2 inGL);

    public abstract void stopProgramUse(GL2 inGL);

    public void setScreenSizeDivisionFactor(int inDivisionFactor) {
        mScreenSizeDivisionFactor = inDivisionFactor;
    }

    public int getScreenSizeDivisionFactor() {
        return mScreenSizeDivisionFactor;
    }

    public void setNumberOfIterations(int inNumberOfIterations) {
        mNumberOfIterations = inNumberOfIterations;
    }

    public int getNumberOfIterations() {
        return mNumberOfIterations;
    }

}
