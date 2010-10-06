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
 ** Provides synchronization between a digital signal processor and speaker output. Slightly 
 ** adapted, stripped down and modified ripoff from KJ-DSS project by Kristofer Fudalewski.
 ** Web: http://sirk.sytes.net - Original author email: sirk_sytes@hotmail.com 
 **
 **/

import java.util.*;
import javax.sound.sampled.*;

public class BaseMusic_DigitalSignalSynchronizer {

    private static final int DEFAULT_OVERRUN_PROTECTION = 4096;
    private static final int DEFAULT_WRITE_CHUNK_SIZE   = 4096;

    private SourceDataLine mSourceDataLine;
    private int mSampleSize;
    private byte[] mAudioDataBuffer;
    private int mFramesPerSecond;
    private int mFrameRateRatioHintCalibration;
    private int mPosition;
    private Context mContext;
    private Normalizer mNormalizer;
    private Synchronizer mSynchronizer;
    private ArrayList<BaseMusic_DigitalSignalProcessorInterface> mRegistered_DigitalSignalProcessors = new ArrayList<BaseMusic_DigitalSignalProcessorInterface>();
    private boolean	sourceDataLineWrite = true;

    public BaseMusic_DigitalSignalSynchronizer(int inFramesPerSecond) {
        this( inFramesPerSecond, inFramesPerSecond );
    }

    public BaseMusic_DigitalSignalSynchronizer(int inFramesPerSecond, int inFrameRateRatioHintCalibration) { 
        mFramesPerSecond = inFramesPerSecond;
        mFrameRateRatioHintCalibration = inFrameRateRatioHintCalibration;
    }

    public Synchronizer getInternalSynchronizer() {
        return mSynchronizer;
    }

    public void add(BaseMusic_DigitalSignalProcessorInterface inSignalProcessor) {
        if (mSynchronizer!=null) {
            inSignalProcessor.initialize(mSampleSize, mSourceDataLine);
        }
        mRegistered_DigitalSignalProcessors.add(inSignalProcessor);
    }

    public Normalizer getNormalizer() {
        if (mNormalizer == null) {
            if (mNormalizer == null && mSynchronizer != null) {
                mNormalizer = new Normalizer(mSourceDataLine.getFormat());
            }
        }
        return mNormalizer;
    }

    public void remove( BaseMusic_DigitalSignalProcessorInterface pSignalProcessor ) {
        mRegistered_DigitalSignalProcessors.remove( pSignalProcessor );
    }

    //start monitoring the specified SourceDataLine ...
    public void start(SourceDataLine inSourceDataLine) {
        if (mSynchronizer == null) {
            mSourceDataLine = inSourceDataLine;
            mSampleSize = (int) (Math.round(mSourceDataLine.getFormat().getFrameRate()/(float)mFramesPerSecond));
            mContext = new Context(mSampleSize);
            mAudioDataBuffer = new byte[inSourceDataLine.getBufferSize()+DEFAULT_OVERRUN_PROTECTION];
            mPosition = 0;
            mNormalizer = null;
            for (BaseMusic_DigitalSignalProcessorInterface wDsp : mRegistered_DigitalSignalProcessors) {
                wDsp.initialize(mSampleSize, inSourceDataLine);
            }
            mSynchronizer = new Synchronizer(mFramesPerSecond,mFrameRateRatioHintCalibration);		
        }
    }

    protected void storeAudioData( byte[] pAudioData, int pOffset, int pLength ) {
        if (mAudioDataBuffer == null) {
            return;
        }
        int wOverrun = 0;
        if (mPosition + pLength > mAudioDataBuffer.length - 1) {
            wOverrun = (mPosition + pLength) - mAudioDataBuffer.length;
            pLength = mAudioDataBuffer.length - mPosition;
        }
        System.arraycopy(pAudioData, pOffset, mAudioDataBuffer, mPosition,pLength);
        if (wOverrun > 0) {
            System.arraycopy(pAudioData, pOffset + pLength, mAudioDataBuffer,0, wOverrun);
            mPosition = wOverrun;
        } else {
            mPosition += pLength;
        }
    }

    //writes the entire specified buffer to the monitored source data line an any registered DSPs.
    public void writeAudioData(byte[] pAudioData) {
        writeAudioData(pAudioData, 0, pAudioData.length);
    }

    //writes part of specified buffer to the monitored source data line an any registered DSPs.
    public void writeAudioData(byte[] pAudioData, int pOffset, int pLength) {
        if (sourceDataLineWrite) {
            writeChunkedAudioData(pAudioData, pOffset, pLength);
        } else {
            storeAudioData(pAudioData, pOffset, pLength);
        }
    }

