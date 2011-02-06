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
 ** Simple fixed function pipeline based fake volume rendering routine using a couple of 
 ** hundred textured slices in cube layout to approximate a 3D volume cube. For an impression
 ** how this routine looks like see here: http://www.youtube.com/watch?v=HghpfkcE4hU
 **
 **/

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.awt.image.*;
import com.jogamp.opengl.util.gl2.*;
import framework.base.*;
import framework.util.*;
import static javax.media.opengl.GL2.*;

public class GL2_VolumeSlicesRenderer extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int[] mVolumeSlicesTextureIDs;
    private int mVolumeSlicesWidth;
    private int mVolumeSlicesHeight;
    private int mDisplayListStartID;
    private int mDisplayListSize;
    private float mSliceWidth;
    private float mSliceHeight;
    private int mNumberOfSlices;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        BufferedImage[] tVolumeSlices = TextureUtils.loadARGBImageSequence("/binaries/textures/Alligator_Mississippiensis_VolumeScan.zip");
        mVolumeSlicesTextureIDs = new int[tVolumeSlices.length];
        mVolumeSlicesWidth = tVolumeSlices[0].getWidth();
        mVolumeSlicesHeight = tVolumeSlices[0].getHeight();
        mSliceWidth = mVolumeSlicesWidth/10.0f;
        mSliceHeight = mVolumeSlicesHeight/10.0f;
        mNumberOfSlices = tVolumeSlices.length;
        for (int i=0; i<tVolumeSlices.length; i++) {
            int[] tTextureIDIntermediate = new int[1];
            TextureUtils.loadBufferedImageAs_GL_TEXTURE_2D_WithTextureDXT1Compression(tVolumeSlices[i], tTextureIDIntermediate, inGL);
            mVolumeSlicesTextureIDs[i] = tTextureIDIntermediate[0];
        }
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
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
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
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
        float tAngle = inFrameNumber%360.0f; 
        //inGL.glRotatef(tAngle, 0f, 0f, 1f);
        inGL.glRotatef(tAngle, 0f, 1f, 0f);
        inGL.glRotatef(tAngle, 1f, 0f, 0f);
        float tAlpha = (1.0f/(float)mNumberOfSlices)*2.5f;
        inGL.glColor4f(tAlpha, tAlpha, tAlpha, tAlpha);
        inGL.glPushMatrix();
            inGL.glTranslatef(-(mSliceWidth/2.0f), -(mSliceHeight/2.0f), -(mNumberOfSlices/16.0f));
            for (int i=0; i<mNumberOfSlices; i++) {
                inGL.glBindTexture(GL_TEXTURE_2D, mVolumeSlicesTextureIDs[i]);
                //mTexture.bind();
                inGL.glPushMatrix();
                    inGL.glTranslatef(0, 0, (float)i/8.0f);
                    //inGL.glTranslatef(0, 0, (float)i*8.0f);
                    inGL.glCallList(mDisplayListStartID+0);
                inGL.glPopMatrix();
            }
        inGL.glPopMatrix();
        inGL.glDisable(GL_BLEND);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteLists(mDisplayListStartID,mDisplayListSize);
        inGL.glFlush();
    }

}
