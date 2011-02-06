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
 ** Basic volume raycasting routine rendering a 3D volume texture inside a unit cube. For an
 ** impression how this routine looks like see here: http://www.youtube.com/watch?v=wcEe2YZV1GE  
 **
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.awt.image.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class GL3_VolumetricRaycasting_VolumeTexture extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mPositionShader;
    private int mVolumeShader;
    private int mVertexArrayObject;
    private int mVertexBufferObject;
    private int mIndexBufferObject;
    private int mBackFacePositionTexture;
    private int mFrameBufferObject;
    private int mVolume3DTexture;    
    private FloatBuffer mVertices;
    private IntBuffer mIndices;
    private double[] mOffsetSinTable;
    private static final int OFFSETSINTABLE_SIZE = 1800;
    private FloatBuffer mScreenDimensionUniform2fv;
    
    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mScreenDimensionUniform2fv = DirectBufferUtils.createDirectFloatBuffer(new float[] {(float)BaseGlobalEnvironment.getInstance().getScreenWidth(), (float)BaseGlobalEnvironment.getInstance().getScreenHeight()});
        mOffsetSinTable = OffsetTableUtils.cosaque_DoublePrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        int tVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/volumeraycasting/position.vs");
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/volumeraycasting/position.fs");   
        mPositionShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);
        tVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/volumeraycasting/position.vs");
        tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/volumeraycasting/volume.fs");
        mVolumeShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);        
        //create vertex array buffer object ...   
        mVertices = GLBuffers.newDirectFloatBuffer(8*3);
        mVertices.put(new float[]{0.0f, 0.0f, 0.0f});
        mVertices.put(new float[]{0.0f, 0.0f, 1.0f});
        mVertices.put(new float[]{1.0f, 0.0f, 0.0f});
        mVertices.put(new float[]{1.0f, 0.0f, 1.0f});
        mVertices.put(new float[]{0.0f, 1.0f, 0.0f});
        mVertices.put(new float[]{0.0f, 1.0f, 1.0f});
        mVertices.put(new float[]{1.0f, 1.0f, 0.0f});
        mVertices.put(new float[]{1.0f, 1.0f, 1.0f});
        mVertices.rewind();
        mVertexBufferObject = GeometryUtils.generateBufferID(inGL);
        inGL.glBindBuffer(GL_ARRAY_BUFFER, mVertexBufferObject);
        inGL.glBufferData(GL_ARRAY_BUFFER, 8*3*GLBuffers.SIZEOF_FLOAT, mVertices, GL_STATIC_DRAW);
        //create index buffer object ...
        mIndices = GLBuffers.newDirectIntBuffer(new int[] {
                2, 3, 0, 0, 3, 1, //bottom
                0, 1, 4, 4, 1, 5, //left
                6, 2, 4, 4, 2, 0, //rear
                6, 7, 2, 2, 7, 3, //right
                3, 7, 1, 1, 7, 5, //front
                4, 5, 6, 6, 5, 7, //top
        });        
        mIndexBufferObject = GeometryUtils.generateBufferID(inGL);
        inGL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferObject);
        inGL.glBufferData(GL_ELEMENT_ARRAY_BUFFER, 36*GLBuffers.SIZEOF_INT, mIndices, GL_STATIC_DRAW);
        //create vertex array object ...
        mVertexArrayObject = GeometryUtils.generateVertexArrayID(inGL);      
        inGL.glBindVertexArray(mVertexArrayObject);
        inGL.glBindBuffer(GL_ARRAY_BUFFER, mVertexBufferObject);
        inGL.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        inGL.glEnableVertexAttribArray(0);
        inGL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferObject);
        //create back face texture for positions ...
        mBackFacePositionTexture = TextureUtils.generateTextureID(inGL);
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glBindTexture(GL_TEXTURE_2D, mBackFacePositionTexture);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, GL_RGBA, GL_FLOAT, null);
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        //create framebuffer object ...
        mFrameBufferObject = FrameBufferObjectUtils.generateFrameBufferObjectID(inGL);
        inGL.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mFrameBufferObject);
        inGL.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mBackFacePositionTexture, 0);
        inGL.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glEnable(GL_CULL_FACE);
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
    }

    private void setupMatrices(GL2 inGL,GLU inGLU) {
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGLU.gluPerspective(45.0f,(((float)BaseGlobalEnvironment.getInstance().getScreenWidth())/((float)BaseGlobalEnvironment.getInstance().getScreenHeight())),0.01f, 10.0f);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGLU.gluLookAt(0.0f, 0.0f, 1.0f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_LÖ(),0.0f, 0.0f, 0.0f,0,1,0);
    }
    
    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glPushAttrib(GL_ALL_ATTRIB_BITS);
        setupMatrices(inGL,inGLU);
        double tYRotation = mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE];
        double tXRotation = mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE];
        inGL.glRotatef((float)tYRotation, 0.25f, 1.0f, 0.5f);
        inGL.glRotatef((float)tXRotation, 0.75f, 0.3f, 0.1f);
        //render position back faces to the texture ...
        inGL.glUseProgram(mPositionShader);
        inGL.glCullFace(GL_FRONT);
        inGL.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mFrameBufferObject);
        inGL.glClear(GL_COLOR_BUFFER_BIT);
        inGL.glBindVertexArray(mVertexArrayObject);
        inGL.glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
        //render volume data to cube ...
        inGL.glUseProgram(mVolumeShader);
        ShaderUtils.setUniform2fv(inGL,mVolumeShader,"resolution",mScreenDimensionUniform2fv);
        ShaderUtils.setUniform1f(inGL,mVolumeShader,"stepsize",0.002f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_AS()/100.0f));
        ShaderUtils.setUniform1f(inGL,mVolumeShader,"alphafactor",0.3f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_DF());
        ShaderUtils.setUniform1f(inGL,mVolumeShader,"iterations",1000.0f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_GH()*100.0f));
        ShaderUtils.setUniform1f(inGL,mVolumeShader,"brightness",1.1f+BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_JK());
        ShaderUtils.setUniform1f(inGL,mVolumeShader,"skipfactor",0.95f+(BaseGlobalEnvironment.getInstance().getParameterKey_FLOAT_YX()/10.0f));
        inGL.glCullFace(GL_BACK);
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glBindTexture(GL_TEXTURE_2D, mBackFacePositionTexture);
        inGL.glActiveTexture(GL_TEXTURE1);
        inGL.glBindTexture(GL_TEXTURE_3D, mVolume3DTexture);
        ShaderUtils.setUniform1i(inGL,mVolumeShader,"positiontexture",0);
        ShaderUtils.setUniform1i(inGL,mVolumeShader,"volumetexture",1);
        inGL.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        inGL.glBindVertexArray(mVertexArrayObject);
        inGL.glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
        //recover state machine ...
        inGL.glUseProgram(0);
        inGL.glBindVertexArray(0);
        inGL.glPopAttrib();
        inGL.glActiveTexture(GL_TEXTURE0);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteTextures(1,Buffers.newDirectIntBuffer(mVolume3DTexture));
        inGL.glDeleteFramebuffers(1,Buffers.newDirectIntBuffer(mFrameBufferObject));
        inGL.glDeleteTextures(1,Buffers.newDirectIntBuffer(mBackFacePositionTexture));
        inGL.glDeleteBuffers(1,Buffers.newDirectIntBuffer(mIndexBufferObject));
        inGL.glDeleteBuffers(1,Buffers.newDirectIntBuffer(mVertexBufferObject));
        inGL.glDeleteVertexArrays(1,Buffers.newDirectIntBuffer(mVertexArrayObject));
        inGL.glDeleteProgram(mVolumeShader);
        inGL.glDeleteProgram(mPositionShader);
    }

}
