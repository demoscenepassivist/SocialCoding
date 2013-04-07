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
 ** More or less some kind of wrapper class wich encalsulates a convenient runtime environment
 ** for classes implementing the BaseRoutineInterface. Also adds some global debug information
 ** overlay. 
 **
 **/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.text.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.awt.*;
import static javax.media.opengl.GL3bc.*;
import com.sun.jna.*;

public class BaseRoutineRuntime {

    private static BaseRoutineRuntime mBaseRoutineRuntimeInstance = null;
    private TextRenderer mTextRenderer;
    private DecimalFormat mDecimalFormat;
    private BaseGlobalEnvironment mBaseGlobalEnvironment;
    private int mFrameCounter;
    private long mCurrentFrameRenderingTimeStart;
    private long mCurrentFrameRenderingTimeEnd;
    private long mLastFrameRenderingTimeStart;
    private long mLastFrameRenderingTimeEnd;
    private BaseRoutineInterface mBaseRoutineInterface;
    private long mAverageFramerateCounter;
    private long mAverageFramerateTimeStart;
    private long mAverageFramerateTimeEnd;
    private static final int cAverageFramerateInterval = 25;
    private int mAverageFramerate;
    private long mFrameSkipAverageFramerateTimeStart;
    private boolean mFrameSkipAverageFrameStartTimeInitialized;
    private long mFrameSkipAverageFramerateTimeEnd;	
    private double mFrameCounterDifference;
    private double mFrameCounterTargetValue;
    private int mSkippedFramesCounter;
    private BaseMusic mBaseMusic;
    private TextureRenderer mTextureRenderer_ScopeAndSpectrumAnalyzer;
    boolean mMusicSyncStartTimeInitialized = false;
    private float mCurrentStereoscopicEyeSeparation;
    
    public interface dwmapi extends Library {
        dwmapi INSTANCE = (dwmapi)Native.loadLibrary("dwmapi",dwmapi.class);
        //http://msdn.microsoft.com/en-us/library/aa969510%28VS.85%29.aspx
        //HRESULT WINAPI DwmEnableComposition(UINT uCompositionAction);
        public int DwmEnableComposition(int uCompositionAction);
    }

    private BaseRoutineRuntime() {
      //Zzzz ... :>
    }

    public static BaseRoutineRuntime getInstance() {
        if (mBaseRoutineRuntimeInstance==null) {
            mBaseRoutineRuntimeInstance=new BaseRoutineRuntime();
        } 
        return mBaseRoutineRuntimeInstance;
    }

    public void initRuntime(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //---
        //workaround for JOGL Bug 403 - White screen when using GLCanvas and "fullscreen exclusive mode".
        //http://jogamp.org/bugzilla/show_bug.cgi?id=403
        if (BaseGlobalEnvironment.getInstance().usesFullScreenMode()) {
            BaseLogging.getInstance().info("FULLSCREEN MODE IS ENABLED ... CHECKING FOR NVIDIA VENDOR!");
            if (inGL.glGetString(GL_VENDOR).toLowerCase().contains("nvidia")) {
                BaseLogging.getInstance().info("VENDOR IS NVIDIA! DISABLING DESKTOP WINDOW MANAGER (DWM) COMPOSITION ...");
                //Vista automatically re-enables composition when dwmapi is unloaded.
                //This happens when the application exits. Alternatively, to re-enable composition:
                //dwmapi.INSTANCE.DwmEnableComposition(1);
                dwmapi.INSTANCE.DwmEnableComposition(0);
            } else {
                BaseLogging.getInstance().info("VENDOR IS ATI! EVERYTHING IS FINE ...");
            }
        }
        //---
        mBaseGlobalEnvironment = BaseGlobalEnvironment.getInstance();
        Font tFont = new Font("SansSerif", Font.PLAIN, 12);
        mTextRenderer = new TextRenderer(tFont, false, false);
        mDecimalFormat = new DecimalFormat("###,###,###,###,###.###");
        mFrameCounter = BaseGlobalEnvironment.getInstance().getStartFrame();
        //mCurrentStereoscopicEyeSeparation = BaseGlobalEnvironment.getInstance().getStereoscopicEyeSeparation();
        try {
            BaseLogging.getInstance().info("CREATING BASEROUTINE CONSTRUCTOR FOR "+mBaseGlobalEnvironment.getBaseRoutineClassName());
            Class<?> tIntermediateClass = Class.forName(mBaseGlobalEnvironment.getBaseRoutineClassName());
            Class<? extends BaseRoutineAdapter> tIntermediateSubclass = tIntermediateClass.asSubclass(BaseRoutineAdapter.class);
            Constructor<? extends BaseRoutineAdapter> tIntermediateSubclassConstructor = tIntermediateSubclass.getConstructor();
            mBaseRoutineInterface = tIntermediateSubclassConstructor.newInstance();
            BaseLogging.getInstance().info("BASEROUTING INSTANCE NAMED "+mBaseGlobalEnvironment.getBaseRoutineClassName()+" CREATED ...");
            BaseLogging.getInstance().info("INITIALIZING BASEROUTINE ...");
            mBaseRoutineInterface.initRoutine(inGL,inGLU,inGLUT);
        } catch (Exception e) {
            BaseLogging.getInstance().fatalerror(e);
        }
        mTextureRenderer_ScopeAndSpectrumAnalyzer = new TextureRenderer(BaseMusic_ScopeAndSpectrumAnalyzer.DEFAULT_WIDTH, BaseMusic_ScopeAndSpectrumAnalyzer.DEFAULT_HEIGHT, true);
        mBaseMusic = new BaseMusic(BaseGlobalEnvironment.getInstance().getMusicFileName());
        mBaseMusic.init();
        mBaseMusic.play();
    }

