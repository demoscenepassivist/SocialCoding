package framework.util;

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
 ** Utility methods dealing with framebufferobject creation, initialization and error checking.
 **
 **/

import javax.media.opengl.*;
import framework.base.*;
import static javax.media.opengl.GL2.*;

public class FrameBufferObjectUtils {
    
    public static boolean isFrameBufferObjectComplete(GL2 inGL) {
        boolean isFrameBufferComplete = true;
        //check FBO status
        BaseLogging.getInstance().info("CHECKING FRAMEBUFFEROBJECT COMPLETENESS ...");
        int tError = inGL.glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch(tError) {
            case GL_FRAMEBUFFER_COMPLETE:
                BaseLogging.getInstance().info("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_COMPLETE_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT");
                isFrameBufferComplete = false;
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT");
                isFrameBufferComplete = false;
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT");
                isFrameBufferComplete = false;
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT");
                isFrameBufferComplete = false;
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT");
                isFrameBufferComplete = false;
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT");
                isFrameBufferComplete = false;
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED:
                BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_UNSUPPORTED_EXT");
                isFrameBufferComplete = false;
                break;
            default:
                BaseLogging.getInstance().error("FRAMEBUFFER CHECK RETURNED UNKNOWN RESULT ...");
                isFrameBufferComplete = false;
        }
        return isFrameBufferComplete;
    }
    
    public static int generateFrameBufferObjectID(GL2 inGL) {
        BaseLogging.getInstance().info("GENERATING FRAMEBUFFEROBJECTID ...");
        int[] result = new int[1];
        inGL.glGenFramebuffers(1, result, 0);
        return result[0];
    }

}
