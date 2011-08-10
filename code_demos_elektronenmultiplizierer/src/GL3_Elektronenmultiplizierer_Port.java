/**
 * Copyright 2011 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2011. Sure it got a little bigger 
 ** while porting but the shader and control code remained more or less untouched. The intro renders
 ** a fullscreen billboard using a single fragment shader. The shader encapsulates basically two 
 ** different routines: A sphere-tracing based raymarcher for a single fractal formula and a bitmap
 ** orbit trap julia+mandelbrot fractal renderer. Additionally an inline-processing analog-distortion
 ** filter is applied to all rendered fragments to make the overall look more interesting.
 **
 ** The different intro parts are all parameter variations of the two routines in the fragment shader 
 ** synched to the music: Parts 3+5 are obviously the mandelbrot and julia bitmap orbit traps, and parts
 ** 1,2,4 and 6 are pure fractal sphere tracing.
 **
 ** During the development of the intro it turned out that perfectly raymarching every pixel of the orbit
 ** trapped julia+mandelbrot fractal was way to slow even on highend hardware. So I inserted a lowres 
 ** intermediate FBO to be used by the bitmap based orbit trap routine wich was ofcourse way faster, but
 ** had the obvious upscaling artefacts. Maybe I'll produce a perfect quality version for very patient 
 ** people with insane hardware :)
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 **
 ** Distance rendering for fractals: http://www.iquilezles.org/www/articles/distancefractals/distancefractals.htm
 ** Geometric orbit traps: http://www.iquilezles.org/www/articles/ftrapsgeometric/ftrapsgeometric.htm
 ** Bitmap orbit traps: http://www.iquilezles.org/www/articles/ftrapsbitmap/ftrapsbitmap.htm
 ** Ambient occlusion techniques: http://www.iquilezles.org/www/articles/ao/ao.htm
 ** Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces: http://graphics.cs.uiuc.edu/~jch/papers/zeno.pdf
 ** Rendering fractals with distance estimation function: http://www.iquilezles.org/www/articles/mandelbulb/mandelbulb.htm
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=lvC8maVHh8Q
 ** Original release from the Revision can be found here: http://www.pouet.net/prod.php?which=56860
 **/

