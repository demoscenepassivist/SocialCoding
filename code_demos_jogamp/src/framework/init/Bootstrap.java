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
 ** Bootstrapping class (application entry point) used for initializing, commandline parameter handling
 ** (mainly parsing) and environment setup. 
 **
 **  Possible commandline parameters are:
 **   -RESOLUTION (=640x480/1280x1024/1920x1200/...)
 **   -ROUTINE (=jogamp.routine.jogl.fixedfunctionpipeline.GL2_DisplayLists/...)	
 **   -FRAMERATE (=15/30/60/.../MAX)
 **   -FULLSCREEN (=TRUE/FALSE)
 **   -MULTISAMPLING (=TRUE/FALSE)
 **   -SAMPLEBUFFERS (=1/2/4)
 **   -ANISOTROPICFILTERING (=TRUE/FALSE)
 **   -ANISOTROPYLEVEL (=1.0/2.0/4.0/8.0/16.0)
 **   -FRAMECAPTURE (=TRUE/FALSE)
 **   -VSYNC (=TRUE/FALSE)
 **   -FRAMESKIP (=TRUE/FALSE)
 **   -WINDOWTOOLKIT (=AWT/NEWT)
 **   -MUSIC=/binaries/music/Little_Bitchard-The_Code_Inside.mp3
 **
 **/

import javax.media.opengl.*;
import java.awt.*;
import framework.base.*;

public class Bootstrap {

    static {
        //stg dirty I dont wanna think about any further X-) ...
        GLProfile.initSingleton();
    }

    public static void main(String args[]) throws Exception {
        int tResolutionX = -1;
        int tResolutionY = -1;
        String tRoutineClassName = null;
        int tFrameRate = -1;
        boolean tFullScreen = true;
        boolean tMultiSampling = false;
        int tNumberOfSampleBuffers = -1;
        boolean tAnisotropicFiltering = false;
        float tAnisotropyLevel = -1.0f;
        boolean tFrameCapture = false;
        boolean tVSync = true;
        boolean tFrameSkip = true;
        String tWindowToolkitName = null;
        String tMusicFileName = null;
        if (args.length>0) {
            for (int i=0; i<args.length; i++) {
                BaseLogging.getInstance().info("PROCESSING CMDLINE PARAMETER ... ARG="+args[i]);
                if (args[i].trim().startsWith("-RESOLUTION=")) {
                    String tResolutionParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("CUSTOM RESOLUTION SET TO '"+tResolutionParameter+"'");
                    if (!tResolutionParameter.equalsIgnoreCase("auto")) {
                        String tXResolution = tResolutionParameter.substring(0,tResolutionParameter.indexOf("x"));
                        String tYResolution = tResolutionParameter.substring(tResolutionParameter.indexOf("x")+1,tResolutionParameter.length());
                        BaseLogging.getInstance().info("PARSED VALUES ARE X="+tXResolution+" Y="+tYResolution);
                        tResolutionX = Integer.parseInt(tXResolution);
                        tResolutionY = Integer.parseInt(tYResolution);
                    }
                } else if(args[i].trim().startsWith("-ROUTINE=")) {
                    String tRoutineParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("ROUTINE TO EXECUTE '"+tRoutineParameter+"'");
                    tRoutineClassName = tRoutineParameter;
                } else if(args[i].trim().startsWith("-FRAMERATE=")) {
                    String tFrameRateParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("CUSTOM FRAMERATE SET TO '"+tFrameRateParameter+"'");
                    if (tFrameRateParameter.equalsIgnoreCase("auto")) {
                        tFrameRate = -1;
                    } else if (tFrameRateParameter.equalsIgnoreCase("max")) {
                        tFrameRate = Integer.MAX_VALUE;
                    } else {
                        tFrameRate = Integer.parseInt(tFrameRateParameter);
                    }
                } else if(args[i].trim().startsWith("-FULLSCREEN=")) {
                    String tFullScreenParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("FULLSCREEN ENABLED '"+tFullScreenParameter+"'");
                    tFullScreen = Boolean.parseBoolean(tFullScreenParameter);
                } else if(args[i].trim().startsWith("-MULTISAMPLING=")) {
                    String tMultiSamplingParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("MULTISAMPLING ENABLED '"+tMultiSamplingParameter+"'");
                    tMultiSampling = Boolean.parseBoolean(tMultiSamplingParameter);
                } else if(args[i].trim().startsWith("-SAMPLEBUFFERS=")) {
                    String tSampleBuffersParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("CUSTOM NUMBER OF SAMPLEBUFFERS SET TO '"+tSampleBuffersParameter+"'");
                    tNumberOfSampleBuffers = Integer.parseInt(tSampleBuffersParameter);	
                } else if(args[i].trim().startsWith("-ANISOTROPICFILTERING=")) {
                    String tAnisotropicFilteringParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("ANISOTROPICFILTERING ENABLED '"+tAnisotropicFilteringParameter+"'");
                    tAnisotropicFiltering = Boolean.parseBoolean(tAnisotropicFilteringParameter);
                } else if(args[i].trim().startsWith("-ANISOTROPYLEVEL=")) {
                    String tAnisotropyLevelParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("CUSTOM ANISOTROPY LEVEL SET TO '"+tAnisotropyLevelParameter+"'");
                    tAnisotropyLevel = Float.parseFloat(tAnisotropyLevelParameter);
                } else if(args[i].trim().startsWith("-FRAMECAPTURE=")) {
                    String tFrameCaptureParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("FRAME CAPTURE ENABLED '"+tFrameCaptureParameter+"'");
                    tFrameCapture = Boolean.parseBoolean(tFrameCaptureParameter);
                } else if(args[i].trim().startsWith("-VSYNC=")) {
                    String tVSyncParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("VSYNC ENABLED '"+tVSyncParameter+"'");
                    tVSync = Boolean.parseBoolean(tVSyncParameter);
                } else if(args[i].trim().startsWith("-FRAMESKIP=")) {
                    String tFrameSkipParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("FRAMESKIP ENABLED '"+tFrameSkipParameter+"'");
                    tFrameSkip = Boolean.parseBoolean(tFrameSkipParameter);
                } else if(args[i].trim().startsWith("-WINDOWTOOLKIT=")) {
                    String tWindowToolkitParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("WINDOW TOOLKIT TO USE '"+tWindowToolkitParameter+"'");
                    tWindowToolkitName = tWindowToolkitParameter;
                } else if(args[i].trim().startsWith("-MUSIC=")) {
                    String tMusicParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("MUSIC TO PLAY '"+tMusicParameter+"'");
                    tMusicFileName = tMusicParameter;
                } else {
                    BaseLogging.getInstance().error("ERROR! ILLEGAL ARGUMENT FOUND! ARGUMENT="+args[i]);
                }
            }
            BaseLogging.getInstance().info("CMDLINE PARAMETERS PARSED AND CONVERTED ...");
        }
        BaseGlobalEnvironment.getInstance().configureWithUserParameters(
                tRoutineClassName,
                tResolutionX,tResolutionY,
                tFrameRate,
                tFullScreen,
                tMultiSampling,tNumberOfSampleBuffers,
                tAnisotropicFiltering,tAnisotropyLevel,
                tFrameCapture,
                tVSync,
                tFrameSkip,
                tWindowToolkitName,
                tMusicFileName
        );
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                BaseGlobalEnvironment.getInstance().initGLEnvironment();
            }
        });
    }

}
