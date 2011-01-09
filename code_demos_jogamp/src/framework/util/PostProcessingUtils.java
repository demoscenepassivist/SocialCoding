package framework.util;

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
 ** Utility methods dealing with postprocessing filter setup, configuration and chaining.
 **
 **/

import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import framework.base.*;
import framework.jogl.postprocessingblenders.*;
import framework.jogl.postprocessingfilters.*;

public class PostProcessingUtils {

    public static final int POSTPROCESSINGFILTER_PREFILTER_NOOP = 0;
    public static final int POSTPROCESSINGFILTER_PREFILTER_BRIGHTNESS = 1;
    public static final int POSTPROCESSINGFILTER_PREFILTER_GRAYSCALE = 2;
    public static final int POSTPROCESSINGFILTER_PREFILTER_SATURATION = 3;
    public static final int POSTPROCESSINGFILTER_PREFILTER_COLORINVERT = 4;
    public static final int POSTPROCESSINGFILTER_PREFILTER_GRAYINVERT = 5;
    public static final int POSTPROCESSINGFILTER_PREFILTER_SEPIA = 6;
    public static final int POSTPROCESSINGFILTER_PREFILTER_ANALOGDISTORTION = 7;

    public static ArrayList<BasePostProcessingFilterChainShaderInterface> generatePreFilterArrayList(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        ArrayList<BasePostProcessingFilterChainShaderInterface> tPreFilters = new ArrayList<BasePostProcessingFilterChainShaderInterface>();
        BasePostProcessingFilterChainShaderInterface tNoOp = new PostProcessingFilter_NoOp();
        tNoOp.setScreenSizeDivisionFactor(1);
        tNoOp.initFilter(inGL);
        tPreFilters.add(tNoOp);
        PostProcessingFilter_Brightness tBrightness = new PostProcessingFilter_Brightness();
        tBrightness.setNumberOfIterations(1);
        tBrightness.setScreenSizeDivisionFactor(4);
        tBrightness.setBrightness(0.33f);
        tBrightness.initFilter(inGL);
        tPreFilters.add(tBrightness);
        BasePostProcessingFilterChainShaderInterface tGrayScale = new PostProcessingFilter_GrayScale();
        tGrayScale.setNumberOfIterations(1);
        tGrayScale.setScreenSizeDivisionFactor(1);
        tGrayScale.initFilter(inGL);
        tPreFilters.add(tGrayScale);
        PostProcessingFilter_Saturation tSaturation = new PostProcessingFilter_Saturation();
        tSaturation.setNumberOfIterations(1);
        tSaturation.setScreenSizeDivisionFactor(1);
        tSaturation.setSaturation(0.33f);
        tSaturation.initFilter(inGL);
        tPreFilters.add(tSaturation);
        BasePostProcessingFilterChainShaderInterface tColorInvert = new PostProcessingFilter_ColorInvert();
        tColorInvert.setNumberOfIterations(1);
        tColorInvert.setScreenSizeDivisionFactor(1);
        tColorInvert.initFilter(inGL);
        tPreFilters.add(tColorInvert);
        BasePostProcessingFilterChainShaderInterface tGrayInvert = new PostProcessingFilter_GrayInvert();
        tGrayInvert.setNumberOfIterations(1);
        tGrayInvert.setScreenSizeDivisionFactor(1);
        tGrayInvert.initFilter(inGL);
        tPreFilters.add(tGrayInvert);
        BasePostProcessingFilterChainShaderInterface tSepia = new PostProcessingFilter_Sepia();
        tSepia.setNumberOfIterations(1);
        tSepia.setScreenSizeDivisionFactor(1);
        tSepia.initFilter(inGL);
        tPreFilters.add(tSepia);
        BasePostProcessingFilterChainShaderInterface tAnalogDistortion = new PostProcessingFilter_AnalogDistortion();
        tAnalogDistortion.setNumberOfIterations(1);
        tAnalogDistortion.setScreenSizeDivisionFactor(1);
        tAnalogDistortion.initFilter(inGL);
        tPreFilters.add(tAnalogDistortion);    
        return tPreFilters;
    }

    public static final int POSTPROCESSINGFILTER_CONVOLUTION_NOOP = 0;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR = 1;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_EROSION = 2;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_DILATION = 3;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_GAUSSIANBLUR_HARDCODED = 4;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_KIRSCHNER_COMPASS = 5;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_LAPLACIAN = 6;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_PREWITT = 7;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_ROBERTS = 8;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_SHARPEN = 9;
    public static final int POSTPROCESSINGFILTER_CONVOLUTION_SOBEL = 10;

