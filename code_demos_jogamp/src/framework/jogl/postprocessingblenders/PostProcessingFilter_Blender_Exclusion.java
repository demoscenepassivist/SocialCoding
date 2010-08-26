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
 ** Postprocessing filter implementing basic 'EXCLUSION' blending mode. For explanation of the different
 ** blending modes see the original Porter-Duff paper: http://dev.processing.org/bugs/attachment.cgi?id=71
 ** or for more up2date formulas take a look here: http://www.nathanm.com/photoshop-blending-math/ and
 ** here http://dunnbypaul.net/blends/
 **
 **/

import javax.media.opengl.GL2;
import framework.base.*;

public class PostProcessingFilter_Blender_Exclusion extends PostProcessingFilter_Blender_Base implements BasePostProcessingFilterChainShaderInterface {

    public void initFilter(GL2 inGL) {
        mFragmentShaderFileName = "/shaders/postprocessingblenders/PostProcessingFilter_Blender_Exclusion.fs";
        super.initFilter(inGL);
    }

}
