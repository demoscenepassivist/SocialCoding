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
 ** Utilities to change display mode, obtain 'preferred', aspect ratio correct display
 ** mode and query graphics device capabilities. Also provides a method to hide the cursor
 ** in fullscreen mode. In addition to the utility methods the most widely used display modes
 ** for different aspect ratios a available as predefined constants.
 **
 **/

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public final class BaseWindowToolkitUtils {

    private static final double AUTOMATIC_RESOLUTION_TOLERANCE = 0.3f;

    /*
    These are the results of Steam hardware survey of August 2009.

    SXGA   1280×1024    21.26%  5:4
    WSXGAP 1680×1050    18.12%  16:10
    WSXGA  1440×900     11.44%  16:10
    XGA    1024×768     17.17%  4:3
    WXGA   1280×800     08.22%  16:10
    WUXGA  1920×1200    05.43%  16:10
    1080p  1920×1080    03.80%  16:10
    WXGA   1152×864     03.06%  4:3
    SXGA-  1280×960     02.01%  4:3
    WXGA   1366×768     01.81%  ~16:9
    UXGA   1600×1200    01.13%  4:3
    WXGA   1280×768     00.96%  5:3
    --------------------------
    Other               02.57%

    For a complete list of display modes see: http://en.wikipedia.org/wiki/List_of_common_resolutions
                                     ... and: http://en.wikipedia.org/wiki/Computer_display_standard

    Breakpoint 2k9 rules (http://breakpoint.untergrund.net/compos_pc.php):
    Supported resolutions are 640x480 (4:3), 1024x768 (4:3), 1280x720 (16:9) and 1920x1080 (16:9) pixels at 60 Hz. 
    We won't have a video mixer or competition video recording this time, so any other resolutions or refresh rates
    will require manual adjustment of the projector while the demo is running. You have been warned.

    All entries will be shown using aspect-ratio preserving stretch; that is, 4:3 and 16:10 video modes will be
    letterboxed (with a black border at the left and right sides), while 16:9 images get to use the full area of
    the big screen. We will not stretch 4:3 demos horizontally to fill the whole screen. Most importantly, do not
    set a 4:3 video mode then perform letterboxing yourself (it will needlessly cause your entry to be shown smaller
    than it needs to be).
     */

    /*
    Performance considerations for different resolutions:
    640x480   =   307.200pel = 1.00x
    800x600   =   480.000pel = 1,56x
    1024x768  =   786.432pel = 2,56x
    1280x1024 = 1.310.720pel = 4,26x
    1600x1200 = 1.920.000pel = 6,25x
    1920x1200 = 2.304.000pel = 7,50x
     */

    //4:3+5:4+5:3 modes
    public static final DisplayMode DISPLAYMODE_QVGA_320x240    = new DisplayMode(320,240,32,60);   //4:3 Quarter Video Graphics Array
    public static final DisplayMode DISPLAYMODE_VGA_640x480     = new DisplayMode(640,480,32,60);   //4:3 Video Graphics Array
    public static final DisplayMode DISPLAYMODE_SVGA_800x600    = new DisplayMode(800,600,32,60);   //4:3 Super Video Graphics Array
    public static final DisplayMode DISPLAYMODE_XGA_1024x768    = new DisplayMode(1024,768,32,60);  //4:3 Extended Graphics Array
    public static final DisplayMode DISPLAYMODE_XGAP_1152x864   = new DisplayMode(1152,864,32,60);  //4:3 Extended Graphics Array Plus
    public static final DisplayMode DISPLAYMODE_WXGA_1280x768   = new DisplayMode(1280,768,32,60);  //5:3 Wide Extended Graphics Array 
    public static final DisplayMode DISPLAYMODE_SXGAM_1280x960  = new DisplayMode(1280,960,32,60);  //4:3 Super Extended Graphics Array Minus
    public static final DisplayMode DISPLAYMODE_SXGA_1280x1024  = new DisplayMode(1280,1024,32,60); //5:4 Super Extended Graphics Array
    public static final DisplayMode DISPLAYMODE_UXGA_1600x1200  = new DisplayMode(1600,1200,32,60); //4:3 Ultra Extended Graphics Array
    public static final DisplayMode DISPLAYMODE_QXGA_2048x1536  = new DisplayMode(2048,1536,32,60); //4:3 Quad Extended Graphics Array

    //16:10 modes
    public static final DisplayMode DISPLAYMODE_WXGAM_1280x720  = new DisplayMode(1280,720,32,60);  //16:9  Wide Extended Graphics Array Minus
    public static final DisplayMode DISPLAYMODE_WXGA_1280x800   = new DisplayMode(1280,800,32,60);  //16:10 Wide Extended Graphics Array
    public static final DisplayMode DISPLAYMODE_WXGA_1366x768   = new DisplayMode(1366,768,32,60);  //~16:9 Wide Extended Graphics Array 	
    public static final DisplayMode DISPLAYMODE_WXGAP_1440x900  = new DisplayMode(1440,900,32,60);  //16:10 Wide Extended Graphics Array Plus
    public static final DisplayMode DISPLAYMODE_WSXGA_1600x1024 = new DisplayMode(1600,1024,32,60); //16:10 Wide Super Extended Graphics Array
    public static final DisplayMode DISPLAYMODE_WSXGAP_1680x1050= new DisplayMode(1680,1050,32,60); //16:10 Wide Super Extended Graphics Array Plus
    public static final DisplayMode DISPLAYMODE_WUXGA_1920x1200 = new DisplayMode(1920,1200,32,60); //16:10 Wide Ultra Extended Graphics Array
    public static final DisplayMode DISPLAYMODE_WQXGA_2560x1600	= new DisplayMode(2560,1600,32,60); //16:10 Wide Quad Extended Graphics Array
    
    //16:9 hd modes
    public static final DisplayMode DISPLAYMODE_HD_1080p        = new DisplayMode(1920,1080,32,60); //16:9
    public static final DisplayMode DISPLAYMODE_HD_720p         = new DisplayMode(1280,720,32,60);  //16:9

    //kb modes (http://breakpoint.untergrund.net/miscbigscreen.php)
    public static final DisplayMode DISPLAYMODE_KB_800x600      = DISPLAYMODE_SVGA_800x600;         //(for captures and TV outs)
    public static final DisplayMode DISPLAYMODE_KB_1024x768     = DISPLAYMODE_XGA_1024x768;         //(for standard video projectors)
    public static final DisplayMode DISPLAYMODE_KB_1280x720     = DISPLAYMODE_HD_720p;              //1280x720 (HDTV 720p)
    public static final DisplayMode DISPLAYMODE_KB_1280x800     = DISPLAYMODE_WXGA_1280x800;        //1280x800 (some laptops)
    public static final DisplayMode DISPLAYMODE_KB_1280x1024    = DISPLAYMODE_SXGA_1280x1024;       //1280x1024 (17" and 19" desktop LCD screens)
    public static final DisplayMode DISPLAYMODE_KB_1368x768     = DISPLAYMODE_WXGA_1366x768;        //(so-called “HD ready” TVs)
    public static final DisplayMode DISPLAYMODE_KB_1440x900     = DISPLAYMODE_WXGAP_1440x900;       //(some other laptops)
    public static final DisplayMode DISPLAYMODE_KB_1600x1200    = DISPLAYMODE_UXGA_1600x1200;       //(some 20" LCD screens)
    public static final DisplayMode DISPLAYMODE_KB_1680x1050    = DISPLAYMODE_WSXGAP_1680x1050;     //(most 20"/22" desktop LCD wide screens and 17" laptops)
    public static final DisplayMode DISPLAYMODE_KB_1920x1080    = DISPLAYMODE_HD_1080p;             //(HDTV 1080p, Breakpoint big screen)
    public static final DisplayMode DISPLAYMODE_KB_1920x1200    = DISPLAYMODE_WUXGA_1920x1200;      //(24" desktop screens, good 17" laptops)

//preferred display modes ... width MUST be always the same ...
    public static DisplayMode[] DEFAULT_DISPLAYMODES = new DisplayMode[] { 
        DISPLAYMODE_WXGAM_1280x720,
        DISPLAYMODE_WXGA_1280x768,
        DISPLAYMODE_WXGA_1280x800,
        DISPLAYMODE_SXGAM_1280x960,
        DISPLAYMODE_SXGA_1280x1024
    };

    //emergency backup display modes if none of the default displaymodes is supported ...
    public static DisplayMode[] BACKUP_DISPLAYMODES = new DisplayMode[] {
        DISPLAYMODE_XGA_1024x768,
        DISPLAYMODE_XGAP_1152x864,
        DISPLAYMODE_WXGA_1366x768,
        DISPLAYMODE_WXGAP_1440x900
    };

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    public static Cursor createHiddenCursor() {
        Toolkit tToolkit = Toolkit.getDefaultToolkit();
        Dimension tDimension = tToolkit.getBestCursorSize(1, 1);
        BufferedImage tCursorImage = new BufferedImage(tDimension.width, tDimension.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tGraphics2D = tCursorImage.createGraphics();
        tGraphics2D.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        tGraphics2D.clearRect(0, 0, tDimension.width, tDimension.height);
        tGraphics2D.dispose();
        Cursor tHiddenCursor = tToolkit.createCustomCursor(tCursorImage, new Point(0,0), "HiddenCursor");
        return tHiddenCursor;
    }

    public static DisplayMode getBestDisplayModeWithBackupModes(
            DisplayMode[] inDefaultDisplayModes,
            DisplayMode[] inBackupDisplayModes,
            DisplayMode inDesktopDisplayMode) {
        DisplayMode tPreferredDisplayMode = getBestDisplayMode(DEFAULT_DISPLAYMODES,inDesktopDisplayMode);
        if (tPreferredDisplayMode==inDesktopDisplayMode) {
            BaseLogging.getInstance().warning("PREFERRED DISPLAYMODES NOT SUPPORTED ... TRYING BACKUP DISPLAYMODES ...");
            DisplayMode tBackupDisplayMode = getBestDisplayMode(BACKUP_DISPLAYMODES,inDesktopDisplayMode);
            return tBackupDisplayMode;
        } else {
            return tPreferredDisplayMode;
        }
    }

    public static DisplayMode getBestDisplayMode(DisplayMode[] inDisplayModes,DisplayMode inDesktopDisplayMode) {
        BaseLogging.getInstance().info("SETTING BEST POSSIBLE DISPLAYMODE ...");
        double tWidthScalingFactor = getResolutionScalingFactor(inDisplayModes[0].getWidth(),inDesktopDisplayMode.getWidth());
        double tHeightScalingFactor = getResolutionScalingFactor(inDisplayModes[0].getHeight(),inDesktopDisplayMode.getHeight());
        BaseLogging.getInstance().info("WIDTH SCALING FACTOR="+tWidthScalingFactor+" HEIGHT SCALING FACTOR="+tHeightScalingFactor);
        if (tWidthScalingFactor>(1.0f+AUTOMATIC_RESOLUTION_TOLERANCE) && tWidthScalingFactor>(1.0f+AUTOMATIC_RESOLUTION_TOLERANCE)) {
            BaseLogging.getInstance().info("RESOLUTION SCALING FACTOR LARGER THAN THRESHOLD ... TRYING DEFAULT_DISPLAYMODES ...");
            DisplayMode tDisplayMode = null;
            double tDesktopAspectRatio = getAspectRatio(inDesktopDisplayMode);
            double tAspectRatioDifference = (double)Integer.MAX_VALUE;
            for (int i=0; i<inDisplayModes.length; i++) {
                if (isDisplayModeSupported(inDisplayModes[i])) {
                    BaseLogging.getInstance().info("DISPLAYMODE SUPPORTED "+convertDisplayModeToString(inDisplayModes[i]));
                    double tCurrentAspectRatioDifference = Math.abs(tDesktopAspectRatio-getAspectRatio(inDisplayModes[i]));
                    BaseLogging.getInstance().info("AR DIFFERENCE="+tCurrentAspectRatioDifference);
                    if (tAspectRatioDifference>tCurrentAspectRatioDifference) {
                        BaseLogging.getInstance().info("CURRENT BEST DISPLAYMODE "+convertDisplayModeToString(inDisplayModes[i]));
                        tDisplayMode = inDisplayModes[i];
                        tAspectRatioDifference = tCurrentAspectRatioDifference;
                    }
                } else {
                    BaseLogging.getInstance().info("SKIPPING UNSUPPORTED DISPLAYMODE="+convertDisplayModeToString(inDisplayModes[i]));
                }
            }
            BaseLogging.getInstance().info("BEST AR SUPPORTED DISPLAYMODE="+convertDisplayModeToString(tDisplayMode));
            if (tDisplayMode!=null) {
                return tDisplayMode;
            } else {
                return inDesktopDisplayMode;
            }
        } else {
            BaseLogging.getInstance().info("RESOLUTION SCALING FACTOR SMALLER THAN THRESHOLD ... USING DESKTOP RESOLUTION ...");
            return inDesktopDisplayMode;
        }
    }

    public static boolean isDisplayModeSupported(DisplayMode inDisplayMode) {
        DisplayMode[] tDisplayModes = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayModes();
        for (int i = 0; i < tDisplayModes.length; i++) {
            if (tDisplayModes[i].getWidth() == inDisplayMode.getWidth() && 
                tDisplayModes[i].getHeight() == inDisplayMode.getHeight()&& 
                tDisplayModes[i].getBitDepth() == inDisplayMode.getBitDepth()) 
            {
                return true;
            }
        }
        return false;
    }

    public static String convertDisplayModeToString(DisplayMode inDisplayMode) {
        StringBuilder tStringBuilder = new StringBuilder();
        tStringBuilder.append(inDisplayMode.getWidth());
        tStringBuilder.append("x");
        tStringBuilder.append(inDisplayMode.getHeight());
        tStringBuilder.append("x");
        tStringBuilder.append(inDisplayMode.getBitDepth());
        tStringBuilder.append("@");
        tStringBuilder.append(inDisplayMode.getRefreshRate());
        tStringBuilder.append(" AR=");
        tStringBuilder.append(getAspectRatio(inDisplayMode));
        return tStringBuilder.toString();
    }

    public static double getAspectRatio(DisplayMode inDisplayMode) {
        return (double)inDisplayMode.getWidth()/(double)inDisplayMode.getHeight();
    }

    public static void printGraphicsCapabilities() {
        GraphicsDevice tGD = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDevice();
        BaseLogging.getInstance().info("---------------------------------------------");
        BaseLogging.getInstance().info("GRAPHICS CONFIGURATION CAPAILITIES:");
        BaseLogging.getInstance().info("---------------------------------------------");
        BaseLogging.getInstance().info("  -GRAPHICS DEVICE: ");
        BaseLogging.getInstance().info("    -AvailableAcceleratedMemory="+tGD.getAvailableAcceleratedMemory());
        BaseLogging.getInstance().info("    -IDstring="+tGD.getIDstring());
        BaseLogging.getInstance().info("    -isDisplayChangeSupported="+tGD.isDisplayChangeSupported());
        BaseLogging.getInstance().info("    -isFullScreenSupported="+tGD.isFullScreenSupported());
        BaseLogging.getInstance().info("---------------------------------------------");
        BaseLogging.getInstance().info("DISPLAY MODES (NON 60hz 32Bit MODES OMITTED):");
        BaseLogging.getInstance().info("---------------------------------------------");
        DisplayMode tDisplayModes[] = getSupportedDisplayModes();
        for(int i=0; i<tDisplayModes.length; i++) {
            BaseLogging.getInstance().info("["+i+"] "+BaseWindowToolkitUtils.convertDisplayModeToString(tDisplayModes[i]));
        }
        BaseLogging.getInstance().info("---------------------------------------------");
    }

    public static double getResolutionScalingFactor(int inResolution1, int inResolution2) {
        double tOnePixelPercent = 100.0d/inResolution1;
        int tResolutionDifference = inResolution1-inResolution2;
        if (tResolutionDifference<=0) {
            return 1.0d+(Math.abs(tResolutionDifference*tOnePixelPercent)/100.0d);
        } else {
            return 1.0d-Math.abs(tResolutionDifference*tOnePixelPercent)/100.0d;
        }
    }

    public static DisplayMode[] getSupportedDisplayModes() {
        GraphicsDevice tGD = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        ArrayList<DisplayMode> tSupportedDisplayModes_ArrayList = new ArrayList<DisplayMode>();
        DisplayMode tDisplayModes[] = tGD.getDisplayModes();
        for(int i=0; i<tDisplayModes.length; i++) {
            if (tDisplayModes[i].getRefreshRate()==60 && tDisplayModes[i].getBitDepth()==32) {
                tSupportedDisplayModes_ArrayList.add(tDisplayModes[i]);
            }
        }
        DisplayMode[] tSupportedDisplayModes_Array = new DisplayMode[tSupportedDisplayModes_ArrayList.size()];
        return tSupportedDisplayModes_ArrayList.toArray(tSupportedDisplayModes_Array);
    }

}
