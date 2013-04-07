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
 ** Initializes the native window environment and event-listeners need for JOGL. Also provides global
 ** accessors for the commandline parameters supplied by the user.
 **
 **/

import java.awt.*;
import java.awt.event.*;
import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import com.jogamp.opengl.util.*;
import com.jogamp.newt.opengl.*;
import com.jogamp.newt.*;
import com.jogamp.newt.util.*;
//import javax.media.nativewindow.*;

public class BaseGlobalEnvironment {

    private static BaseGlobalEnvironment mBaseGlobalEnvironmentInstance = null;
    private Runtime mRuntime;
    private BaseGLEventListener mBaseGLEventListener;

    //keylistener variables
    private int mParameterKey_INT_12;
    private int mParameterKey_INT_34;
    private int mParameterKey_INT_56;
    private int mParameterKey_INT_78;
    private int mParameterKey_INT_90;
    private int mParameterKey_INT_QW;
    private int mParameterKey_INT_ER;
    private int mParameterKey_INT_TZ;
    private int mParameterKey_INT_UI;
    private int mParameterKey_INT_OP;
    private float mParameterKey_FLOAT_AS;
    private float mParameterKey_FLOAT_DF;
    private float mParameterKey_FLOAT_GH;
    private float mParameterKey_FLOAT_JK;
    private float mParameterKey_FLOAT_LÖ;
    private float mParameterKey_FLOAT_YX;
    private float mParameterKey_FLOAT_CV;
    private float mParameterKey_FLOAT_BN;
    private float mParameterKey_FLOAT_MCOMMA;
    private float mParameterKey_FLOAT_X;
    private float mParameterKey_FLOAT_Y;
    private float mParameterKey_FLOAT_Z;

    private BaseGlobalEnvironment() {
        BaseLogging.getInstance().info("CONSTRUCTING BASEGLOBALENVIRONMENT ...");
        mRuntime = Runtime.getRuntime();
    }

    public static BaseGlobalEnvironment getInstance() {
        if (mBaseGlobalEnvironmentInstance==null) {
            mBaseGlobalEnvironmentInstance=new BaseGlobalEnvironment();
        } 
        return mBaseGlobalEnvironmentInstance;
    }

    public void initGLEnvironment() {
        BaseLogging.getInstance().info("CHOOSING WINDOWTOOLKIT ... PREFERED TOOLKIT NAME="+getWindowToolkitName());
        if (getWindowToolkitName().equalsIgnoreCase("AWT")) {
            BaseLogging.getInstance().info("USING AWT AS WINDOWTOOLKIT ...");
            initGLEnvironment_AWT();
        } else if (getWindowToolkitName().equalsIgnoreCase("NEWT")) {
            BaseLogging.getInstance().info("USING NEWT AS WINDOWTOOLKIT ...");
            initGLEnvironment_NEWT();
        } else {
            BaseLogging.getInstance().fatalerror("UNKNOWN WINDOWTOOLKIT!!!");
        }
    }

