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
	
	public void initRoutine(GL inGL,GLU inGLU,GLUT inGLUT) {
		GL2 tGL2 = inGL.getGL2();
		mVertexShader = ShaderUtils.loadVertexShaderFromFile(tGL2,"/shaders/textureshaders/parallaxocclusionmapping.vs");
		mFragmentShader = ShaderUtils.loadFragmentShaderFromFile(tGL2,"/shaders/textureshaders/parallaxocclusionmapping.fs");
		mLinkedShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(tGL2,mVertexShader,mFragmentShader);
		mOffsetSinTable = OffsetTableUtils.cosaque_SinglePrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
		mTexture_Diffuse = TextureUtils.loadImageAsTexture_UNMODIFIED("/binaries/textures/Cube_Diffuse.png");
		mTexture_Specular = TextureUtils.loadImageAsTexture_UNMODIFIED("/binaries/textures/Cube_Specular.png");
		mTexture_Normal = TextureUtils.loadImageAsTexture_UNMODIFIED("/binaries/textures/Cube_Normals_TangentSpace.png");
		mTexture_Height = TextureUtils.loadImageAsTexture_UNMODIFIED("/binaries/textures/Cube_Displacement.png");
		mDisplayListID = WavefrontObjectLoader.loadWavefrontObjectAsDisplayList(tGL2,"/binaries/geometry/Cube.wobj");
		tGL2.glValidateProgram(mLinkedShader);	
		tGL2.glUseProgram(mLinkedShader);
		ShaderUtils.setSampler2DUniformOnTextureUnit(tGL2,mLinkedShader,"sampler0_diffuse",mTexture_Diffuse,GL_TEXTURE0,0,true);
		ShaderUtils.setSampler2DUniformOnTextureUnit(tGL2,mLinkedShader,"sampler1_gloss",mTexture_Specular,GL_TEXTURE1,1,true);
		ShaderUtils.setSampler2DUniformOnTextureUnit(tGL2,mLinkedShader,"sampler2_normal",mTexture_Normal,GL_TEXTURE2,2,false);
		ShaderUtils.setSampler2DUniformOnTextureUnit(tGL2,mLinkedShader,"sampler3_height",mTexture_Height,GL_TEXTURE3,3,true);
		ShaderUtils.setUniform4fv(tGL2,mLinkedShader,"lightPos",DirectBufferUtils.createDirectFloatBuffer(new float[]{-100.0f,100.0f,50.0f,1.0f}));	
		tGL2.glUseProgram(0);
		//reset active texture unit ... 
		inGL.glActiveTexture(GL_TEXTURE0);
	    tGL2.glEnable(GL_CULL_FACE);
	    tGL2.glEnable(GL_DEPTH_TEST);
	}
	
	public void mainLoop(int inFrameNumber,GL inGL,GLU inGLU,GLUT inGLUT) {	
		GL2 tGL2 = inGL.getGL2();
	    float tYRotation = mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE];
	    float tXRotation = mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE];
	    tGL2.glUseProgram(mLinkedShader);
		tGL2.glPushMatrix();
			tGL2.glLoadIdentity();
			tGL2.glTranslatef(0.0f, 0.0f, -3.5f);
			tGL2.glRotatef(tYRotation, 0.25f, 1.0f, 0.5f);
			tGL2.glRotatef(tXRotation, 0.75f, 0.3f, 0.1f);
			tGL2.glCallList(mDisplayListID);
	    tGL2.glPopMatrix();
	    tGL2.glUseProgram(0);		
	}
	
	public void cleanupRoutine(GL inGL,GLU inGLU,GLUT inGLUT) {
		GL2 tGL2 = inGL.getGL2();
		tGL2.glDeleteShader(mVertexShader);
		tGL2.glDeleteShader(mFragmentShader);
		tGL2.glDeleteLists(mDisplayListID,1);
		mTexture_Diffuse.destroy(inGL);
		mTexture_Specular.destroy(inGL);
		mTexture_Normal.destroy(inGL);
		mTexture_Height.destroy(inGL);
		inGL.glFlush();
	}
	
}