    public static ArrayList<BasePostProcessingFilterChainShaderInterface> generatePostProcessingFilterArrayList(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        ArrayList<BasePostProcessingFilterChainShaderInterface> tConvolutions = new ArrayList<BasePostProcessingFilterChainShaderInterface>();
        BasePostProcessingFilterChainShaderInterface tNoOp = new PostProcessingFilter_NoOp();
        tNoOp.setScreenSizeDivisionFactor(1);
        tNoOp.initFilter(inGL);
        tConvolutions.add(tNoOp);
        BasePostProcessingFilterChainShaderInterface tBoxBlur = new PostProcessingFilter_BoxBlur();
        tBoxBlur.setScreenSizeDivisionFactor(4);
        tBoxBlur.initFilter(inGL);
        tConvolutions.add(tBoxBlur);
        BasePostProcessingFilterChainShaderInterface tErosion = new PostProcessingFilter_Erosion();
        tErosion.setScreenSizeDivisionFactor(4);
        tErosion.initFilter(inGL);
        tConvolutions.add(tErosion);
        BasePostProcessingFilterChainShaderInterface tDilation = new PostProcessingFilter_Dilation();
        tDilation.setScreenSizeDivisionFactor(4);
        tDilation.initFilter(inGL);
        tConvolutions.add(tDilation);
        BasePostProcessingFilterChainShaderInterface tGaussianBlur_Hardcoded = new PostProcessingFilter_GaussianBlur_Hardcoded();
        tGaussianBlur_Hardcoded.setScreenSizeDivisionFactor(4);
        tGaussianBlur_Hardcoded.initFilter(inGL);
        tConvolutions.add(tGaussianBlur_Hardcoded);
        BasePostProcessingFilterChainShaderInterface tKirschnerCompass = new PostProcessingFilter_KirschnerCompass();
        tKirschnerCompass.setScreenSizeDivisionFactor(4);
        tKirschnerCompass.initFilter(inGL);
        tConvolutions.add(tKirschnerCompass);
        BasePostProcessingFilterChainShaderInterface tLaplacian = new PostProcessingFilter_Laplacian();
        tLaplacian.setScreenSizeDivisionFactor(4);
        tLaplacian.initFilter(inGL);
        tConvolutions.add(tLaplacian);
        BasePostProcessingFilterChainShaderInterface tPrewitt = new PostProcessingFilter_Prewitt();
        tPrewitt.setScreenSizeDivisionFactor(4);
        tPrewitt.initFilter(inGL);
        tConvolutions.add(tPrewitt);
        BasePostProcessingFilterChainShaderInterface tRoberts = new PostProcessingFilter_Roberts();
        tRoberts.setScreenSizeDivisionFactor(4);
        tRoberts.initFilter(inGL);
        tConvolutions.add(tRoberts);
        BasePostProcessingFilterChainShaderInterface tSharpen = new PostProcessingFilter_Sharpen();
        tSharpen.setScreenSizeDivisionFactor(4);
        tSharpen.initFilter(inGL);
        tConvolutions.add(tSharpen);
        BasePostProcessingFilterChainShaderInterface tSobel = new PostProcessingFilter_Sobel();
        tSobel.setScreenSizeDivisionFactor(4);
        tSobel.initFilter(inGL);
        tConvolutions.add(tSobel);
        return tConvolutions;
    }

    public static final int POSTPROCESSINGFILTER_BLENDER_NOOP = 0;
    public static final int POSTPROCESSINGFILTER_BLENDER_ADD = 1;
    public static final int POSTPROCESSINGFILTER_BLENDER_AVERAGE = 2;
    public static final int POSTPROCESSINGFILTER_BLENDER_DARKEN = 3;
    public static final int POSTPROCESSINGFILTER_BLENDER_LIGHTEN = 4;
    public static final int POSTPROCESSINGFILTER_BLENDER_MULTIPLY = 5;
    public static final int POSTPROCESSINGFILTER_BLENDER_COLORDODGE = 6;
    public static final int POSTPROCESSINGFILTER_BLENDER_COLORBURN = 7;
    public static final int POSTPROCESSINGFILTER_BLENDER_DIFFERENCE = 8;
    public static final int POSTPROCESSINGFILTER_BLENDER_EXCLUSION = 9;
    public static final int POSTPROCESSINGFILTER_BLENDER_HARDLIGHT = 10;
    public static final int POSTPROCESSINGFILTER_BLENDER_INVERSEDIFFRENCE = 11;
    public static final int POSTPROCESSINGFILTER_BLENDER_OPACITY = 12;
    public static final int POSTPROCESSINGFILTER_BLENDER_SCREEN = 13;
    public static final int POSTPROCESSINGFILTER_BLENDER_SOFTLIGHT = 14;
    public static final int POSTPROCESSINGFILTER_BLENDER_SUBSTRACT = 15;

