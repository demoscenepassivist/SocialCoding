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
 ** Utility methods dealing with texture input/output and miscellaneous related topics
 ** like filtering, texture-compression and mipmaps. 
 **
 **/

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.media.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.awt.*;
import framework.base.*;
import static javax.media.opengl.GL.*;

public class TextureUtils {
	
	public static Texture loadImageAsTexture_UNMODIFIED(String inFileName) {
		BaseLogging.getInstance().info("LOADING IMAGE FILE "+inFileName+" AS TEXTURE UNFLIPPED ...");
		//kinda 'soften' exception-handling ... -:-)
		try {
			Texture tTexture = TextureIO.newTexture(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inFileName)),true,null);
			tTexture.setTexParameterf(GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
			tTexture.setTexParameterf(GL_TEXTURE_MAG_FILTER,GL_LINEAR);
			tTexture.setTexParameterf(GL_TEXTURE_WRAP_S,GL_REPEAT);
			tTexture.setTexParameterf(GL_TEXTURE_WRAP_T,GL_REPEAT);
			BaseLogging.getInstance().info("TEXTURE "+inFileName+" ("+tTexture.getWidth()+"x"+tTexture.getWidth()+" AUTOMIPMAPS:"+tTexture.isUsingAutoMipmapGeneration()+") LOADED! ESTIMATED MEMORY SIZE: "+tTexture.getEstimatedMemorySize());
			return tTexture;
		} catch (Exception e) {
			BaseLogging.getInstance().exception(e);
		}
		BaseLogging.getInstance().fatalerror("LOADING IMAGE "+inFileName+" AS TEXTURE UNFLIPPED FAILED! RETURNING NULL REFERENCE!");
		return null;
	}
	
	public static Texture loadImageAsTexture_FLIPPED(String inFileName) {
		//kinda 'soften' exception-handling ... -:-)
		BaseLogging.getInstance().info("LOADING IMAGE FILE "+inFileName+" AS TEXTURE FLIPPED ...");
		try {
			BufferedImage tBufferedImage = ImageIO.read(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inFileName)));
			ImageUtil.flipImageVertically(tBufferedImage);
			Texture tTexture = AWTTextureIO.newTexture(GLProfile.getDefault(), tBufferedImage, true);
			tTexture.setTexParameterf(GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
			tTexture.setTexParameterf(GL_TEXTURE_MAG_FILTER,GL_LINEAR);
			tTexture.setTexParameterf(GL_TEXTURE_WRAP_S,GL_REPEAT);
			tTexture.setTexParameterf(GL_TEXTURE_WRAP_T,GL_REPEAT);
			BaseLogging.getInstance().info("TEXTURE "+inFileName+" ("+tTexture.getWidth()+"x"+tTexture.getWidth()+" AUTOMIPMAPS:"+tTexture.isUsingAutoMipmapGeneration()+") LOADED! ESTIMATED MEMORY SIZE: "+tTexture.getEstimatedMemorySize());
			return tTexture;
		} catch (Exception e) {
			BaseLogging.getInstance().exception(e);
		}
		BaseLogging.getInstance().fatalerror("LOADING IMAGE "+inFileName+"  AS TEXTURE FLIPPED FAILED! RETURNING NULL REFERENCE!");
		return null;
	}
	
	public static Texture loadImageAsTexture_CONVENIENT(String inFileName) {
		Texture tTexture = TextureUtils.loadImageAsTexture_FLIPPED(inFileName);
		tTexture.enable();
		tTexture.bind();
		if (BaseGlobalEnvironment.getInstance().preferAnisotropicFiltering()) {
			tTexture.setTexParameterf(GL_TEXTURE_MAX_ANISOTROPY_EXT,new float[]{BaseGlobalEnvironment.getInstance().getAnisotropyLevel()}[0]);
		}
		return tTexture;
	}
		
	public static void preferAnisotropicFilteringOnTextureTarget(GL2 inGL,int inTarget) {
		if (BaseGlobalEnvironment.getInstance().preferAnisotropicFiltering()) {
			inGL.glTexParameterf(inTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT , new float[]{BaseGlobalEnvironment.getInstance().getAnisotropyLevel()}[0]);
		}
	}

	public static void preferAnisotropicFilteringOnTextureTarget(GL2 inGL,Texture inTexture) {
		TextureUtils.preferAnisotropicFilteringOnTextureTarget(inGL,inTexture.getTarget());
	}
	
	public static int generateTextureID(GL2 inGL) {
		int[] result = new int[1];
		inGL.glGenTextures(1, result, 0);
		BaseLogging.getInstance().info("ALLOCATED NEW JOGL TEXTURE ID="+result[0]);
		return result[0];
	}
	
	public static void deleteTextureID(GL2 inGL, int inTextureID) {
		BaseLogging.getInstance().info("DELETING JOGL TEXTURE ID="+inTextureID);
		inGL.glDeleteTextures(1, new int[] {inTextureID}, 0); 
	}
	
}
