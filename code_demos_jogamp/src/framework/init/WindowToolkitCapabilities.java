package framework.init;

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
 ** Small helper class wich main purpose is to list the capabilities from NEWT and AWT without the
 ** overhead of the full framework initialization. Greatly helps with diagnosing basic window
 ** toolkit problems.
 **
 **/

import java.util.*;
import javax.media.opengl.*;
import com.jogamp.newt.*;
import framework.base.*;

public class WindowToolkitCapabilities {

    static {
        //stg dirty I dont wanna think about any further X-) ...
        GLProfile.initSingleton(true);
    }

    public static void main(String args[]) throws Exception {
        BaseLogging.getInstance().info("LOGGING BASIC WINDOW TOOLKIT CAPABILITIES ...");
        BaseLogging.getInstance().info("--------------------------------------------------------------------------------");
        logBasicNEWTCapabilities();
        BaseLogging.getInstance().info("--------------------------------------------------------------------------------");
        logBasicAWTCapabilities();
    }
    
    public static void logBasicNEWTCapabilities() {
        BaseLogging.getInstance().info("LOGGING BASIC NEWT CAPABILITIES ...");
        BaseLogging.getInstance().info("ALL NEWT DISPLAYS:");
        Collection<?> tDisplays = Display.getAllDisplays();
        Iterator<?> tDisplayIterator = tDisplays.iterator();
        while (tDisplayIterator.hasNext()) {
            Display tDisplay = (Display)tDisplayIterator.next();
            BaseLogging.getInstance().info("DISPLAY ID="+tDisplay.getId()+" NAME="+tDisplay.getName()+" TYPE="+tDisplay.getType());      
        }
        BaseLogging.getInstance().info("ALL NEWT SCREENS:");
        Collection<?> tScreens = Screen.getAllScreens();
        Iterator<?> tScreenIterator = tScreens.iterator();
        while (tScreenIterator.hasNext()) {
            Screen tScreen = (Screen)tScreenIterator.next();
            BaseLogging.getInstance().info("SCREEN INDEX="+tScreen.getIndex()+" WIDTH="+tScreen.getWidth()+" HEIGHT="+tScreen.getHeight());
            ScreenMode tCurrentScreenMode = tScreen.getCurrentScreenMode();
            BaseLogging.getInstance().info(
                    "CURRENT SCREEN MODE - REFRESRATE="+tCurrentScreenMode.getMonitorMode().getRefreshRate()+
                    " SCREENSIZEMM="+tCurrentScreenMode.getMonitorMode().getScreenSizeMM()+
                    " SURFACE SIZE="+tCurrentScreenMode.getMonitorMode().getSurfaceSize()
            );
            ScreenMode tOriginalScreenMode = tScreen.getOriginalScreenMode();
            BaseLogging.getInstance().info(
                    "ORIGINAL SCREEN MODE - REFRESRATE="+tOriginalScreenMode.getMonitorMode().getRefreshRate()+
                    " SCREENSIZEMM="+tOriginalScreenMode.getMonitorMode().getScreenSizeMM()+
                    " SURFACE SIZE="+tOriginalScreenMode.getMonitorMode().getSurfaceSize()
            );
            
            java.util.List<?> tScreenModes = tScreen.getScreenModes();
            java.util.ListIterator<?> tScreenModesIterator = tScreenModes.listIterator();
            while (tScreenModesIterator.hasNext()) {
                ScreenMode tScreenMode = (ScreenMode)tScreenModesIterator.next();
                BaseLogging.getInstance().info(
                        "SCREEN MODE - REFRESRATE="+tScreenMode.getMonitorMode().getRefreshRate()+
                        " SCREENSIZEMM="+tScreenMode.getMonitorMode().getScreenSizeMM()+
                        " SURFACE SIZE="+tScreenMode.getMonitorMode().getSurfaceSize()
                );
            }       
        }
    }
    
    public static void logBasicAWTCapabilities() {
        //print AWT graphics capabilities ... values are very unreliable X-)
        BaseWindowToolkitUtils.printGraphicsCapabilities();
    }
    
}
