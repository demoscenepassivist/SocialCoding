package jogamp.routine.jogl.programmablepipeline;

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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2012. Sure it got a little bigger 
 ** while porting but the shader and control code remained more or less untouched. The intro renders
 ** a fullscreen billboard using a single fragment shader. The shader basically encapsulates a 
 ** sphere-tracing based raymarcher for a single fractal formula with camera handling. Additionally
 ** a second post-processing shader is applied to the render output from the raymarching shader. Post
 ** effects are god-rays, tv-lines and noise to make the overall look more interesting and less 'sterile'.
 ** The different intro parts are all parameter and camera position variations of the same fractal. 
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 **
 ** Distance rendering for fractals: http://www.iquilezles.org/www/articles/distancefractals/distancefractals.htm
 ** Ambient occlusion techniques: http://www.iquilezles.org/www/articles/ao/ao.htm
 ** Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces: http://graphics.cs.uiuc.edu/~jch/papers/zeno.pdf
 ** Rendering fractals with distance estimation function: http://www.iquilezles.org/www/articles/mandelbulb/mandelbulb.htm
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=UjgRGDhgehA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=59086
 **/

import java.nio.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_Hartverdrahtet_Port extends BaseRoutineAdapter implements BaseRoutineInterface {

    protected int mLinkedShaderID_Main;
    protected int mLinkedShaderID_Post;
    protected int mFrameBufferTextureID;
    protected int mFrameBufferObjectID;
    protected FloatBuffer mScreenDimensionUniform2fv;
    protected int mSyncEventNumber;
    protected float mEffectTime;
    protected int mEffectSyncTime;
    protected int mLastEffectSyncTime;
    protected int mBrightnessSyncTime;
    protected float mCurrent_Scene_Timer;
    protected float mLast_Scene_Timer; 
    protected int mNote60_SyncCounter;
    protected int[] mNote60_SyncPoints = new int[] {1375744,2053120,2392064,2730496,3746816,4424192,5080576,5757952,6435328,Integer.MAX_VALUE};
    protected boolean[] mNote60_SyncPoints_Check = new boolean[mNote60_SyncPoints.length];    
    protected int mNote72_SyncCounter;
    protected int[] mNote72_SyncPoints = new int[] { 
            2111488,2132480,2280960,2301952,2450432,2471424,2619392,2640896,2788864,2809856,2958336,
            2979328,4482560,4503552,4651520,4673024,4820992,4841984,4990464,5011456,5159936,5180928,
            5328896,5350400,5498368,5519360,5667840,5688832,6514688,6535680,6683648,6705152,6853120,
            6874112,7022592,7043584,7192064,7213056,7361024,7382528,Integer.MAX_VALUE
    };
    protected boolean[] mNote72_SyncPoints_Check = new boolean[mNote72_SyncPoints.length];

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/hartverdrahtet_port_main.fs");
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/hartverdrahtet_development_main.fs");
        mLinkedShaderID_Main = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        //tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/hartverdrahtet_port_post.fs");
        tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/hartverdrahtet_development_post.fs");
        mLinkedShaderID_Post = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);    
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()});
        //generate framebufferobject
        mFrameBufferTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glBindTexture(GL_TEXTURE_2D, mFrameBufferTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        //allocate the framebuffer object ...
        int[] result = new int[1];
        inGL.glGenFramebuffers(1, result, 0);
        mFrameBufferObjectID = result[0];
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mFrameBufferTextureID, 0);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    protected float getTimeMultiplier(int inSceneNumber) {
        float tMultiplier = -1.0f;
        if (inSceneNumber==0) { tMultiplier = 6.5f;  } //fadein  
        if (inSceneNumber==1) { tMultiplier = 11.0f; } //wires sidescroller
        if (inSceneNumber==2) { tMultiplier = 14.0f; } //wires insidescroller
        if (inSceneNumber==3) { tMultiplier = 14.0f; } //hall of harad
        if (inSceneNumber==4) { tMultiplier = 9.5f; }  //grid side overview
        if (inSceneNumber==5) { tMultiplier = 14.0f; } //spike vertical core
        if (inSceneNumber==6) { tMultiplier = 14.0f; } //spike introduction
        if (inSceneNumber==7) { tMultiplier = 10.0f; } //spike wiredballs
        if (inSceneNumber==8) { tMultiplier = 16.0f; } //alien_backbone
        if (inSceneNumber==9) { tMultiplier = 6.75f;  } //alien reactorcore
        return tMultiplier;
    }
    
    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //use this for offline rendering/capture ...
        int MMTime_u_ms = (int)((((double)inFrameNumber)*44100.0f)/60.0f);
        //int MMTime_u_ms = (int)(BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()*(44100.0f/1000.0f));  
        if (MMTime_u_ms>=mNote60_SyncPoints[mNote60_SyncCounter]  && !mNote60_SyncPoints_Check[mNote60_SyncCounter]) { 
            mNote60_SyncPoints_Check[mNote60_SyncCounter] = true; 
            mSyncEventNumber++;
            mLastEffectSyncTime = mEffectSyncTime;
            mEffectSyncTime = MMTime_u_ms;
            BaseLogging.getInstance().info("NEW SYNC EVENT! tSyncEventNumber="+mSyncEventNumber+" tSyncTime="+MMTime_u_ms);
            mNote60_SyncCounter++;
        }
        if (MMTime_u_ms>=mNote72_SyncPoints[mNote72_SyncCounter]  && !mNote72_SyncPoints_Check[mNote72_SyncCounter]) { 
            mNote72_SyncPoints_Check[mNote72_SyncCounter] = true; 
            mBrightnessSyncTime = MMTime_u_ms;
            BaseLogging.getInstance().info("NEW SYNC EVENT! tSyncEventNumber="+mSyncEventNumber+" tSyncTime="+MMTime_u_ms);
            mNote72_SyncCounter++;
        }
        float tBrightnessSync = 2.5f-((MMTime_u_ms-mBrightnessSyncTime)/15000.0f);
        if (tBrightnessSync<1.0f) {
            tBrightnessSync=1.0f;
        }
        mEffectTime = (float)((MMTime_u_ms-mEffectSyncTime)/100000.0);
        mEffectTime *= getTimeMultiplier(mSyncEventNumber);
        mCurrent_Scene_Timer = mEffectTime;
        mLast_Scene_Timer = (float)((MMTime_u_ms-mLastEffectSyncTime)/100000.0);
        mLast_Scene_Timer *= getTimeMultiplier(mSyncEventNumber-1);
        //calculate current time based on 60fps reference framerate ...
        MMTime_u_ms = (int)((((double)inFrameNumber)*44100.0f)/60.0f);
        float XRESf = BaseGlobalEnvironment.getInstance().getScreenWidth();
        float YRESf = BaseGlobalEnvironment.getInstance().getScreenHeight();
        inGL.glDisable(GL_LIGHTING);
        inGL.glDisable(GL_CULL_FACE);
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, XRESf, YRESf, 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glActiveTexture(GL_TEXTURE0);
        //render fractal to FBO ...
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        inGL.glUseProgram(mLinkedShaderID_Main);
        ShaderUtils.setUniform1i(inGL,mLinkedShaderID_Main,"sn",mSyncEventNumber);
        ShaderUtils.setUniform1f(inGL,mLinkedShaderID_Main,"st",mCurrent_Scene_Timer);
        ShaderUtils.setUniform1f(inGL,mLinkedShaderID_Main,"lt",mLast_Scene_Timer);
        ShaderUtils.setUniform2fv(inGL,mLinkedShaderID_Main,"rs",mScreenDimensionUniform2fv);
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
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);        
        //render FBO to fullscreen quad and apply post-effect shader ...
        inGL.glUseProgram(mLinkedShaderID_Post);
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glBindTexture(GL_TEXTURE_2D, mFrameBufferTextureID);
        ShaderUtils.setUniform1i(inGL,mLinkedShaderID_Post,"s0",0);
        ShaderUtils.setUniform1f(inGL,mLinkedShaderID_Post,"tm",MMTime_u_ms/44100.0f);
        ShaderUtils.setUniform1i(inGL,mLinkedShaderID_Post,"sn",mSyncEventNumber);
        ShaderUtils.setUniform1f(inGL,mLinkedShaderID_Post,"st",mCurrent_Scene_Timer);
        ShaderUtils.setUniform1f(inGL,mLinkedShaderID_Post,"cm",tBrightnessSync);
        ShaderUtils.setUniform2fv(inGL,mLinkedShaderID_Post,"rs",mScreenDimensionUniform2fv);
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
        TextureUtils.deleteTextureID(inGL,mFrameBufferTextureID);
        inGL.glDeleteShader(mLinkedShaderID_Main);
        inGL.glDeleteShader(mLinkedShaderID_Post);
        inGL.glFlush();
    }

}
