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
 ** Simple routine using different plane deformation fragment shaders to render a distorted 
 ** texture to a fullscreen billboard. Most fragment shader code is from this site: 
 ** http://www.iquilezles.org/www/articles/deform/deform.htm - For an impression how this
 ** routine looks like see here: http://www.youtube.com/watch?v=9ZpKXjPW8tw
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

public class GL3_PlaneDeformation extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mLinkedShader;
    private int[] mFragmentShaders;
    private int[] mLinkedShaders;
    private Texture mTexture;
    private static final int OFFSETSINTABLE_SIZE = 2048;
    private float[] mOffsetSinTable;
    private FloatBuffer mScreenDimensionUniform2fv;
    private FloatBuffer mPositionUniform3fv;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mFragmentShaders = new int[12];
        mFragmentShaders[0] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/squaretunnel.fs");
        mFragmentShaders[1] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/bumptunnel.fs");
        mFragmentShaders[2] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/radialblur.fs");
        mFragmentShaders[3] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/water.fs");
        mFragmentShaders[4] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/infiniteplanes.fs");
        mFragmentShaders[5] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/tunnel.fs");
        mFragmentShaders[6] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/zinvert.fs");
        mFragmentShaders[7] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/kaleidoscope.fs");
        mFragmentShaders[8] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/deform.fs");
        mFragmentShaders[9] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/star.fs");
        mFragmentShaders[10] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/twist.fs");
        mFragmentShaders[11] = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/planedeformationshaders/backtothe90s.fs");
        mLinkedShaders = new int[mFragmentShaders.length];
        for (int i=0; i<mFragmentShaders.length; i++) {
            mLinkedShaders[i] = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,mFragmentShaders[i]);
        }
        mLinkedShader = mLinkedShaders[0];
        mTexture = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PlaneDeformation_Seamless.jpg");
        mOffsetSinTable = OffsetTableUtils.cosaque_SinglePrecision(OFFSETSINTABLE_SIZE,1280,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()});
        mPositionUniform3fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {0.0f, 0.0f, 0.0f, 0.0f });
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mLinkedShader = mLinkedShaders[Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()%mLinkedShaders.length)];
        //display texture billboard
        inGL.glDisable(GL_LIGHTING);
        inGL.glDisable(GL_CULL_FACE);
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glBindTexture(GL_TEXTURE_2D, mTexture.getTextureObject(inGL));
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glTexEnvi(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_MODULATE);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
        inGL.glUseProgram(mLinkedShader);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",0);
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"time",inFrameNumber/50.0f); 
        //suboptimal approach ... but who cares 8:]
        DirectBufferUtils.updateDirectFloatBuffer(mPositionUniform3fv,new float[] {mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE], mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE], 0.0f, 0.0f });
        ShaderUtils.setUniform3fv(inGL,mLinkedShader,"position", mPositionUniform3fv);
        ShaderUtils.setUniform2fv(inGL,mLinkedShader,"resolution",mScreenDimensionUniform2fv);
        inGL.glValidateProgram(mLinkedShader);
        inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight());
        inGL.glEnd();
        inGL.glUseProgram(0);
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        inGL.glDisable(GL_TEXTURE_2D);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        for (int i=0; i<mFragmentShaders.length; i++) {
            inGL.glDeleteShader(mFragmentShaders[i]);
        }
    }

}