    public void mainLoopRuntime(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //if NO music is used sync to mainloop start ...
        if (!mFrameSkipAverageFrameStartTimeInitialized) {
            mFrameSkipAverageFrameStartTimeInitialized = true;
            mFrameSkipAverageFramerateTimeStart = System.nanoTime();
        }
        if (!BaseRoutineRuntime.getInstance().getBaseMusic().isOffline()) {
            //if music IS used sync to first second of music ...
            if (BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()>0 && !mMusicSyncStartTimeInitialized) {
                BaseLogging.getInstance().info("Synching to BaseMusic ...");
                mFrameSkipAverageFramerateTimeStart = (long)(System.nanoTime()-((double)BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()*1000000.0d));
                mMusicSyncStartTimeInitialized = true;
            }
        }
        mLastFrameRenderingTimeStart = mCurrentFrameRenderingTimeStart;
        mLastFrameRenderingTimeEnd = System.nanoTime();
        mCurrentFrameRenderingTimeStart = System.nanoTime();
        //allow music DSP's to synchronize with framerate ...
        mBaseMusic.synchonizeMusic();
        
        if (!mBaseGlobalEnvironment.wantsStereoscopic()) {
            //non stereoscopic rendering path ...
            //create default frustum state ...
            resetFrustumToDefaultState(inGL,inGLU,inGLUT);
            //clear screen and z-buffer ...
            inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            mBaseRoutineInterface.mainLoop(mFrameCounter,inGL,inGLU,inGLUT);            
            checkForGlError(inGL,inGLU);
            //optional fraps/kkapture-style screencapture logging ... ->=:-)X	   
            if (mBaseGlobalEnvironment.wantsFrameCapture()) {
                BufferedImage tScreenshot = Screenshot.readToBufferedImage(0,0, mBaseGlobalEnvironment.getScreenWidth(), mBaseGlobalEnvironment.getScreenHeight(),false);
                BaseLogging.getInstance().logCapture(tScreenshot, mFrameCounter);
            }
        } else {
            //stereoscopic rendering path ...
            
            mCurrentStereoscopicEyeSeparation = -1.0f*BaseGlobalEnvironment.getInstance().getStereoscopicEyeSeparation();
            
            //create default frustum state ...
            resetFrustumToDefaultState(inGL,inGLU,inGLUT);
            //clear screen and z-buffer ...
            inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            mBaseRoutineInterface.mainLoop(mFrameCounter,inGL,inGLU,inGLUT);
            checkForGlError(inGL,inGLU);
            //optional fraps/kkapture-style screencapture logging ... ->=:-)X      
            BufferedImage tScreenshot_Left = Screenshot.readToBufferedImage(0,0, mBaseGlobalEnvironment.getScreenWidth(), mBaseGlobalEnvironment.getScreenHeight(),false);
            
            mCurrentStereoscopicEyeSeparation = +1.0f*BaseGlobalEnvironment.getInstance().getStereoscopicEyeSeparation();
            
            //create default frustum state ...
            resetFrustumToDefaultState(inGL,inGLU,inGLUT);
            //clear screen and z-buffer ...
            inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            mBaseRoutineInterface.mainLoop(mFrameCounter,inGL,inGLU,inGLUT);
            checkForGlError(inGL,inGLU);
            //optional fraps/kkapture-style screencapture logging ... ->=:-)X      
            BufferedImage tScreenshot_Right = Screenshot.readToBufferedImage(0,0, mBaseGlobalEnvironment.getScreenWidth(), mBaseGlobalEnvironment.getScreenHeight(),false);
            
            if (BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("ALL")) {
                mCurrentStereoscopicEyeSeparation = 0.0f;
                //create default frustum state ...
                resetFrustumToDefaultState(inGL,inGLU,inGLUT);
                //clear screen and z-buffer ...
                inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                mBaseRoutineInterface.mainLoop(mFrameCounter,inGL,inGLU,inGLUT);
                checkForGlError(inGL,inGLU);
                //optional fraps/kkapture-style screencapture logging ... ->=:-)X      
                BufferedImage tScreenshot_Normal = Screenshot.readToBufferedImage(0,0, mBaseGlobalEnvironment.getScreenWidth(), mBaseGlobalEnvironment.getScreenHeight(),false);
                //dump normal 2D image ...
                BaseLogging.getInstance().logCapture(tScreenshot_Normal, mFrameCounter);
            }
            
            int tImageWidth = tScreenshot_Left.getWidth();
            int tImageHeight = tScreenshot_Left.getHeight();
            
            //dump Half-Side-By-Side (HSBS) image ...
            if (BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("HSBS") || 
                BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("ALL")) {
                BufferedImage tScreenshot_Combined = new BufferedImage(tImageWidth,tImageHeight,tScreenshot_Left.getType());
                Graphics2D tG2D = tScreenshot_Combined.createGraphics();
                //tG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);           
                tG2D.drawImage(
                        tScreenshot_Left,
                        0,
                        0,
                        tImageWidth/2, 
                        tImageHeight,
                        null
                );
                tG2D.drawImage(
                        tScreenshot_Right,
                        tImageWidth/2, 
                        0,
                        tImageWidth/2, 
                        tImageHeight,
                        null
                );
                tG2D.dispose();
                BaseLogging.getInstance().logCapture(tScreenshot_Combined, mFrameCounter,"HSBS");
            }
            
            //dump Half-Over-Under (HOU) image ...
            if (BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("HOU") || 
                BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("ALL")) {
                BufferedImage tScreenshot_Combined = new BufferedImage(tImageWidth,tImageHeight,tScreenshot_Left.getType());
                Graphics2D tG2D = tScreenshot_Combined.createGraphics();
                //tG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);           
                tG2D.drawImage(
                        tScreenshot_Left,
                        0,
                        0,
                        tImageWidth, 
                        tImageHeight/2,
                        null
                );
                tG2D.drawImage(
                        tScreenshot_Right,
                        0, 
                        tImageHeight/2,
                        tImageWidth, 
                        tImageHeight/2,
                        null
                 );
                 tG2D.dispose();
                 BaseLogging.getInstance().logCapture(tScreenshot_Combined, mFrameCounter,"HOU");
            }
            
            //dump Full-Over-Under (FOU) image ...
            if (BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("FOU") || 
                BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("ALL")) {
                BufferedImage tScreenshot_Combined = new BufferedImage(tImageWidth,tImageHeight*2,tScreenshot_Left.getType());
                Graphics2D tG2D = tScreenshot_Combined.createGraphics();
                //tG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);           
                tG2D.drawImage(
                        tScreenshot_Left,
                        0,
                        0,
                        tImageWidth, 
                        tImageHeight,
                        null
                );
                tG2D.drawImage(
                        tScreenshot_Right,
                        0, 
                        tImageHeight,
                        tImageWidth, 
                        tImageHeight,
                        null
                 );
                 tG2D.dispose();
                 BaseLogging.getInstance().logCapture(tScreenshot_Combined, mFrameCounter,"FOU");
            }
            
            //dump Full-Side-by-Side (FSBS) image ...
            if (BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("FSBS") || 
                BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("ALL")) {
                BufferedImage tScreenshot_Combined = new BufferedImage(tImageWidth*2,tImageHeight,tScreenshot_Left.getType());
                Graphics2D tG2D = tScreenshot_Combined.createGraphics();
                //tG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);           
                tG2D.drawImage(
                        tScreenshot_Left,
                        0,
                        0,
                        tImageWidth, 
                        tImageHeight,
                        null
                );
                tG2D.drawImage(
                        tScreenshot_Right,
                        tImageWidth, 
                        0,
                        tImageWidth, 
                        tImageHeight,
                        null
                 );
                 tG2D.dispose();
                 BaseLogging.getInstance().logCapture(tScreenshot_Combined, mFrameCounter,"FSBS");
            }
            
            //full frame sequential
            if (BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("FFS") || 
                BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode().contains("ALL")) {
                BaseLogging.getInstance().logCapture(tScreenshot_Left, mFrameCounter*2+0,"FFS");
                BaseLogging.getInstance().logCapture(tScreenshot_Right, mFrameCounter*2+1,"FFS"); 
            }
    
        }
        //---
        mCurrentFrameRenderingTimeEnd = System.nanoTime();
        renderDebugInformation(inGL,inGLU,inGLUT);
        //----
        mFrameCounter++;
        if (BaseGlobalEnvironment.getInstance().wantsFrameSkip() && !mBaseGlobalEnvironment.wantsFrameCapture() && !mBaseGlobalEnvironment.wantsStereoscopic()) {
            mFrameSkipAverageFramerateTimeEnd = System.nanoTime();
            double tDesiredFrameRate = (float)BaseGlobalEnvironment.getInstance().getDesiredFramerate();
            double tSingleFrameTime = 1000000000.0f/tDesiredFrameRate;
            double tElapsedTime = mFrameSkipAverageFramerateTimeEnd - mFrameSkipAverageFramerateTimeStart;
            //BaseLogging.getInstance().info("mFrameCounter="+mFrameCounter+" getPositionInMilliseconds="+BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()+" tElapsedTime="+tElapsedTime/1000000.0f);
            mFrameCounterTargetValue = tElapsedTime/tSingleFrameTime;
            mFrameCounterDifference = mFrameCounterTargetValue-mFrameCounter;
            if (mFrameCounterDifference>2) {
                mFrameCounter+=mFrameCounterDifference;
                mSkippedFramesCounter+=mFrameCounterDifference;
            } else if (mFrameCounterDifference<-2) {
                //hold framecounter advance ...
                mFrameCounter--;
            }
        }
        if (mFrameCounter>BaseGlobalEnvironment.getInstance().getEndFrame()) {
            //quite dirty ... should stop the Animator first X-)
            BaseLogging.getInstance().info("KILLING APPLICATION ... ENDFRAME NUMBER REACHED ... mFrameCounter="+mFrameCounter+" STARTFRAME="+BaseGlobalEnvironment.getInstance().getStartFrame()+" ENDFRAME="+BaseGlobalEnvironment.getInstance().getEndFrame());
            System.exit(0);
        }
    }

