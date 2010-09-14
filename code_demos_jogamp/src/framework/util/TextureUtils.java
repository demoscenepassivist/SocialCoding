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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import javax.imageio.*;
import javax.media.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.awt.*;
import framework.base.*;
import static javax.media.opengl.GL2.*;

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

    public static ByteBuffer convertARGBBufferedImageToJOGLRGBADirectByteBuffer(BufferedImage inBufferedImage) {
        BaseLogging.getInstance().info("CONVERTING ARGB BUFFERED IMAGE TO JOGL RGBA DIRECT BYTE BUFFER "+inBufferedImage.getWidth()+"x"+inBufferedImage.getHeight());
        ByteBuffer tBufferedImageByteBuffer = ByteBuffer.allocateDirect(inBufferedImage.getWidth()*inBufferedImage.getHeight()*4); 
        tBufferedImageByteBuffer.order(ByteOrder.nativeOrder()); 
        int[] tBufferedImage_ARGB = ((DataBufferInt)inBufferedImage.getRaster().getDataBuffer()).getData();
        for (int i=0; i<tBufferedImage_ARGB.length; i++) {          
            byte tRed   = (byte)((tBufferedImage_ARGB[i] >> 16) & 0xFF);
            byte tGreen = (byte)((tBufferedImage_ARGB[i] >>  8) & 0xFF);
            byte tBlue  = (byte)((tBufferedImage_ARGB[i]      ) & 0xFF);
            byte tAlpha = (byte)((tBufferedImage_ARGB[i] >> 24) & 0xFF);
            tBufferedImageByteBuffer.put(tRed);
            tBufferedImageByteBuffer.put(tGreen);
            tBufferedImageByteBuffer.put(tBlue);
            tBufferedImageByteBuffer.put(tAlpha);
        }
        tBufferedImageByteBuffer.rewind(); 
        return tBufferedImageByteBuffer; 
    }

    public static BufferedImage createARGBBufferedImage(int inWidth, int inHeight) {
        BaseLogging.getInstance().info("CREATING NEW BUFFEREDIMAGE ... "+inWidth+"x"+inHeight);
        BufferedImage tARGBImageIntermediate = new BufferedImage(inWidth,inHeight, BufferedImage.TYPE_INT_ARGB);
        fillImageWithTransparentColor(tARGBImageIntermediate);
        return tARGBImageIntermediate;
    }

    public static void fillImageWithTransparentColor(Image inImage) {
        Color TRANSPARENT = new Color(0,0,0,0);
        fillImageWithColor(inImage,TRANSPARENT);
    }

    public static void fillImageWithColor(Image inImage,Color inColor) {
        Graphics2D tGraphics2D = (Graphics2D)inImage.getGraphics(); 
        tGraphics2D.setColor(inColor);
        tGraphics2D.setComposite(AlphaComposite.Src);
        tGraphics2D.fillRect(0,0,inImage.getWidth(null),inImage.getHeight(null));
        tGraphics2D.dispose();
    }

    public static int generateTexture1DFromBufferedImage(GL2 inGL,BufferedImage inBufferedImage,int inBorderMode) {
        BaseLogging.getInstance().info("GENERATING 1D TEXTURE FROM ARGB BUFFERED IMAGE "+inBufferedImage.getWidth()+"x"+inBufferedImage.getHeight());
        inGL.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        int t1DTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glEnable(GL_TEXTURE_1D);
        inGL.glBindTexture(GL_TEXTURE_1D, t1DTextureID);
        inGL.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, inBufferedImage.getWidth(), 0, GL_RGBA, GL_UNSIGNED_BYTE, TextureUtils.convertARGBBufferedImageToJOGLRGBADirectByteBuffer(inBufferedImage));
        inGL.glTexParameteri(GL_TEXTURE_1D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_1D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_1D,GL_TEXTURE_WRAP_S,inBorderMode);
        inGL.glTexParameteri(GL_TEXTURE_1D,GL_TEXTURE_WRAP_T,inBorderMode);
        return t1DTextureID;
    }

}
