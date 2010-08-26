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
 ** Adapter for the BaseRoutineInterface to be implemented by all demonstration routines. Just
 ** for convenience if one or more methods do not need to be implemented +<||:-)
 **
 **/

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;

public class BaseRoutineAdapter implements BaseRoutineInterface {

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //Zzzz ... =|:-)
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //Zzzz ... 7:^]
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //Zzzz ... =:o]
    }

}
