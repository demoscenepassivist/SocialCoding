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
 ** Interface to be implemented by all post-/pre-processing filters. Provides method prototypes
 ** for initialization, runtime and end/cleanup of a filter.
 **
 **/

import javax.media.opengl.*;

public interface BasePostProcessingFilterChainShaderInterface {

    public void initFilter(GL2 inGL);
    public void cleanupFilter(GL2 inGL);
    public void prepareForProgramUse(GL2 inGL);
    public void stopProgramUse(GL2 inGL);
    public void setScreenSizeDivisionFactor(int inDivisionFactor);
    public int getScreenSizeDivisionFactor();
    public void setNumberOfIterations(int inNumberOfIterations);
    public int getNumberOfIterations();
    public String toString();

}