    public static ArrayList<BasePostProcessingFilterChainShaderInterface> generateBlenderFilterArrayList(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        ArrayList<BasePostProcessingFilterChainShaderInterface> tBlenders = new ArrayList<BasePostProcessingFilterChainShaderInterface>();
        BasePostProcessingFilterChainShaderInterface tNoOp = new PostProcessingFilter_NoOp();
        tNoOp.setScreenSizeDivisionFactor(1);
        tNoOp.initFilter(inGL);
        tBlenders.add(tNoOp);
        BasePostProcessingFilterChainShaderInterface tBlender_Add = new PostProcessingFilter_Blender_Add();
        tBlender_Add.initFilter(inGL);
        tBlenders.add(tBlender_Add);
        BasePostProcessingFilterChainShaderInterface tBlender_Average = new PostProcessingFilter_Blender_Average();
        tBlender_Average.initFilter(inGL);
        tBlenders.add(tBlender_Average);
        BasePostProcessingFilterChainShaderInterface tBlender_Darken = new PostProcessingFilter_Blender_Darken();
        tBlender_Darken.initFilter(inGL);
        tBlenders.add(tBlender_Darken);
        BasePostProcessingFilterChainShaderInterface tBlender_Lighten = new PostProcessingFilter_Blender_Lighten();
        tBlender_Lighten.initFilter(inGL);
        tBlenders.add(tBlender_Lighten);
        BasePostProcessingFilterChainShaderInterface tBlender_Multiply = new PostProcessingFilter_Blender_Multiply();
        tBlender_Multiply.initFilter(inGL);
        tBlenders.add(tBlender_Multiply);
        BasePostProcessingFilterChainShaderInterface tBlender_ColorDodge = new PostProcessingFilter_Blender_ColorDodge();
        tBlender_ColorDodge.initFilter(inGL);
        tBlenders.add(tBlender_ColorDodge);
        BasePostProcessingFilterChainShaderInterface tBlender_ColorBurn = new PostProcessingFilter_Blender_ColorBurn();
        tBlender_ColorBurn.initFilter(inGL);
        tBlenders.add(tBlender_ColorBurn);
        BasePostProcessingFilterChainShaderInterface tBlender_Difference = new PostProcessingFilter_Blender_Difference();
        tBlender_Difference.initFilter(inGL);
        tBlenders.add(tBlender_Difference);
        BasePostProcessingFilterChainShaderInterface tBlender_Exclusion = new PostProcessingFilter_Blender_Exclusion();
        tBlender_Exclusion.initFilter(inGL);
        tBlenders.add(tBlender_Exclusion);
        BasePostProcessingFilterChainShaderInterface tBlender_HardLight = new PostProcessingFilter_Blender_HardLight();
        tBlender_HardLight.initFilter(inGL);
        tBlenders.add(tBlender_HardLight);
        BasePostProcessingFilterChainShaderInterface tBlender_InverseDifference = new PostProcessingFilter_Blender_InverseDifference();
        tBlender_InverseDifference.initFilter(inGL);
        tBlenders.add(tBlender_InverseDifference);
        BasePostProcessingFilterChainShaderInterface tBlender_Opacity = new PostProcessingFilter_Blender_Opacity();
        tBlender_Opacity.initFilter(inGL);
        tBlenders.add(tBlender_Opacity);
        BasePostProcessingFilterChainShaderInterface tBlender_Screen = new PostProcessingFilter_Blender_Screen();
        tBlender_Screen.initFilter(inGL);
        tBlenders.add(tBlender_Screen);
        BasePostProcessingFilterChainShaderInterface tBlender_SoftLight = new PostProcessingFilter_Blender_SoftLight();
        tBlender_SoftLight.initFilter(inGL);
        tBlenders.add(tBlender_SoftLight);
        BasePostProcessingFilterChainShaderInterface tBlender_Substract = new PostProcessingFilter_Blender_Substract();
        tBlender_Substract.initFilter(inGL);
        tBlenders.add(tBlender_Substract);
        return tBlenders;
    }

}
