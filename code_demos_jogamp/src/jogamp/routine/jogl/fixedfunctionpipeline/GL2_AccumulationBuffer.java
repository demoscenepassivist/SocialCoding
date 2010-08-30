package jogamp.routine.jogl.fixedfunctionpipeline;

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
 ** Simple GL2-Profile routine demonstrating accumulation buffer functionality (kinda tries to
 ** imitate simple motion blur). Also uses GLUT, display-lists, basic materials and lighting. For an 
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=3Mi5bFdc7Tk
 **
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL2_AccumulationBuffer extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mDisplayListID;
    private int[] mOffsetSinTable;
    private static final int OFFSETSINTABLE_SIZE = 300;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mOffsetSinTable = OffsetTableUtils.cosaque_IntegerPrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        mDisplayListID = inGL.glGenLists(1);
        inGL.glNewList(mDisplayListID,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,0.0f,0.0f,1.0f}));
            inGLUT.glutSolidTorus(0.125, 0.35, 61, 37);
        inGL.glEndList();
        //setup lighting, materials, shading and culling ...
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glEnable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.1f,0.1f,0.1f,1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.25f,0.25f,0.25f,1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,1.0f,1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_POSITION, DirectBufferUtils.createDirectFloatBuffer(new float[]{-100.0f,100.0f,50.0f,1.0f}));
        inGL.glEnable(GL_LIGHT0);
        inGL.glMaterialfv(GL_FRONT, GL_SPECULAR, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,1.0f,1.0f}));
        inGL.glMateriali(GL_FRONT, GL_SHININESS, 12);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glTranslatef(0.0f,0.0f,-1.5f);
        //motionblur passes to be accumulated ...
        int tPasses = 20;
        for(int i = 0; i < tPasses; i++) {
            DrawGeometry(mOffsetSinTable[(inFrameNumber+i)%OFFSETSINTABLE_SIZE],inGL,inGLU,inGLUT);
            //accumulate to back buffer ...
            if(i==0) {
                inGL.glAccum(GL_LOAD,0.5f);
            } else {
                inGL.glAccum(GL_ACCUM,0.5f*(1.0f/tPasses));
            }
        }
        //copy accumulation buffer to color buffer ...
        inGL.glAccum(GL_RETURN, 1.0f);
    }

    void DrawGeometry(float inRotation,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        inGL.glPushMatrix();
            inGL.glRotatef(inRotation, 0.25f, 1.0f, 0.5f);
            inGL.glCallList(mDisplayListID);
        inGL.glPopMatrix();
    }

    public void cleanupRoutineJOGL(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteLists(mDisplayListID,1);
        inGL.glFlush();
    }

}
