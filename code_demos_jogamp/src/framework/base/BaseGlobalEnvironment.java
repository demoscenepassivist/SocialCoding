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

public class BaseGlobalEnvironment {

	private static BaseGlobalEnvironment mBaseGlobalEnvironmentInstance = null;
	private Runtime mRuntime;
	
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
		//get current display mode/desktop display mode ...
		DisplayMode tDesktopDisplayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		BaseLogging.getInstance().info("DESKTOP DISPLAYMODE "+BaseWindowToolkitUtils.convertDisplayModeToString(tDesktopDisplayMode));
		//use desktop resolution but force 32bit@60hz ... (;
		DisplayMode tDesktopDisplayModeNormalized = new DisplayMode(tDesktopDisplayMode.getWidth(),tDesktopDisplayMode.getHeight(),32,60);
		//print graphics capabilities ... values are very unreliable X-)
		BaseWindowToolkitUtils.printGraphicsCapabilities();
		//create native window container and AWT GLCanvas ... :-0
		final Frame tFrame = new Frame("Jogamp.org - JOGL Demos");
	    GLCapabilities tGLCapabilities = new GLCapabilities(GLProfile.getDefault());
	    //enable/configure multisampling support ...
        if (preferMultiSampling()) {
        	tGLCapabilities.setSampleBuffers(true);
        	tGLCapabilities.setNumSamples(getNumberOfSamplingBuffers());
        }
	    GLCanvas tGLCanvas = new GLCanvas(tGLCapabilities);
	    BaseGLEventListener tBaseGLEventListener = new BaseGLEventListener();
	    tGLCanvas.addGLEventListener(tBaseGLEventListener);
	    tFrame.add(tGLCanvas);
	    final Animator tAnimator;
	    //if vsync is requested use the vsync framerate ... otherwise use custom framerate ...
	    if (BaseGlobalEnvironment.getInstance().wantsVSync()) {
	    	tAnimator = new Animator(tGLCanvas);
	    	tAnimator.setRunAsFastAsPossible(true);
	    } else {
	    	//i'm currently not sure if it is a good idea to use system-clock based timing ... :+)
	    	tAnimator = new FPSAnimator(tGLCanvas,mCommandLineParameter_FrameRate,true);
	    }	    
	    tFrame.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		//run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting ...
	    		new Thread(new Runnable() {
	    			public void run() {
	    				tAnimator.stop();
	    				System.exit(0);
	    			}
	    		}).start();
	        }
	    });
	    tFrame.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar()=='\u001B') {
					//run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting ...
					new Thread(new Runnable() {
						public void run() {
							tAnimator.stop();
							System.exit(0);
						}
					}).start();
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
	    } else {
	    	BaseLogging.getInstance().info("FULLSCREEN MODE NOT SUPPORTED ... RUNNING IN WINDOWED MODE INSTEAD!");
		    tFrame.setIgnoreRepaint(true);
		    tFrame.setResizable(false);
		    DisplayMode tDisplayMode = BaseWindowToolkitUtils.getBestDisplayModeWithBackupModes(BaseWindowToolkitUtils.DEFAULT_DISPLAYMODES,BaseWindowToolkitUtils.BACKUP_DISPLAYMODES,tDesktopDisplayModeNormalized);
		    mScreenWidth = tDisplayMode.getWidth();
		    mScreenHeight =  tDisplayMode.getHeight();
		    tFrame.setVisible(true);
			tFrame.setSize(tDisplayMode.getWidth()+tFrame.getInsets().left+tFrame.getInsets().right, tDisplayMode.getHeight()+tFrame.getInsets().top+tFrame.getInsets().bottom);	
			tFrame.setLocation((tDesktopDisplayMode.getWidth()-tDisplayMode.getWidth())/2, (tDesktopDisplayMode.getHeight()-tDisplayMode.getHeight())/2);
			tFrame.requestFocus();
	    }
        tAnimator.start();	
	}
		
	/* --------------------------------------------------------------------------------------------------------------------------------------------------- */
	
	private int mScreenWidth;
	private int mScreenHeight;
	public int getScreenWidth()  { return mScreenWidth; }
	public int getScreenHeight() { return mScreenHeight; }
	
	public long getFreeMem()	 { return mRuntime.freeMemory(); }
	public long getTotalMem()	 { return mRuntime.totalMemory(); }
	public long getMaxMem() 	 { return mRuntime.maxMemory(); }
	public long getUsedMem() 	 { return mRuntime.totalMemory()-mRuntime.freeMemory(); }

	private String      mCommandLineParameter_BaseRoutineClassName;
	private DisplayMode	mCommandLineParameter_DisplayMode;
	private int 		mCommandLineParameter_FrameRate;
	private boolean 	mCommandLineParameter_FullScreen;
	private boolean 	mCommandLineParameter_MultiSampling;
	private int 		mCommandLineParameter_NumberOfSampleBuffers;
	private boolean 	mCommandLineParameter_AnisotropicFiltering;
	private float 		mCommandLineParameter_AnisotropyLevel;
	private boolean 	mCommandLineParameter_FrameCapture;
	private boolean		mCommandLineParameter_VSync;
	
	public String	getBaseRoutineClassName()		{ return mCommandLineParameter_BaseRoutineClassName; }
	public boolean	preferMultiSampling() 			{ return mCommandLineParameter_MultiSampling; }
	public int		getNumberOfSamplingBuffers()	{ return mCommandLineParameter_NumberOfSampleBuffers; }
	public boolean	preferAnisotropicFiltering() 	{ return mCommandLineParameter_AnisotropicFiltering; }
	public float	getAnisotropyLevel() 			{ return mCommandLineParameter_AnisotropyLevel; }
	public boolean	wantsFrameCapture() 			{ return mCommandLineParameter_FrameCapture; }
	public boolean	wantsVSync()					{ return mCommandLineParameter_VSync; }
	
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
			boolean inVSync
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
	}
	
}
