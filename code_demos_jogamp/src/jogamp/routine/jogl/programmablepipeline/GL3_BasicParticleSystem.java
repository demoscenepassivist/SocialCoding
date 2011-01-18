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
 ** Simple particle system routine based on point based rendering. Uses GLUT, display-lists
 ** and a vertex shader implementing a stateless particle system. Also uses ShaderUtils to
 ** ease the use of vertex shaders (especially with uniforms). For an impression how this
 ** routine looks like see here: http://www.youtube.com/watch?v=CTf6e3T93o4
 **
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_BasicParticleSystem extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mDisplayListID;
    private int mLinkedShader;
    private int mVertexShader;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mDisplayListID = WavefrontObjectLoader_DisplayList.loadWavefrontObjectAsDisplayList(inGL,"/binaries/geometry/PointBasedRendering_Low.wobj.zip");
        mVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/basicparticlesystem.vs");
        mLinkedShader = ShaderUtils.generateSimple_1xVS_ShaderProgramm(inGL,mVertexShader);
        inGL.glValidateProgram(mLinkedShader);
        inGL.glUseProgram(mLinkedShader);
            ShaderUtils.setUniform3fv(inGL,mLinkedShader,"lightPos[0]",DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f, 3.0f, 3.0f,0.0f}));
            ShaderUtils.setUniform3fv(inGL,mLinkedShader,"lightPos[1]",DirectBufferUtils.createDirectFloatBuffer(new float[]{3.0f, 3.0f, 0.0f,0.0f}));
            ShaderUtils.setUniform3fv(inGL,mLinkedShader,"lightPos[2]",DirectBufferUtils.createDirectFloatBuffer(new float[]{0.0f,-3.0f,-3.0f,0.0f}));
            ShaderUtils.setUniform4fv(inGL,mLinkedShader,"lightCol[0]",DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f, 0.25f, 0.25f, 1.0f}));
            ShaderUtils.setUniform4fv(inGL,mLinkedShader,"lightCol[1]",DirectBufferUtils.createDirectFloatBuffer(new float[]{0.25f, 1.0f, 0.25f, 1.0f}));
            ShaderUtils.setUniform4fv(inGL,mLinkedShader,"lightCol[2]",DirectBufferUtils.createDirectFloatBuffer(new float[]{0.25f, 0.25f, 1.0f, 1.0f}));
        inGL.glUseProgram(0);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glPushAttrib(GL_ALL_ATTRIB_BITS);
            inGL.glEnable(GL_DEPTH_TEST);
            inGL.glEnable(GL_CULL_FACE);
            inGL.glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
            inGL.glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
            inGL.glUseProgram(mLinkedShader);
            ShaderUtils.setUniform1f(inGL,mLinkedShader,"time",inFrameNumber/1000.0f);
            inGL.glValidateProgram(mLinkedShader);
            inGL.glTranslatef(0.0f,0.0f,-1.25f);
            inGL.glPushMatrix();
                //inGL.glRotatef((inFrameNumber/3.0f)%360.0f, 0.5f, 1.0f, 0.5f);
                inGL.glRotatef((inFrameNumber/5.0f)%360.0f, 0.0f, 1.0f, 0.0f);
                inGL.glCallList(mDisplayListID);
            inGL.glPopMatrix();
            inGL.glUseProgram(0);
        inGL.glPopAttrib();
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteLists(mDisplayListID,1);
    }

}