    public void cleanupRuntime(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseRoutineInterface.cleanupRoutine(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    public float getCurrentStereoscopicEyeSeparation() {
        return mCurrentStereoscopicEyeSeparation;
    }

    public BaseMusic getBaseMusic() {
        return mBaseMusic;
    }
    
    public void resetFrameCounter() {
        mFrameCounter = BaseGlobalEnvironment.getInstance().getStartFrame();
    }

    public static void resetFrustumToDefaultState(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        resetFrustumToDefaultState(inGL,inGLU,inGLUT,BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight());
    }

    public static void resetFrustumToDefaultState(GL2 inGL,GLU inGLU,GLUT inGLUT,int inScreenWidth,int inScreenHeight) {
        inGL.glViewport(0, 0, inScreenWidth, inScreenHeight);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        double tAspectRatio = (double)inScreenWidth/(double)inScreenHeight;
        inGLU.gluPerspective(45.0, tAspectRatio, 0.01, 200.0);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(
                0f, 0f, 70f,
                0f, 0f, 0f,
                0.0f, 1.0f, 0.0f
        );
    }

    private static final boolean DEBUGDISPLAY_STATS = true;
    private static final boolean DEBUGDISPLAY_MUSIC = true;
    
    private void renderDebugInformation(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        if (!mBaseMusic.isOffline() && DEBUGDISPLAY_MUSIC) {
            Graphics2D tTextureGraphics2D = mTextureRenderer_ScopeAndSpectrumAnalyzer.createGraphics();
            tTextureGraphics2D.drawImage(mBaseMusic.getScopeAndSpectrumAnalyzerVisualization(),0,0,null);
            tTextureGraphics2D.dispose();
            mTextureRenderer_ScopeAndSpectrumAnalyzer.markDirty(0, 0, BaseMusic_ScopeAndSpectrumAnalyzer.DEFAULT_WIDTH, BaseMusic_ScopeAndSpectrumAnalyzer.DEFAULT_HEIGHT);
            mTextureRenderer_ScopeAndSpectrumAnalyzer.beginOrthoRendering(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glEnable(GL_BLEND);
            inGL.glBlendFunc(GL_ONE, GL_ONE);
            mTextureRenderer_ScopeAndSpectrumAnalyzer.drawOrthoRect(0, 0);
            inGL.glDisable(GL_BLEND);
            mTextureRenderer_ScopeAndSpectrumAnalyzer.endOrthoRendering();
        }
        if (DEBUGDISPLAY_STATS) {
            if (++mAverageFramerateCounter == cAverageFramerateInterval) {
                mAverageFramerateTimeEnd = System.nanoTime();
                mAverageFramerateCounter = 0;
                mAverageFramerate = (int)(1000000000.0f/((mAverageFramerateTimeEnd - mAverageFramerateTimeStart)/cAverageFramerateInterval));
                mAverageFramerateTimeStart = System.nanoTime();
            }
            long tPossibleFrameRate = (long)(1000000000.0f/(mCurrentFrameRenderingTimeEnd-mCurrentFrameRenderingTimeStart));
            long tActualFrameRate = (long)(1000000000.0f/(mLastFrameRenderingTimeEnd-mLastFrameRenderingTimeStart));
            String[] tDebugInformation = new String[7];
            tDebugInformation[0] = "JOGL: "+"GL_VENDOR:"+inGL.glGetString(GL_VENDOR)+" GL_RENDERER:"+inGL.glGetString(GL_RENDERER);
            tDebugInformation[1] = "GL_VERSION: "+inGL.glGetString(GL_VERSION)+" GLSL_VERSION: "+inGL.glGetString(GL_SHADING_LANGUAGE_VERSION); 
            tDebugInformation[2] = "VMMEM: USED: "+mDecimalFormat.format(mBaseGlobalEnvironment.getUsedMem())+" FREE: "+mDecimalFormat.format(mBaseGlobalEnvironment.getFreeMem())+" TOTAL: "+mDecimalFormat.format(mBaseGlobalEnvironment.getTotalMem())+" MAX: "+mDecimalFormat.format(mBaseGlobalEnvironment.getMaxMem());
            tDebugInformation[3] = "DISPLAY RESOLUTION: "+mBaseGlobalEnvironment.getScreenWidth()+"x"+mBaseGlobalEnvironment.getScreenHeight()+" FRAME: "+mFrameCounter+" AVERAGE FPS:"+mAverageFramerate+" ACTUAL FPS: "+tActualFrameRate+" POSSIBLE FPS: "+mDecimalFormat.format(tPossibleFrameRate);    
            tDebugInformation[4] = "ROUTINE: "+mBaseGlobalEnvironment.getBaseRoutineClassName();
            if (BaseGlobalEnvironment.getInstance().wantsFrameSkip() && !mBaseGlobalEnvironment.wantsFrameCapture()) {
                tDebugInformation[5] = "FRAMESKIP: CURRENT FRAMECOUNTER: "+(int)mFrameCounter+" TARGET FRAMECOUNTER="+(int)mFrameCounterTargetValue+" DIFFERENCE:"+mDecimalFormat.format(mFrameCounterDifference)+" TOTAL SKIPPED:"+mSkippedFramesCounter;
            } else {
                tDebugInformation[5] = "FRAMESKIP: DISABLED!";
            }
            tDebugInformation[6] = "STEREOSCOPIC="+mBaseGlobalEnvironment.wantsStereoscopic()+" EYESEPARANTION="+BaseGlobalEnvironment.getInstance().getStereoscopicEyeSeparation()+" OUTPUTMODE="+BaseGlobalEnvironment.getInstance().getStereoscopicOutputMode();          
            for (int i=0; i<tDebugInformation.length; i++) {
                mTextRenderer.beginRendering(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
                mTextRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                mTextRenderer.draw(tDebugInformation[i], 0, BaseGlobalEnvironment.getInstance().getScreenHeight()-11*(i+1));
                mTextRenderer.endRendering();
            }
        }
    }

    private void checkForGlError(GL2 inGL,GLU inGLU) {
        int tError = inGL.glGetError();
        String tErrorString = "!!! GL-ERROR !!! GLU ERROR STRING FOR ERROR="+inGLU.gluErrorString(tError);
        if (tError!=GL_NO_ERROR) {
            BaseLogging.getInstance().error(tErrorString);
        }
    }

}
