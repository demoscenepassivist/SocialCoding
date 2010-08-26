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
 ** Simple GL2-Profile demonstration using GLUT, display-lists and mutliple vertex shaders.
 ** Also uses ShaderUtils to ease the use of vertex shaders (especially with uniforms). For an
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=VovI20JntjQ
 **
 **/

import framework.base.*;
import framework.util.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL2_BasicVertexShading extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mLinkedShader;
    private int[] mVertexShaders;
    private int[] mLinkedShaders;
    private int mLinkedShaderIndex;
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

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mVertexShaders = new int[4];
        mVertexShaders[0] = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/basiclightingshaders/simple.vs");
        mVertexShaders[1] = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/basiclightingshaders/diffuse.vs");
        mVertexShaders[2] = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/basiclightingshaders/specular.vs");
        mVertexShaders[3] = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/basiclightingshaders/3lights.vs");
        mLinkedShaders = new int[mVertexShaders.length];
        for (int i=0; i<mVertexShaders.length; i++) {
            mLinkedShaders[i] = ShaderUtils.generateSimple_1xVS_ShaderProgramm(inGL,mVertexShaders[i]);
        }
        //create lightsource position/color uniforms ...
        FloatBuffer tLightPos0 = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 3.0f, 3.0f,0.0f});
        FloatBuffer tLightPos1 = DirectBufferUtils.createDirectFloatBuffer(new float[]{3.0f, 3.0f, 0.0f,0.0f});
        FloatBuffer tLightPos2 = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f,-3.0f,-3.0f,0.0f});
        FloatBuffer tLightCol0 = DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f, 0.25f, 0.25f, 1.0f});
        FloatBuffer tLightCol1 = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.25f, 1.0f, 0.25f, 1.0f});
        FloatBuffer tLightCol2 = DirectBufferUtils.createDirectFloatBuffer(new float[]{0.25f, 0.25f, 1.0f, 1.0f});
        //setup diffuse.vs ...
        inGL.glValidateProgram(mLinkedShaders[1]);
        inGL.glUseProgram(mLinkedShaders[1]);
        ShaderUtils.setUniform3fv(inGL,mLinkedShaders[1],"lightPos[0]",tLightPos0);
        inGL.glUseProgram(0);
        //setup specular.vs ...
        inGL.glValidateProgram(mLinkedShaders[2]);
        inGL.glUseProgram(mLinkedShaders[2]);
        ShaderUtils.setUniform3fv(inGL,mLinkedShaders[2],"lightPos[0]",tLightPos0);
        inGL.glUseProgram(0);
        //setup 3lights.vs ...
        inGL.glValidateProgram(mLinkedShaders[3]);
        inGL.glUseProgram(mLinkedShaders[3]);
        ShaderUtils.setUniform3fv(inGL,mLinkedShaders[3],"lightPos[0]",tLightPos0);
        ShaderUtils.setUniform3fv(inGL,mLinkedShaders[3],"lightPos[1]",tLightPos1);
        ShaderUtils.setUniform3fv(inGL,mLinkedShaders[3],"lightPos[2]",tLightPos2);
        ShaderUtils.setUniform4fv(inGL,mLinkedShaders[3],"lightCol[0]",tLightCol0);
        ShaderUtils.setUniform4fv(inGL,mLinkedShaders[3],"lightCol[1]",tLightCol1);
        ShaderUtils.setUniform4fv(inGL,mLinkedShaders[3],"lightCol[2]",tLightCol2);
        inGL.glUseProgram(0);
        mLinkedShader = mLinkedShaders[0];
        mDisplayListSize = 1;
        mDisplayListStartID = inGL.glGenLists(mDisplayListSize);
        inGL.glNewList(mDisplayListStartID,GL_COMPILE);
            inGLUT.glutSolidTorus(0.3, 0.5, 61, 37);
        inGL.glEndList();
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        if (inFrameNumber%100==0) {
            mLinkedShaderIndex++;
            mLinkedShader = mLinkedShaders[mLinkedShaderIndex%mLinkedShaders.length];
        }
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glTranslatef(0.0f,0.0f,70.0f);	
        float tX = -0.65f;
        float tY =  0.0f;
        float tZ = -2.0f;
        inGL.glUseProgram(mLinkedShader);
        for (int j=0; j<12; j+=2) {
            inGL.glPushMatrix();
                inGL.glTranslatef(tX, tY, tZ);
                inGL.glColor3f(mRainbowColors[j+1][0],mRainbowColors[j+1][1],mRainbowColors[j+1][2]);
                inGL.glRotatef((inFrameNumber+(j*10))%360, 1.0f, 0.5f, 0.0f);
                inGL.glCallList(mDisplayListStartID);
            inGL.glPopMatrix();
            tX+=1.0f;
            tZ-=1.0f;
        }
        inGL.glUseProgram(0);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        for (int i=0; i<mVertexShaders.length; i++) {
            inGL.glDeleteShader(mVertexShaders[i]);
        }
        inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
        inGL.glFlush();	
    }

}

