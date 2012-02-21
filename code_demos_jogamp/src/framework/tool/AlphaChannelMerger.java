package framework.tool;

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
 ** Small tool that takes two source images, one representing the RGB-components of the output
 ** image and another grey image representing the alpha-component of the output image. Both source
 ** images are muxed/merged into single .png image in RGBA format. Came in quite handy while 
 ** experimenting with different fractal orbit trap images.
 **
 **/

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class AlphaChannelMerger {

    public static BufferedImage createARGBBufferedImage(int inWidth, int inHeight) {
        System.out.println("CREATING NEW BUFFEREDIMAGE ... "+inWidth+"x"+inHeight);
        BufferedImage tARGBImageIntermediate = new BufferedImage(inWidth,inHeight, BufferedImage.TYPE_INT_ARGB);
        AlphaChannelMerger.fillImageWithTransparentColor(tARGBImageIntermediate);
        return tARGBImageIntermediate;
    }

    public static void fillImageWithTransparentColor(Image inImage) {
        Color TRANSPARENT = new Color(0,0,0,0);
        AlphaChannelMerger.fillImageWithColor(inImage,TRANSPARENT);
    }

    public static void fillImageWithColor(Image inImage,Color inColor) {
        Graphics2D tGraphics2D = (Graphics2D)inImage.getGraphics(); 
        tGraphics2D.setColor(inColor);
        tGraphics2D.setComposite(AlphaComposite.Src);
        tGraphics2D.fillRect(0,0,inImage.getWidth(null),inImage.getHeight(null));
        tGraphics2D.dispose();
    }

    public static BufferedImage loadARGBImage(String inARGBImageFileName,InputStream inInputStream) {
        try {
            BufferedImage tARGBImage = ImageIO.read(inInputStream);
            System.out.println("LOADED IMAGE FROM INPUT STREAM: "+tARGBImage.getWidth()+"x"+tARGBImage.getHeight());
            BufferedImage tARGBImageIntermediate = AlphaChannelMerger.createARGBBufferedImage(tARGBImage.getWidth(),tARGBImage.getHeight());
            ((Graphics2D)tARGBImageIntermediate.getGraphics()).drawImage(tARGBImage, 0,0, null);    
            return tARGBImageIntermediate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveARGBImage(String inARGBImageFileName,BufferedImage inBufferedImage) {
        try {
            System.out.println("SAVING MERGED ARGB IMAGE AS PNG ... "+inARGBImageFileName);
            ImageIO.write(inBufferedImage, "png", new File(inARGBImageFileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[] getARGBDataBufferFromBufferedImage(BufferedImage inBufferedImage) {
        return ((DataBufferInt)inBufferedImage.getRaster().getDataBuffer()).getData();
    }

    public static BufferedImage getCombinedRGBAndAlphaImage(String inRGBFileName,String inAlphaFileName) {
        try {
            System.out.println("LOADING ALPHA CHANNEL IMAGE ... "+inAlphaFileName);
            BufferedImage tAlphaImage = AlphaChannelMerger.loadARGBImage(inAlphaFileName,new BufferedInputStream(new FileInputStream(inAlphaFileName)));
            System.out.println("LOADING RGB CHANNEL IMAGE ... "+inRGBFileName);
            BufferedImage tRGBImage = AlphaChannelMerger.loadARGBImage(inRGBFileName,new BufferedInputStream(new FileInputStream(inRGBFileName)));
            int[] tRGBA_Alpha = AlphaChannelMerger.getARGBDataBufferFromBufferedImage(tAlphaImage);
            int[] tRGBA_RGB = AlphaChannelMerger.getARGBDataBufferFromBufferedImage(tRGBImage);
            System.out.println("MERGING IMAGES AS RGBA...");
            BufferedImage tComposedImage = new BufferedImage(tRGBImage.getWidth(),tRGBImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int[] tRGBA_RGBA = ((DataBufferInt)tComposedImage.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < tRGBImage.getHeight(); y++) {
                for (int x = 0; x < tRGBImage.getWidth(); x++) {
                    int r = tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x00FF0000;
                    int g = tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x0000FF00;
                    int b = tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x000000FF;
                    int a = tRGBA_Alpha[(y * tRGBImage.getWidth()) + x]<<8 & 0xFF000000;
                    tRGBA_RGBA[(y * tRGBImage.getWidth()) + x] = a+r+g+b;
                }
            }
            System.out.println("MERGING SUCCESSFULLY FINISHED ...");
            return tComposedImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage generateGreyscaleImageFromRGB(String inRGBFileName) {
        try {
            System.out.println("LOADING RGB CHANNEL IMAGE ... "+inRGBFileName);
            BufferedImage tRGBImage = AlphaChannelMerger.loadARGBImage(inRGBFileName,new BufferedInputStream(new FileInputStream(inRGBFileName)));
            int[] tRGBA_RGB = AlphaChannelMerger.getARGBDataBufferFromBufferedImage(tRGBImage);
            System.out.println("GENERATING GREYSCALE IMAGE ...");
            BufferedImage tGreyscaleImage = new BufferedImage(tRGBImage.getWidth(),tRGBImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int[] tRGBA_RGBA = ((DataBufferInt)tGreyscaleImage.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < tRGBImage.getHeight(); y++) {
                for (int x = 0; x < tRGBImage.getWidth(); x++) {
                    int r = (tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x00FF0000) >> 16;
                    int g = (tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x0000FF00) >> 8;
                    int b = (tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x000000FF);
                    int grey = (int)((float)r*0.299f+(float)g*0.587f+b*0.114f);
                    int rg = grey << 16;
                    int gg = grey << 8;
                    int bg = grey;
                    tRGBA_RGBA[(y * tRGBImage.getWidth()) + x] = 0xFF000000+rg+gg+bg;
                }
            }
            System.out.println("GREY CONVERSION SUCCESSFULLY FINISHED ...");
            return tGreyscaleImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    public static BufferedImage getCombinedRGBAndAlphaGenerateImage(String inRGBFileName) {
        try {
            System.out.println("LOADING RGB CHANNEL IMAGE ... "+inRGBFileName);
            BufferedImage tRGBImage = AlphaChannelMerger.loadARGBImage(inRGBFileName,new BufferedInputStream(new FileInputStream(inRGBFileName)));
            int[] tRGBA_RGB = AlphaChannelMerger.getARGBDataBufferFromBufferedImage(tRGBImage);
            System.out.println("GENERATING ALPHA FROM GREYSCALE AND MERGING IMAGES AS RGBA...");
            BufferedImage tComposedImage = new BufferedImage(tRGBImage.getWidth(),tRGBImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int[] tRGBA_RGBA = ((DataBufferInt)tComposedImage.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < tRGBImage.getHeight(); y++) {
                for (int x = 0; x < tRGBImage.getWidth(); x++) {
                    int r = tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x00FF0000;
                    int g = tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x0000FF00;
                    int b = tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x000000FF;
                    int ra = (tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x00FF0000) >> 16;
                    int ga = (tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x0000FF00) >> 8;
                    int ba = (tRGBA_RGB[(y * tRGBImage.getWidth()) + x] & 0x000000FF);
                    int generated_alpha = (int)((float)ra*0.299f+(float)ga*0.587f+ba*0.114f);
                    //System.out.println("R="+ra+" G="+ga+" B="+ba+" A="+generated_alpha);
                    int a = generated_alpha<<24 & 0xFF000000;
                    tRGBA_RGBA[(y * tRGBImage.getWidth()) + x] = a+r+g+b;
                }
            }
            System.out.println("MERGING SUCCESSFULLY FINISHED ...");
            return tComposedImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    */

    public static void main(String[] args) {
        System.out.println("INITIALIZING ALPHACHANNEL MERGER ...");
        if (args.length==2) {
            System.out.println("RGB+ALPHA PROVIDED ...");
            BufferedImage tCombinedImage = AlphaChannelMerger.getCombinedRGBAndAlphaImage(args[0],args[1]);
            AlphaChannelMerger.saveARGBImage(args[0]+"_ARGB.png",tCombinedImage);
        } else if (args.length==1) {
            BufferedImage tGreyscaleImage = AlphaChannelMerger.generateGreyscaleImageFromRGB(args[0]);
            AlphaChannelMerger.saveARGBImage(args[0]+"_GREY.png",tGreyscaleImage);
            System.out.println("ONLY RGB PROVIDED ... GENERATING ALPHA FROM GREY IMAGE ...");
            BufferedImage tCombinedImage = AlphaChannelMerger.getCombinedRGBAndAlphaImage(args[0],args[0]+"_GREY.png");
            AlphaChannelMerger.saveARGBImage(args[0]+"_ARGB.png",tCombinedImage);
        } else {
            System.out.println("INVALID NUMBER OF ARGUMENTS ...");
        }
        System.out.println("ALPHA MERGER FINISHED ...");
    }

}
