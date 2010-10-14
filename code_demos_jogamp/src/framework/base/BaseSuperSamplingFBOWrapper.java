package framework.base;

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
 ** This helper class could be utilized to ease the use of fragment shader based custom FSAA also
 ** known as "super sampling". The idea is to construct an oversized FBO, render the whole 
 ** scene/content at an oversampled resolution to the FBO and then downsample the FBO to screen
 ** resolution using an on-the-fly generated fragment shader wich implements super sampling for
 ** the given oversampled resolution. This class takes care of all the above described details.
 ** The only thing the user has to worry about is to supply a BaseFrameBufferObjectRendererInterface
 ** implementation and the desired super sampling amount.
 **
 **/

import java.text.*;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import framework.util.*;

public class BaseSuperSamplingFBOWrapper implements BaseFrameBufferObjectRendererInterface {

    private BaseFrameBufferObjectRendererExecutor mBaseFrameBufferObjectRendererExecutor;
    private float mSuperSamplingFactor;
    private int mSuperSamplingFBO_Width;
    private int mSuperSamplingFBO_Height;
    private int mLinkedShader_SuperSampling;
    private BaseFrameBufferObjectRendererInterface mBaseFrameBufferObjectRendererInterface;
    
    public BaseSuperSamplingFBOWrapper(float inSuperSamplingFactor,BaseFrameBufferObjectRendererInterface inBaseFrameBufferObjectRendererInterface) {
        mSuperSamplingFactor = inSuperSamplingFactor;
        mBaseFrameBufferObjectRendererInterface = inBaseFrameBufferObjectRendererInterface;
        mSuperSamplingFBO_Width = (int)(BaseGlobalEnvironment.getInstance().getScreenWidth()*mSuperSamplingFactor);
        mSuperSamplingFBO_Height = (int)(BaseGlobalEnvironment.getInstance().getScreenHeight()*mSuperSamplingFactor);
    }
    
    public int getSuperSampledWidth() { return mSuperSamplingFBO_Width; }
    public int getSuperSampledHeight() { return mSuperSamplingFBO_Height; }
    
    private float[] generateTextureCoordinateOffsets() {
        BaseLogging.getInstance().info("CALCULATING TEXTUREOFFSETS FOR 2FV UNIFORM ...");
        float[] tTextureCoordinateOffsets = new float[(int)(mSuperSamplingFactor*mSuperSamplingFactor)*2];
        float tXIncrease = 1.0f/(float)mSuperSamplingFBO_Width;
        float tYIncrease = 1.0f/(float)mSuperSamplingFBO_Height;
        int tSuperSamplingFactor = (int)mSuperSamplingFactor;
        DecimalFormat tDecimalFormat = new DecimalFormat("0.00000000");
        for (int i=0; i<tSuperSamplingFactor; i++) {
            for (int j=0; j<tSuperSamplingFactor; j++) {
                tTextureCoordinateOffsets[(((i*tSuperSamplingFactor)+j)*2)+0] = (float)i*tXIncrease;
                tTextureCoordinateOffsets[(((i*tSuperSamplingFactor)+j)*2)+1] = (float)j*tYIncrease;
                BaseLogging.getInstance().info("TEXTURECOORDINATE OFFSETS 2FV("+i+","+j+")="+tDecimalFormat.format(tTextureCoordinateOffsets[(((i*tSuperSamplingFactor)+j)*2)+0])+"/"+tDecimalFormat.format(tTextureCoordinateOffsets[(((i*tSuperSamplingFactor)+j)*2)+1]));
            }
        }
        return tTextureCoordinateOffsets;
    }
    
    private String generateSuperSamplingShader() {
        float[] tTextureCoordinateOffsets = generateTextureCoordinateOffsets();
        BaseLogging.getInstance().info("GENERATING SUPERSAMPLING SHADER CODE ...");
        int tNumberOfSamples = (int)(mSuperSamplingFactor*mSuperSamplingFactor);
        StringBuilder tStringBuilder = new StringBuilder();
        tStringBuilder.append("uniform sampler2D sampler0;\n");
        tStringBuilder.append("void main(void) {\n");
        tStringBuilder.append("    vec4 supersamplesum;\n");
        for (int i=0; i<tNumberOfSamples; i++) {
            tStringBuilder.append("    supersamplesum += texture2D(sampler0, gl_TexCoord[0].st + vec2("+tTextureCoordinateOffsets[i*2]+","+tTextureCoordinateOffsets[i*2+1]+"));\n");
        }
        tStringBuilder.append("    gl_FragColor = supersamplesum/"+tNumberOfSamples+".0;\n");
        tStringBuilder.append("}\n");
        BaseLogging.getInstance().info("GENERATED FRAGMENT SHADER CODE:\n"+tStringBuilder.toString());
        return tStringBuilder.toString();
    }
    
    public void init(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor = new BaseFrameBufferObjectRendererExecutor(mSuperSamplingFBO_Width,mSuperSamplingFBO_Height,mBaseFrameBufferObjectRendererInterface);
        mBaseFrameBufferObjectRendererExecutor.init(inGL,inGLU,inGLUT);
        int tFragmentShader = ShaderUtils.generateFragmentShader(inGL,generateSuperSamplingShader());
        mLinkedShader_SuperSampling = ShaderUtils.generateSimple_1xFS_ShaderProgramm(inGL,tFragmentShader);
    }

    public void executeToFrameBuffer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        inGL.glUseProgram(mLinkedShader_SuperSampling);
        ShaderUtils.setUniform1i(inGL,mLinkedShader_SuperSampling,"sampler0",0);
        inGL.glValidateProgram(mLinkedShader_SuperSampling);
        mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        inGL.glUseProgram(0);
    }
    
    public void executeToFBORendererExecutor(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT,BaseFrameBufferObjectRendererExecutor inBaseFrameBufferObjectRendererExecutor) {
        mBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
        inBaseFrameBufferObjectRendererExecutor.renderToFrameBuffer(inFrameNumber,inGL,inGLU,inGLUT);
    }
    
    public void cleanup(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        mBaseFrameBufferObjectRendererExecutor.cleanup(inGL,inGLU,inGLUT);
    }
    
    /* --------------------------------------------------------------------------------------------------------------------------------------------------- */
    
    public void init_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}
    
    public void mainLoop_FBORenderer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glUseProgram(mLinkedShader_SuperSampling);
        ShaderUtils.setUniform1i(inGL,mLinkedShader_SuperSampling,"sampler0",0);
        inGL.glValidateProgram(mLinkedShader_SuperSampling);
        mBaseFrameBufferObjectRendererExecutor.renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT);
        inGL.glUseProgram(0);
    }
    
    public void cleanup_FBORenderer(GL2 inGL,GLU inGLU,GLUT inGLUT) {}    
    
}
