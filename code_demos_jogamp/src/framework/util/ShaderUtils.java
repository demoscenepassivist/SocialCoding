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
 ** Utility methods dealing with shader loading, compilation, linking, verification and
 ** uniform setup. Currently vertex- and fragment-shaders are supported.
 **
 **/

import java.io.*;
import java.nio.*;
import javax.media.opengl.*;
import framework.base.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.util.texture.*;
import static javax.media.opengl.GL2.*;


public class ShaderUtils {
	
    public static void checkShaderLogInfo(GL2 inGL2, int inShaderObjectID) {
        IntBuffer tReturnValue = Buffers.newDirectIntBuffer(1);
        inGL2.glGetObjectParameterivARB(inShaderObjectID, GL_OBJECT_INFO_LOG_LENGTH_ARB, tReturnValue);
        int tLogLength = tReturnValue.get();
        if (tLogLength <= 1) {
            return;
        }
        ByteBuffer tShaderLog = Buffers.newDirectByteBuffer(tLogLength);
        tReturnValue.flip();
        inGL2.glGetInfoLogARB(inShaderObjectID, tLogLength, tReturnValue, tShaderLog);
        byte[] tShaderLogBytes = new byte[tLogLength];
        tShaderLog.get(tShaderLogBytes);
        String tShaderValidationLog = new String(tShaderLogBytes);
        StringReader tStringReader = new StringReader(tShaderValidationLog);
        LineNumberReader tLineNumberReader = new LineNumberReader(tStringReader);
        String tCurrentLine;
	    try {
	    	while ((tCurrentLine = tLineNumberReader.readLine()) != null) {
	    		if (tCurrentLine.trim().length()>0) {
	    			BaseLogging.getInstance().info("GLSL VALIDATION: "+tCurrentLine.trim());
	    		}
	    	}
	    } catch (Exception e) {
	    	BaseLogging.getInstance().fatalerror(e);
	    }
    }
	
	public static String loadShaderSourceFileAsString(String inFileName) {
		BaseLogging.getInstance().info("LOADING SHADER SOURCECODE FROM "+inFileName);
		try {
			BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader((new Object()).getClass().getResourceAsStream(inFileName)));
			StringBuilder tStringBuilder = new StringBuilder();
		    String tCurrentLine;
		    while ((tCurrentLine = tBufferedReader.readLine()) != null) {
		    	tStringBuilder.append(tCurrentLine);
		        tStringBuilder.append("\n");
		    }
		    return tStringBuilder.toString();
		} catch (Exception e) {
			BaseLogging.getInstance().fatalerror(e);
		}
		return null;
	}
	
    public static int loadVertexShaderFromFile(GL2 inGL2,String inShaderSourceFileName) {
    	return generateVertexShader(inGL2,loadShaderSourceFileAsString(inShaderSourceFileName));
    }
    
    public static int loadFragmentShaderFromFile(GL2 inGL2,String inShaderSourceFileName) {
    	return generateFragmentShader(inGL2,loadShaderSourceFileAsString(inShaderSourceFileName));
    }
    
    public static int generateVertexShader(GL2 inGL2,String inShaderSource) {
        return generateShader(inGL2,inShaderSource,GL_VERTEX_SHADER);
    }
    
    public static int generateFragmentShader(GL2 inGL2,String inShaderSource) {
        return generateShader(inGL2,inShaderSource,GL_FRAGMENT_SHADER);
    }
    
    public static int generateShader(GL2 inGL2,String inShaderSource,int inShaderType) {
        int tShader = inGL2.glCreateShader(inShaderType);
        String[] tShaderSource = {inShaderSource};
        inGL2.glShaderSource(tShader, 1, tShaderSource, (int[])null, 0);
        inGL2.glCompileShader(tShader);
        checkShaderLogInfo(inGL2, tShader);
        return tShader;
    }
    
    public static int generateSimple_1xVS_ShaderProgramm(GL2 inGL2,int inVertexShaderObjectID) {
    	return generateSimple_1xFS_OR_1xVS_ShaderProgramm(inGL2,inVertexShaderObjectID);
    }
    
    public static int generateSimple_1xFS_ShaderProgramm(GL2 inGL2, int inFragmentShaderObjectID) {
    	return generateSimple_1xFS_OR_1xVS_ShaderProgramm(inGL2,inFragmentShaderObjectID);
    }
    
    public static int generateSimple_1xFS_OR_1xVS_ShaderProgramm(GL2 inGL2, int inGenericShaderObjectID) {
    	int tLinkedShader = inGL2.glCreateProgram();
        inGL2.glAttachShader(tLinkedShader, inGenericShaderObjectID);
        inGL2.glLinkProgram(tLinkedShader);
        inGL2.glValidateProgram(tLinkedShader);
        checkShaderLogInfo(inGL2, tLinkedShader);
        return tLinkedShader;
    }
    
    public static int generateSimple_1xVS_1xFS_ShaderProgramm(GL2 inGL2, int inVertexShaderObjectID, int inFragmentShaderObjectID) {
    	int tLinkedShader = inGL2.glCreateProgram();
    	inGL2.glAttachShader(tLinkedShader, inVertexShaderObjectID);
    	inGL2.glAttachShader(tLinkedShader, inFragmentShaderObjectID);
    	inGL2.glLinkProgram(tLinkedShader);
    	inGL2.glValidateProgram(tLinkedShader);
        checkShaderLogInfo(inGL2, tLinkedShader);
        return tLinkedShader;
    }

	public static void setUniform3fv(GL2 inGL2,int inProgramID, String inName, FloatBuffer inValues) {
	    int tUniformLocation = inGL2.glGetUniformLocation(inProgramID, inName);
	    if (tUniformLocation != -1) {
	        inGL2.glUniform3fv(tUniformLocation, 1, inValues);
	    } else {
	    	BaseLogging.getInstance().warning("UNIFORM COULD NOT BE FOUND! NAME="+inName);
	    }
	}
	
	public static void setUniform4fv(GL2 inGL2,int inProgramID, String inName, FloatBuffer inValues) {
	    int tUniformLocation = inGL2.glGetUniformLocation(inProgramID, inName);
	    if (tUniformLocation != -1) {
	        inGL2.glUniform4fv(tUniformLocation, 1, inValues);
	    } else {
	    	BaseLogging.getInstance().warning("UNIFORM COULD NOT BE FOUND! NAME="+inName);
	    }
	}
	
	public static void setUniform1i(GL2 inGL2,int inProgramID,String inName,int inValue) {
		int tUniformLocation = inGL2.glGetUniformLocation(inProgramID,inName);
		if (tUniformLocation != -1) {
			inGL2.glUniform1i(tUniformLocation, inValue);
	    } else {
	    	BaseLogging.getInstance().warning("UNIFORM COULD NOT BE FOUND! NAME="+inName);
	    }
	}
	
	public static void setSampler2DUniformOnTextureUnit(GL2 inGL2,int inProgramID,String inSamplerUniformName,Texture inTexture,int inTextureUnit,int inTextureUnitNumber,boolean inPreferAnisotropy) {
		inGL2.glActiveTexture(inTextureUnit);
		inTexture.enable();
		inTexture.bind();
		if (inPreferAnisotropy) {
			TextureUtils.preferAnisotropicFilteringOnTextureTarget(inGL2,inTexture.getTarget());        
		}
		ShaderUtils.setUniform1i(inGL2,inProgramID,inSamplerUniformName,inTextureUnitNumber); 
		inTexture.disable();
	}
	
}
