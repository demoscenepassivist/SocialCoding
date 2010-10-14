package jogamp.routine.jogl.programmablepipeline;

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
 ** This routine demonstrates fragment shader based custom FSAA also known as "super sampling".
 ** It makes use of the BaseSuperSamplingFBOWrapper utility helper class wich handles all the
 ** details of custom FSAA. Other than that this routine is a carbon copy of the programmable
 ** pipeline fractal shader demonstration GL3_Mandelbrot.
 **
 **/

import framework.base.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;

public class GL3_SuperSampling_Mandelbrot extends GL3_Mandelbrot implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {
    
    private BaseSuperSamplingFBOWrapper mBaseSuperSamplingFBOWrapper;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        super.initRoutine(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper = new BaseSuperSamplingFBOWrapper(2.0f,this);
        mBaseSuperSamplingFBOWrapper.init(inGL,inGLU,inGLUT);
    }
    
    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseSuperSamplingFBOWrapper.executeToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
    }
    
    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        super.cleanupRoutine(inGL,inGLU,inGLUT);
        mBaseSuperSamplingFBOWrapper.cleanup(inGL,inGLU,inGLUT);
    }
    
}
