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
 ** for a given numer of pixels in three dimensions. The vertex shader for the point sprite array
 ** then calculates an 'image/animation' used for the coloring and displacement of the point 
 ** sprites on-the-fly. As most of the work is done in the vertex shader, the setup code is 
 ** merely to create the 3D point plane and scaling the point sprites along each dimension. Also 
 ** some rudimentary 3D transform and shader selection key-listeners are set up. For an 
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=XXXXXXXXXXX
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

import java.nio.*;
import framework.base.*;
import framework.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;
import static javax.media.opengl.GL3bc.*;

public class GL3_PointSprites_VolumeShader extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int[] mLinkedShaderIDs;
    protected Texture mTexture_PointSprite[];
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;
    private int mDisplayListID;
    
    private final static boolean USE_IMMEDIATE_MODE = true;
    private final static float VERTEX_DISTANCE_SCALING = 1.75f;
    private final static int DIMENSION = 128;
    private final static int NUM_PARTICLES_X = DIMENSION;
    private final static int NUM_PARTICLES_Y = DIMENSION;
    private final static int NUM_PARTICLES_Z = DIMENSION;  
    private final static int NUM_PARTICLES_TOTAL = NUM_PARTICLES_X*NUM_PARTICLES_Y*NUM_PARTICLES_Z;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mLinkedShaderIDs = new int[2];
        mLinkedShaderIDs[0] = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(
                inGL,
                ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/pointspritevolumeshaders/pointsprite_volumeshader_01.vs"),
                ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/pointspritevolumeshaders/pointsprite_volumeshader_01.fs")
        );      
        mLinkedShaderIDs[1] = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(
                inGL,
                ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/pointspritevolumeshaders/pointsprite_volumeshader_02.vs"),
                ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/pointspritevolumeshaders/pointsprite_volumeshader_02.fs")
        );
        
        mTexture_PointSprite = new Texture[11];       
        mTexture_PointSprite[0] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H100.png");
        mTexture_PointSprite[1] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H090.png");
        mTexture_PointSprite[2] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H080.png");
        mTexture_PointSprite[3] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H070.png");
        mTexture_PointSprite[4] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H060.png");
        mTexture_PointSprite[5] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H050.png");
        mTexture_PointSprite[6] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H040.png");
        mTexture_PointSprite[7] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H030.png");
        mTexture_PointSprite[8] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H020.png");
        mTexture_PointSprite[9] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H010.png");
        mTexture_PointSprite[10] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_Volume_00_H000.png");

        for (int i=0; i<mTexture_PointSprite.length; i++) {
            mTexture_PointSprite[i].setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            mTexture_PointSprite[i].setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            mTexture_PointSprite[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);
            mTexture_PointSprite[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
        }
            
        BaseLogging.getInstance().info("CREATING NUM_PARTICLES_TOTAL="+NUM_PARTICLES_TOTAL+" NUM_PARTICLES_X="+NUM_PARTICLES_X+" NUM_PARTICLES_Y="+NUM_PARTICLES_Y+" NUM_PARTICLES_Z="+NUM_PARTICLES_Z+" ...");
        int tVertexBufferSize = 3*GLBuffers.SIZEOF_FLOAT*NUM_PARTICLES_TOTAL;
        BaseLogging.getInstance().info("VERTEXBUFFER (int) ... size="+tVertexBufferSize);
        mVertexBuffer = GLBuffers.newDirectFloatBuffer(tVertexBufferSize);      
        int tTextureCoordinateBufferSize = 3*GLBuffers.SIZEOF_FLOAT*NUM_PARTICLES_TOTAL;
        BaseLogging.getInstance().info("TEXTURECOORDINATEBUFFER (int) ... size="+tTextureCoordinateBufferSize);
        mTextureCoordinateBuffer = GLBuffers.newDirectFloatBuffer(tTextureCoordinateBufferSize);
        BaseLogging.getInstance().info("NATIVE DIRECT BUFFERS CREATED ...");      
        if (!USE_IMMEDIATE_MODE) {
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
        inGL.glActiveTexture(GL_TEXTURE0);
        
        int tPointSpriteTextureNumber = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_34())%mTexture_PointSprite.length;
        mTexture_PointSprite[tPointSpriteTextureNumber].enable(inGL);
        mTexture_PointSprite[tPointSpriteTextureNumber].bind(inGL);   
        inGL.glTranslatef(0, 0, -75.0f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_Y()*3.0f));
        inGL.glRotatef(inFrameNumber*0.1f, 0f, 1f, 0f);
        inGL.glRotatef(inFrameNumber*0.2f, 1f, 0f, 0f);
        inGL.glRotatef(inFrameNumber*0.3f, 0f, 0f, 1f);       
        inGL.glEnable(GL_POINT_SPRITE);
        inGL.glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
        
        int tCurrentLinkedShaderID = mLinkedShaderIDs[Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_34())%mLinkedShaderIDs.length];      
        inGL.glUseProgram(tCurrentLinkedShaderID);
        ShaderUtils.setUniform1f(inGL,tCurrentLinkedShaderID,"time",inFrameNumber/1000.0f);
        ShaderUtils.setUniform1i(inGL,tCurrentLinkedShaderID,"sampler0",0);
        ShaderUtils.setUniform1f(inGL,tCurrentLinkedShaderID,"pointspritesize",13.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS());
        inGL.glCallList(mDisplayListID);
        inGL.glUseProgram(0); 
        inGL.glDisable(GL_VERTEX_PROGRAM_POINT_SIZE); 
        inGL.glDisable(GL_POINT_SPRITE);
        inGL.glDisable(GL_TEXTURE_2D);        
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        for (int i=0; i<mLinkedShaderIDs.length; i++) {
            inGL.glDeleteShader(mLinkedShaderIDs[i]);
        }
        inGL.glFlush();
    }

}

