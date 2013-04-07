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
 ** Routine demonstrating more advanced point sprite usage. It creates an array of point sprites
 ** for a given numer of pixels in three dimensions. A slice stack texture for a voxel cube is loaded
 ** into a texture and then processed by a vertex shader. The point sprite array colored according to 
 ** the voxel data in the volume slice stack. As most of the work is done in the vertex shader, the setup
 ** code is merely to create the 3D point plane and scaling the point sprites along each dimension. Also 
 ** some rudimentary 3D transform and shader selection key-listeners are set up. For an 
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=XXXXXXXXXXX
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

import java.awt.image.*;
import java.nio.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL3bc.*;

public class GL3_PointSprites_VolumeTexture extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mLinkedShaderID;
    private Texture mTexture_PointSprite;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;
    private int mDisplayListID;
    private int mVolume3DTexture;  
    
    private final static boolean USE_IMMEDIATE_MODE = false;
    private final static float VERTEX_DISTANCE_SCALING = 1.75f;
    private final static int NUM_PARTICLES_X = 256;
    private final static int NUM_PARTICLES_Y = 256;
    private final static int NUM_PARTICLES_Z = 256;  
    private final static int NUM_PARTICLES_TOTAL = NUM_PARTICLES_X*NUM_PARTICLES_Y*NUM_PARTICLES_Z;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tVertexShaderID = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/pointspritevolumeshaders/pointsprite_volumetexture.vs");
        int tFragmentShaderID = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/pointspritevolumeshaders/pointsprite_volumetexture.fs");
        mLinkedShaderID = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShaderID,tFragmentShaderID);        
        //upload volume data ...
        ByteBuffer tVolumeData = GLBuffers.newDirectByteBuffer(512*512*384);
        BufferedImage[] tVolumeSlices = TextureUtils.loadARGBImageSequence("/binaries/textures/Alligator_Mississippiensis_VolumeScan.zip");      
        for (int i=0; i<tVolumeSlices.length; i++) {
            ByteBuffer tByteBuffer = TextureUtils.convertARGBBufferedImageToJOGLRDirectByteBuffer(tVolumeSlices[i]);
            tVolumeData.put(tByteBuffer);
            
        }
        tVolumeData.rewind();
        mVolume3DTexture = TextureUtils.generateTextureID(inGL);
        inGL.glActiveTexture(GL_TEXTURE1);
        inGL.glBindTexture(GL_TEXTURE_3D, mVolume3DTexture);
        inGL.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        //inGL.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        inGL.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        inGL.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_BORDER);
        inGL.glTexImage3D(GL_TEXTURE_3D, 0, GL_COMPRESSED_LUMINANCE, 512, 512, 384, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, tVolumeData);        
        inGL.glGenerateMipmap(GL_TEXTURE_3D);
        inGL.glBindTexture(GL_TEXTURE_3D, 0);    
        mTexture_PointSprite = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_001.png");
        BaseLogging.getInstance().info("CREATING NUM_PARTICLES_TOTAL="+NUM_PARTICLES_TOTAL+" NUM_PARTICLES_X="+NUM_PARTICLES_X+" NUM_PARTICLES_Y="+NUM_PARTICLES_Y+" NUM_PARTICLES_Z="+NUM_PARTICLES_Z+" ...");       
        if (!USE_IMMEDIATE_MODE) {
            BaseLogging.getInstance().info("USING VERTEX BUFFER MODE TO CREATE POINTSPRITE VERTICES ...");
            int tVertexBufferSize = 3*GLBuffers.SIZEOF_FLOAT*NUM_PARTICLES_TOTAL;
            BaseLogging.getInstance().info("VERTEXBUFFER (int) ... size="+tVertexBufferSize);
            mVertexBuffer = GLBuffers.newDirectFloatBuffer(tVertexBufferSize);
            int tTextureCoordinateBufferSize = 3*GLBuffers.SIZEOF_FLOAT*NUM_PARTICLES_TOTAL;
            BaseLogging.getInstance().info("TEXTURECOORDINATEBUFFER (int) ... size="+tTextureCoordinateBufferSize);
            mTextureCoordinateBuffer = GLBuffers.newDirectFloatBuffer(tTextureCoordinateBufferSize);
            BaseLogging.getInstance().info("NATIVE DIRECT BUFFERS CREATED ...");
            float[] tVertices = new float[NUM_PARTICLES_TOTAL*3];
            float[] tTextureCoordinates = new float[NUM_PARTICLES_TOTAL*3];
            float tTextureCoord_XIncrease = 1.0f/(float)NUM_PARTICLES_X;
            float tTextureCoord_YIncrease = 1.0f/(float)NUM_PARTICLES_Y;
            float tTextureCoord_ZIncrease = 1.0f/(float)NUM_PARTICLES_Z;
            float tXCoord = 0.0f;
            float tYCoord = 0.0f;
            float tZCoord = 0.0f;
            int i = 0;
            for (int z=-(NUM_PARTICLES_Z/2); z<(NUM_PARTICLES_Z/2); z++) {
                tYCoord = 0.0f; 
                for (int y=-(NUM_PARTICLES_Y/2); y<(NUM_PARTICLES_Y/2); y++) {
                    tXCoord = 0.0f;
                    for (int x=-(NUM_PARTICLES_X/2); x<(NUM_PARTICLES_X/2); x++) {               
                        tVertices[i*3 + 0] = x/VERTEX_DISTANCE_SCALING;
                        tVertices[i*3 + 1] = y/VERTEX_DISTANCE_SCALING;
                        tVertices[i*3 + 2] = z/VERTEX_DISTANCE_SCALING;
                        tTextureCoordinates[i*3 + 0] = tXCoord;
                        tTextureCoordinates[i*3 + 1] = tYCoord;
                        tTextureCoordinates[i*3 + 2] = tZCoord;
                        i++;
                        tXCoord +=tTextureCoord_XIncrease;
                    }
                    tYCoord+=tTextureCoord_YIncrease;
                }
                tZCoord+=tTextureCoord_ZIncrease;            
            }
            mVertexBuffer.put(tVertices).position(0);
            mTextureCoordinateBuffer.put(tTextureCoordinates).position(0);                
            mDisplayListID = inGL.glGenLists(1);
            inGL.glNewList(mDisplayListID,GL_COMPILE);
                inGL.glEnableClientState(GL_VERTEX_ARRAY);
                inGL.glEnableClientState(GL_TEXTURE_COORD_ARRAY);       
                inGL.glVertexPointer(3,GL_FLOAT,0,mVertexBuffer);        
                inGL.glTexCoordPointer(3,GL_FLOAT,0,mTextureCoordinateBuffer);
                inGL.glDrawArrays(GL_POINTS,0,NUM_PARTICLES_TOTAL);
                inGL.glDisableClientState(GL_VERTEX_ARRAY);
                inGL.glDisableClientState(GL_TEXTURE_COORD_ARRAY);      
            inGL.glEndList();
        } else {
            BaseLogging.getInstance().info("USING IMMEDIATE MODE TO CREATE POINTSPRITE VERTICES ...");
            float tTextureCoord_XIncrease = 1.0f/(float)NUM_PARTICLES_X;
            float tTextureCoord_YIncrease = 1.0f/(float)NUM_PARTICLES_Y;
            float tTextureCoord_ZIncrease = 1.0f/(float)NUM_PARTICLES_Z;
            float tXCoord = 0.0f;
            float tYCoord = 0.0f;
            float tZCoord = 0.0f;
            int i = 0;  
            mDisplayListID = inGL.glGenLists(1);
            inGL.glNewList(mDisplayListID,GL_COMPILE);
            inGL.glBegin(GL_POINTS);
            for (int z=-(NUM_PARTICLES_Z/2); z<(NUM_PARTICLES_Z/2); z++) {
                tYCoord = 0.0f; 
                for (int y=-(NUM_PARTICLES_Y/2); y<(NUM_PARTICLES_Y/2); y++) {
                    tXCoord = 0.0f;
                    for (int x=-(NUM_PARTICLES_X/2); x<(NUM_PARTICLES_X/2); x++) {               
                        inGL.glTexCoord3f(tXCoord, tYCoord,tZCoord);
                        inGL.glVertex3f(x/VERTEX_DISTANCE_SCALING, y/VERTEX_DISTANCE_SCALING,z/VERTEX_DISTANCE_SCALING);
                        i++;
                        tXCoord +=tTextureCoord_XIncrease;
                    }
                    tYCoord+=tTextureCoord_YIncrease;
                }
                tZCoord+=tTextureCoord_ZIncrease;
                BaseLogging.getInstance().info("VERTEX COUNT="+i+" ... ADDING ANOTHER "+(NUM_PARTICLES_X*NUM_PARTICLES_Y)+" VERTICES ...");
            }
            inGL.glEnd();
            inGL.glEndList();
        }
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
        if (Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12())%2==1) {
            inGL.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        } else {
            inGL.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_COLOR);
        }
        inGL.glEnable(GL_TEXTURE_3D);
        inGL.glActiveTexture(GL_TEXTURE1);        
        inGL.glBindTexture(GL_TEXTURE_3D, mVolume3DTexture);
        inGL.glActiveTexture(GL_TEXTURE0);
        mTexture_PointSprite.enable(inGL);
        mTexture_PointSprite.bind(inGL);
        inGL.glTranslatef(0, 0, -75.0f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_Y()*3.0f));
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
        inGL.glDisable(GL_TEXTURE_2D);
        inGL.glActiveTexture(GL_TEXTURE1);
        inGL.glDisable(GL_TEXTURE_3D);
        inGL.glActiveTexture(GL_TEXTURE0);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mLinkedShaderID);
        inGL.glFlush();
    }

}
