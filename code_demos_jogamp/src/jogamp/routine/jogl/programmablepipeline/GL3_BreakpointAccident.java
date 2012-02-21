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
 ** Advanced GL3-Profile demonstration using GLUT, display-lists and vertex+pixel shaders and FBOs.
 ** Also uses ShaderUtils to ease the use of vertex&pixel shaders (especially with more complex stuff 
 ** like the sampler uniforms). In addition to the infrastructure used the visuals are synchronized
 ** to the background music using BaseMusic and related classes like the spectrum analyzer. Other than
 ** that it enables the user to play around with the post-processing filter infrastructure using keys 1+2 
 ** and 3+4 to change convolution iterations for erosion and boxblur. For an impression how this routine
 ** looks like see here: http://www.youtube.com/watch?v=SNEb91qaSgg
 **
 **/

import java.util.*;
import framework.base.*;
import framework.util.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL3_BreakpointAccident extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {

    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    private BasePostProcessingFilterChainExecutor mBasePostProcessingFilterChainExecutor;
    private ArrayList<BasePostProcessingFilterChainShaderInterface> mConvolutions;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBasePostProcessingFilterChainExecutor = new BasePostProcessingFilterChainExecutor(4);
        mBasePostProcessingFilterChainExecutor.init(inGL,inGLU,inGLUT);
        mConvolutions = PostProcessingUtils.generatePostProcessingFilterArrayList(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),this);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.removeAllFilters();
        mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR).setNumberOfIterations(2+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_34()));
        mBasePostProcessingFilterChainExecutor.addFilter(mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR));
        mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_EROSION).setNumberOfIterations(1+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()));
        mBasePostProcessingFilterChainExecutor.addFilter(mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_EROSION));
        mBasePostProcessingFilterChainExecutor.executeFilterChain(inFrameNumber,inGL,inGLU,inGLUT,mBaseFrameBufferObjectRendererExecutor,true);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    private float mYRotation;
    private float mXRotation;
    private float[] mOffsetSinTable;
    private static final int OFFSETSINTABLE_SIZE = 2700;
    private Texture mTexture_Diffuse;
    private Texture mTexture_Displace;
    private int mDisplayListID;
    private int mLinkedShaderFBO;
    private int mVertexShaderFBO;
    private int mFragmentShaderFBO;
    private FloatBuffer mTextureCoordinateOffsets;

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mVertexShaderFBO = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/breakup_displacementmapping.vs");
        mFragmentShaderFBO = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/breakup_displacementmapping.fs");
        mLinkedShaderFBO = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,mVertexShaderFBO,mFragmentShaderFBO);
        mOffsetSinTable = OffsetTableUtils.cosaque_SinglePrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        mTexture_Diffuse = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/MysticIceSphere_Normals.png");
        mTexture_Displace = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/MysticIceSphere_Displacement.png");
        mDisplayListID = WavefrontObjectLoader_DisplayList.loadWavefrontObjectAsDisplayList(inGL,"/binaries/geometry/MysticIceSphere.wobj.zip");
        float[] tTextureCoordinateOffsets = new float[18];
        float tXIncrease = 1.0f / (float)BaseGlobalEnvironment.getInstance().getScreenWidth();
        float tYIncrease = 1.0f / (float)BaseGlobalEnvironment.getInstance().getScreenHeight();
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                tTextureCoordinateOffsets[(((i*3)+j)*2)+0] = (-1.0f*tXIncrease)+((float)i*tXIncrease);
                tTextureCoordinateOffsets[(((i*3)+j)*2)+1] = (-1.0f*tYIncrease)+((float)j*tYIncrease);
            }
        }
        mTextureCoordinateOffsets = DirectBufferUtils.createDirectFloatBuffer(tTextureCoordinateOffsets);
    }

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glCullFace(GL_BACK);
        inGL.glFrontFace(GL_CCW);
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glEnable(GL_BLEND);
        inGL.glBlendFunc(GL_ONE, GL_ONE);
        mYRotation = mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE];
        mXRotation = mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE];
        inGL.glUseProgram(mLinkedShaderFBO);
        float[] tFFTSpectrum = BaseRoutineRuntime.getInstance().getBaseMusic().getFFTSpectrum();
        float tDisplacementScale = (tFFTSpectrum[0]+tFFTSpectrum[1]+tFFTSpectrum[2])*1000.0f;
        ShaderUtils.setUniform1f(inGL,mLinkedShaderFBO,"scale",tDisplacementScale);
        ShaderUtils.setUniform2fv(inGL,mLinkedShaderFBO,"tc_offset",mTextureCoordinateOffsets);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShaderFBO,"sampler1_diffuse",mTexture_Diffuse,GL_TEXTURE1,1,true);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShaderFBO,"sampler0_displace",mTexture_Displace,GL_TEXTURE0,0,false);
        inGL.glValidateProgram(mLinkedShaderFBO);
        inGL.glPushMatrix();
            inGL.glTranslatef(0.0f, 0.05f, -4.1f);
            inGL.glRotatef(mYRotation, 0.25f, 1.0f, 0.5f);
            inGL.glRotatef(mXRotation, 0.75f, 0.3f, 0.1f);
            inGL.glCallList(mDisplayListID);
        inGL.glPopMatrix();
        inGL.glUseProgram(0);
        inGL.glDisable(GL_BLEND);
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mVertexShaderFBO);
        inGL.glDeleteShader(mFragmentShaderFBO);
        inGL.glDeleteLists(mDisplayListID,1);
        mTexture_Diffuse.destroy(inGL);
        mTexture_Displace.destroy(inGL);
        inGL.glFlush();
    }

}
