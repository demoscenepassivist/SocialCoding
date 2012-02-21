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
 ** Advanced GL3-Profile demonstration using GLUT, display-lists and vertex+pixel shaders.
 ** Also uses ShaderUtils to ease the use of vertex&pixel shaders (especially with more complex 
 ** like the sampler uniforms). For an impression how this routine looks like see here: 
 ** http://www.youtube.com/watch?v=6J3kJ5XAa1o
 **
 **/

import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL3_ParallaxOcclusionMapping extends BaseRoutineAdapter implements BaseRoutineInterface {

    private int mVertexShader;
    private int mFragmentShader;
    private int mLinkedShader;
    private int mDisplayListID;
    private float[] mOffsetSinTable;
    private static final int OFFSETSINTABLE_SIZE = 2700;
    private Texture mTexture_Diffuse;
    private Texture mTexture_Specular;
    private Texture mTexture_Normal;
    private Texture mTexture_Height;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/textureshaders/parallaxocclusionmapping.vs");
        mFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/textureshaders/parallaxocclusionmapping.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,mVertexShader,mFragmentShader);
        mOffsetSinTable = OffsetTableUtils.cosaque_SinglePrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        mTexture_Diffuse = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/Cube_Diffuse.png");
        mTexture_Specular = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/Cube_Specular.png");
        mTexture_Normal = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/Cube_Normals_TangentSpace.png");
        mTexture_Height = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/Cube_Displacement.png");
        mDisplayListID = WavefrontObjectLoader_DisplayList.loadWavefrontObjectAsDisplayList(inGL,"/binaries/geometry/Cube.wobj");
        inGL.glValidateProgram(mLinkedShader);
        inGL.glUseProgram(mLinkedShader);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShader,"sampler0_diffuse",mTexture_Diffuse,GL_TEXTURE0,0,true);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShader,"sampler1_gloss",mTexture_Specular,GL_TEXTURE1,1,true);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShader,"sampler2_normal",mTexture_Normal,GL_TEXTURE2,2,false);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShader,"sampler3_height",mTexture_Height,GL_TEXTURE3,3,true);
        ShaderUtils.setUniform4fv(inGL,mLinkedShader,"lightPos",DirectBufferUtils.createDirectFloatBuffer(new float[]{-100.0f,100.0f,50.0f,1.0f}));
        inGL.glUseProgram(0);
        //reset active texture unit ... workaround for: ATI Catalyst 10.6 - 8.692.1 on Mobility Radeon HD 4570
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_DEPTH_TEST);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        float tYRotation = mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE];
        float tXRotation = mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE];
        inGL.glUseProgram(mLinkedShader);
        inGL.glValidateProgram(mLinkedShader);
        inGL.glPushMatrix();
            inGL.glLoadIdentity();
            inGL.glTranslatef(0.0f, 0.0f, -3.5f);
            inGL.glRotatef(tYRotation, 0.25f, 1.0f, 0.5f);
            inGL.glRotatef(tXRotation, 0.75f, 0.3f, 0.1f);
            inGL.glCallList(mDisplayListID);
        inGL.glPopMatrix();
        inGL.glUseProgram(0);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mVertexShader);
        inGL.glDeleteShader(mFragmentShader);
        inGL.glDeleteLists(mDisplayListID,1);
        mTexture_Diffuse.destroy(inGL);
        mTexture_Specular.destroy(inGL);
        mTexture_Normal.destroy(inGL);
        mTexture_Height.destroy(inGL);
        inGL.glFlush();
    }

}
