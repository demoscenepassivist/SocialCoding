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
 ** Routine simulating refraction (and reflection) using vertex and fragment shader. Also takes
 ** fresnels term into account to allow a view dependant blend of reflection/refraction. For an
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=XSeMdl9WpFw
 ** 
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL3_Refraction extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mLinkedShader;
    private int mVertexShader;
    private int mFragmentShader;	
    private int mDisplayListID;
    private static final int OFFSETSINTABLE_SIZE = 2700;
    private float[] mOffsetSinTable;
    private Texture mTexture;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mOffsetSinTable = OffsetTableUtils.cosaque_SinglePrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        mDisplayListID = WavefrontObjectLoader_DisplayList.loadWavefrontObjectAsDisplayList(inGL,"/binaries/geometry/Refraction.wobj.zip");
        mTexture = TextureUtils.loadImageAsTexture_FLIPPED(inGL,"/binaries/textures/Wallpaper_JOGAMP_PubNeon_02_1920pel.png");
        mVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/refraction.vs");
        mFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/refraction.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,mVertexShader,mFragmentShader);
        inGL.glActiveTexture(GL_TEXTURE0);
        mTexture.enable(inGL);
        mTexture.bind(inGL);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //render background image ...
        inGL.glDisable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glDisable(GL_CULL_FACE);
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glBegin(GL_QUADS);
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight());
        inGL.glEnd();
        //render glass object ...
        inGL.glPushAttrib(GL_ALL_ATTRIB_BITS);
            BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT);
            inGL.glEnable(GL_CULL_FACE);
            inGL.glEnable(GL_DEPTH_TEST);
            inGL.glUseProgram(mLinkedShader); 
            ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShader,"sampler0",mTexture,GL_TEXTURE0,0,true);
            inGL.glValidateProgram(mLinkedShader);
            inGL.glPushMatrix();
                inGL.glTranslatef(0.0f,0.0f,62.0f);
                inGL.glRotatef(mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE], 0.25f, 1.0f, 0.5f);
                inGL.glRotatef(mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE], 0.75f, 0.3f, 0.1f);
                inGL.glCallList(mDisplayListID);
            inGL.glPopMatrix();
            inGL.glUseProgram(0);
        inGL.glPopAttrib();
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mVertexShader);
        inGL.glDeleteShader(mFragmentShader);
        inGL.glDeleteLists(mDisplayListID,1);
        inGL.glFlush();	
    }

}
