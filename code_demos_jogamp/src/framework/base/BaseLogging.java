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
 ** Provides simple logging to the commandline (system.out) or textfile as singleton instance.
 ** Also provides functionality to write a screenshot (BufferedImage) as PNG to a sequentially
 ** numbered imagefile.
 **
 **/

import java.text.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public final class BaseLogging {

    //debug constants to emulate preprocessor style ifndef functionality ... 8*)
    private static final boolean DEBUG = true;
    private static final boolean LOGGINGOUTPUT_FILE = false;
    private static final boolean LOGGINGOUTPUT_CONSOLE = true;

    private static final String cLOGGING_OUTPUTFILENAME_PREFIX = "JOGAMP_FILELOG";
    private static final String cLOGGING_OUTPUTFILENAME_SUFFIX = ".txt";
    public static final String cLOGGING_CAPTUREOUTPUTFILENAME_PREFIX = "JOGAMP_SCREENCAPTURE";
    public static final String cLOGGING_CAPTUREOUTPUTFILENAME_SUFFIX = ".bmp";
    public static final String cLOGGING_CAPTUREOUTPUTDIRECTORYNAME = "capture\\";
    
    private static BaseLogging mLoggingInstance = null;
    private File mLogFile = null;
    private PrintWriter mPrintWriter;
    private SimpleDateFormat mTimeStampFormatter = new SimpleDateFormat("[HH:mm:ss:SSS]",Locale.US);
    private SimpleDateFormat mFileTimeStampFormatter = new SimpleDateFormat("[HH-mm-ss-SSS]",Locale.US);

    private BaseLogging() {
        try {
            System.out.println("INITIALIZING BASELOGGING ...");
            System.out.println("");
            System.out.println("   __ __|_  ___________________________________________________________________________  ___|__ __    ");
            System.out.println("  //    /\\                                           _                                  /\\    \\\\  ");
            System.out.println(" //____/  \\__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \\____\\\\ ");
            System.out.println("  \\    \\  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\\ \\  /    /  ");
            System.out.println("   \\____\\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   \"  \\_\\/____/  ");
            System.out.println("  /\\    \\     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\\      ");
            System.out.println(" /  \\____\\                       http://jogamp.org  |_|                              /____/  \\     ");
            System.out.println(" \\  /   \"' _________________________________________________________________________ `\"   \\  /    ");
            System.out.println("  \\/____.                                                                             .____\\/       ");
            System.out.println("");
            if (LOGGINGOUTPUT_FILE) {
                System.out.println("BASELOGGING TO FILE ENABLED ... INITIALIZING ...");
                mLogFile = new File(cLOGGING_OUTPUTFILENAME_PREFIX+"_"+mFileTimeStampFormatter.format(Calendar.getInstance().getTime())+cLOGGING_OUTPUTFILENAME_SUFFIX);
                FileWriter tFileWriter = new FileWriter(mLogFile);
                //Currently disable buffered writer ... X-)
                //BufferedWriter tBufferedWriter = new BufferedWriter(tFileWriter,4096);
                //mPrintWriter = new PrintWriter(tBufferedWriter);
                mPrintWriter = new PrintWriter(tFileWriter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized BaseLogging getInstance() {
        if (mLoggingInstance==null) {
            mLoggingInstance=new BaseLogging();
        } 
        return mLoggingInstance;
    }

    public synchronized void info(String inMessage) {
        if (BaseLogging.DEBUG) { 
            logMessage(inMessage,"INFO "); 
        }
    }

    public synchronized void warning(String inMessage) {
        if (BaseLogging.DEBUG) {
            logMessage(inMessage,"WARN ");
        }
    }

    public synchronized void error(String inMessage) {
        if (BaseLogging.DEBUG) {
            logMessage(inMessage,"ERROR");
        }
    }

    public synchronized void fatalerror(Throwable inThrowable) {
        //fatal error logging isn't controlled by the DEBUG flag ...
        exception(inThrowable);
        //automatically kill application when a fatal error occurs ...
        System.exit(-1);
    }

    public synchronized void fatalerror(String inMessage) {
        //fatal error logging isn't controlled by the DEBUG flag ...
        logMessage(inMessage,"FATAL");
        //automatically kill application when a fatal error occurs ...
        System.exit(-1);
    }

    public synchronized void exception(Throwable inThrowable) {
        //exception logging isn't controlled by the DEBUG flag ...
        inThrowable.fillInStackTrace();
        logMessage(inThrowable.toString(),"EXCEP");
        StackTraceElement[] tStackTraceElements = inThrowable.getStackTrace();
        int counter = 0;
        for (int i = 0; i < tStackTraceElements.length; i++) {
            StackTraceElement tStackTraceElement = tStackTraceElements[i];
            counter++;
            StringBuilder tStringBuilder = new StringBuilder();
            NumberFormat tNumberFormat = new DecimalFormat("00");
            tStringBuilder.append(tNumberFormat.format(counter));
            tStringBuilder.append(" ");
            tStringBuilder.append(tStackTraceElement.toString());
            logMessage(tStringBuilder.toString(),"EXCEP");
        }
        mPrintWriter.flush();
    }

    private void logMessage(String inMessage,String inMessageType) {
        StringBuilder tStringBuilder = new StringBuilder();
        tStringBuilder.append("[");
        tStringBuilder.append(inMessageType);
        tStringBuilder.append("]");
        tStringBuilder.append("[");
        tStringBuilder.append(mTimeStampFormatter.format(Calendar.getInstance().getTime()));
        tStringBuilder.append("]");
        tStringBuilder.append("[");
        tStringBuilder.append(inMessage);
        tStringBuilder.append("]");
        dumpMessage(tStringBuilder.toString());
    }

    private void dumpMessage(String inMessage) {
        if (LOGGINGOUTPUT_CONSOLE) {
            //dump to system.out
            System.out.println(inMessage);
        }
        if (LOGGINGOUTPUT_FILE) {
            //dump to file ...
            mPrintWriter.println(inMessage);
            mPrintWriter.flush();
        }
    }

    public synchronized void logCapture(BufferedImage inBufferedImage,int inFrameNumber) {
        try {
            DecimalFormat tDecimalFormatter = new DecimalFormat("000000");
            String tFileName = cLOGGING_CAPTUREOUTPUTDIRECTORYNAME+cLOGGING_CAPTUREOUTPUTFILENAME_PREFIX+"_"+tDecimalFormatter.format(inFrameNumber)+cLOGGING_CAPTUREOUTPUTFILENAME_SUFFIX;
            File tScreenCaptureImageFile = new File(tFileName);
            this.info("WRITING SCREENCAPTURE FOR FRAME NUMBER "+inFrameNumber+" TO FILE "+tFileName);
            ImageIO.write(inBufferedImage, "bmp", tScreenCaptureImageFile);
            //ImageIO.write(inBufferedImage, "png", tScreenCaptureImageFile);
        } catch (Exception e) {
            this.fatalerror(e);
        }
    }

    public synchronized void logCapture(BufferedImage inBufferedImage,int inFrameNumber, String inCustomPrefix) {
        try {
            DecimalFormat tDecimalFormatter = new DecimalFormat("000000");
            String tFileName = cLOGGING_CAPTUREOUTPUTDIRECTORYNAME+cLOGGING_CAPTUREOUTPUTFILENAME_PREFIX+"_"+inCustomPrefix+"_"+tDecimalFormatter.format(inFrameNumber)+cLOGGING_CAPTUREOUTPUTFILENAME_SUFFIX;
            File tScreenCaptureImageFile = new File(tFileName);
            this.info("WRITING SCREENCAPTURE FOR FRAME NUMBER "+inFrameNumber+" TO FILE "+tFileName);
            ImageIO.write(inBufferedImage, "bmp", tScreenCaptureImageFile);
            //ImageIO.write(inBufferedImage, "png", tScreenCaptureImageFile);
        } catch (Exception e) {
            this.fatalerror(e);
        }
    }
    
}
