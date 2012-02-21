package jogamp.routine.jogl.mixedfunctionpipeline;

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
 ** This routine is more or less expanding on the fractal-bitmap-orbit-trapping into 3D. In the 2D
 ** case shown in GL3_FractalBitmapOrbitTrapping (also used in my port of Elektronenmultiplizierer)
 ** all orbit-trap iterations are composited into a single pixel (sort of flatened). This routine
 ** instead renders every orbit-trap-iteration as a different layer of volume-slice stack simliar to
 ** GL2_VolumeSlicesRenderer, thereby creating some sort of 'fake 3D impression' while the IFS results
 ** spiral down to zero. So its more or less an experimental combination of GL3_FractalBitmapOrbitTrapping
 ** and GL2_VolumeSlicesRenderer to create 3D-fractal-bitmap-orbit-traps ...
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=XXXXXXXXXXX
 **
 **/

import java.nio.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL2GL3_VolumeSlicedFractalBitmapOrbitTrapping extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    protected int mVolumeSlicesWidth = 1280;
    protected int mVolumeSlicesHeight = 720;
    protected int mDisplayListStartID;
    protected int mDisplayListSize;
    protected float mSliceWidth;
    protected float mSliceHeight;
    protected int mNumberOfSlices = 150;
    protected BaseFrameBufferObjectRendererExecutor[] mBaseFrameBufferObjectRendererExecutor;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mSliceWidth = mVolumeSlicesWidth/10.0f;
        mSliceHeight = mVolumeSlicesHeight/10.0f;
        mDisplayListSize = 1;
        mDisplayListStartID = inGL.glGenLists(mDisplayListSize);
        float tSliceHeight = 1.0f/8.0f;
        inGL.glNewList(mDisplayListStartID+0,GL_COMPILE);
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex2f(0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex2f(mSliceWidth, 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex2f(mSliceWidth, mSliceHeight);
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex2f(0.0f, mSliceHeight);
            inGL.glEnd(); 
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex3f(0.0f, 0.0f,tSliceHeight);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex3f(mSliceWidth, 0.0f,tSliceHeight);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex3f(mSliceWidth, mSliceHeight,0.0f);
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex3f(0.0f, mSliceHeight,0.0f);
            inGL.glEnd(); 
            inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex3f(0.0f, 0.0f, tSliceHeight);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex3f(mSliceWidth, 0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex3f(mSliceWidth, mSliceHeight,0.0f);
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex3f(0.0f, mSliceHeight,tSliceHeight);
            inGL.glEnd(); 
        inGL.glEndList();
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor[mNumberOfSlices];
        for (int i=0; i<mNumberOfSlices; i++) {
            mBaseFrameBufferObjectRendererExecutor[i] = new BaseFrameBufferObjectRendererExecutor(mVolumeSlicesWidth,mVolumeSlicesHeight,this);
            mBaseFrameBufferObjectRendererExecutor[i].init(inGL,inGLU,inGLUT);
        }
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        for (int i=0; i<mNumberOfSlices; i++) {
            mBaseFrameBufferObjectRendererExecutor[i].renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
            this.setIterationNumber(i);
        }
        //adjust frustum for "more depth" than the default settings ...
        inGL.glViewport(0, 0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        double tAspectRatio = (double)BaseGlobalEnvironment.getInstance().getScreenWidth()/(double)BaseGlobalEnvironment.getInstance().getScreenHeight();
        inGLU.gluPerspective(45.0, tAspectRatio, 0.01, 400.0);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(
                0f, 0f, 70f,
                0f, 0f, 0f,
                0.0f, 1.0f, 0.0f
        );
        TextureUtils.preferAnisotropicFilteringOnTextureTarget(inGL,GL_TEXTURE_2D);
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glDisable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glDisable(GL_CULL_FACE);
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glEnable(GL_BLEND);
        inGL.glBlendFunc(GL_ONE, GL_ONE);
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glTexEnvi(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_MODULATE);
        float tAngle = inFrameNumber;
        int tTransformSettingsNumber = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_QW())%5;
        tTransformSettingsNumber = 2;
        if(tTransformSettingsNumber==0) {
            inGL.glTranslatef(0, 0, -32.0f);
            inGL.glRotatef(180.0f+(float)Math.cos(inFrameNumber*0.025f)*40, 0f, 1f, 0f);
            inGL.glRotatef((float)Math.sin(inFrameNumber*0.025f)*40, 1f, 0f, 0f);
            inGL.glRotatef(180.0f, 0f, 0f, 1f);
        } else if(tTransformSettingsNumber==1) {
            inGL.glTranslatef(0, 0, -32.0f);
            inGL.glRotatef(tAngle*0.25f, 1f, 0f, 0f);
            inGL.glRotatef(180.0f, 0f, 0f, 1f);
        } else if(tTransformSettingsNumber==2) {
            inGL.glTranslatef(0, 0, -32.0f);
            inGL.glRotatef(tAngle*0.25f, 0f, 1f, 0f);
            inGL.glRotatef(180.0f, 0f, 0f, 1f);
        } else if(tTransformSettingsNumber==3) {
            inGL.glTranslatef(0, 0, -32.0f);
            inGL.glRotatef(tAngle*0.25f, 0f, 1f, 0f);
            inGL.glRotatef(tAngle*0.25f, 1f, 0f, 0f);
            inGL.glRotatef(180.0f, 0f, 0f, 1f);
        } else if(tTransformSettingsNumber==4) {
            inGL.glTranslatef(0, 0, -32.0f);
            inGL.glRotatef(tAngle*0.15f, 0f, 0f, 1f);
            inGL.glRotatef(tAngle*0.25f, 0f, 1f, 0f);
            inGL.glRotatef(tAngle*0.35f, 1f, 0f, 0f);
        } 
        float tAlpha = (1.0f/(float)mNumberOfSlices)*15.0f;
        inGL.glColor4f(tAlpha, tAlpha, tAlpha, tAlpha);
        inGL.glPushMatrix();
            inGL.glTranslatef(-(mSliceWidth/2.0f), -(mSliceHeight/2.0f), -(mNumberOfSlices/16.0f));
            for (int i=0; i<mNumberOfSlices*2; i++) {
                inGL.glBindTexture(GL_TEXTURE_2D, mBaseFrameBufferObjectRendererExecutor[i/2].getColorTextureID());
                inGL.glPushMatrix();
                    inGL.glTranslatef(0, 0, (float)i/8.0f);
                    inGL.glCallList(mDisplayListStartID+0);
                inGL.glPopMatrix();
            }
        inGL.glPopMatrix();
        inGL.glDisable(GL_BLEND);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
        for (int i=0; i<mNumberOfSlices; i++) {
            mBaseFrameBufferObjectRendererExecutor[i].cleanup(inGL,inGLU,inGLUT);
        }
        inGL.glFlush();
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected int mLinkedShader;
    protected FloatBuffer mScreenDimensionUniform2fv;
    protected Texture mBitmapOrbitTrap[];
    protected boolean mAlreadyInitialized = false;

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        if (mAlreadyInitialized) {
            return;
        }
        mAlreadyInitialized = true;
        String tFragmentShaderString = ShaderUtils.loadShaderSourceFileAsString("/shaders/fractalshaders/fractalbitmaporbittrapping.fs");
        //tFragmentShaderString = "#define supersamplingfactor 0.25"+"\n"+tFragmentShaderString;
        tFragmentShaderString = "#define singleiteration"+"\n"+tFragmentShaderString;
        int tFragmentShader = ShaderUtils.generateFragmentShader(inGL,tFragmentShaderString);
        mLinkedShader = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)mBaseFrameBufferObjectRendererExecutor[0].getWidth(), (float)mBaseFrameBufferObjectRendererExecutor[0].getHeight()});
        mBitmapOrbitTrap = new Texture[39];
        mBitmapOrbitTrap[0] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_001.png");
        mBitmapOrbitTrap[1] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_002.png");
        mBitmapOrbitTrap[2] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_003.png");
        mBitmapOrbitTrap[3] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_004.png");
        mBitmapOrbitTrap[4] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_005.png");
        mBitmapOrbitTrap[5] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_006.png");
        mBitmapOrbitTrap[6] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_007.png");
        mBitmapOrbitTrap[7] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_008.png");
        mBitmapOrbitTrap[8] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_009.png");
        mBitmapOrbitTrap[9] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_010.png");
        mBitmapOrbitTrap[10] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_011.png");
        mBitmapOrbitTrap[11] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_012.png");
        mBitmapOrbitTrap[12] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_013.png");
        mBitmapOrbitTrap[13] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_014.png");
        mBitmapOrbitTrap[14] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_015.png");
        mBitmapOrbitTrap[15] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_016.png");
        mBitmapOrbitTrap[16] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_017.png");
        mBitmapOrbitTrap[17] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_018.png");
        mBitmapOrbitTrap[18] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_019.png");
        mBitmapOrbitTrap[19] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_020.png");
        mBitmapOrbitTrap[20] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_021.png");
        mBitmapOrbitTrap[21] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_022.png");
        mBitmapOrbitTrap[22] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_023.png");
        mBitmapOrbitTrap[23] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_024.png");
        mBitmapOrbitTrap[24] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_025.png");
        mBitmapOrbitTrap[25] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_026.png");
        mBitmapOrbitTrap[26] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_027.png");
        mBitmapOrbitTrap[27] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_028.png");
        mBitmapOrbitTrap[28] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_029.png");
        mBitmapOrbitTrap[29] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_030.png");
        mBitmapOrbitTrap[30] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_031.png");
        mBitmapOrbitTrap[31] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_032.png");
        mBitmapOrbitTrap[32] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_033.png");
        mBitmapOrbitTrap[33] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_034.png");
        mBitmapOrbitTrap[34] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_035.png");
        mBitmapOrbitTrap[35] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_036.png");
        mBitmapOrbitTrap[36] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_037.png");
        mBitmapOrbitTrap[37] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_038.png");
        mBitmapOrbitTrap[38] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_039.png");
        for (int i=0; i<mBitmapOrbitTrap.length; i++) {
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
            //mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_REPEAT);
            //mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_REPEAT);
        }
    }

    protected int mIterationNumber;
    
    public void setIterationNumber(int inIterationNumber) {
        mIterationNumber = inIterationNumber;
    }

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDisable(GL_LIGHTING);
        inGL.glDisable(GL_CULL_FACE);
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, mBaseFrameBufferObjectRendererExecutor[0].getWidth(), mBaseFrameBufferObjectRendererExecutor[0].getHeight(), 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glUseProgram(mLinkedShader);
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"time",inFrameNumber/100.0f);
        ShaderUtils.setUniform2fv(inGL,mLinkedShader,"resolution",mScreenDimensionUniform2fv);
        int zoominvalue = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_34())%2;
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"zoomin",zoominvalue);
        if (zoominvalue==0) {
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",40+BaseGlobalEnvironment.getInstance().getParameterKey_INT_90());
        } else {
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",(256+BaseGlobalEnvironment.getInstance().getParameterKey_INT_90()%256));
        }
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",mIterationNumber);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliamode",Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_56())%2);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliapower",2+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_78()));
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"orbittrapscale",3.5f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS());
        //manually override keyboard settting for offline rendering ...
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliamode",1);
        //ShaderUtils.setUniform1i(inGL,mLinkedShader,"zoomin",1);
        //---
        inGL.glActiveTexture(GL_TEXTURE0);
        int tCurrentTexture = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()%mBitmapOrbitTrap.length);
        tCurrentTexture = 34;
        
        mBitmapOrbitTrap[tCurrentTexture].enable(inGL);
        mBitmapOrbitTrap[tCurrentTexture].bind(inGL);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"texture",0);
        inGL.glValidateProgram(mLinkedShader);
        inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor[0].getWidth(), 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(mBaseFrameBufferObjectRendererExecutor[0].getWidth(), mBaseFrameBufferObjectRendererExecutor[0].getHeight());
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, mBaseFrameBufferObjectRendererExecutor[0].getHeight());
        inGL.glEnd();
        inGL.glUseProgram(0);
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        inGL.glDisable(GL_TEXTURE_2D);
        inGL.glActiveTexture(GL_TEXTURE0);
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mLinkedShader);
        inGL.glFlush();
    }

}

