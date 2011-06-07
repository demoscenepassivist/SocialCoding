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

import java.nio.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_Elektronenmultiplizierer_Port extends BaseRoutineAdapter implements BaseRoutineInterface {

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
        //int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/elektronenmultiplizierer_port.fs");
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/raymarchingshaders/elektronenmultiplizierer_development.fs");
        mLinkedShaderID = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()});
        //generate framebufferobject
        mFrameBufferTextureID = TextureUtils.generateTextureID(inGL);
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
        BaseLogging.getInstance().info("NEW SYNC EVENT! tSyncEventNumber="+mSyncEventNumber+" tSyncTime="+mSyncTime);
        if (mSyncEventNumber==0 || mSyncEventNumber==2 || mSyncEventNumber==5 || mSyncEventNumber==8) {
            mEffectSyncTime = inMMTime_u_ms;
        }
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //use this for offline rendering/capture ...
        //int MMTime_u_ms = (int)((((double)inFrameNumber)*44100.0f)/60.0f);
        int MMTime_u_ms = (int)(BaseRoutineRuntime.getInstance().getBaseMusic().getPositionInMilliseconds()*(44100.0f/1000.0f));
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
             ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"en",2);
         }
         if(mSyncEventNumber==4) {
             ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"en",7);
         }
         ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"fb",0);
         ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"tm",MMTime_u_ms/40000.0f);
         ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"br",tBrightnessSync);
         ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"et",9.1f);
         ShaderUtils.setUniform2fv(inGL,mLinkedShaderID,"resolution",mScreenDimensionUniform2fv);
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
         ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"en",mEffectNumber);
         ShaderUtils.setUniform1f(inGL,mLinkedShaderID,"et",mEffectTime);
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
        TextureUtils.deleteTextureID(inGL,mFrameBufferTextureID);
        inGL.glDeleteShader(mLinkedShaderID);
        inGL.glFlush();
    }

}
