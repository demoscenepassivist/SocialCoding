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
 ** Simple GL2-Profile demonstration using GLUT, multiple display-lists, basic materials and lighting.
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=BpqXcQuRqXY
 **
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL2_DisplayLists extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mDisplayListStartID;
    private int mDisplayListSize;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //initialize the display lists ...
        mDisplayListSize = 9;
        mDisplayListStartID = inGL.glGenLists(mDisplayListSize);
        inGL.glNewList(mDisplayListStartID+0,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,0.5f,0.5f}));
            inGLUT.glutSolidSphere(1.0f, 16, 16);
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+1,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,0.75f,0.5f}));
            inGLUT.glutSolidCube(1.0f);
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+2,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,0.5f}));
            inGLUT.glutSolidOctahedron();
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+3,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.75f,1.0f,0.5f}));
            inGL.glDisable(GL_CULL_FACE);
            inGLUT.glutSolidCone(1.0f,1.5f,16,16);
            inGL.glEnable(GL_CULL_FACE);
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+4,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,1.0f,0.5f}));
            inGL.glDisable(GL_CULL_FACE);
            inGLUT.glutSolidCylinder(1.0f,1.0f,16,16);
            inGL.glEnable(GL_CULL_FACE);
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+5,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,1.0f,0.75f}));
            inGLUT.glutSolidTorus(0.5f,1.0f,16,16);
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+6,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,1.0f,1.0f}));
            inGLUT.glutSolidIcosahedron();
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+7,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,0.75f,1.0f}));
            inGLUT.glutSolidTetrahedron();
        inGL.glEndList();
        inGL.glNewList(mDisplayListStartID+8,GL_COMPILE);
            inGL.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.5f,0.5f,1.0f}));
            inGLUT.glutSolidRhombicDodecahedron();
        inGL.glEndList();
        //setup lighting, materials, shading and culling ...
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glEnable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.1f,0.1f,0.1f,1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_AMBIENT, DirectBufferUtils.createDirectFloatBuffer(new float[]{0.3f,0.3f,0.3f,1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_DIFFUSE, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,1.0f,1.0f}));
        inGL.glLightfv(GL_LIGHT0, GL_POSITION, DirectBufferUtils.createDirectFloatBuffer(new float[]{-50.0f,50.0f,100.0f,1.0f}));
        inGL.glEnable(GL_LIGHT0);
        inGL.glMaterialfv(GL_FRONT, GL_SPECULAR, DirectBufferUtils.createDirectFloatBuffer(new float[]{1.0f,1.0f,1.0f,1.0f}));
        inGL.glMateriali(GL_FRONT, GL_SHININESS, 64);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //could ofcourse be compiled into one big display list for better performance ... \|=//
        int tYLength = 8;
        int tXLength = 8;
        int tZLength = 8;
        int tDisplayListIDCounter = mDisplayListStartID;
        float tSpacing = 2.50f;
        for (int z=-7; z<tZLength; z++) {
            for (int y=-7; y<tYLength; y++) {
                for (int x=-7; x<tXLength; x++) {
                    inGL.glPushMatrix();
                        inGL.glRotatef((inFrameNumber/2.0f)%360,1.0f,1.0f,1.0f);
                        inGL.glTranslatef(x*tSpacing,y*tSpacing,z*tSpacing);
                        if (tDisplayListIDCounter>(mDisplayListStartID+8)) {
                            tDisplayListIDCounter = mDisplayListStartID;
                        }
                        inGL.glCallList(tDisplayListIDCounter);
                    inGL.glPopMatrix();
                    tDisplayListIDCounter++;
                }
            }
        }
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
        inGL.glFlush();
    }

}
