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
 ** Utility wrapper class wich encapsulates the framebuffer-object feature (also known as render-
 ** 2-texture) of OpenGL in a more convenient way. Handles all the boilerplate initialization,
 ** runtime and cleanup code needed to use the FBO. The rendering calls must be provided as
 ** an implementation of the BaseFrameBufferObjectRendererInterface.
 **
 **/

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.util.gl2.*;
import static javax.media.opengl.GL2.*;

public class BaseFrameBufferObjectRendererExecutor {

    private int mFrameBufferObjectID;
    private int mColorTextureID;
    private int mDepthTextureID;
    private int mTextureWidth;
    private int mTextureHeight;
    private BaseFrameBufferObjectRendererInterface mBaseFrameBufferObjectRendererInterface;

    public BaseFrameBufferObjectRendererExecutor(int inTextureWidth,int inTextureHeight,BaseFrameBufferObjectRendererInterface inBaseFrameBufferObjectRendererInterface) {
        mTextureWidth = inTextureWidth;
        mTextureHeight = inTextureHeight;
        mBaseFrameBufferObjectRendererInterface = inBaseFrameBufferObjectRendererInterface;
    }

    public int getColorTextureID() { return mColorTextureID; }
    public int getDepthTextureID() { return mDepthTextureID; }
    public int getWidth() { return mTextureWidth; }
    public int getHeight() { return mTextureHeight; }

    public void init(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        BaseLogging.getInstance().info("INITIALIZING BaseFrameBufferObjectRendererExecutor ... "+mTextureWidth+"x"+mTextureHeight);
        //allocate the framebuffer object ...
        int[] result = new int[1];
        inGL.glGenFramebuffers(1, result, 0);
        mFrameBufferObjectID = result[0];
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        //allocate the colour texture ...
        inGL.glGenTextures(1, result, 0);
        mColorTextureID = result[0];
        inGL.glBindTexture(GL_TEXTURE_2D, mColorTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        inGL.glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,mTextureWidth,mTextureHeight,0,GL_RGBA,GL_UNSIGNED_BYTE,null);
        //allocate the depth texture ...
        inGL.glGenTextures(1, result, 0);
        mDepthTextureID = result[0];
        inGL.glBindTexture(GL_TEXTURE_2D, mDepthTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        inGL.glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH_COMPONENT32,mTextureWidth,mTextureHeight,0,GL_DEPTH_COMPONENT,GL_UNSIGNED_INT,null);
        //attach the textures to the framebuffer
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,mColorTextureID,0);
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,mDepthTextureID,0);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //check if fbo is set up correctly ...
        checkFrameBufferObjectCompleteness(inGL);
        if (mBaseFrameBufferObjectRendererInterface!=null) {
            //initialize the assigned fbo renderer ...
            mBaseFrameBufferObjectRendererInterface.init_FBORenderer(inGL,inGLU,inGLUT);
        } else {
            BaseLogging.getInstance().warning("BaseFrameBufferObjectRendererInterface FOR THIS EXECUTOR IS NULL! init_FBORenderer() SKIPPED!");
        }
    }

    public void renderToFrameBuffer(int inFrameNumber,GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glPushAttrib(GL_TRANSFORM_BIT | GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
            //bind the framebuffer ...
            inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
            inGL.glPushAttrib(GL_VIEWPORT_BIT);
            inGL.glViewport(0,0,mTextureWidth,mTextureHeight);
            if (mBaseFrameBufferObjectRendererInterface!=null) {
                mBaseFrameBufferObjectRendererInterface.mainLoop_FBORenderer(inFrameNumber,inGL,inGLU,inGLUT);
            } else {
                BaseLogging.getInstance().warning("BaseFrameBufferObjectRendererInterface FOR THIS EXECUTOR IS NULL! renderToFrameBuffer() SKIPPED!");
            }
            inGL.glPopAttrib();
            //unbind the framebuffer ...
            inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        inGL.glPopAttrib();
    }

    public void prepareForColouredRendering(GL2 inGL, int inTextureUnitID) {
        inGL.glPushAttrib(GL_TEXTURE_BIT);
        inGL.glActiveTexture(inTextureUnitID);
        inGL.glBindTexture(GL_TEXTURE_2D, mColorTextureID);
        //set the texture up to be used for painting a surface ...
        int textureTarget = GL_TEXTURE_2D;
        inGL.glEnable(textureTarget);
        inGL.glTexEnvi(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_MODULATE);
        inGL.glTexParameteri(textureTarget,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL.glTexParameteri(textureTarget,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL.glTexParameteri(textureTarget,GL_TEXTURE_WRAP_S,GL_REPEAT);
        inGL.glTexParameteri(textureTarget,GL_TEXTURE_WRAP_T,GL_REPEAT);
    }

    public void stopColouredRendering(GL2 inGL) {
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        //restore the active texture ...
        inGL.glPopAttrib();
    }
    
    public void renderFBOAsFullscreenBillboard(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT,false);
    }

    public void renderFBOAsFullscreenBillboard_FLIPPED(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        renderFBOAsFullscreenBillboard(inGL,inGLU,inGLUT,true);
    }

    public void renderFBOAsFullscreenBillboard(GL2 inGL,GLU inGLU,GLUT inGLUT,boolean inFlipped) {
        //reset frustum to default state ...
        BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT);
        inGL.glShadeModel(GL_SMOOTH);
        inGL.glDisable(GL_LIGHTING);
        inGL.glFrontFace(GL_CCW);
        inGL.glDisable(GL_CULL_FACE);
        //disable depth test so that billboards can be rendered on top of each other ...
        inGL.glDisable(GL_DEPTH_TEST);			
        inGL.glMatrixMode(GL_PROJECTION);
        inGL.glLoadIdentity();
        inGL.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, -1, 1);
        inGL.glMatrixMode(GL_MODELVIEW);
        inGL.glLoadIdentity();
        this.prepareForColouredRendering(inGL,GL_TEXTURE0);
        inGL.glBegin(GL_QUADS);
        if (inFlipped) {
            //flipped billboard
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight());
        } else {
            inGL.glTexCoord2f(0.0f, 1.0f);
            inGL.glVertex2f(0.0f, 0.0f);
            inGL.glTexCoord2f(1.0f, 1.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), 0.0f);
            inGL.glTexCoord2f(1.0f, 0.0f);
            inGL.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
            inGL.glTexCoord2f(0.0f, 0.0f);
            inGL.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight());    
        }
        inGL.glEnd(); 
        this.stopColouredRendering(inGL);
    }

    public void cleanup(GL2 inGL,GLU inGLU,GLUT inGLUT) {
        inGL.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mFrameBufferObjectID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mColorTextureID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mDepthTextureID));
        if (mBaseFrameBufferObjectRendererInterface!=null) {
            mBaseFrameBufferObjectRendererInterface.cleanup_FBORenderer(inGL,inGLU,inGLUT);
        } else {
            BaseLogging.getInstance().warning("BaseFrameBufferObjectRendererInterface FOR THIS EXECUTOR IS NULL! cleanup() SKIPPED!");
        }
    }

    private void checkFrameBufferObjectCompleteness(GL2 inGL) {
        BaseLogging.getInstance().info("CHECKING FRAMEBUFFEROBJECT COMPLETENESS ...");
        int tError = inGL.glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch(tError) {
            case GL_FRAMEBUFFER_COMPLETE:
                BaseLogging.getInstance().info("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_COMPLETE_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT");
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_UNSUPPORTED_EXT");
                break;
            default:
                BaseLogging.getInstance().error("FRAMEBUFFER CHECK RETURNED UNKNOWN RESULT ...");
        }
    }

}