import java.awt.*;
import java.io.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.common.nio.*;
import com.jogamp.newt.*;
import com.jogamp.newt.opengl.*;
import com.jogamp.newt.util.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_Elektronenmultiplizierer_Port implements GLEventListener {

//BEGIN --- shader utils section ---

    //I like it the oldskool way ... :)
    private void checkShaderLogInfo(GL2 inGL, int inShaderObjectID) {
        IntBuffer tReturnValue = Buffers.newDirectIntBuffer(1);
        inGL.glGetObjectParameterivARB(inShaderObjectID, GL_OBJECT_INFO_LOG_LENGTH_ARB, tReturnValue);
        int tLogLength = tReturnValue.get();
        if (tLogLength <= 1) {
            return;
        }
        ByteBuffer tShaderLog = Buffers.newDirectByteBuffer(tLogLength);
        tReturnValue.flip();
        inGL.glGetInfoLogARB(inShaderObjectID, tLogLength, tReturnValue, tShaderLog);
        byte[] tShaderLogBytes = new byte[tLogLength];
        tShaderLog.get(tShaderLogBytes);
        String tShaderValidationLog = new String(tShaderLogBytes);
        StringReader tStringReader = new StringReader(tShaderValidationLog);
        LineNumberReader tLineNumberReader = new LineNumberReader(tStringReader);
        String tCurrentLine;
        try {
            while ((tCurrentLine = tLineNumberReader.readLine()) != null) {
                if (tCurrentLine.trim().length()>0) {
                    System.out.println("GLSL VALIDATION: "+tCurrentLine.trim());
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private String loadShaderSourceFileAsString(String inFileName) {
        System.out.println("LOADING SHADER SOURCECODE FROM "+inFileName);
        try {
            BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader((new Object()).getClass().getResourceAsStream(inFileName)));
            StringBuilder tStringBuilder = new StringBuilder();
            String tCurrentLine;
            while ((tCurrentLine = tBufferedReader.readLine()) != null) {
                tStringBuilder.append(tCurrentLine);
                tStringBuilder.append("\n");
            }
            return tStringBuilder.toString();
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    private int loadFragmentShaderFromFile(GL2 inGL,String inShaderSourceFileName) {
        return generateFragmentShader(inGL,loadShaderSourceFileAsString(inShaderSourceFileName));
    }

    private int generateFragmentShader(GL2 inGL,String inShaderSource) {
        return generateShader(inGL,inShaderSource,GL_FRAGMENT_SHADER);
    }

    private int generateShader(GL2 inGL,String inShaderSource,int inShaderType) {
        int tShader = inGL.glCreateShader(inShaderType);
        String[] tShaderSource = {inShaderSource};
        inGL.glShaderSource(tShader, 1, tShaderSource, (int[])null, 0);
        inGL.glCompileShader(tShader);
        checkShaderLogInfo(inGL, tShader);
        return tShader;
    }

    private int generateSimple_1xFS_ShaderProgramm(GL2 inGL, int inFragmentShaderObjectID) {
        return generateSimple_1xFS_OR_1xVS_ShaderProgramm(inGL,inFragmentShaderObjectID);
    }

    private int generateSimple_1xFS_OR_1xVS_ShaderProgramm(GL2 inGL, int inGenericShaderObjectID) {
        int tLinkedShader = inGL.glCreateProgram();
        inGL.glAttachShader(tLinkedShader, inGenericShaderObjectID);
        inGL.glLinkProgram(tLinkedShader);
        inGL.glValidateProgram(tLinkedShader);
        checkShaderLogInfo(inGL, tLinkedShader);
        return tLinkedShader;
    }

    private void setUniform1f(GL2 inGL,int inProgramID, String inName,float inValue) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID,inName);
        if (tUniformLocation != -1) {
            inGL.glUniform1f(tUniformLocation, inValue);
        } else {
            System.out.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

    private void setUniform2fv(GL2 inGL,int inProgramID, String inName, FloatBuffer inValues) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID,inName);
        if (tUniformLocation != -1) {
            inGL.glUniform2fv(tUniformLocation, inValues.capacity()/2, inValues);
        } else {
            System.out.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

    private void setUniform1i(GL2 inGL,int inProgramID,String inName,int inValue) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID,inName);
        if (tUniformLocation != -1) {
            inGL.glUniform1i(tUniformLocation, inValue);
        } else {
            System.out.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

//END --- shader utils section ---

//BEGIN --- texture utils section ---

    private int generateTextureID(GL2 inGL) {
        int[] result = new int[1];
        inGL.glGenTextures(1, result, 0);
        System.out.println("ALLOCATED NEW JOGL TEXTURE ID="+result[0]);
        return result[0];
    }

    private void deleteTextureID(GL2 inGL, int inTextureID) {
        System.out.println("DELETING JOGL TEXTURE ID="+inTextureID);
        inGL.glDeleteTextures(1, new int[] {inTextureID}, 0); 
    }

//END --- texture utils section---

//BEGIN --- direct buffer utils section ---

    private FloatBuffer createDirectFloatBuffer(float[] inFloatArray) {
        FloatBuffer tDirectFloatBuffer = Buffers.newDirectFloatBuffer(inFloatArray.length);
        tDirectFloatBuffer.put(inFloatArray);
        tDirectFloatBuffer.rewind();
        if (tDirectFloatBuffer.isDirect()) {
            System.out.println("ALLOCATED DIRECT FLOATBUFFER ... LENGHT="+inFloatArray.length);
        } else {
            System.out.println("ALLOCATED NON-DIRECT FLOATBUFFER ... LENGHT="+inFloatArray.length);
        }
        return tDirectFloatBuffer;
    }

//END --- direct buffer utils section ---

//BEGIN --- BaseGlobalEnvironment replacement ---

    private int mScreenWidth;
    private int mScreenHeight;
    public int getScreenWidth()  { return mScreenWidth; }
    public int getScreenHeight() { return mScreenHeight; }

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
    private boolean     mUsesFullScreenMode;

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
    public boolean  usesFullScreenMode()            { return mUsesFullScreenMode; }

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
            String inMusicFileName
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
    }

    public void initGLEnvironment_NEWT() {
        //get current display mode/desktop display mode ... uses AWT stuff X-(
        DisplayMode tDesktopDisplayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        System.out.println("DESKTOP DISPLAYMODE "+tDesktopDisplayMode);
        //use desktop resolution but force 32bit@60hz ... (;
        DisplayMode tDesktopDisplayModeNormalized = new DisplayMode(tDesktopDisplayMode.getWidth(),tDesktopDisplayMode.getHeight(),32,60);
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
        tNEWTWindow.setTitle("Jogamp.org - Elektronenmultiplizierer - Monolith Port NEWT");   
        tNEWTWindow.addGLEventListener(this);
        final AnimatorBase tAnimator;
        //if vsync is requested use the vsync framerate ... otherwise use custom framerate ...
        if (wantsVSync() || mCommandLineParameter_FrameRate==Integer.MAX_VALUE) {
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
        if (mCommandLineParameter_FullScreen) {
            System.out.println("SWITCHING TO NEWT FULLSCREEN MODE!");
            //need to change this to display mode querying in the future, when NEWT is ready X-)
            DisplayMode tDisplayMode = tDesktopDisplayModeNormalized;
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
            //filter available ScreenModes and get the nearest one ... doesn't seem to work correctly at the moment :-(
            java.util.List<?> screenModes = tScreen.getScreenModes();
            screenModes = ScreenModeUtil.filterByRate(screenModes, tOriginalRefreshRate);
            screenModes = ScreenModeUtil.filterByRotation(screenModes, tRotation);
            screenModes = ScreenModeUtil.filterByResolution(screenModes, tResolution);
            screenModes = ScreenModeUtil.getHighestAvailableBpp(screenModes);
            //pick 1st one ...
            tScreen.setCurrentScreenMode((ScreenMode)screenModes.get(0));
            mUsesFullScreenMode = true;
        } else {
            System.out.println("FULLSCREEN MODE NOT SUPPORTED ... RUNNING IN WINDOWED MODE INSTEAD!");
            DisplayMode tDisplayMode;
            if (mCommandLineParameter_DisplayMode!=null) {
                tDisplayMode = mCommandLineParameter_DisplayMode;
            } else {
                //choose best preferred display mode ...
                tDisplayMode = tDesktopDisplayModeNormalized;
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

//END --- BaseGlobalEnvironment replacement ---

//BEGIN --- BaseGLEventListener replacement ---

    private GLU mGLU;
    private GLUT mGLUT;

    public void init(GLAutoDrawable inDrawable) {
        mGLU = new GLU();
        mGLUT = new GLUT();
        GL2 tGL = inDrawable.getGL().getGL2();
        if (wantsVSync()) {
            //enable vertical sync ... :*
            tGL.setSwapInterval(1);
        }
        initRuntime(tGL,mGLU,mGLUT);
    }

    public void display(GLAutoDrawable inDrawable) {
        GL2 tGL = inDrawable.getGL().getGL2();
        mainLoopRuntime(tGL,mGLU,mGLUT);
    }

    public void reshape(GLAutoDrawable inDrawable, int inX, int inY, int inWidth, int inHeight) {
        System.out.println("RESHAPE CALLED ON GLAUTODRAWALBE! X="+inX+" Y="+inY+" WIDTH="+inWidth+" HEIGHT="+inHeight);
    }

    public void displayChanged(GLAutoDrawable inDrawable, boolean inModeChanged, boolean inDeviceChanged) {
        System.out.println("DISPLAYCHANGED CALLED ON GLAUTODRAWALBE! MODECHANGED="+inModeChanged+" DEVICECHANGED="+inDeviceChanged);
    }

    public void dispose(GLAutoDrawable inDrawable) {
        System.out.println("DISPOSE CALLED ON GLAUTODRAWALBE!");
        GL2 tGL = inDrawable.getGL().getGL2();
        cleanupRuntime(tGL,mGLU,mGLUT);
    }

//END --- BaseGLEventListener replacement ---

//BEGIN --- BaseRoutineRuntime replacement ---

    private int mFrameCounter;
    private long mFrameSkipAverageFramerateTimeStart;
    private boolean mFrameSkipAverageFrameStartTimeInitialized;
    private long mFrameSkipAverageFramerateTimeEnd; 
    private double mFrameCounterDifference;
    private double mFrameCounterTargetValue;
    private int mSkippedFramesCounter;
//    private BaseMusic mBaseMusic;
    boolean mMusicSyncStartTimeInitialized = false;

    public void initRuntime(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mFrameCounter = 0;
        System.out.println("INITIALIZING BASEROUTINE ...");
        initRoutine(inGL,inGLU,inGLUT);
//        mBaseMusic = new BaseMusic(BaseGlobalEnvironment.getInstance().getMusicFileName());
//        mBaseMusic.init();
//        mBaseMusic.play();
    }

    public void mainLoopRuntime(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //if NO music is used sync to mainloop start ...
        if (!mFrameSkipAverageFrameStartTimeInitialized) {
            mFrameSkipAverageFrameStartTimeInitialized = true;
            mFrameSkipAverageFramerateTimeStart = System.nanoTime();
        }
//        if (!getBaseMusic().isOffline()) {
//            //if music IS used sync to first second of music ...
//            if (BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()>0 && !mMusicSyncStartTimeInitialized) {
//                BaseLogging.getInstance().info("Synching to BaseMusic ...");
//                mFrameSkipAverageFramerateTimeStart = (long)(System.nanoTime()-((double)BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()*1000000.0d));
//                mMusicSyncStartTimeInitialized = true;
//            }
//        }
        //allow music DSP's to synchronize with framerate ...
//        mBaseMusic.synchonizeMusic();
        //create default frustum state ...
        resetFrustumToDefaultState(inGL,inGLU,inGLUT);
        //clear screen and z-buffer ...
        inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        mainLoop(mFrameCounter,inGL,inGLU,inGLUT);
        checkForGlError(inGL,inGLU);
        //---
        mFrameCounter++;
        if (wantsFrameSkip()) {
            mFrameSkipAverageFramerateTimeEnd = System.nanoTime();
            double tDesiredFrameRate = (float)getDesiredFramerate();
            double tSingleFrameTime = 1000000000.0f/tDesiredFrameRate;
            double tElapsedTime = mFrameSkipAverageFramerateTimeEnd - mFrameSkipAverageFramerateTimeStart;
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
    }

    public void cleanupRuntime(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        cleanupRoutine(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

//    public BaseMusic getBaseMusic() {
//        return mBaseMusic;
//    }
    
    public void resetFrameCounter() {
        mFrameCounter = 0;
    }

    public void resetFrustumToDefaultState(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        resetFrustumToDefaultState(inGL,inGLU,inGLUT,getScreenWidth(),getScreenHeight());
    }

    public void resetFrustumToDefaultState(GL2 inGL,GLU inGLU,GLUT inGLUT,int inScreenWidth,int inScreenHeight) {
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

    private void checkForGlError(GL2 inGL,GLU inGLU) {
        int tError = inGL.glGetError();
        String tErrorString = "!!! GL-ERROR !!! GLU ERROR STRING FOR ERROR="+inGLU.gluErrorString(tError);
        if (tError!=GL_NO_ERROR) {
            System.out.println(tErrorString);
        }
    }

//END --- BaseRoutineRuntime ---

    protected int mLinkedShaderID;
    protected FloatBuffer mScreenDimensionUniform2fv;

    protected int mFrameBufferTextureID;
    protected int mFrameBufferObjectID;
    protected int mSyncTime;
    protected int mSyncEventNumber;
    protected float mEffectTime;
    protected int mEffectNumber;
    protected int mEffectSyncTime;

    protected boolean mSyncEvent_01;
    protected boolean mSyncEvent_02;
    protected boolean mSyncEvent_03;
    protected boolean mSyncEvent_04;
    protected boolean mSyncEvent_05;
    protected boolean mSyncEvent_06;
    protected boolean mSyncEvent_07;
    protected boolean mSyncEvent_08;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/elektronenmultiplizierer_port.fs");
        int tFragmentShader = loadFragmentShaderFromFile(inGL,"/shaders/elektronenmultiplizierer_development.fs");
        mLinkedShaderID = generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = createDirectFloatBuffer(new float[] {(float)getScreenWidth(), (float)getScreenHeight()});
        //generate framebufferobject
        mFrameBufferTextureID = generateTextureID(inGL);
        inGL.glBindTexture(GL_TEXTURE_2D, mFrameBufferTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 384, 384, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        //allocate the framebuffer object ...
        int[] result = new int[1];
        inGL.glGenFramebuffers(1, result, 0);
        mFrameBufferObjectID = result[0];
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mFrameBufferTextureID, 0);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void handleSyncEvent(int inMMTime_u_ms) {
        mSyncTime = inMMTime_u_ms;
        mSyncEventNumber++;
        System.out.println("NEW SYNC EVENT! tSyncEventNumber="+mSyncEventNumber+" tSyncTime="+mSyncTime);
        if (mSyncEventNumber==0 || mSyncEventNumber==2 || mSyncEventNumber==5 || mSyncEventNumber==8) {
            mEffectSyncTime = inMMTime_u_ms;
        }
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //use this for offline rendering/capture ...
        int MMTime_u_ms = (int)((((double)inFrameNumber)*44100.0f)/60.0f);
        //use this for music synched rendering ...
        //int MMTime_u_ms = (int)(BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()*(44100.0f/1000.0f));
        //dedicated sync variable for each event ... kinda lame but who cares X-)
        if (MMTime_u_ms>=522240  && !mSyncEvent_01) { mSyncEvent_01 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=1305480 && !mSyncEvent_02) { mSyncEvent_02 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=1827720 && !mSyncEvent_03) { mSyncEvent_03 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=2349960 && !mSyncEvent_04) { mSyncEvent_04 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=3394440 && !mSyncEvent_05) { mSyncEvent_05 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=3916680 && !mSyncEvent_06) { mSyncEvent_06 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=4438408 && !mSyncEvent_07) { mSyncEvent_07 = true; handleSyncEvent(MMTime_u_ms); }
        if (MMTime_u_ms>=5482831 && !mSyncEvent_08) { mSyncEvent_08 = true; handleSyncEvent(MMTime_u_ms); }
        //calculate current time based on 60fps reference framerate ...
        MMTime_u_ms = (int)((((double)inFrameNumber)*44100.0f)/60.0f);
        float XRESf = getScreenWidth();
        float YRESf = getScreenHeight();
        inGL.glDisable(GL_LIGHTING);
        inGL.glDisable(GL_CULL_FACE);
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, XRESf, YRESf, 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glActiveTexture(GL_TEXTURE0);
        //gogogo! O-)
        float tBrightnessSync = 40.0f-((MMTime_u_ms-mSyncTime)/1000.0f);
        if (tBrightnessSync<1) {
            tBrightnessSync=1;
        }
        mEffectTime = (float)((MMTime_u_ms-mEffectSyncTime)/100000.0f);
        if (mSyncEventNumber==0 && mEffectTime<4.0f) {
            //fadein and fullscreen rotate
            tBrightnessSync = mEffectTime/4.0f;
         }
         if (mSyncEventNumber==8 && mEffectTime>12.0f) {
             //fullscrenn mushroom transform
             tBrightnessSync = 1.0f-((mEffectTime-12.0f)/3.5f);
         }
         if (mSyncEventNumber==0 || mSyncEventNumber==1) {
             //zoomin from fog
             mEffectNumber = 3;
             mEffectTime *= 1.75;
             float tEffectTimeMax = 9.3f; 
             if (mEffectTime>=tEffectTimeMax) {
                 mEffectTime=tEffectTimeMax;
             }
         } else if(mSyncEventNumber==2 || mSyncEventNumber==3) {
             //transform big after zoomin
             mEffectNumber = 4;
             mEffectTime *= 0.25f;
         } else if(mSyncEventNumber==4) {
             //mandelbrot orbit-trap zoomout
             mEffectNumber = 1;
             mEffectTime *= 0.0002f;
         } else if(mSyncEventNumber==5 || mSyncEventNumber==6) {
             //inside fractal
             mEffectNumber = 5;
             mEffectTime *= 0.02f;
         } else if(mSyncEventNumber==7) {
             //spiral orbit-trap
             mEffectNumber = 0;
             mEffectTime *= 0.02f;
         } else if(mSyncEventNumber==8) {
             //fadeout fractal
             mEffectNumber = 6;
             mEffectTime *= 0.364f;
         }
         inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
         inGL.glUseProgram(mLinkedShaderID);
         if(mSyncEventNumber==7) {
             setUniform1i(inGL,mLinkedShaderID,"en",2);
         }
         if(mSyncEventNumber==4) {
             setUniform1i(inGL,mLinkedShaderID,"en",7);
         }
         setUniform1i(inGL,mLinkedShaderID,"fb",0);
         setUniform1f(inGL,mLinkedShaderID,"tm",MMTime_u_ms/40000.0f);
         setUniform1f(inGL,mLinkedShaderID,"br",tBrightnessSync);
         setUniform1f(inGL,mLinkedShaderID,"et",9.1f);
         setUniform2fv(inGL,mLinkedShaderID,"resolution",mScreenDimensionUniform2fv);
         if(mSyncEventNumber==4 || mSyncEventNumber==7) {
             //render to fbo only when using julia/mandel orbittrap ...
             inGL.glBegin(GL_QUADS);
                 inGL.glTexCoord2f(0.0f, 1.0f);
                 inGL.glVertex2f(0.0f, 0.0f);
                 inGL.glTexCoord2f(1.0f, 1.0f);
                 inGL.glVertex2f(XRESf, 0.0f);
                 inGL.glTexCoord2f(1.0f, 0.0f);
                 inGL.glVertex2f(XRESf, YRESf);
                 inGL.glTexCoord2f(0.0f, 0.0f);
                 inGL.glVertex2f(0.0f, YRESf);
             inGL.glEnd();
         }
         inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
         setUniform1i(inGL,mLinkedShaderID,"en",mEffectNumber);
         setUniform1f(inGL,mLinkedShaderID,"et",mEffectTime);
         inGL.glEnable(GL_TEXTURE_2D);
         inGL.glBindTexture(GL_TEXTURE_2D, mFrameBufferTextureID);
         inGL.glBegin(GL_QUADS);
             inGL.glTexCoord2f(0.0f, 1.0f);
             inGL.glVertex2f(0.0f, 0.0f);
             inGL.glTexCoord2f(1.0f, 1.0f);
             inGL.glVertex2f(XRESf, 0.0f);
             inGL.glTexCoord2f(1.0f, 0.0f);
             inGL.glVertex2f(XRESf, YRESf);
             inGL.glTexCoord2f(0.0f, 0.0f);
             inGL.glVertex2f(0.0f, YRESf);
         inGL.glEnd();
         inGL.glUseProgram(0);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mFrameBufferObjectID));
        deleteTextureID(inGL,mFrameBufferTextureID);
        inGL.glDeleteShader(mLinkedShaderID);
        inGL.glFlush();
    }

//BEGIN --- main entry point ---

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
        if (args.length>0) {
            for (int i=0; i<args.length; i++) {
                System.out.println("PROCESSING CMDLINE PARAMETER ... ARG="+args[i]);
                if (args[i].trim().startsWith("-RESOLUTION=")) {
                    String tResolutionParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("CUSTOM RESOLUTION SET TO '"+tResolutionParameter+"'");
                    if (!tResolutionParameter.equalsIgnoreCase("auto")) {
                        String tXResolution = tResolutionParameter.substring(0,tResolutionParameter.indexOf("x"));
                        String tYResolution = tResolutionParameter.substring(tResolutionParameter.indexOf("x")+1,tResolutionParameter.length());
                        System.out.println("PARSED VALUES ARE X="+tXResolution+" Y="+tYResolution);
                        tResolutionX = Integer.parseInt(tXResolution);
                        tResolutionY = Integer.parseInt(tYResolution);
                    }
                } else if(args[i].trim().startsWith("-ROUTINE=")) {
                    String tRoutineParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("ROUTINE TO EXECUTE '"+tRoutineParameter+"'");
                    tRoutineClassName = tRoutineParameter;
                } else if(args[i].trim().startsWith("-FRAMERATE=")) {
                    String tFrameRateParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("CUSTOM FRAMERATE SET TO '"+tFrameRateParameter+"'");
                    if (tFrameRateParameter.equalsIgnoreCase("auto")) {
                        tFrameRate = -1;
                    } else if (tFrameRateParameter.equalsIgnoreCase("max")) {
                        tFrameRate = Integer.MAX_VALUE;
                    } else {
                        tFrameRate = Integer.parseInt(tFrameRateParameter);
                    }
                } else if(args[i].trim().startsWith("-FULLSCREEN=")) {
                    String tFullScreenParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("FULLSCREEN ENABLED '"+tFullScreenParameter+"'");
                    tFullScreen = Boolean.parseBoolean(tFullScreenParameter);
                } else if(args[i].trim().startsWith("-MULTISAMPLING=")) {
                    String tMultiSamplingParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("MULTISAMPLING ENABLED '"+tMultiSamplingParameter+"'");
                    tMultiSampling = Boolean.parseBoolean(tMultiSamplingParameter);
                } else if(args[i].trim().startsWith("-SAMPLEBUFFERS=")) {
                    String tSampleBuffersParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("CUSTOM NUMBER OF SAMPLEBUFFERS SET TO '"+tSampleBuffersParameter+"'");
                    tNumberOfSampleBuffers = Integer.parseInt(tSampleBuffersParameter); 
                } else if(args[i].trim().startsWith("-ANISOTROPICFILTERING=")) {
                    String tAnisotropicFilteringParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("ANISOTROPICFILTERING ENABLED '"+tAnisotropicFilteringParameter+"'");
                    tAnisotropicFiltering = Boolean.parseBoolean(tAnisotropicFilteringParameter);
                } else if(args[i].trim().startsWith("-ANISOTROPYLEVEL=")) {
                    String tAnisotropyLevelParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("CUSTOM ANISOTROPY LEVEL SET TO '"+tAnisotropyLevelParameter+"'");
                    tAnisotropyLevel = Float.parseFloat(tAnisotropyLevelParameter);
                } else if(args[i].trim().startsWith("-FRAMECAPTURE=")) {
                    String tFrameCaptureParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("FRAME CAPTURE ENABLED '"+tFrameCaptureParameter+"'");
                    tFrameCapture = Boolean.parseBoolean(tFrameCaptureParameter);
                } else if(args[i].trim().startsWith("-VSYNC=")) {
                    String tVSyncParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("VSYNC ENABLED '"+tVSyncParameter+"'");
                    tVSync = Boolean.parseBoolean(tVSyncParameter);
                } else if(args[i].trim().startsWith("-FRAMESKIP=")) {
                    String tFrameSkipParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("FRAMESKIP ENABLED '"+tFrameSkipParameter+"'");
                    tFrameSkip = Boolean.parseBoolean(tFrameSkipParameter);
                } else if(args[i].trim().startsWith("-WINDOWTOOLKIT=")) {
                    String tWindowToolkitParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("WINDOW TOOLKIT TO USE '"+tWindowToolkitParameter+"'");
                    tWindowToolkitName = tWindowToolkitParameter;
                } else if(args[i].trim().startsWith("-MUSIC=")) {
                    String tMusicParameter = args[i].substring(args[i].indexOf("=")+1,args[i].length());
                    System.out.println("MUSIC TO PLAY '"+tMusicParameter+"'");
                    tMusicFileName = tMusicParameter;
                } else {
                    System.out.println("ERROR! ILLEGAL ARGUMENT FOUND! ARGUMENT="+args[i]);
                }
            }
            System.out.println("CMDLINE PARAMETERS PARSED AND CONVERTED ...");
        }
        final GL3_Elektronenmultiplizierer_Port tGL3_Elektronenmultiplizierer = new GL3_Elektronenmultiplizierer_Port();
        tGL3_Elektronenmultiplizierer.configureWithUserParameters(
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
                tGL3_Elektronenmultiplizierer.initGLEnvironment_NEWT();
            }
        });
    }
    
//END --- main entry point ---
    
}
