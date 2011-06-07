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
 ** Simple adapter implementation of the GLEventListener interface added to the GLAutoDrawable
 ** initialized by the BaseGlobalEnvironment. Does some basic environment setup but mainly 
 ** forewards the inferface calls to the BaseRoutineInterface encapsulated/handled by the
 ** BaseRoutineRuntime.
 **
 **/

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;

public class BaseGLEventListener implements GLEventListener {

    private GLU mGLU;
    private GLUT mGLUT;
    private BaseRoutineRuntime mBaseRoutineRuntime;

    public BaseGLEventListener() {
        mGLU = new GLU();
        mGLUT = new GLUT();
        mBaseRoutineRuntime = BaseRoutineRuntime.getInstance();
    }

    public BaseRoutineRuntime getBaseRoutineRuntime() {
        return mBaseRoutineRuntime;
    }

    public void init(GLAutoDrawable inDrawable) {
        GL2 tGL = inDrawable.getGL().getGL2();
        if (BaseGlobalEnvironment.getInstance().wantsVSync()) {
            //enable vertical sync ... :*
            tGL.setSwapInterval(1);
        }
        mBaseRoutineRuntime.initRuntime(tGL,mGLU,mGLUT);
    }

    public void display(GLAutoDrawable inDrawable) {        
        GL2 tGL = inDrawable.getGL().getGL2();
        mBaseRoutineRuntime.mainLoopRuntime(tGL,mGLU,mGLUT);
    }

    public void reshape(GLAutoDrawable inDrawable, int inX, int inY, int inWidth, int inHeight) {
        BaseLogging.getInstance().warning("RESHAPE CALLED ON GLAUTODRAWALBE! X="+inX+" Y="+inY+" WIDTH="+inWidth+" HEIGHT="+inHeight);
    }

    public void displayChanged(GLAutoDrawable inDrawable, boolean inModeChanged, boolean inDeviceChanged) {
        BaseLogging.getInstance().warning("DISPLAYCHANGED CALLED ON GLAUTODRAWALBE! MODECHANGED="+inModeChanged+" DEVICECHANGED="+inDeviceChanged);
    }

    public void dispose(GLAutoDrawable inDrawable) {
        BaseLogging.getInstance().warning("DISPOSE CALLED ON GLAUTODRAWALBE!");
        GL2 tGL = inDrawable.getGL().getGL2();
        mBaseRoutineRuntime.cleanupRuntime(tGL,mGLU,mGLUT);
    }

}
