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
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.media.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.awt.*;
import framework.base.*;
import static javax.media.opengl.GL2.*;

public class TextureUtils {

    public static Texture loadImageAsTexture_UNMODIFIED(GL2 inGL,String inFileName) {
        BaseLogging.getInstance().info("LOADING IMAGE FILE "+inFileName+" AS TEXTURE UNFLIPPED ...");
        //kinda 'soften' exception-handling ... -:-)
        try {
            Texture tTexture = TextureIO.newTexture(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inFileName)),true,null);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_REPEAT);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_REPEAT);
            BaseLogging.getInstance().info("TEXTURE "+inFileName+" ("+tTexture.getWidth()+"x"+tTexture.getWidth()+" AUTOMIPMAPS:"+tTexture.isUsingAutoMipmapGeneration()+") LOADED! ESTIMATED MEMORY SIZE: "+tTexture.getEstimatedMemorySize());
            return tTexture;
        } catch (Exception e) {
            BaseLogging.getInstance().exception(e);
        }
        BaseLogging.getInstance().fatalerror("LOADING IMAGE "+inFileName+" AS TEXTURE UNFLIPPED FAILED! RETURNING NULL REFERENCE!");
        return null;
    }

    public static Texture loadImageAsTexture_FLIPPED(GL2 inGL,String inFileName) {
        //kinda 'soften' exception-handling ... -:-)
        BaseLogging.getInstance().info("LOADING IMAGE FILE "+inFileName+" AS TEXTURE FLIPPED ...");
        try {
            BufferedImage tBufferedImage = ImageIO.read(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inFileName)));
            ImageUtil.flipImageVertically(tBufferedImage);
            Texture tTexture = AWTTextureIO.newTexture(GLProfile.getDefault(), tBufferedImage, true);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_WRAP_S,GL_REPEAT);
            tTexture.setTexParameterf(inGL,GL_TEXTURE_WRAP_T,GL_REPEAT);
            BaseLogging.getInstance().info("TEXTURE "+inFileName+" ("+tTexture.getWidth()+"x"+tTexture.getWidth()+" AUTOMIPMAPS:"+tTexture.isUsingAutoMipmapGeneration()+") LOADED! ESTIMATED MEMORY SIZE: "+tTexture.getEstimatedMemorySize());
            return tTexture;
        } catch (Exception e) {
            BaseLogging.getInstance().exception(e);
        }
        BaseLogging.getInstance().fatalerror("LOADING IMAGE "+inFileName+"  AS TEXTURE FLIPPED FAILED! RETURNING NULL REFERENCE!");
        return null;
    }

    public static Texture loadImageAsTexture_CONVENIENT(GL2 inGL,String inFileName) {
        Texture tTexture = TextureUtils.loadImageAsTexture_FLIPPED(inGL,inFileName);
        tTexture.enable(inGL);
        tTexture.bind(inGL);
        if (BaseGlobalEnvironment.getInstance().preferAnisotropicFiltering()) {
            tTexture.setTexParameterf(inGL,GL_TEXTURE_MAX_ANISOTROPY_EXT,new float[]{BaseGlobalEnvironment.getInstance().getAnisotropyLevel()}[0]);
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
        return convertARGBBufferedImageToJOGLDirectByteBuffer(inBufferedImage,true,true,true,true);
    }

    public static ByteBuffer convertARGBBufferedImageToJOGLRDirectByteBuffer(BufferedImage inBufferedImage) {
        return convertARGBBufferedImageToJOGLDirectByteBuffer(inBufferedImage,true,false,false,false);
    }

    public static ByteBuffer convertARGBBufferedImageToJOGLDirectByteBuffer(BufferedImage inBufferedImage,boolean inPutR,boolean inPutG,boolean inPutB,boolean inPutA) {
        BaseLogging.getInstance().info("CONVERTING ARGB BUFFERED IMAGE TO JOGL RGBA DIRECT BYTE BUFFER "+inBufferedImage.getWidth()+"x"+inBufferedImage.getHeight());
        int tSizeMultiplier = 0;
        if (inPutR) {tSizeMultiplier++;}
        if (inPutG) {tSizeMultiplier++;}
        if (inPutB) {tSizeMultiplier++;}
        if (inPutA) {tSizeMultiplier++;}
        ByteBuffer tBufferedImageByteBuffer = ByteBuffer.allocateDirect(inBufferedImage.getWidth()*inBufferedImage.getHeight()*tSizeMultiplier); 
        tBufferedImageByteBuffer.order(ByteOrder.nativeOrder()); 
        int[] tBufferedImage_ARGB = ((DataBufferInt)inBufferedImage.getRaster().getDataBuffer()).getData();
        for (int i=0; i<tBufferedImage_ARGB.length; i++) {          
            if (inPutR) {
                byte tRed   = (byte)((tBufferedImage_ARGB[i] >> 16) & 0xFF);
                tBufferedImageByteBuffer.put(tRed);
            }
            if (inPutG) {
                byte tGreen = (byte)((tBufferedImage_ARGB[i] >>  8) & 0xFF);
                tBufferedImageByteBuffer.put(tGreen);
            }
            if (inPutB) {
                byte tBlue  = (byte)((tBufferedImage_ARGB[i]      ) & 0xFF);
                tBufferedImageByteBuffer.put(tBlue);
            }
            if (inPutA) {
                byte tAlpha = (byte)((tBufferedImage_ARGB[i] >> 24) & 0xFF);
                tBufferedImageByteBuffer.put(tAlpha);
            }
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

    public static BufferedImage[] loadARGBImageSequence(String inARGBImageSequenceFileName) {
        BaseLogging.getInstance().info("LOADING IMAGESEQUENCE FROM ARCHIVE: "+inARGBImageSequenceFileName);
        try {
            ZipInputStream tZipInputStream = new ZipInputStream(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inARGBImageSequenceFileName)));
            Hashtable<String,BufferedImage> tHashtable = new Hashtable<String,BufferedImage>();
            ArrayList<String> tZipEntryFileNames = new ArrayList<String>();
            ZipEntry tZipEntry;
            while((tZipEntry = tZipInputStream.getNextEntry())!=null) {
                String inZipEntryName = tZipEntry.getName();
                BaseLogging.getInstance().info("EXTRACTING: "+inZipEntryName);
                if (!tZipEntry.isDirectory()) {
                    BufferedImage tARGBImage = ImageIO.read(tZipInputStream);
                    BaseLogging.getInstance().info("LOADED IMAGE FROM INPUT STREAM: "+tARGBImage.getWidth()+"x"+tARGBImage.getHeight());
                    BufferedImage tARGBImageIntermediate = new BufferedImage(tARGBImage.getWidth(),tARGBImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    ((Graphics2D)tARGBImageIntermediate.getGraphics()).drawImage(tARGBImage, 0,0, null);
                    tHashtable.put(inZipEntryName,tARGBImageIntermediate);
                    tZipEntryFileNames.add(inZipEntryName);
                } else {
                    BaseLogging.getInstance().fatalerror("ERROR! ZIP ENTRY IS DIRECTORY! SHOULD BE PLAIN FILE!");
                }
            }
            tZipInputStream.close();
            //sorts the specified list into ascending order, according to the natural ordering of its elements ...
            BaseLogging.getInstance().info("SORTING FILENAMES ACCORDING TO NATURAL ORDER ...");
            Collections.sort(tZipEntryFileNames);
            BufferedImage[] tBufferedImages = new BufferedImage[tZipEntryFileNames.size()];
            for (int i=0; i<tZipEntryFileNames.size(); i++) {
                BaseLogging.getInstance().info("BUFFEREDIMAGE["+i+"]="+tZipEntryFileNames.get(i));              
                tBufferedImages[i] = tHashtable.get(tZipEntryFileNames.get(i));
            }
            BaseLogging.getInstance().info("ADDING IMAGESEQUENCE TO BUFFEREDIMAGESEQUENCECACHE ...");
            return tBufferedImages;
        } catch (Exception e) {
            BaseLogging.getInstance().fatalerror(e);
            return null;
        }
    }

    public static void loadBufferedImageAs_GL_TEXTURE_2D_WithTextureDXT1Compression(BufferedImage inBufferedImage, int[] inTextureID, GL2 inGL) {
        int tWidth = inBufferedImage.getWidth();
        int tHeight = inBufferedImage.getHeight();
        inGL.glGenTextures(1, inTextureID, 0);
        inGL.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        inGL.glBindTexture(GL_TEXTURE_2D, inTextureID[0]);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, tWidth, tHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, convertARGBBufferedImageToJOGLRGBADirectByteBuffer(inBufferedImage));
        int[] tIsCompressed = new int[1]; 
        inGL.glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_COMPRESSED, tIsCompressed, 0);
        BaseLogging.getInstance().info("TEXTURE DXT1(6:1 FOR RGB) COMPRESSED="+tIsCompressed[0]);
        int[] tCompressedTextureSize = new int[1]; 
        inGL.glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_COMPRESSED_IMAGE_SIZE, tCompressedTextureSize, 0);
        BaseLogging.getInstance().info("UNCOMPRESSED TEXTURE SIZE="+(tWidth*tHeight*4));
        BaseLogging.getInstance().info("DXT1 COMPRESSED TEXTURE SIZE="+tCompressedTextureSize[0]);
    }

    public static byte[] readRawFileAsByteArray(String inFilename) {
        try {
            InputStream tInputStream = (new Object()).getClass().getResourceAsStream(inFilename);
            ByteArrayOutputStream tByteArrayOutputStream = new ByteArrayOutputStream(4096);
            byte [] tBuffer = new byte [1024];
            int tBytesRead;
            while ( (tBytesRead = tInputStream.read (tBuffer)) > 0 ) {
                tByteArrayOutputStream.write(tBuffer,0,tBytesRead);
            }
            tInputStream.close ();
            return tByteArrayOutputStream.toByteArray ();
        } catch (Exception e) {
            BaseLogging.getInstance().exception(e);
        }
        return null;
    }

}
