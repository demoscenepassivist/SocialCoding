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
 ** Main class for the music subsystem of the framework. Provides an easy to use interface for
 ** asynchronous music playback. Given the 3rd party service provider libraries are supplied it
 ** is capable to playback "ogg vorbis" and "mpeg layer 3" music files. Other than simple 
 ** playback it internally uses the "KJ-DSS Project" by Kristofer Fudalewski (http://sirk.sytes.net)
 ** to provide a joined FFT spectrum via getFFTSpectrum() and a graphical scope and spectrum 
 ** analyzer via getScopeAndSpectrumAnalyzerVisualization(). The FFT spectrum can be utilized to
 ** get some easy synchronization of music an visuals.
 **
 **/

import java.io.*;
import javax.sound.sampled.*;
import java.awt.image.*;

public class BaseMusic {

    private String mFilename;
    private long mPosition;
    private boolean mOffline;
    private Thread mPlayerThread;
    private BaseMusic_ScopeAndSpectrumAnalyzer mBaseMusic_ScopeAndSpectrumAnalyzer;
    private BaseMusic_DigitalSignalSynchronizer mBaseMusic_DigitalSignalSynchronizer;
    private float[] mFFTSpectrum_Empty;

    public BaseMusic(String inFilename) {
        mFilename = inFilename;
        mPosition = -1;
        mOffline = true;
        mFFTSpectrum_Empty = new float[BaseMusic_ScopeAndSpectrumAnalyzer.DEFAULT_SPECTRUM_ANALYSER_BAND_COUNT];
    }

    public boolean isOffline() { return mOffline; }

    public void init() {
        if (mFilename !=null && mFilename != "") {
            try {
                mBaseMusic_DigitalSignalSynchronizer = new BaseMusic_DigitalSignalSynchronizer(BaseGlobalEnvironment.getInstance().getDesiredFramerate());
                mBaseMusic_ScopeAndSpectrumAnalyzer = new BaseMusic_ScopeAndSpectrumAnalyzer();
                mBaseMusic_DigitalSignalSynchronizer.add(mBaseMusic_ScopeAndSpectrumAnalyzer);
                BufferedInputStream tBufferedInputStream = new BufferedInputStream((new Object()).getClass().getResourceAsStream(mFilename));
                AudioInputStream tAudioInputStream = AudioSystem.getAudioInputStream(tBufferedInputStream);
                AudioFormat tAudioBaseFormat = tAudioInputStream.getFormat();
                final AudioFormat tAudioDecodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        tAudioBaseFormat.getSampleRate(),
                        16,
                        tAudioBaseFormat.getChannels(),
                        tAudioBaseFormat.getChannels() * 2,
                        tAudioBaseFormat.getSampleRate(),
                        false
                );
                final AudioInputStream tFinalAudioInputStream = AudioSystem.getAudioInputStream(tAudioDecodedFormat, tAudioInputStream);
                //prepare player thread ...
                mPlayerThread = new Thread() {
                    public void run() { 
                        try { 
                            byte[] tDataBuffer = new byte[4096];
                            SourceDataLine tLine = getLine(tAudioDecodedFormat);
                            //System.out.println("tLine.getBufferSize()="+tLine.getBufferSize());
                            if (tLine!=null) {
                                tLine.start();
                                mBaseMusic_DigitalSignalSynchronizer.start(tLine);
                                int tNumberOfBytesRead = 0;
                                while (tNumberOfBytesRead != -1) {
                                    tNumberOfBytesRead = tFinalAudioInputStream.read(tDataBuffer, 0, tDataBuffer.length);
                                    if (tNumberOfBytesRead != -1) {
                                        mBaseMusic_DigitalSignalSynchronizer.writeAudioData( tDataBuffer, 0, tNumberOfBytesRead );
                                    }
                                    mPosition = tLine.getMicrosecondPosition();
                                }
                                tLine.drain();
                                tLine.stop();
                                tLine.close();
                                tFinalAudioInputStream.close();
                            }
                        } catch (Exception e) { 
                            e.printStackTrace();
                        }
                        BaseLogging.getInstance().info("MUSIC STREAM FINISHED! AUTOCLOSE APPLICATION ...");
                        System.exit(0);
                    }
                };
                mPlayerThread.setPriority(Thread.MAX_PRIORITY);
                mOffline = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private SourceDataLine getLine(AudioFormat inAudioFormat) throws LineUnavailableException {
        SourceDataLine tSourceDataLine = null;
        DataLine.Info tDataLineInfo = new DataLine.Info(SourceDataLine.class,inAudioFormat,4096);
        tSourceDataLine = (SourceDataLine)AudioSystem.getLine(tDataLineInfo);
        tSourceDataLine.open(inAudioFormat);
        return tSourceDataLine;
    }

    public void synchonizeMusic() {
        if (!mOffline) {
            BaseMusic_DigitalSignalSynchronizer.Synchronizer tSynchronizer = mBaseMusic_DigitalSignalSynchronizer.getInternalSynchronizer();
            if (tSynchronizer!=null) {
                tSynchronizer.synchronize();
            }
        }
    }

    public void play() {
        if (!mOffline) {
            mPlayerThread.start();
        }
    }

    public void close() {
        if (!mOffline) {
            //Zzzz ...
        }
    }

    public int getPositionInMicroseconds() {
        return (int)mPosition;
    }

    public int getPositionInMilliseconds() {
        return getPositionInMicroseconds()/1000;
    }

    public float[] getFFTSpectrum() {
        if (!mOffline) {
            float[] tFFTSpectrum = mBaseMusic_ScopeAndSpectrumAnalyzer.getFFTSpectrum();
            if (tFFTSpectrum!=null) {
                return tFFTSpectrum;
            }
        }
        return mFFTSpectrum_Empty;
    }

    public BufferedImage getScopeAndSpectrumAnalyzerVisualization() {
        return mBaseMusic_ScopeAndSpectrumAnalyzer.getScopeAndSpectrumAnalyzerVisualization();
    }

}