    public void initGLEnvironment_NEWT() {
        //get current display mode/desktop display mode ...
        DisplayMode tDesktopDisplayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        BaseLogging.getInstance().info("DESKTOP DISPLAYMODE "+BaseWindowToolkitUtils.convertDisplayModeToString(tDesktopDisplayMode));
        //use desktop resolution but force 32bit@60hz ... (;
        DisplayMode tDesktopDisplayModeNormalized = new DisplayMode(tDesktopDisplayMode.getWidth(),tDesktopDisplayMode.getHeight(),32,60);
        //print graphics capabilities ... values are very unreliable X-)
        BaseWindowToolkitUtils.printGraphicsCapabilities();
        GLCapabilities tGLCapabilities = new GLCapabilities(GLProfile.getDefault());
        //enable/configure multisampling support ...
        if (preferMultiSampling()) {
            tGLCapabilities.setSampleBuffers(true);
            tGLCapabilities.setNumSamples(getNumberOfSamplingBuffers());
            tGLCapabilities.setAccumAlphaBits(16);
            tGLCapabilities.setAccumBlueBits(16);
            tGLCapabilities.setAccumGreenBits(16);
            tGLCapabilities.setAccumRedBits(16);
            //turns out we need to have alpha, otherwise no AA will be visible ?-)
            tGLCapabilities.setAlphaBits(8); 
        }
        //create NEWT native window ... :-0
        GLWindow tNEWTWindow = GLWindow.create(tGLCapabilities);
        tNEWTWindow.setTitle("Jogamp.org - JOGL Demos - NEWT");   
        mBaseGLEventListener = new BaseGLEventListener();
        tNEWTWindow.addGLEventListener(mBaseGLEventListener);
        final AnimatorBase tAnimator;
        //if vsync is requested use the vsync framerate ... otherwise use custom framerate ...
        if (BaseGlobalEnvironment.getInstance().wantsVSync() || mCommandLineParameter_FrameRate==Integer.MAX_VALUE) {
            tAnimator = new Animator(tNEWTWindow);
            ((Animator)tAnimator).setRunAsFastAsPossible(true);
        } else {
            //i'm currently not sure if it is a good idea to use system-clock based timing ... :+)
            tAnimator = new FPSAnimator(tNEWTWindow,mCommandLineParameter_FrameRate,true);
        }
        tNEWTWindow.addWindowListener(new com.jogamp.newt.event.WindowAdapter() {
            public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent e) {
                tAnimator.stop();
                System.exit(0);
            }
        });
        tNEWTWindow.addKeyListener(new com.jogamp.newt.event.KeyAdapter() {

            public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    mParameterKey_FLOAT_X-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_X="+mParameterKey_FLOAT_X);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    mParameterKey_FLOAT_X+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_X="+mParameterKey_FLOAT_X);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    mParameterKey_FLOAT_Y+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Y="+mParameterKey_FLOAT_Y);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    mParameterKey_FLOAT_Y-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Y="+mParameterKey_FLOAT_Y);
                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    mParameterKey_FLOAT_Z-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Z="+mParameterKey_FLOAT_Z);
                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    mParameterKey_FLOAT_Z+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Z="+mParameterKey_FLOAT_Z);
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    BaseLogging.getInstance().info("!!!RESETTING EVERY PARAMETER VALUE TO 0/0.0!!!");
                    mParameterKey_INT_12=0;
                    mParameterKey_INT_34=0;
                    mParameterKey_INT_56=0;
                    mParameterKey_INT_78=0;
                    mParameterKey_INT_90=0;
                    mParameterKey_INT_QW=0;
                    mParameterKey_INT_ER=0;
                    mParameterKey_INT_TZ=0;
                    mParameterKey_INT_UI=0;
                    mParameterKey_INT_OP=0;
                    mParameterKey_FLOAT_AS=0.0f;
                    mParameterKey_FLOAT_DF=0.0f;
                    mParameterKey_FLOAT_GH=0.0f;
                    mParameterKey_FLOAT_JK=0.0f;
                    mParameterKey_FLOAT_LÖ=0.0f;
                    mParameterKey_FLOAT_YX=0.0f;
                    mParameterKey_FLOAT_CV=0.0f;
                    mParameterKey_FLOAT_BN=0.0f;
                    mParameterKey_FLOAT_MCOMMA=0.0f;
                    mParameterKey_FLOAT_X=0.0f;
                    mParameterKey_FLOAT_Y=0.0f;
                    mParameterKey_FLOAT_Z=0.0f;
                    
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    mBaseGLEventListener.getBaseRoutineRuntime().resetFrameCounter();
                }
            }

            public void keyTyped(com.jogamp.newt.event.KeyEvent e) {
                if (e.getKeyChar()=='\u001B') {
                    BaseLogging.getInstance().info("--------------------------------------------");
                    BaseLogging.getInstance().info("FINAL PARAMETER VALUES FROM KEYLISTENERS:");
                    BaseLogging.getInstance().info("--------------------------------------------");
                    BaseLogging.getInstance().info("mParameterKey_INT_12="+mParameterKey_INT_12);
                    BaseLogging.getInstance().info("mParameterKey_INT_34="+mParameterKey_INT_34);
                    BaseLogging.getInstance().info("mParameterKey_INT_56="+mParameterKey_INT_56);
                    BaseLogging.getInstance().info("mParameterKey_INT_78="+mParameterKey_INT_78);
                    BaseLogging.getInstance().info("mParameterKey_INT_90="+mParameterKey_INT_90);
                    BaseLogging.getInstance().info("mParameterKey_INT_QW="+mParameterKey_INT_QW);
                    BaseLogging.getInstance().info("mParameterKey_INT_ER="+mParameterKey_INT_ER);
                    BaseLogging.getInstance().info("mParameterKey_INT_TZ="+mParameterKey_INT_TZ);
                    BaseLogging.getInstance().info("mParameterKey_INT_UI="+mParameterKey_INT_UI);
                    BaseLogging.getInstance().info("mParameterKey_INT_OP="+mParameterKey_INT_OP);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_AS="+mParameterKey_FLOAT_AS);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_DF="+mParameterKey_FLOAT_DF);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_GH="+mParameterKey_FLOAT_GH);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_JK="+mParameterKey_FLOAT_JK);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_LÖ="+mParameterKey_FLOAT_LÖ);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_YX="+mParameterKey_FLOAT_YX);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_CV="+mParameterKey_FLOAT_CV);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_BN="+mParameterKey_FLOAT_BN);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_MCOMMA="+mParameterKey_FLOAT_MCOMMA);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_X="+mParameterKey_FLOAT_X);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_Y="+mParameterKey_FLOAT_Y);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_Z="+mParameterKey_FLOAT_Z);
                    BaseLogging.getInstance().info("--------------------------------------------");
                    tAnimator.stop();
                    System.exit(0);
                } else if (e.getKeyChar()=='1') {
                    mParameterKey_INT_12++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_12="+mParameterKey_INT_12);
                } else if (e.getKeyChar()=='2') {
                    mParameterKey_INT_12--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_12="+mParameterKey_INT_12);
                } else if (e.getKeyChar()=='3') {
                    mParameterKey_INT_34++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_34="+mParameterKey_INT_34);
                } else if (e.getKeyChar()=='4') {
                    mParameterKey_INT_34--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_34="+mParameterKey_INT_34);
                } else if (e.getKeyChar()=='5') {
                    mParameterKey_INT_56++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_56="+mParameterKey_INT_56);
                } else if (e.getKeyChar()=='6') {
                    mParameterKey_INT_56--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_56="+mParameterKey_INT_56);
                } else if (e.getKeyChar()=='7') {
                    mParameterKey_INT_78++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_78="+mParameterKey_INT_78);
                } else if (e.getKeyChar()=='8') {
                    mParameterKey_INT_78--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_78="+mParameterKey_INT_78);
                } else if (e.getKeyChar()=='9') {
                    mParameterKey_INT_90++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_90="+mParameterKey_INT_90);
                } else if (e.getKeyChar()=='0') {
                    mParameterKey_INT_90--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_90="+mParameterKey_INT_90);
                } else if (e.getKeyChar()=='q') {
                    mParameterKey_INT_QW++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_QW="+mParameterKey_INT_QW);
                } else if (e.getKeyChar()=='w') {
                    mParameterKey_INT_QW--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_QW="+mParameterKey_INT_QW); 
                } else if (e.getKeyChar()=='e') {
                    mParameterKey_INT_ER++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_ER="+mParameterKey_INT_ER);
                } else if (e.getKeyChar()=='r') {
                    mParameterKey_INT_ER--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_ER="+mParameterKey_INT_ER); 
                } else if (e.getKeyChar()=='t') {
                    mParameterKey_INT_TZ++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_TZ="+mParameterKey_INT_TZ);
                } else if (e.getKeyChar()=='z') {
                    mParameterKey_INT_TZ--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_TZ="+mParameterKey_INT_TZ);
                } else if (e.getKeyChar()=='u') {
                    mParameterKey_INT_UI++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_UI="+mParameterKey_INT_UI);
                } else if (e.getKeyChar()=='i') {
                    mParameterKey_INT_UI--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_UI="+mParameterKey_INT_UI);
                } else if (e.getKeyChar()=='o') {
                    mParameterKey_INT_OP++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_OP="+mParameterKey_INT_OP);
                } else if (e.getKeyChar()=='p') {
                    mParameterKey_INT_OP--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_OP="+mParameterKey_INT_OP);
                } else if (e.getKeyChar()=='a') {
                    mParameterKey_FLOAT_AS+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_AS="+mParameterKey_FLOAT_AS);
                } else if (e.getKeyChar()=='s') {
                    mParameterKey_FLOAT_AS-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_AS="+mParameterKey_FLOAT_AS);
                } else if (e.getKeyChar()=='d') {
                    mParameterKey_FLOAT_DF+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_DF="+mParameterKey_FLOAT_DF);
                } else if (e.getKeyChar()=='f') {
                    mParameterKey_FLOAT_DF-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_DF="+mParameterKey_FLOAT_DF); 
                } else if (e.getKeyChar()=='g') {
                    mParameterKey_FLOAT_GH+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_GH="+mParameterKey_FLOAT_GH);
                } else if (e.getKeyChar()=='h') {
                    mParameterKey_FLOAT_GH-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_GH="+mParameterKey_FLOAT_GH);
                } else if (e.getKeyChar()=='j') {
                    mParameterKey_FLOAT_JK+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_JK="+mParameterKey_FLOAT_JK);
                } else if (e.getKeyChar()=='k') {
                    mParameterKey_FLOAT_JK-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_JK="+mParameterKey_FLOAT_JK);
                } else if (e.getKeyChar()=='l') {
                    mParameterKey_FLOAT_LÖ+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_LÖ="+mParameterKey_FLOAT_LÖ);
                } else if (e.getKeyChar()=='ö') {
                    mParameterKey_FLOAT_LÖ-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_LÖ="+mParameterKey_FLOAT_LÖ);
                } else if (e.getKeyChar()=='y') {
                    mParameterKey_FLOAT_YX+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_YX="+mParameterKey_FLOAT_YX);
                } else if (e.getKeyChar()=='x') {
                    mParameterKey_FLOAT_YX-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_YX="+mParameterKey_FLOAT_YX);
                } else if (e.getKeyChar()=='c') {
                    mParameterKey_FLOAT_CV+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_CV="+mParameterKey_FLOAT_CV);
                } else if (e.getKeyChar()=='v') {
                    mParameterKey_FLOAT_CV-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_CV="+mParameterKey_FLOAT_CV);
                } else if (e.getKeyChar()=='b') {
                    mParameterKey_FLOAT_BN+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_BN="+mParameterKey_FLOAT_BN);
                } else if (e.getKeyChar()=='n') {
                    mParameterKey_FLOAT_BN-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_BN="+mParameterKey_FLOAT_BN);
                } else if (e.getKeyChar()=='m') {
                    mParameterKey_FLOAT_MCOMMA+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_MCOMMA="+mParameterKey_FLOAT_MCOMMA);
                } else if (e.getKeyChar()==',') {
                    mParameterKey_FLOAT_MCOMMA-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_MCOMMA="+mParameterKey_FLOAT_MCOMMA);
                } else {
                    //BaseLogging.getInstance().info("UNKNOWN KEY TYPED - KEYCHAR="+e.getKeyChar());
                }
            }
        });
        if (mCommandLineParameter_FullScreen) {
            BaseLogging.getInstance().info("SWITCHING TO NEWT FULLSCREEN MODE!");
            //using awt to query for the supported display modes ... need to change that in the future when NEWT is ready X-)
            DisplayMode tDisplayMode;
            if (mCommandLineParameter_DisplayMode!=null) {
                tDisplayMode = mCommandLineParameter_DisplayMode;
            } else {
                //choose best preferred display mode ...
                tDisplayMode = BaseWindowToolkitUtils.getBestDisplayModeWithBackupModes(BaseWindowToolkitUtils.DEFAULT_DISPLAYMODES,BaseWindowToolkitUtils.BACKUP_DISPLAYMODES,tDesktopDisplayModeNormalized);
            }
            mScreenWidth = tDisplayMode.getWidth();
            mScreenHeight =  tDisplayMode.getHeight();
            tNEWTWindow.setUndecorated(true);
            tNEWTWindow.setVisible(true);
            tNEWTWindow.setFullscreen(true);
            //create local display on screen 0 ...
            Display tDisplay = NewtFactory.createDisplay(null);
            Screen tScreen = NewtFactory.createScreen(tDisplay, 0);
            //determine target refresh rate ...
            ScreenMode tOriginalScreenMode = tScreen.getOriginalScreenMode();
            int tOriginalRefreshRate = tOriginalScreenMode.getMonitorMode().getRefreshRate();
            //target resolution
            javax.media.nativewindow.util.Dimension tResolution = new javax.media.nativewindow.util.Dimension(mScreenWidth, mScreenHeight);
            //target rotation
            int tRotation = 0;
            //filter available ScreenModes and get the nearest one ...
            java.util.List<ScreenMode> screenModes = tScreen.getScreenModes();
            screenModes = ScreenModeUtil.filterByRate(screenModes, tOriginalRefreshRate);
            screenModes = ScreenModeUtil.filterByRotation(screenModes, tRotation);
            screenModes = ScreenModeUtil.filterByResolution(screenModes, tResolution);
            screenModes = ScreenModeUtil.getHighestAvailableBpp(screenModes);
            //pick 1st one ...
            tScreen.setCurrentScreenMode((ScreenMode)screenModes.get(0));
            mUsesFullScreenMode = true;
        } else {
            BaseLogging.getInstance().info("FULLSCREEN MODE NOT SUPPORTED ... RUNNING IN WINDOWED MODE INSTEAD!");
            DisplayMode tDisplayMode;
            if (mCommandLineParameter_DisplayMode!=null) {
                tDisplayMode = mCommandLineParameter_DisplayMode;
            } else {
                //choose best preferred display mode ...
                tDisplayMode = BaseWindowToolkitUtils.getBestDisplayModeWithBackupModes(BaseWindowToolkitUtils.DEFAULT_DISPLAYMODES,BaseWindowToolkitUtils.BACKUP_DISPLAYMODES,tDesktopDisplayModeNormalized);
            }
            mScreenWidth = tDisplayMode.getWidth();
            mScreenHeight =  tDisplayMode.getHeight();
            tNEWTWindow.setVisible(true);
            tNEWTWindow.setSize(tDisplayMode.getWidth(),tDisplayMode.getHeight());
            tNEWTWindow.setPosition((tDesktopDisplayMode.getWidth()-tDisplayMode.getWidth())/2, (tDesktopDisplayMode.getHeight()-tDisplayMode.getHeight())/2);
            mUsesFullScreenMode = false;
        }
        tAnimator.start();
    }

    public void initGLEnvironment_AWT() {
        //get current display mode/desktop display mode ...
        DisplayMode tDesktopDisplayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        BaseLogging.getInstance().info("DESKTOP DISPLAYMODE "+BaseWindowToolkitUtils.convertDisplayModeToString(tDesktopDisplayMode));
        //use desktop resolution but force 32bit@60hz ... (;
        DisplayMode tDesktopDisplayModeNormalized = new DisplayMode(tDesktopDisplayMode.getWidth(),tDesktopDisplayMode.getHeight(),32,60);
        //print graphics capabilities ... values are very unreliable X-)
        BaseWindowToolkitUtils.printGraphicsCapabilities();
        //create native window container and AWT GLCanvas ... :-0
        final Frame tFrame = new Frame("Jogamp.org - JOGL Demos - AWT");
        GLCapabilities tGLCapabilities = new GLCapabilities(GLProfile.getDefault());
        //enable/configure multisampling support ...
        if (preferMultiSampling()) {
            tGLCapabilities.setSampleBuffers(true);
            tGLCapabilities.setNumSamples(getNumberOfSamplingBuffers());
            tGLCapabilities.setAccumAlphaBits(16);
            tGLCapabilities.setAccumBlueBits(16);
            tGLCapabilities.setAccumGreenBits(16);
            tGLCapabilities.setAccumRedBits(16);
        }
        /*
        //test method for JOGL2 mutisampling bug: http://jogamp.org/bugzilla/show_bug.cgi?id=410
        GLCanvas tGLCanvas = new GLCanvas(tGLCapabilities, new GLCapabilitiesChooser() {
            public int chooseCapabilities(Capabilities cpblts, Capabilities[] cpbltss, int i) {
                for (Capabilities caps : cpbltss) {
                    System.out.println(caps);
                }
                System.out.println("recommended:");
                System.out.println(cpblts);
                return i;
            }
        }, null, null);
        */
        GLCanvas tGLCanvas = new GLCanvas(tGLCapabilities);
        mBaseGLEventListener = new BaseGLEventListener();
        tGLCanvas.addGLEventListener(mBaseGLEventListener);
        tFrame.add(tGLCanvas);
        final AnimatorBase tAnimator;
        //if vsync is requested use the vsync framerate ... otherwise use custom framerate ...
        if (BaseGlobalEnvironment.getInstance().wantsVSync() || mCommandLineParameter_FrameRate==Integer.MAX_VALUE) {
            tAnimator = new Animator(tGLCanvas);
            ((Animator)tAnimator).setRunAsFastAsPossible(true);
        } else {
            //i'm currently not sure if it is a good idea to use system-clock based timing ... :+)
            tAnimator = new FPSAnimator(tGLCanvas,mCommandLineParameter_FrameRate,true);
        }
        tFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                //run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting ...
                new Thread(new Runnable() {
                    public void run() {
                        tAnimator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        tFrame.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    mParameterKey_FLOAT_X-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_X="+mParameterKey_FLOAT_X);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    mParameterKey_FLOAT_X+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_X="+mParameterKey_FLOAT_X);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    mParameterKey_FLOAT_Y+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Y="+mParameterKey_FLOAT_Y);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    mParameterKey_FLOAT_Y-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Y="+mParameterKey_FLOAT_Y);
                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    mParameterKey_FLOAT_Z-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Z="+mParameterKey_FLOAT_Z);
                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    mParameterKey_FLOAT_Z+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_Z="+mParameterKey_FLOAT_Z);
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    BaseLogging.getInstance().info("!!!RESETTING EVERY PARAMETER VALUE TO 0/0.0!!!");
                    mParameterKey_INT_12=0;
                    mParameterKey_INT_34=0;
                    mParameterKey_INT_56=0;
                    mParameterKey_INT_78=0;
                    mParameterKey_INT_90=0;
                    mParameterKey_INT_QW=0;
                    mParameterKey_INT_ER=0;
                    mParameterKey_INT_TZ=0;
                    mParameterKey_INT_UI=0;
                    mParameterKey_INT_OP=0;
                    mParameterKey_FLOAT_AS=0.0f;
                    mParameterKey_FLOAT_DF=0.0f;
                    mParameterKey_FLOAT_GH=0.0f;
                    mParameterKey_FLOAT_JK=0.0f;
                    mParameterKey_FLOAT_LÖ=0.0f;
                    mParameterKey_FLOAT_YX=0.0f;
                    mParameterKey_FLOAT_CV=0.0f;
                    mParameterKey_FLOAT_BN=0.0f;
                    mParameterKey_FLOAT_MCOMMA=0.0f;
                    mParameterKey_FLOAT_X=0.0f;
                    mParameterKey_FLOAT_Y=0.0f;
                    mParameterKey_FLOAT_Z=0.0f;
                    mBaseGLEventListener.getBaseRoutineRuntime().resetFrameCounter();
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    mBaseGLEventListener.getBaseRoutineRuntime().resetFrameCounter();
                }
            }

            public void keyTyped(java.awt.event.KeyEvent e) {
                if (e.getKeyChar()=='\u001B') {
                    BaseLogging.getInstance().info("--------------------------------------------");
                    BaseLogging.getInstance().info("FINAL PARAMETER VALUES FROM KEYLISTENERS:");
                    BaseLogging.getInstance().info("--------------------------------------------");
                    BaseLogging.getInstance().info("mParameterKey_INT_12="+mParameterKey_INT_12);
                    BaseLogging.getInstance().info("mParameterKey_INT_34="+mParameterKey_INT_34);
                    BaseLogging.getInstance().info("mParameterKey_INT_56="+mParameterKey_INT_56);
                    BaseLogging.getInstance().info("mParameterKey_INT_78="+mParameterKey_INT_78);
                    BaseLogging.getInstance().info("mParameterKey_INT_90="+mParameterKey_INT_90);
                    BaseLogging.getInstance().info("mParameterKey_INT_QW="+mParameterKey_INT_QW);
                    BaseLogging.getInstance().info("mParameterKey_INT_ER="+mParameterKey_INT_ER);
                    BaseLogging.getInstance().info("mParameterKey_INT_TZ="+mParameterKey_INT_TZ);
                    BaseLogging.getInstance().info("mParameterKey_INT_UI="+mParameterKey_INT_UI);
                    BaseLogging.getInstance().info("mParameterKey_INT_OP="+mParameterKey_INT_OP);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_AS="+mParameterKey_FLOAT_AS);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_DF="+mParameterKey_FLOAT_DF);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_GH="+mParameterKey_FLOAT_GH);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_JK="+mParameterKey_FLOAT_JK);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_LÖ="+mParameterKey_FLOAT_LÖ);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_YX="+mParameterKey_FLOAT_YX);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_CV="+mParameterKey_FLOAT_CV);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_BN="+mParameterKey_FLOAT_BN);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_MCOMMA="+mParameterKey_FLOAT_MCOMMA);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_X="+mParameterKey_FLOAT_X);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_Y="+mParameterKey_FLOAT_Y);
                    BaseLogging.getInstance().info("mParameterKey_FLOAT_Z="+mParameterKey_FLOAT_Z);
                    BaseLogging.getInstance().info("--------------------------------------------");
                    //run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting ...
                    new Thread(new Runnable() {
                        public void run() {
                            tAnimator.stop();
                            System.exit(0);
                        }
                    }).start();
                } else if (e.getKeyChar()=='1') {
                    mParameterKey_INT_12++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_12="+mParameterKey_INT_12);
                } else if (e.getKeyChar()=='2') {
                    mParameterKey_INT_12--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_12="+mParameterKey_INT_12);
                } else if (e.getKeyChar()=='3') {
                    mParameterKey_INT_34++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_34="+mParameterKey_INT_34);
                } else if (e.getKeyChar()=='4') {
                    mParameterKey_INT_34--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_34="+mParameterKey_INT_34);
                } else if (e.getKeyChar()=='5') {
                    mParameterKey_INT_56++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_56="+mParameterKey_INT_56);
                } else if (e.getKeyChar()=='6') {
                    mParameterKey_INT_56--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_56="+mParameterKey_INT_56);
                } else if (e.getKeyChar()=='7') {
                    mParameterKey_INT_78++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_78="+mParameterKey_INT_78);
                } else if (e.getKeyChar()=='8') {
                    mParameterKey_INT_78--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_78="+mParameterKey_INT_78);
                } else if (e.getKeyChar()=='9') {
                    mParameterKey_INT_90++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_90="+mParameterKey_INT_90);
                } else if (e.getKeyChar()=='0') {
                    mParameterKey_INT_90--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_90="+mParameterKey_INT_90);
                } else if (e.getKeyChar()=='q') {
                    mParameterKey_INT_QW++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_QW="+mParameterKey_INT_QW);
                } else if (e.getKeyChar()=='w') {
                    mParameterKey_INT_QW--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_QW="+mParameterKey_INT_QW);	
                } else if (e.getKeyChar()=='e') {
                    mParameterKey_INT_ER++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_ER="+mParameterKey_INT_ER);
                } else if (e.getKeyChar()=='r') {
                    mParameterKey_INT_ER--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_ER="+mParameterKey_INT_ER);	
                } else if (e.getKeyChar()=='t') {
                    mParameterKey_INT_TZ++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_TZ="+mParameterKey_INT_TZ);
                } else if (e.getKeyChar()=='z') {
                    mParameterKey_INT_TZ--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_TZ="+mParameterKey_INT_TZ);
                } else if (e.getKeyChar()=='u') {
                    mParameterKey_INT_UI++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_UI="+mParameterKey_INT_UI);
                } else if (e.getKeyChar()=='i') {
                    mParameterKey_INT_UI--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_UI="+mParameterKey_INT_UI);
                } else if (e.getKeyChar()=='o') {
                    mParameterKey_INT_OP++;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_OP="+mParameterKey_INT_OP);
                } else if (e.getKeyChar()=='p') {
                    mParameterKey_INT_OP--;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_INT_OP="+mParameterKey_INT_OP);
                } else if (e.getKeyChar()=='a') {
                    mParameterKey_FLOAT_AS+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_AS="+mParameterKey_FLOAT_AS);
                } else if (e.getKeyChar()=='s') {
                    mParameterKey_FLOAT_AS-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_AS="+mParameterKey_FLOAT_AS);
                } else if (e.getKeyChar()=='d') {
                    mParameterKey_FLOAT_DF+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_DF="+mParameterKey_FLOAT_DF);
                } else if (e.getKeyChar()=='f') {
                    mParameterKey_FLOAT_DF-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_DF="+mParameterKey_FLOAT_DF);	
                } else if (e.getKeyChar()=='g') {
                    mParameterKey_FLOAT_GH+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_GH="+mParameterKey_FLOAT_GH);
                } else if (e.getKeyChar()=='h') {
                    mParameterKey_FLOAT_GH-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_GH="+mParameterKey_FLOAT_GH);
                } else if (e.getKeyChar()=='j') {
                    mParameterKey_FLOAT_JK+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_JK="+mParameterKey_FLOAT_JK);
                } else if (e.getKeyChar()=='k') {
                    mParameterKey_FLOAT_JK-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_JK="+mParameterKey_FLOAT_JK);
                } else if (e.getKeyChar()=='l') {
                    mParameterKey_FLOAT_LÖ+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_LÖ="+mParameterKey_FLOAT_LÖ);
                } else if (e.getKeyChar()=='ö') {
                    mParameterKey_FLOAT_LÖ-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_LÖ="+mParameterKey_FLOAT_LÖ);
                } else if (e.getKeyChar()=='y') {
                    mParameterKey_FLOAT_YX+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_YX="+mParameterKey_FLOAT_YX);
                } else if (e.getKeyChar()=='x') {
                    mParameterKey_FLOAT_YX-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_YX="+mParameterKey_FLOAT_YX);
                } else if (e.getKeyChar()=='c') {
                    mParameterKey_FLOAT_CV+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_CV="+mParameterKey_FLOAT_CV);
                } else if (e.getKeyChar()=='v') {
                    mParameterKey_FLOAT_CV-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_CV="+mParameterKey_FLOAT_CV);
                } else if (e.getKeyChar()=='b') {
                    mParameterKey_FLOAT_BN+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_BN="+mParameterKey_FLOAT_BN);
                } else if (e.getKeyChar()=='n') {
                    mParameterKey_FLOAT_BN-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_BN="+mParameterKey_FLOAT_BN);
                } else if (e.getKeyChar()=='m') {
                    mParameterKey_FLOAT_MCOMMA+=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_MCOMMA="+mParameterKey_FLOAT_MCOMMA);
                } else if (e.getKeyChar()==',') {
                    mParameterKey_FLOAT_MCOMMA-=0.1f;
                    BaseLogging.getInstance().info("CHANGE PARAMETER KEY VARIABLE mParameterKey_FLOAT_MCOMMA="+mParameterKey_FLOAT_MCOMMA);
                } else {
                    //BaseLogging.getInstance().info("UNKNOWN KEY TYPED - KEYCHAR="+e.getKeyChar());
                }	
            }
        });
        if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported() && mCommandLineParameter_FullScreen) {
            BaseLogging.getInstance().info("FULLSCREEN MODE SUPPORTED ... SWITCHING TO FULLSCREEN EXCLUSIVE MODE!");
            tFrame.setIgnoreRepaint(true);
            tFrame.setUndecorated(true); 
            DisplayMode tDisplayMode;
            if (mCommandLineParameter_DisplayMode!=null) {
                tDisplayMode = mCommandLineParameter_DisplayMode;
            } else {
                //choose best preferred display mode ...
                tDisplayMode = BaseWindowToolkitUtils.getBestDisplayModeWithBackupModes(BaseWindowToolkitUtils.DEFAULT_DISPLAYMODES,BaseWindowToolkitUtils.BACKUP_DISPLAYMODES,tDesktopDisplayModeNormalized);
            }
            mScreenWidth = tDisplayMode.getWidth();
            mScreenHeight =  tDisplayMode.getHeight();
            tFrame.setSize(mScreenWidth,mScreenHeight);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(tFrame);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setDisplayMode(tDisplayMode);
            tFrame.setCursor(BaseWindowToolkitUtils.createHiddenCursor());
            tFrame.setVisible(true);
            tFrame.requestFocus();
            mUsesFullScreenMode = true;
        } else {
            BaseLogging.getInstance().info("FULLSCREEN MODE NOT SUPPORTED ... RUNNING IN WINDOWED MODE INSTEAD!");
            tFrame.setIgnoreRepaint(true);
            tFrame.setResizable(false);
            DisplayMode tDisplayMode;
            if (mCommandLineParameter_DisplayMode!=null) {
                tDisplayMode = mCommandLineParameter_DisplayMode;
            } else {
                //choose best preferred display mode ...
                tDisplayMode = BaseWindowToolkitUtils.getBestDisplayModeWithBackupModes(BaseWindowToolkitUtils.DEFAULT_DISPLAYMODES,BaseWindowToolkitUtils.BACKUP_DISPLAYMODES,tDesktopDisplayModeNormalized);
            }            
            mScreenWidth = tDisplayMode.getWidth();
            mScreenHeight =  tDisplayMode.getHeight();
            tFrame.setVisible(true);
            tFrame.setSize(tDisplayMode.getWidth()+tFrame.getInsets().left+tFrame.getInsets().right, tDisplayMode.getHeight()+tFrame.getInsets().top+tFrame.getInsets().bottom);
            tFrame.setLocation((tDesktopDisplayMode.getWidth()-tDisplayMode.getWidth())/2, (tDesktopDisplayMode.getHeight()-tDisplayMode.getHeight())/2);
            tFrame.requestFocus();
            mUsesFullScreenMode = false;
        }
        tAnimator.start();
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    private int mScreenWidth;
    private int mScreenHeight;
    public int getScreenWidth()  { return mScreenWidth; }
    public int getScreenHeight() { return mScreenHeight; }

    public long getFreeMem()     { return mRuntime.freeMemory(); }
    public long getTotalMem()    { return mRuntime.totalMemory(); }
    public long getMaxMem()      { return mRuntime.maxMemory(); }
    public long getUsedMem()     { return mRuntime.totalMemory()-mRuntime.freeMemory(); }

    private String      mCommandLineParameter_BaseRoutineClassName;
    private DisplayMode mCommandLineParameter_DisplayMode;
    private int         mCommandLineParameter_FrameRate;
    private boolean     mCommandLineParameter_FullScreen;
    private boolean     mCommandLineParameter_MultiSampling;
    private int         mCommandLineParameter_NumberOfSampleBuffers;
    private boolean     mCommandLineParameter_AnisotropicFiltering;
    private float       mCommandLineParameter_AnisotropyLevel;
    private boolean     mCommandLineParameter_FrameCapture;
    private boolean     mCommandLineParameter_VSync;
    private boolean     mCommandLineParameter_FrameSkip;
    private String      mCommandLineParameter_WindowToolkit;
    private String      mCommandLineParameter_MusicFileName;
    private int         mCommandLineParameter_StartFrame;
    private int         mCommandLineParameter_EndFrame;
    private boolean     mUsesFullScreenMode;
    private boolean     mCommandLineParameter_Stereoscopic;
    private float       mCommandLineParameter_StereoscopicEyeSeparation;
    private String      mCommandLineParameter_StereoscopicOutputMode;
    
    public String   getBaseRoutineClassName()       { return mCommandLineParameter_BaseRoutineClassName; }
    public boolean  preferMultiSampling()           { return mCommandLineParameter_MultiSampling; }
    public int      getNumberOfSamplingBuffers()    { return mCommandLineParameter_NumberOfSampleBuffers; }
    public boolean  preferAnisotropicFiltering()    { return mCommandLineParameter_AnisotropicFiltering; }
    public float    getAnisotropyLevel()            { return mCommandLineParameter_AnisotropyLevel; }
    public boolean  wantsFrameCapture()             { return mCommandLineParameter_FrameCapture; }
    public boolean  wantsVSync()                    { return mCommandLineParameter_VSync; }
    public int      getDesiredFramerate()           { return mCommandLineParameter_FrameRate; }
    public boolean  wantsFrameSkip()                { return mCommandLineParameter_FrameSkip; }
    public String   getWindowToolkitName()          { return mCommandLineParameter_WindowToolkit; }
    public String   getMusicFileName()              { return mCommandLineParameter_MusicFileName; }
    public int      getStartFrame()                 { return mCommandLineParameter_StartFrame; }
    public int      getEndFrame()                   { return mCommandLineParameter_EndFrame; }
    public boolean  usesFullScreenMode()            { return mUsesFullScreenMode; }   
    public boolean  wantsStereoscopic()             { return mCommandLineParameter_Stereoscopic; }
    public float    getStereoscopicEyeSeparation()  { return mCommandLineParameter_StereoscopicEyeSeparation; }
    public String   getStereoscopicOutputMode()     { return mCommandLineParameter_StereoscopicOutputMode; }
    
    public void configureWithUserParameters(
            String inBaseRoutineClassName,
            int inResolutionX,
            int inResolutionY,
            int inFrameRate,
            boolean inFullScreen,
            boolean inMultiSampling,
            int inNumberOfSampleBuffers,
            boolean inAnisotropicFiltering,
            float inAnisotropyLevel,
            boolean inFrameCapture,
            boolean inVSync,
            boolean inFrameSkip,
            String inWindowToolkitName,
            String inMusicFileName,
            int inStartFrame,
            int inEndFrame,
            boolean inStereoscopic,
            float inStereoscopicEyeSeparation,
            String inStereoscopicOutputMode
    ) {
        mCommandLineParameter_BaseRoutineClassName = inBaseRoutineClassName;
        mCommandLineParameter_DisplayMode = (inResolutionX!=-1 && inResolutionY!=-1) ? new DisplayMode(inResolutionX,inResolutionY,32,60) : null;
        mCommandLineParameter_FrameRate = (inFrameRate==-1) ? 60 : inFrameRate;
        mCommandLineParameter_FullScreen = inFullScreen;
        mCommandLineParameter_MultiSampling = inMultiSampling;
        mCommandLineParameter_NumberOfSampleBuffers = (inNumberOfSampleBuffers==-1) ? 2 : inNumberOfSampleBuffers;
        mCommandLineParameter_AnisotropicFiltering = inAnisotropicFiltering;
        mCommandLineParameter_AnisotropyLevel = (inAnisotropyLevel==-1.0f) ? 2.0f : inAnisotropyLevel;
        mCommandLineParameter_FrameCapture = inFrameCapture;
        mCommandLineParameter_VSync = inVSync;
        mCommandLineParameter_FrameSkip = inFrameSkip;
        mCommandLineParameter_WindowToolkit = inWindowToolkitName;
        mCommandLineParameter_MusicFileName = inMusicFileName;
        mCommandLineParameter_StartFrame = inStartFrame;
        mCommandLineParameter_EndFrame = inEndFrame;
        mCommandLineParameter_Stereoscopic = inStereoscopic;
        mCommandLineParameter_StereoscopicEyeSeparation = inStereoscopicEyeSeparation;
        mCommandLineParameter_StereoscopicOutputMode = inStereoscopicOutputMode;
    }

    public int getParameterKey_INT_12() { return mParameterKey_INT_12; }
    public int getParameterKey_INT_34() { return mParameterKey_INT_34; }
    public int getParameterKey_INT_56() { return mParameterKey_INT_56; }
    public int getParameterKey_INT_78() { return mParameterKey_INT_78; }
    public int getParameterKey_INT_90() { return mParameterKey_INT_90; }
    public int getParameterKey_INT_QW() { return mParameterKey_INT_QW; }
    public int getParameterKey_INT_ER() { return mParameterKey_INT_ER; }
    public int getParameterKey_INT_TZ() { return mParameterKey_INT_TZ; }
    public int getParameterKey_INT_UI() { return mParameterKey_INT_UI; }
    public int getParameterKey_INT_OP() { return mParameterKey_INT_OP; }
    public float getParameterKey_FLOAT_AS() { return mParameterKey_FLOAT_AS; }
    public float getParameterKey_FLOAT_DF() { return mParameterKey_FLOAT_DF; }
    public float getParameterKey_FLOAT_GH() { return mParameterKey_FLOAT_GH; }
    public float getParameterKey_FLOAT_JK() { return mParameterKey_FLOAT_JK; }
    public float getParameterKey_FLOAT_LÖ() { return mParameterKey_FLOAT_LÖ; }
    public float getParameterKey_FLOAT_YX() { return mParameterKey_FLOAT_YX; }
    public float getParameterKey_FLOAT_CV() { return mParameterKey_FLOAT_CV; }
    public float getParameterKey_FLOAT_BN() { return mParameterKey_FLOAT_BN; }
    public float getParameterKey_FLOAT_MCOMMA() { return mParameterKey_FLOAT_MCOMMA; }
    public float getParameterKey_FLOAT_X() { return mParameterKey_FLOAT_X; }
    public float getParameterKey_FLOAT_Y() { return mParameterKey_FLOAT_Y; }
    public float getParameterKey_FLOAT_Z() { return mParameterKey_FLOAT_Z; }

}
