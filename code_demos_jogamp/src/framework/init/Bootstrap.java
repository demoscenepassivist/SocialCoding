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
 **   -STARTFRAME (=0)
 **   -ENDFRAME (=Integer.MAX_VALUE)
 **   -RESUMEFRAMECAPTURE (=TRUE/FALSE)
 **   -STEREOSCOPIC (=TRUE/FALSE)
 **   -STEREOSCOPICEYESEPARATION (=0.0-1.0)
 **   -STEREOSCOPICOUTPUTMODE (=HSBS/HOU/FSBS/FOU/FFS)
 **/

import javax.media.opengl.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;

import framework.base.*;

public class Bootstrap {

    static {
        //stg dirty I dont wanna think about any further X-) ...
        GLProfile.initSingleton(true);
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
        int tStartFrame = 0;
        int tEndFrame = Integer.MAX_VALUE;
        boolean tStereoscopic = false;
        float tStereoscopicEyeSeparation = 0.0f;
        String tStereoscopicOutputMode = "HSBS";
        
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
                } else if(args[i].trim().startsWith("-STARTFRAME=")) {
                    String tStartFrameParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("CUSTOM START FRAME NUMBER SET TO '"+tStartFrameParameter+"'");
                    tStartFrame = Integer.parseInt(tStartFrameParameter); 
                } else if(args[i].trim().startsWith("-ENDFRAME=")) {
                    String tEndFrameParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("CUSTOM END FRAME NUMBER SET TO '"+tEndFrameParameter+"'");
                    tEndFrame = Integer.parseInt(tEndFrameParameter);                    
                } else if(args[i].trim().startsWith("-RESUMEFRAMECAPTURE=")) {
                    //resume frame capture implicitly sets FRAMECAPTURE=TRUE and STARTFRAME
                    String tResumeFrameCaptureParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("RESUME FRAME CAPTURE ENABLED '"+tResumeFrameCaptureParameter+"'");
                    boolean tResumeFrameCapture = Boolean.parseBoolean(tResumeFrameCaptureParameter);
                    if (tResumeFrameCapture) {
                        tFrameCapture = true;
                        BaseLogging.getInstance().info("SEARCHING CAPTURE DIRECTORY TO FIND RESUME FRAME ...");
                        //String tFileName = "capture\\"+BaseLogging.cLOGGING_CAPTUREOUTPUTFILENAME_PREFIX+"_"+tDecimalFormatter.format(inFrameNumber)+BaseLogging.cLOGGING_CAPTUREOUTPUTFILENAME_SUFFIX;
                        File tScreenCaptureDirectory = new File(BaseLogging.cLOGGING_CAPTUREOUTPUTDIRECTORYNAME);
                        String[] tFileList = tScreenCaptureDirectory.list(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                if (name.contains(BaseLogging.cLOGGING_CAPTUREOUTPUTFILENAME_PREFIX) && 
                                    name.endsWith(BaseLogging.cLOGGING_CAPTUREOUTPUTFILENAME_SUFFIX)) {
                                    return true;
                                }
                                return false;   
                            }                        
                        });                      
                        Arrays.sort(tFileList);
                        int tResumeStartFrameNumber = tStartFrame;
                        if (tFileList.length>2) {
                            BaseLogging.getInstance().info("STARTFRAME FILENAME IS "+tFileList[0]);
                            BaseLogging.getInstance().info("ENDFRAME FILENAME IS "+tFileList[tFileList.length-1]);
                            String tEndFrameNumberString = tFileList[tFileList.length-1].substring(21,27);
                            int tEndFrameNumber = Integer.parseInt(tEndFrameNumberString);
                            BaseLogging.getInstance().info("PARSED ENDFRAME IS "+tEndFrameNumber);
                            tResumeStartFrameNumber = tEndFrameNumber-2;
                            if (tResumeStartFrameNumber<0) {
                                tResumeStartFrameNumber = 0;
                            }
                            BaseLogging.getInstance().info("SETTING NEW STARTFRAME BACK -2 AND RESUME CAPTURE ... STARTFRAME="+tResumeStartFrameNumber);
                        } else {
                            BaseLogging.getInstance().info("NUMBER OF ALREADY CAPTURED FRAMES TO SMALL TO RESUME ... RESUME FRAME NUMBER SET TO STARTFRAME ...");
                        }
                        tStartFrame = tResumeStartFrameNumber;
                    } 
                } else if(args[i].trim().startsWith("-STEREOSCOPIC=")) {
                    String tStereoscopicParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("STEREOSCOPIC ENABLED '"+tStereoscopicParameter+"'");
                    tStereoscopic = Boolean.parseBoolean(tStereoscopicParameter);    
                } else if(args[i].trim().startsWith("-STEREOSCOPICEYESEPARATION=")) {
                    String tStereoscopicEyeSeparationParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("STEREOSCOPIC EYE SEPARATION SET TO '"+tStereoscopicEyeSeparationParameter+"'");
                    tStereoscopicEyeSeparation = Float.parseFloat(tStereoscopicEyeSeparationParameter);     
                } else if(args[i].trim().startsWith("-STEREOSCOPICOUTPUTMODE=")) {
                    String tStereoscopicOutputModeParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    BaseLogging.getInstance().info("STEREOSCOPIC OUTPUT MODE SET TO '"+tStereoscopicOutputModeParameter+"'");
                    tStereoscopicOutputMode = tStereoscopicOutputModeParameter;                  
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
                tMusicFileName,
                tStartFrame,
                tEndFrame,
                tStereoscopic, 
                tStereoscopicEyeSeparation,
                tStereoscopicOutputMode
        );
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                BaseGlobalEnvironment.getInstance().initGLEnvironment();
            }
        });
    }

}
