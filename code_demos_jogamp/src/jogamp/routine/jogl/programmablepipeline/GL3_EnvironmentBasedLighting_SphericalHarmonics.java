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
 ** Vertex Shader program implementing "Image Based Lighting" with spherical harmonics as described
 ** in the Siggraph 2001 paper "An Efficient Representation for Irradiance Environment Maps" by 
 ** Ravi Ramamoorthi and Pat Hanrahan. In addition to spherical harmonics lighting the mesh is
 ** shaded with a prebaked ambient occlusion map (blender uv-unwrap) to further refine the fake
 ** global illumination impression. The routine also makes use of a completely deferred rendering
 ** pipeline with super sampling and post processing bloom. For an impression how this routine
 ** looks like see here: http://www.youtube.com/watch?v=08hVs9_Jxog
 **
 **/

import java.util.*;
import framework.base.*;
import framework.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;

public class GL3_EnvironmentBasedLighting_SphericalHarmonics extends BaseRoutineAdapter implements BaseRoutineInterface,BaseFrameBufferObjectRendererInterface {
    
    private BaseSuperSamplingFBOWrapper mBaseSuperSamplingFBOWrapper;
    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    private BasePostProcessingFilterChainExecutor mBasePostProcessingFilterChainExecutor;
    private ArrayList<BasePostProcessingFilterChainShaderInterface> mConvolutions;
    private ArrayList<BasePostProcessingFilterChainShaderInterface> mBlenders;

    public void initRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseSuperSamplingFBOWrapper = new BaseSuperSamplingFBOWrapper(2.0f,this);
        mBaseSuperSamplingFBOWrapper.init(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(BaseGlobalEnvironment.getInstance().getScreenWidth(),BaseGlobalEnvironment.getInstance().getScreenHeight(),mBaseSuperSamplingFBOWrapper);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor = new BasePostProcessingFilterChainExecutor(4);
        mBasePostProcessingFilterChainExecutor.init(inGL,inGLU,inGLUT);
        mConvolutions = PostProcessingUtils.generatePostProcessingFilterArrayList(inGL,inGLU,inGLUT);
        mBlenders = PostProcessingUtils.generateBlenderFilterArrayList(inGL,inGLU,inGLUT); 
    }

