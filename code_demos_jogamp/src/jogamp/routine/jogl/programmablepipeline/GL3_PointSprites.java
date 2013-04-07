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
 ** Routine demonstrating basic point sprite usage. It loads an image and creates an array of point
 ** sprites for every pixel in both dimensions. The color of the pixel corresponds to the color
 ** of the point sprite. The calculated luma is used as Z-offset. Most of the work is done in the
 ** vertex shader, the setup code is merely to create the 2D point plane and scaling the point
 ** sprites along this plane. Also some rudimentary 3D transform is set up. For an impression 
 ** how this routine looks like see here: http://www.youtube.com/watch?v=XXXXXXXXXXX
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

import java.nio.FloatBuffer;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL3bc.*;

public class GL3_PointSprites extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mLinkedShaderID;
    private Texture mTexture_PointSprite;
    private Texture mTexture_Displace;    
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;
    private int mDisplayListID;

    private final static int NUM_PARTICLES_X = 640;
    private final static int NUM_PARTICLES_Y = 360;
    private final static int NUM_PARTICLES_TOTAL = NUM_PARTICLES_X*NUM_PARTICLES_Y;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tVertexShaderID = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/pointsprites.vs");
        int tFragmentShaderID = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/pointsprites.fs");
        mLinkedShaderID = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShaderID,tFragmentShaderID);
        mTexture_PointSprite = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_BlueOrb.png");
        mTexture_Displace = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/Wallpaper_JOGAMP_PubNeon_02_1920pel.png");

        BaseLogging.getInstance().info("CREATING NUM_PARTICLES_TOTAL="+NUM_PARTICLES_TOTAL+" NUM_PARTICLES_X="+NUM_PARTICLES_X+" NUM_PARTICLES_Y="+NUM_PARTICLES_Y+" ...");
        mVertexBuffer = GLBuffers.newDirectFloatBuffer(3*GLBuffers.SIZEOF_FLOAT*NUM_PARTICLES_TOTAL);
        mTextureCoordinateBuffer = GLBuffers.newDirectFloatBuffer(2*GLBuffers.SIZEOF_FLOAT*NUM_PARTICLES_TOTAL);
        float[] tVertices = new float[NUM_PARTICLES_TOTAL*3];
        float[] tTextureCoordinates = new float[NUM_PARTICLES_TOTAL*2];
        float tTextureCoord_XIncrease = 1.0f/(float)NUM_PARTICLES_X;
        float tTextureCoord_YIncrease = 1.0f/(float)NUM_PARTICLES_Y;
        float tXCoord = 0.0f;
        float tYCoord = 0.0f;
        int i = 0;
        for (int y=-(NUM_PARTICLES_Y/2); y<(NUM_PARTICLES_Y/2); y++) {
            tXCoord = 0.0f;
            for (int x=-(NUM_PARTICLES_X/2); x<(NUM_PARTICLES_X/2); x++) {               
                tVertices[i*3 + 0] = x/1.75f;
                tVertices[i*3 + 1] = y/1.75f;
                tVertices[i*3 + 2] = 0.0f;
                tTextureCoordinates[i*2 + 0] = tXCoord;
                tTextureCoordinates[i*2 + 1] = tYCoord;
                i++;
                tXCoord +=tTextureCoord_XIncrease;
            }
            tYCoord+=tTextureCoord_YIncrease;
        }
        mVertexBuffer.put(tVertices).position(0);
        mTextureCoordinateBuffer.put(tTextureCoordinates).position(0);
                
        mDisplayListID = inGL.glGenLists(1);
        inGL.glNewList(mDisplayListID,GL_COMPILE);
            inGL.glEnableClientState(GL_VERTEX_ARRAY);
            inGL.glEnableClientState(GL_TEXTURE_COORD_ARRAY);       
            inGL.glVertexPointer(3,GL_FLOAT,0,mVertexBuffer);        
            inGL.glTexCoordPointer(2,GL_FLOAT,0,mTextureCoordinateBuffer);
            inGL.glDrawArrays(GL_POINTS,0,NUM_PARTICLES_TOTAL);
            inGL.glDisableClientState(GL_VERTEX_ARRAY);
            inGL.glDisableClientState(GL_TEXTURE_COORD_ARRAY);      
        inGL.glEndList();     
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {        
        //adjust frustum for "more depth" than the default settings ...
        inGL.glViewport(0, 0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        double tAspectRatio = (double)BaseGlobalEnvironment.getInstance().getScreenWidth()/(double)BaseGlobalEnvironment.getInstance().getScreenHeight();
        inGLU.gluPerspective(45.0, tAspectRatio, 0.01, 2000.0);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(
                0f, 0f, 70f,
                0f, 0f, 0f,
                0.0f, 1.0f, 0.0f
        );
        
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glEnable(GL_BLEND);
        inGL.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glActiveTexture(GL_TEXTURE1);
        mTexture_Displace.enable(inGL);
        mTexture_Displace.bind(inGL);
        inGL.glActiveTexture(GL_TEXTURE0);
        mTexture_PointSprite.enable(inGL);
        mTexture_PointSprite.bind(inGL);
        inGL.glTranslatef(0, 0, -500.0f);
        inGL.glRotatef(inFrameNumber*0.1f, 0f, 1f, 0f);
        inGL.glRotatef(inFrameNumber*0.2f, 1f, 0f, 0f);
        inGL.glRotatef(inFrameNumber*0.3f, 0f, 0f, 1f);       
        inGL.glEnable(GL_POINT_SPRITE);
        inGL.glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
        inGL.glUseProgram(mLinkedShaderID);
        ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"sampler0",0);
        ShaderUtils.setUniform1i(inGL,mLinkedShaderID,"sampler1",1);        
        inGL.glCallList(mDisplayListID);
        inGL.glUseProgram(0); 
        inGL.glDisable(GL_VERTEX_PROGRAM_POINT_SIZE); 
        inGL.glDisable(GL_POINT_SPRITE);
        inGL.glActiveTexture(GL_TEXTURE1);
        inGL.glDisable(GL_TEXTURE_2D);
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glDisable(GL_TEXTURE_2D);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mLinkedShaderID);
        inGL.glFlush();
    }

}
