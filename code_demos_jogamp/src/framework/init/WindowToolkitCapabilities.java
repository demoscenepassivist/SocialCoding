package framework.init;

import java.util.*;
import com.jogamp.newt.*;
import framework.base.*;

public class WindowToolkitCapabilities {

    public static void main(String args[]) throws Exception {
        BaseLogging.getInstance().info("LOGGING BASIC WINDOW TOOLKIT CAPABILITIES ...");
        BaseLogging.getInstance().info("--------------------------------------------------------------------------------");
        logBasicNEWTCapabilities();
        BaseLogging.getInstance().info("--------------------------------------------------------------------------------");
        logBasicAWTCapabilities();
    }
    
    public static void logBasicNEWTCapabilities() {
        BaseLogging.getInstance().info("LOGGING BASIC NEWT CAPABILITIES ...");
        StringBuffer tStringBuffer = new StringBuffer();
        NewtVersion.getInstance().getFullManifestInfo(tStringBuffer);
        BaseLogging.getInstance().info("NEWT MANIFEST INFORMATION:");
        BaseLogging.getInstance().info(tStringBuffer.toString());       
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