    public void mainLoop(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseSuperSamplingFBOWrapper.executeToFBORendererExecutor(inFrameNumber,inGL,inGLU,inGLUT,mBaseFrameBufferObjectRendererExecutor);
        mBasePostProcessingFilterChainExecutor.removeAllFilters();
        mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR).setNumberOfIterations(26+Math.abs(BaseGlobalEnvironment.getInstance().getParameterKey_INT_12()));
        mBasePostProcessingFilterChainExecutor.addFilter(mConvolutions.get(PostProcessingUtils.POSTPROCESSINGFILTER_CONVOLUTION_BOXBLUR));
        mBasePostProcessingFilterChainExecutor.addFilter(mBlenders.get(PostProcessingUtils.POSTPROCESSINGFILTER_BLENDER_LIGHTEN));
        mBasePostProcessingFilterChainExecutor.executeFilterChain(inFrameNumber,inGL,inGLU,inGLUT,mBaseFrameBufferObjectRendererExecutor,true);
    }

    public void cleanupRoutine(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseSuperSamplingFBOWrapper.cleanup(inGL,inGLU,inGLUT);
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
        mBasePostProcessingFilterChainExecutor.cleanup(inGL,inGLU,inGLUT);
    }

    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */

    private int mLinkedShader;
    //private int mDisplayListID;
    WavefrontObjectLoader_VertexBufferObject mWavefrontObjectLoader_VertexBufferObject;
    private static final int OFFSETSINTABLE_SIZE = 2700;
    private float[] mOffsetSinTable;
    private Texture mTexture_AmbientOcclusion;
    private Texture mTexture_Background; 

    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mOffsetSinTable = OffsetTableUtils.cosaque_SinglePrecision(OFFSETSINTABLE_SIZE,360,true,OffsetTableUtils.TRIGONOMETRIC_FUNCTION.SIN);
        int tVertexShader = ShaderUtils.loadVertexShaderFromFile(inGL,"/shaders/environmentbasedlightingshaders/sphericalharmonics_pervertex.vs");
        int tFragmentShader = ShaderUtils.loadFragmentShaderFromFile(inGL,"/shaders/environmentbasedlightingshaders/sphericalharmonics_pervertex.fs");
        mLinkedShader = ShaderUtils.generateSimple_1xVS_1xFS_ShaderProgramm(inGL,tVertexShader,tFragmentShader);
        //mDisplayListID = WavefrontObjectLoader_DisplayList.loadWavefrontObjectAsDisplayList(inGL,"/binaries/geometry/LinkingStars.wobj.zip");
        mWavefrontObjectLoader_VertexBufferObject = new WavefrontObjectLoader_VertexBufferObject("/binaries/geometry/LinkingStars.wobj.zip");
        mTexture_AmbientOcclusion = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/LinkingStars_BakedAmbientOcclusion.png");
        mTexture_Background = TextureUtils.loadImageAsTexture_UNMODIFIED(inGL,"/binaries/textures/GraceCathedral_Background.png");
    }

    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        //clear screen and z-buffer ...
        inGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        inGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderBackgroundTexture(mTexture_Background,mBaseSuperSamplingFBOWrapper.getSuperSampledWidth(), mBaseSuperSamplingFBOWrapper.getSuperSampledHeight(),inGL,inGLU,inGLUT);
        BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT,mBaseSuperSamplingFBOWrapper.getSuperSampledWidth(), mBaseSuperSamplingFBOWrapper.getSuperSampledHeight());
        inGL.glCullFace(GL_BACK);
        inGL.glEnable(GL_CULL_FACE);
        inGL.glEnable(GL_DEPTH_TEST);
        inGL.glTranslatef(0.0f,0.0f,63.0f); 
        inGL.glValidateProgram(mLinkedShader);
        inGL.glUseProgram(mLinkedShader);
        ShaderUtils.setSampler2DUniformOnTextureUnit(inGL,mLinkedShader,"sampler0",mTexture_AmbientOcclusion,GL_TEXTURE0,0,true);
        float tYRotation = mOffsetSinTable[(inFrameNumber)%OFFSETSINTABLE_SIZE];
        float tXRotation = mOffsetSinTable[(int)(inFrameNumber*1.5f)%OFFSETSINTABLE_SIZE];
        inGL.glPushMatrix();
            inGL.glRotatef(tYRotation, 0.25f, 1.0f, 0.5f);
            inGL.glRotatef(tXRotation, 0.75f, 0.3f, 0.1f);
            //inGL.glCallList(mDisplayListID+0);
            mWavefrontObjectLoader_VertexBufferObject.DrawModel(inGL);
        inGL.glPopMatrix();
        inGL.glUseProgram(0);
    }

    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteShader(mLinkedShader);
        //inGL.glDeleteLists(mDisplayListID,1);
        inGL.glFlush(); 
    }

    private void renderBackgroundTexture(Texture inTexture, int inScreenWidth, int inScreenHeight, GL2 inGL, GLU inGLU, GLUT inGLUT) {
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glDisable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glDisable(GL_CULL_FACE);
        //disable depth test so that billboards can be rendered on top of each other ...
        inGL.glDisable(GL_DEPTH_TEST);
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, inScreenWidth, inScreenHeight, 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        inGL.glPushAttrib(GL_TEXTURE_BIT);
            inGL.glActiveTexture(GL_TEXTURE0);
            inTexture.enable(inGL);
            inTexture.bind(inGL);
            inGL.glBegin(GL_QUADS);
                inGL.glTexCoord2f(0.0f, 0.0f);
                inGL.glVertex2f(0.0f, 0.0f);
                inGL.glTexCoord2f(1.0f, 0.0f);
                inGL.glVertex2f(inScreenWidth, 0.0f);
                inGL.glTexCoord2f(1.0f, 1.0f);
                inGL.glVertex2f(inScreenWidth, inScreenHeight);
                inGL.glTexCoord2f(0.0f, 1.0f);
                inGL.glVertex2f(0.0f, inScreenHeight);
            inGL.glEnd();
        inGL.glPopAttrib();
    }

}
