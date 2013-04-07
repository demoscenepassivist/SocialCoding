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
 ** for a given number of pixels in two dimensions. The vertex shader for the point sprite array
 ** then calculates an 'image/animation' used for the coloring and displacement of the point 
 ** sprites on-the-fly. The pixels generated here are the same fractal-bitmap-orbit trapping as
 ** in GL3_FractalBitmapOrbitTrapping. Most of the work is done in the vertex shader, the setup 
 ** code is merely to create the 2D point plane and scaling the point sprites along this plane. 
 ** Also some rudimentary 3D transform and shader selection key-listeners are set up. For an 
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=XXXXXXXXXXX
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

import framework.base.*;
import framework.util.*;
import java.nio.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.GL2GL3.*;

public class GL3_FractalBitmapOrbitTrapping_PointSprites extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    protected BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    protected BasePostProcessingFilterChainExecutor mBasePostProcessingFilterChainExecutor;
    protected ArrayList<BasePostProcessingFilterChainShaderInterface> mConvolutions;
    protected ArrayList<BasePostProcessingFilterChainShaderInterface> mBlenders;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor = new BasePostProcessingFilterChainExecutor(2);
        mBasePostProcessingFilterChainExecutor.init(inGL,inGLU,inGLUT);
        mConvolutions = PostProcessingUtils.generatePostProcessingFilterArrayList(inGL,inGLU,inGLUT);
        mBlenders = PostProcessingUtils.generateBlenderFilterArrayList(inGL,inGLU,inGLUT); 
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        //mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.removeAllFilters();
        mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR).setNumberOfIterations(26+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()));
        mBasePostProcessingFilterChainExecutor.addFilter(mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR));
        mBasePostProcessingFilterChainExecutor.addFilter(mBlenders.get(PostProcessingUtils.POSTPROCESSINGFILTER_BLENDER_SCREEN));
        //mBasePostProcessingFilterChainExecutor.addFilter(mBlenders.get(PostProcessingUtils.POSTPROCESSINGFILTER_BLENDER_ADD));
        mBasePostProcessingFilterChainExecutor.executeFilterChain(inFrameNumber,inGL,inGLU,inGLUT,mBaseFrameBufferObjectRendererExecutor,true);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    protected int mLinkedShader;
    protected FloatBuffer mScreenDimensionUniform2fv;
    protected Texture mBitmapOrbitTrap[];
    protected Texture mPointSprite[];    
    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mTextureCoordinateBuffer;
    protected int mDisplayListID;
    protected final static int NUM_PARTICLES_X = 384;
    protected final static int NUM_PARTICLES_Y = 384;
    protected final static int NUM_PARTICLES_TOTAL = NUM_PARTICLES_X*NUM_PARTICLES_Y;
    
    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        int tVertexShaderID = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/fractalshaders/fractalbitmaporbittrap_pointsprites.vs");
        int tFragmentShaderID = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/fractalshaders/fractalbitmaporbittrap_pointsprites.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShaderID,tFragmentShaderID);        
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {NUM_PARTICLES_X, NUM_PARTICLES_Y});
        mBitmapOrbitTrap = new Texture[39];
        mBitmapOrbitTrap[0] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_001.png");     
        mBitmapOrbitTrap[1] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_002.png");
        mBitmapOrbitTrap[2] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_003.png");
        mBitmapOrbitTrap[3] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_004.png");
        mBitmapOrbitTrap[4] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_005.png");
        mBitmapOrbitTrap[5] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_006.png");
        mBitmapOrbitTrap[6] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_007.png");
        mBitmapOrbitTrap[7] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_008.png");
        mBitmapOrbitTrap[8] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_009.png");
        mBitmapOrbitTrap[9] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_010.png");
        mBitmapOrbitTrap[10] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_011.png");
        mBitmapOrbitTrap[11] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_012.png");
        mBitmapOrbitTrap[12] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_013.png");
        mBitmapOrbitTrap[13] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_014.png");
        mBitmapOrbitTrap[14] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_015.png");
        mBitmapOrbitTrap[15] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_016.png");
        mBitmapOrbitTrap[16] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_017.png");
        mBitmapOrbitTrap[17] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_018.png");
        mBitmapOrbitTrap[18] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_019.png");
        mBitmapOrbitTrap[19] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_020.png");
        mBitmapOrbitTrap[20] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_021.png");
        mBitmapOrbitTrap[21] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_022.png");
        mBitmapOrbitTrap[22] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_023.png");
        mBitmapOrbitTrap[23] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_024.png");
        mBitmapOrbitTrap[24] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_025.png");
        mBitmapOrbitTrap[25] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_026.png");
        mBitmapOrbitTrap[26] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_027.png");
        mBitmapOrbitTrap[27] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_028.png");
        mBitmapOrbitTrap[28] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_029.png");
        mBitmapOrbitTrap[29] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_030.png");
        mBitmapOrbitTrap[30] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_031.png");
        mBitmapOrbitTrap[31] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_032.png");
        mBitmapOrbitTrap[32] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_033.png");
        mBitmapOrbitTrap[33] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_034.png");
        mBitmapOrbitTrap[34] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_035.png");
        mBitmapOrbitTrap[35] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_036.png");
        mBitmapOrbitTrap[36] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_037.png");
        mBitmapOrbitTrap[37] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_038.png");
        mBitmapOrbitTrap[38] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/BitmapOrbitTrap_039.png");
        for (int i=0; i<mBitmapOrbitTrap.length; i++) {
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);
            mBitmapOrbitTrap[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
        }      
        mPointSprite = new Texture[6];
        mPointSprite[0] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_BlueOrb.png");    
        mPointSprite[1] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_001.png");    
        mPointSprite[2] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_002.png");    
        mPointSprite[3] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_003.png");    
        mPointSprite[4] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_004.png");    
        mPointSprite[5] = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/PointSprite_005.png");   
        for (int i=0; i<mPointSprite.length; i++) {
            mPointSprite[i].setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
            mPointSprite[i].setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            mPointSprite[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);
            mPointSprite[i].setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
        }
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

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //explicitly clear FBO ...
        inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        inGL.glClear(GL_COLOR_BUFFER_BIT);
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
        //---
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glEnable(GL_BLEND);
        inGL.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glActiveTexture(GL_TEXTURE1);
        int tCurrentTexture = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()%mBitmapOrbitTrap.length);
        mBitmapOrbitTrap[tCurrentTexture].enable(inGL);
        mBitmapOrbitTrap[tCurrentTexture].bind(inGL);
        inGL.glActiveTexture(GL_TEXTURE0);
        int tCurrentPointSprite = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_QW()%mPointSprite.length);
        mPointSprite[tCurrentPointSprite].enable(inGL);
        mPointSprite[tCurrentPointSprite].bind(inGL);
        int tCurrentTransform = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_ER()%5);
        if (tCurrentTransform==0) {
            inGL.glTranslatef(0, 0, -250.0f);
            inGL.glRotatef(inFrameNumber*0.2f, 0f, 1f, 0f);
            //inGL.glRotatef(inFrameNumber*0.2f, 1f, 0f, 0f);
            //inGL.glRotatef(inFrameNumber*0.3f, 0f, 0f, 1f);       
        } else if(tCurrentTransform==1) {
            inGL.glTranslatef(0, 0, -150.0f);
            inGL.glRotatef(inFrameNumber*0.2f, 0f, 1f, 0f);
            //inGL.glRotatef(inFrameNumber*0.2f, 1f, 0f, 0f);
            //inGL.glRotatef(inFrameNumber*0.3f, 0f, 0f, 1f);                  
        } else if(tCurrentTransform==2) {
            inGL.glTranslatef(0, BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_DF(), -150.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_CV());
            inGL.glRotatef(180.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_GH(), 0f, 1f, 0f);
            inGL.glRotatef(65.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_JK(), 1f, 0f, 0f);
            inGL.glRotatef(0.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_YX(), 0f, 0f, 1f);                  
        } else if(tCurrentTransform==3) {
            inGL.glTranslatef(0, -15+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_DF(), -60.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_CV());
            inGL.glRotatef(180.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_GH(), 0f, 1f, 0f);
            inGL.glRotatef(85.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_JK(), 1f, 0f, 0f);
            inGL.glRotatef(0.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_YX(), 0f, 0f, 1f);                  
        } else if(tCurrentTransform==4) {
            inGL.glTranslatef(0, -15-7+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_DF(), -60.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_CV());
            inGL.glRotatef(inFrameNumber*0.2f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_GH(), 0f, 1f, 0f);
            inGL.glRotatef(90.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_JK(), 1f, 0f, 0f);
            inGL.glRotatef(0.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_YX(), 0f, 0f, 1f);                  
        }
        inGL.glEnable(GL_POINT_SPRITE);
        inGL.glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
        inGL.glUseProgram(mLinkedShader);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"sampler0",0);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"texture",1);
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"time",inFrameNumber/100.0f);
        ShaderUtils.setUniform2fv(inGL,mLinkedShader,"resolution",mScreenDimensionUniform2fv);      
        int zoominvalue = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_34())%2;
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"zoomin",zoominvalue);
        if (zoominvalue==0) {
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",40+BaseGlobalEnvironment.getInstance().getParameterKey_INT_90());
        } else {
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"iterationslimit",(256+BaseGlobalEnvironment.getInstance().getParameterKey_INT_90()%256));
        }
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliamode",Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_56())%2);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"juliapower",2+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_78()));
        ShaderUtils.setUniform1f(inGL,mLinkedShader,"orbittrapscale",3.5f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS());
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"rubber",BaseGlobalEnvironment.getInstance().getParameterKey_INT_UI()%2);
        ShaderUtils.setUniform1i(inGL,mLinkedShader,"invertdisplace",0);
        int tCurrentMirror = Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_TZ()%4);
        if (tCurrentMirror==0) {
            inGL.glCallList(mDisplayListID);
        } else if(tCurrentMirror==1) {
            inGL.glCallList(mDisplayListID);
            ShaderUtils.setUniform1i(inGL,mLinkedShader,"invertdisplace",1);
            inGL.glCallList(mDisplayListID);
        } else if(tCurrentMirror==2) {
            inGL.glCallList(mDisplayListID);
            inGL.glRotatef(180.0f, 0f, 1f, 0f);
            inGL.glCallList(mDisplayListID);
        } else if(tCurrentMirror==3) {
            inGL.glCallList(mDisplayListID);
            inGL.glRotatef(180.0f, 1f, 0f, 0f);
            inGL.glCallList(mDisplayListID);
        }
        inGL.glUseProgram(0); 
        inGL.glDisable(GL_VERTEX_PROGRAM_POINT_SIZE); 
        inGL.glDisable(GL_POINT_SPRITE);
        inGL.glActiveTexture(GL_TEXTURE1);
        inGL.glDisable(GL_TEXTURE_2D);
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glDisable(GL_TEXTURE_2D);        
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mLinkedShader);
        inGL.glFlush();
    }

}
