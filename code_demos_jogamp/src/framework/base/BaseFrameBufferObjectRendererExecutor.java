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

import static javax.media.opengl.GL.GL_CCW;
import static javax.media.opengl.GL.GL_CULL_FACE;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

public class BaseFrameBufferObjectRendererExecutor {

    private int mFrameBufferObjectID;
    private int mColourTextureID;
    private int mDepthTextureID;
    private int mTextureWidth;
    private int mTextureHeight;
    private BaseFrameBufferObjectRendererInterface mBaseFrameBufferObjectRendererInterface;
    
    public BaseFrameBufferObjectRendererExecutor(int inTextureWidth,int inTextureHeight,BaseFrameBufferObjectRendererInterface inBaseFrameBufferObjectRendererInterface) {
    	mTextureWidth = inTextureWidth;
    	mTextureHeight = inTextureHeight;
    	mBaseFrameBufferObjectRendererInterface = inBaseFrameBufferObjectRendererInterface;
    }

    public void init(GL2 inGL2,GLU inGLU,GLUT inGLUT) {
    	//allocate the framebuffer object ...
        int[] result = new int[1];
        inGL2.glGenFramebuffers(1, result, 0);
        mFrameBufferObjectID = result[0];
        inGL2.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        //allocate the colour texture ...
        inGL2.glGenTextures(1, result, 0);
        mColourTextureID = result[0];
        inGL2.glBindTexture(GL_TEXTURE_2D, mColourTextureID);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        inGL2.glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,mTextureWidth,mTextureHeight,0,GL_RGBA,GL_UNSIGNED_BYTE,null);
        //allocate the depth texture ...
        inGL2.glGenTextures(1, result, 0);
        mDepthTextureID = result[0];
        inGL2.glBindTexture(GL_TEXTURE_2D, mDepthTextureID);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        inGL2.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        inGL2.glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH_COMPONENT32,mTextureWidth,mTextureHeight,0,GL_DEPTH_COMPONENT,GL_UNSIGNED_INT,null);
        //attach the textures to the framebuffer
        inGL2.glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,mColourTextureID,0);
        inGL2.glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,mDepthTextureID,0);
        inGL2.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //check if fbo is set up correctly ...
        checkFrameBufferObjectCompleteness(inGL2);
        //initialize the assigned fbo renderer ...
        mBaseFrameBufferObjectRendererInterface.init_FBORenderer(inGL2,inGLU,inGLUT);
    }

    public void renderToFrameBuffer(int inFrameNumber,GL2 inGL2,GLU inGLU,GLUT inGLUT) {
    	inGL2.glPushAttrib(GL_TRANSFORM_BIT | GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
    		//bind the framebuffer ...
    		inGL2.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
    		inGL2.glPushAttrib(GL_VIEWPORT_BIT);
    			inGL2.glViewport(0,0,mTextureWidth,mTextureHeight);
    			mBaseFrameBufferObjectRendererInterface.mainLoop_FBORenderer(inFrameNumber,inGL2,inGLU,inGLUT);
    		inGL2.glPopAttrib();
    		//unbind the framebuffer ...
    		inGL2.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        inGL2.glPopAttrib();
    }

    public void prepareForColouredRendering(GL2 inGL2, int inTextureUnitID) {
    	inGL2.glPushAttrib(GL_TEXTURE_BIT);
    	inGL2.glActiveTexture(inTextureUnitID);
    	inGL2.glBindTexture(GL_TEXTURE_2D, mColourTextureID);
        //set the texture up to be used for painting a surface ...
        int textureTarget = GL_TEXTURE_2D;
        inGL2.glEnable(textureTarget);
        inGL2.glTexEnvi(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_MODULATE);
        inGL2.glTexParameteri(textureTarget,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL2.glTexParameteri(textureTarget,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL2.glTexParameteri(textureTarget,GL_TEXTURE_WRAP_S,GL_REPEAT);
        inGL2.glTexParameteri(textureTarget,GL_TEXTURE_WRAP_T,GL_REPEAT);
    }

    public void stopColouredRendering(GL2 inGL2) {
    	inGL2.glBindTexture(GL_TEXTURE_2D, 0);
        //restore the active texture ...
        inGL2.glPopAttrib();
    }
    
    public void renderFBOAsFullscreenBillboard(GL2 inGL2,GLU inGLU,GLUT inGLUT) {
        //reset frustum to default state ...
        BaseRoutineRuntime.resetFrustumToDefaultState(inGL2,inGLU,inGLUT);
        inGL2.glShadeModel(GL_SMOOTH);
        inGL2.glDisable(GL_LIGHTING);
        inGL2.glFrontFace(GL_CCW);
        inGL2.glDisable(GL_CULL_FACE);
    	//disable depth test so that billboards can be rendered on top of each other ...
        inGL2.glDisable(GL_DEPTH_TEST);			
        inGL2.glMatrixMode(GL_PROJECTION);
        inGL2.glLoadIdentity();
        inGL2.glOrtho(0, BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight(), 0, -1, 1);
        inGL2.glMatrixMode(GL_MODELVIEW);
        inGL2.glLoadIdentity();
        this.prepareForColouredRendering(inGL2,GL_TEXTURE0);
        inGL2.glBegin(GL_QUADS);
	        inGL2.glTexCoord2f(0.0f, 0.0f);
	        inGL2.glVertex2f(0.0f, 0.0f);
	        inGL2.glTexCoord2f(1.0f, 0.0f);
	        inGL2.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), 0.0f);
	        inGL2.glTexCoord2f(1.0f, 1.0f);
	        inGL2.glVertex2f(BaseGlobalEnvironment.getInstance().getScreenWidth(), BaseGlobalEnvironment.getInstance().getScreenHeight());
	        inGL2.glTexCoord2f(0.0f, 1.0f);
	        inGL2.glVertex2f(0.0f, BaseGlobalEnvironment.getInstance().getScreenHeight());
        inGL2.glEnd(); 
    	this.stopColouredRendering(inGL2);
    }
    
    public void cleanup(GL2 inGL2,GLU inGLU,GLUT inGLUT) {
    	inGL2.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mFrameBufferObjectID));
    	inGL2.glDeleteTextures(1, Buffers.newDirectIntBuffer(mColourTextureID));
    	inGL2.glDeleteTextures(1, Buffers.newDirectIntBuffer(mDepthTextureID));
    	mBaseFrameBufferObjectRendererInterface.cleanup_FBORenderer(inGL2,inGLU,inGLUT);
    }
    
	private void checkFrameBufferObjectCompleteness(GL2 inGL2) {
		BaseLogging.getInstance().info("CHECKING FRAMEBUFFEROBJECT COMPLETENESS ...");
		int tError = inGL2.glCheckFramebufferStatus(GL_FRAMEBUFFER);
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