    //writes part of specified buffer to the monitored source data line an any registered DSPs.
    protected void writeChunkedAudioData( byte[] pAudioData, int pOffset, int pLength ) {
        if (mAudioDataBuffer == null) {
            return;
        }
        int wWl;
        for (int o = pOffset; o < pOffset + pLength; o += DEFAULT_WRITE_CHUNK_SIZE) {
            wWl = DEFAULT_WRITE_CHUNK_SIZE;
            if (o + wWl >= pLength) {
                wWl = pLength - o;
            }
            mSourceDataLine.write(pAudioData, o, wWl);
            storeAudioData(pAudioData, o, wWl);
        }
    }

//---

    public class Context {

        private int    mBufferOffset;
        private int    mSampleLength;
        private float  mFrameRatioHint;

        //create a DSS context with a fixed sample length.
        public Context( int pLength ) {
            mSampleLength = pLength;
        }

        //returns the data buffer of this DSS.
        public byte[] getDataBuffer() {
            return mAudioDataBuffer;
        }

        //returns a normalized sample from the DSS data buffer.
        public float[][] getDataNormalized() {
            return getNormalizer().normalize( mAudioDataBuffer, mBufferOffset, mSampleLength );
        }

        public float getFrameRatioHint() {
            return mFrameRatioHint;
        }

        //Returns the sample length to read from the data buffer.
        public int getLength() {
            return mSampleLength;
        }

        //Returns the data buffer offset to start reading from. Please note that the offset + length 
        //can be beyond the buffere length. This simply means, the rest of data sample has rolled over
        //to the beginning of the data buffer. See the Normalizer inner class for an example. 
        public int getOffset() {
            return mBufferOffset;
        }

        //returns the monitored source data line.
        public SourceDataLine getSourceDataLine() {
            return mSourceDataLine;
        }

    }

//---

    public class Normalizer {

        private AudioFormat audioFormat;		
        private float[][] channels;
        private int  channelSize;
        private long audioSampleSize;

        public Normalizer(AudioFormat pFormat) {
            audioFormat = pFormat;
            channels = new float[pFormat.getChannels()][];
            for (int c = 0; c < pFormat.getChannels(); c++) {
                channels[c] = new float[mSampleSize];
            }
            channelSize = audioFormat.getFrameSize() / audioFormat.getChannels();
            audioSampleSize = (1 << (audioFormat.getSampleSizeInBits() - 1));
        }

        public float[][] normalize( byte[] pData, int pPosition, int pLength ) {
            int wChannels  = audioFormat.getChannels();
            int wSsib      = audioFormat.getSampleSizeInBits();
            int wFrameSize = audioFormat.getFrameSize();
            //loop through audio data.
            for( int sp = 0; sp < mSampleSize; sp++ ) { 
                if ( pPosition >= pData.length ) {
                    pPosition = 0;
                }
                int cdp = 0;
                //loop through channels.
                for( int ch = 0; ch < wChannels; ch++ ) {
                    //sign least significant byte. (PCM_SIGNED)
                    long sm = ( pData[ pPosition + cdp ] & 0xFF ) - 128;
                    for( int bt = 8, bp = 1; bt < wSsib; bt += 8 ) {
                        sm += pData[ pPosition + cdp + bp ] << bt;
                        bp++;
                    }
                    //store normalized data.
                    channels[ ch ][ sp ] = (float)sm / audioSampleSize;
                    cdp += channelSize;
                }
                pPosition += wFrameSize;
            }
            return channels;
        }
    }

//---

    public class Synchronizer {

        private int mFrameSize;
        private long mCurrentFramesPerSecondInNanoSeconds;
        private long mFramesPerSecondInNanoSeconds;
        private long mFrameRateRatioHintCalibrationInNanoSeconds;

        public Synchronizer(int inFramesPerSecond, int inFrameRateRatioHintCalibration) {
            mFramesPerSecondInNanoSeconds = 1000000000L /(long)inFramesPerSecond;
            mCurrentFramesPerSecondInNanoSeconds = mFramesPerSecondInNanoSeconds;	
            mFrameRateRatioHintCalibrationInNanoSeconds = 1000000000L / inFrameRateRatioHintCalibration;
            mFrameSize = mSourceDataLine.getFormat().getFrameSize();
        }

        private int calculateSamplePosition() {
            return (int) ((long) (mSourceDataLine.getLongFramePosition() * mFrameSize) % (long) (mAudioDataBuffer.length));
        }

        public void synchronize() {
            mContext.mBufferOffset = calculateSamplePosition();
            //Calculate the frame rate ratio hint. This value can be used by
            //animated DSP's to fast forward animation frames to make up for
            //inconsistencies with the frame rate.
            mContext.mFrameRatioHint = (float) mCurrentFramesPerSecondInNanoSeconds / (float) mFrameRateRatioHintCalibrationInNanoSeconds;
            //dispatch sample data to digtal signal processors.
            for (int a = 0; a < mRegistered_DigitalSignalProcessors.size(); a++) {
                ((BaseMusic_DigitalSignalProcessorInterface) mRegistered_DigitalSignalProcessors.get(a)).process(mContext);
            }
        }

    }
}
