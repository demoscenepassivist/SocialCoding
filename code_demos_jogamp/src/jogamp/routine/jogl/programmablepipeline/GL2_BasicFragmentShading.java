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
 ** Simple GL2-Profile demonstration using GLUT, display-lists and mutliple fragment shaders
 ** rendering directly to a framebuffer-object (known as render-2-texture). Also uses ShaderUtils
 ** to ease the use of fragment shaders (especially with uniforms). The FBO usage is encapsulated 
 ** here by the BaseFrameBufferObjectRendererInterface/Excecutor wich handles all the overhead
 ** implied by the FBO usage. For an impression how this routine looks like see here:
 ** http://www.youtube.com/watch?v=_N25FiDnUFY
 **
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL2_BasicFragmentShading extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    private int mLinkedShader;
    private int[] mFragmentShaders;
    private int[] mLinkedShaders;
    private int mLinkedShaderIndex;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        mFragmentShaders = new int[5];
        mFragmentShaders[0] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/rasterizationshaders/noop.fs");
        mFragmentShaders[1] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/rasterizationshaders/grayscale.fs");
        mFragmentShaders[2] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/rasterizationshaders/grayinvert.fs");
        mFragmentShaders[3] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/rasterizationshaders/colorinvert.fs");
        mFragmentShaders[4] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/rasterizationshaders/sepia.fs");
        mLinkedShaders = new int[mFragmentShaders.length];
        for (int i=0; i<mFragmentShaders.length; i++) {
            mLinkedShaders[i] = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,mFragmentShaders[i]);
        }
        mLinkedShader = mLinkedShaders[0];
        mLinkedShaderIndex = 0;
        //setup lighting ...
        inGL.glLightfv(GL_LIGHT0, GL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_POSITION, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 3.0f, 3.0f, 0.0f}));
        inGL.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.2f, 0.2f, 0.2f, 1.0f}));
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        if (inFrameNumber%100==0) {
            mLinkedShaderIndex++;
            mLinkedShader = mLinkedShaders[mLinkedShaderIndex%mLinkedShaders.length];
        }
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
        for (int i=0; i<mFragmentShaders.length; i++) {
            inGL.glDeleteShader(mFragmentShaders[i]);
        }
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    private int mDisplayListStartID;
    private int mDisplayListSize;

    private final float[][] mRainbowColors = {
            {1.0f,0.5f,0.5f},
            {1.0f,0.75f,0.5f},
            {1.0f,1.0f,0.5f},
            {0.75f,1.0f,0.5f},
            {0.5f,1.0f,0.5f},
            {0.5f,1.0f,0.75f},
            {0.5f,1.0f,1.0f},
            {0.5f,0.75f,1.0f},
            {0.5f,0.5f,1.0f},
            {0.75f,0.5f,1.0f},
            {1.0f,0.5f,1.0f},
            {1.0f,0.5f,0.75f}
    };

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mDisplayListSize = 1;
        mDisplayListStartID = inGL.glGenLists(mDisplayListSize);
        inGL.glNewList(mDisplayListStartID,GL_COMPILE);
            inGLUT.glutSolidTorus(0.3, 0.5, 61, 37);
        inGL.glEndList();
    }

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glCullFace(GL_BACK);
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_LIGHTING);
        inGL.glEnable(GL_LIGHT0);
        inGL.glEnable(GL_AUTO_NORMAL);
        inGL.glEnable(GL_NORMALIZE);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        inGL.glTranslatef(0.0f,0.0f,70.0f);	
        float tX = -0.65f;
        float tY =  0.0f;
        float tZ = -2.0f;
        inGL.glValidateProgram(mLinkedShader);
        inGL.glUseProgram(mLinkedShader);
        for (int j=0; j<12; j+=2) {
            renderTorus(
                inFrameNumber+(j*10),inGL, 
                tX, tY, tZ, 
                mRainbowColors[j][0],mRainbowColors[j][1],mRainbowColors[j][2],
                mRainbowColors[j+1][0],mRainbowColors[j+1][1],mRainbowColors[j+1][2],
                1.0f, 1.0f, 1.0f, 
                0.6f
            );
            tX+=1.0f;
            tZ-=1.0f;
        }
        inGL.glUseProgram(0);
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
        inGL.glFlush();
    }

    private void renderTorus(
            int inFrameNumber,GL2 inGL, 
            float inX, float inY, float inZ, 
            float inAmbientRed, float inAmbientGreen, float inAmbientBlue, 
            float inDiffuseRed, float inDiffuseGreen, float inDiffuseBlue,
            float inSpecularRed, float inSpecularGreen, float inSpecularBlue, 
            float inShine
    ) {
        float tMaterialColor[] = new float[4];
        inGL.glPushMatrix();
            inGL.glTranslatef(inX, inY, inZ);
            tMaterialColor[0] = inAmbientRed;
            tMaterialColor[1] = inAmbientGreen;
            tMaterialColor[2] = inAmbientBlue;
            tMaterialColor[3] = 1.0f;
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT, tMaterialColor, 0);
            tMaterialColor[0] = inDiffuseRed;
            tMaterialColor[1] = inDiffuseGreen;
            tMaterialColor[2] = inDiffuseBlue;
            inGL.glMaterialfv(GL_FRONT, GL_DIFFUSE, tMaterialColor, 0);
            tMaterialColor[0] = inSpecularRed;
            tMaterialColor[1] = inSpecularGreen;
            tMaterialColor[2] = inSpecularBlue;
            inGL.glMaterialfv(GL_FRONT, GL_SPECULAR, tMaterialColor, 0);
            inGL.glMaterialf(GL_FRONT, GL_SHININESS, inShine * 128.0f);
            inGL.glRotatef(inFrameNumber%360, 1.0f, 0.5f, 0.0f);
            inGL.glCallList(mDisplayListStartID);
        inGL.glPopMatrix();
    }

}
