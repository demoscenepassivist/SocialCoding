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
 ** Used to display a realtime scope, spectrum analyser, or volume meter. Slightly adapted, 
 ** stripped down and modified ripoff from KJ-DSS project by Kristofer Fudalewski.
 ** Web: http://sirk.sytes.net - Original author email: sirk_sytes@hotmail.com 
 **
 **/

import java.awt.*;
import java.awt.image.*;
import java.text.*;
import java.util.List;
import java.util.ArrayList;
import javax.sound.sampled.*;
import framework.util.*;

public class BaseMusic_ScopeAndSpectrumAnalyzer implements BaseMusic_DigitalSignalProcessorInterface {

    public static final BandDistribution    BAND_DISTRIBUTION_LINEAR = new LinearBandDistribution();
    public static final BandDistribution    BAND_DISTRIBUTION_LOG    = new LogBandDistribution( 4, 20.0 );		
    public static final BandGain            BAND_GAIN_FLAT           = new FlatBandGain( 4.0f );
    public static final BandGain            BAND_GAIN_FREQUENCY      = new FrequencyBandGain( 4.0f );
    public static final int                 DISPLAY_MODE_SCOPE             = 0;
    public static final int                 DISPLAY_MODE_SPECTRUM_ANALYSER = 1;
    public static final int                 DISPLAY_MODE_VU_METER          = 2;
    public static final int                 DEFAULT_WIDTH  = 512;
    public static final int                 DEFAULT_HEIGHT = 256-64;
    public static final int                 DEFAULT_SCOPE_DETAIL_LEVEL = 1;
    public static final int                 DEFAULT_SPECTRUM_ANALYSER_BAND_COUNT        = 64;
    public static final BandDistribution    DEFAULT_SPECTRUM_ANALYSER_BAND_DISTRIBUTION = BAND_DISTRIBUTION_LOG;
    public static final BandGain            DEFAULT_SPECTRUM_ANALYSER_BAND_GAIN         = BAND_GAIN_FLAT;
    public static final float               DEFAULT_SPECTRUM_ANALYSER_DECAY             = 0.03f;
    public static final float               DEFAULT_SPECTRUM_ANALYSER_GAIN              = 1.0f;
    public static final Color               DEFAULT_BACKGROUND_COLOR = new Color( 0,   0,   128 );	
    public static final Color               DEFAULT_SCOPE_COLOR      = new Color( 255, 192, 0 );
    public static final float               DEFAULT_VU_METER_DECAY   = 0.02f;

    private static final Font SMALL_FONT = new Font( "fixed", Font.PLAIN, 9 );
    private BufferedImage mBufferedImage_RenderBuffer;
//---//private Color   scopeColor      = DEFAULT_SCOPE_COLOR; 
    private Color[] colorScaleTable = getDefaultColorScale();

    //scope ...
    private int     scopeDetailLevel = DEFAULT_SCOPE_DETAIL_LEVEL;

    //spectrum analyzer ...
    private float saColorScale;
    private int saFFTSampleSize;
    private float saFFTSampleRate;
    private float saDecay = DEFAULT_SPECTRUM_ANALYSER_DECAY;
    private float saGain = DEFAULT_SPECTRUM_ANALYSER_GAIN;
    private int mSpectrumAnalyzer_BandCount;
    private BandDistribution mSpectrumAnalizer_BandDistribution = DEFAULT_SPECTRUM_ANALYSER_BAND_DISTRIBUTION;
    private Band[] mSpectrumAnalyser_BandDistributionTable;
    private BandGain saBandGain = DEFAULT_SPECTRUM_ANALYSER_BAND_GAIN;
    private float[] sabgTable;
    private float saBandWidth;
    private boolean saShowFrequencies = true;
    private BaseMusic_FastFourierTransform mBaseMusic_FastFourierTransform; 
    private float[] mCurrentFFTData;

    //vu meter
    //private float[] oldVolume;
    //private float vuDecay = DEFAULT_VU_METER_DECAY; 
    //private float vuColorScale;   

    public BaseMusic_ScopeAndSpectrumAnalyzer() {
        initialize();
    }

    public float[] getFFTSpectrum() {
        return mCurrentFFTData;
    }

    public BufferedImage getScopeAndSpectrumAnalyzerVisualization() {
        return mBufferedImage_RenderBuffer;
    }

    //computes a color scale value for both the spectrum analyzers and volume meter bars.
    private void computeColorScale() {
        saColorScale = ( (float)colorScaleTable.length / ( DEFAULT_HEIGHT - 32 ) ) * 2.0f;
        //vuColorScale = ( (float)colorScaleTable.length / ( DEFAULT_WIDTH - 32 ) ) * 2.0f;
    }

    //Computes and stores a band distribution and gain tables for the spectrum analyzer. This is 
    //performed using the current band distribution and gain instances. 
    //See setSpectrumAnalyzerBandDistribution() or setSpectrumAnalyserBandGain() methods.
    private void computeBandTables() {	
        if (mSpectrumAnalyzer_BandCount > 0 && saFFTSampleSize > 0 & mBaseMusic_FastFourierTransform != null) {
            //create band table.
            mSpectrumAnalyser_BandDistributionTable = mSpectrumAnalizer_BandDistribution.create( mSpectrumAnalyzer_BandCount, mBaseMusic_FastFourierTransform, saFFTSampleRate );
            mSpectrumAnalyzer_BandCount   = mSpectrumAnalyser_BandDistributionTable.length;
            updateSpectrumAnalyserBandWidth();
            //resolve band descriptions.
            resolveBandDescriptions(mSpectrumAnalyser_BandDistributionTable);	
            //create gain table.
            sabgTable = saBandGain.create( mBaseMusic_FastFourierTransform, saFFTSampleRate );
        }
    }

    private void drawScope(Graphics pGrp, float[][] pChannels, float pFrrh) {
        drawScope(pGrp, channelMerge( pChannels ), pFrrh);
    }

    //Draws a scope of the audio data across the entire width and height of this component.
    private void drawScope(Graphics pGrp, float[] pSample, float pFrrh) {
        pGrp.setColor(Color.WHITE);
        int wLas = (int) (pSample[0] * (float) (DEFAULT_HEIGHT >> 1)) + (DEFAULT_HEIGHT >> 1);
        for (int a = scopeDetailLevel, c = 0; c < DEFAULT_WIDTH && a < pSample.length; a += scopeDetailLevel, c += scopeDetailLevel) {
            int wAs = (int) (pSample[a] * (float) (DEFAULT_HEIGHT >> 1)) + (DEFAULT_HEIGHT >> 1);
            pGrp.drawLine(c, wLas, c + scopeDetailLevel, wAs);
            wLas = wAs;
        }
    }

    private void drawSpectrumAnalyser(Graphics pGrp, float[][] pChannels,float pFrrh) {
        drawSpectrumAnalyser(pGrp, channelMerge(pChannels), pFrrh);
    }

    //Draws a spectrum analyzer across the entire width and height if this component.
    protected void drawSpectrumAnalyser(Graphics inGraphics, float[] pSample,float pFrrh) {
        float c = 16;
        float wSadfrr = (saDecay * pFrrh);
        int b, bd, i, li = 0, mi;
        float fs, m;
        int wBm = 1;
        //preparation used for rendering band frequencies.
        if (saShowFrequencies) {
            inGraphics.setFont(SMALL_FONT);
            wBm = Math.round(32.0f / saBandWidth);
            if (wBm == 0) {
                wBm = 1;
            }
        }
        //FFT processing ...
        float[] wFFT = mBaseMusic_FastFourierTransform.calculate(pSample);
        //group up available bands using band distribution table.
        for (bd = 0; bd < mSpectrumAnalyzer_BandCount; bd++) {
            //get band distribution entry.
            i = mSpectrumAnalyser_BandDistributionTable[bd].distribution;
            m = 0;
            mi = 0;
            //find loudest band in group. (Group is from 'li' to 'i')
            for (b = li; b < i; b++) {
                float lf = wFFT[b];
                if (lf > m) {
                    m = lf;
                    mi = b;
                }
            }
            li = i;
            //calculate gain using log, then static gain.
            fs = (m * sabgTable[mi]) * saGain;
            //limit over-saturation.
            if (fs > 1.0f) {
                fs = 1.0f;
            }
            //compute decay.
            if (fs >= (mCurrentFFTData[bd] - wSadfrr)) {
                mCurrentFFTData[bd] = fs;
            } else {
                mCurrentFFTData[bd] -= wSadfrr;
                if (mCurrentFFTData[bd] < 0) {
                    mCurrentFFTData[bd] = 0;
                }
                fs = mCurrentFFTData[bd];
            }
            //draw band
            drawSpectrumAnalyserBand(inGraphics, Math.round(c),
                    DEFAULT_HEIGHT - 16, Math.round(c + saBandWidth) - Math.round(c) - 1,
                    (int) (fs * (DEFAULT_HEIGHT - 32)),
                    mSpectrumAnalyser_BandDistributionTable[bd],
                    saShowFrequencies && (bd % wBm) == 0);
            c += saBandWidth;
        }
    }

    /*
    private void drawVolumeMeter(Graphics pGrp, float[][] pChannels,float pFrrh) {
    float[] wVolume = new float[pChannels.length];
        float wSadfrr = (vuDecay * pFrrh);
        int wHeight = (DEFAULT_HEIGHT - 32 - ((pChannels.length - 1) * 8)) / pChannels.length;
        int wY = 16;
        for (int a = 0; a < pChannels.length; a++) {
            for (int b = 0; b < pChannels[a].length; b++) {
                float wAmp = Math.abs(pChannels[a][b]);
                if (wAmp > wVolume[a]) {
                    wVolume[a] = wAmp;
                }
            }
            if (wVolume[a] >= (oldVolume[a] - wSadfrr)) {
                oldVolume[a] = wVolume[a];
            } else {
                oldVolume[a] -= wSadfrr;
                if (oldVolume[a] < 0) {
                    oldVolume[a] = 0;
                }
            }
            drawVolumeMeterBar(pGrp, 16, wY, (int) (oldVolume[a] * (float) (DEFAULT_WIDTH - 32)), wHeight);
            wY += wHeight + 8;
        }
    }
    */

    //Draws a single spectrum analyzer band on this component and the specified coordinates.
    private void drawSpectrumAnalyserBand(Graphics pGraphics, int pX, int pY, int pWidth, int pHeight, Band pBandInfo, boolean pRenderFrequency) {
        float c = 0;
        for (int a = pY; a >= pY - pHeight; a -= 2) {
            c += saColorScale;
            if (c < 256.0f) {
                pGraphics.setColor(colorScaleTable[(int) c]);
            }
            pGraphics.fillRect(pX, a, pWidth, 1);
        }
        if (pRenderFrequency) {
            pGraphics.setColor(Color.GREEN);
            int wSx = pX + ((pWidth - pGraphics.getFontMetrics().stringWidth(pBandInfo.description)) >> 1);
            pGraphics.drawLine(pX + (pWidth >> 1),pY + 2,pX + (pWidth >> 1),pY + (pGraphics.getFontMetrics().getHeight() - pGraphics.getFontMetrics().getAscent()));
            pGraphics.drawString(pBandInfo.description, wSx, pY + pGraphics.getFontMetrics().getHeight());
        }
    }

    /*
    // Draws a volume meter bar on this component at the specified coordinates.
    private void drawVolumeMeterBar(Graphics pGraphics, int pX, int pY, int pWidth, int pHeight) {
        float c = 0;
        for (int a = pX; a <= pX + pWidth; a += 2) {
            c += vuColorScale;
            if (c < 256.0f) {
                pGraphics.setColor(colorScaleTable[(int) c]);
            }
            pGraphics.fillRect(a, pY, 1, pHeight);
        }
    }
    */

    private BufferedImage getRenderBuffer() {
        if (mBufferedImage_RenderBuffer == null) {
            updateSpectrumAnalyserBandWidth();
            computeColorScale();
            mBufferedImage_RenderBuffer = TextureUtils.createARGBBufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        return mBufferedImage_RenderBuffer;
    }

    // Creates a color array containing the default color spread of the spectrum analyzer and vu meter.
    private Color[] getDefaultColorScale() {
        Color[] wColors = new Color[256];
        for (int a = 0; a < 128; a++) {
            wColors[a] = new Color(0, (a >> 1) + 192, 0);
        }
        for (int a = 0; a < 64; a++) {
            wColors[a + 128] = new Color(a << 2, 255, 0);
        }
        for (int a = 0; a < 64; a++) {
            wColors[a + 192] = new Color(255, 255 - (a << 2), 0);
        }
        return wColors;
    }

    private void initialize() {
        setSpectrumAnalyserBandCount(DEFAULT_SPECTRUM_ANALYSER_BAND_COUNT);
    }

    public void initialize(int pSampleSize, SourceDataLine pSourceDataLine) {
        setSpectrumAnalyserSampleSizeAndRate(pSampleSize, pSourceDataLine.getFormat().getSampleRate());
        //oldVolume = new float[pSourceDataLine.getFormat().getChannels()];
    }


    // entry point for synchronizer ...
    public void process(BaseMusic_DigitalSignalSynchronizer.Context pDssContext) {
        float[][] wChannels = pDssContext.getDataNormalized();
        Image wDb = getRenderBuffer();
        Graphics wGrp = wDb.getGraphics();
        wGrp.setColor(Color.BLACK);
        wGrp.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        //drawVolumeMeter( wGrp, wChannels, pDssContext.getFrameRatioHint() );
        drawScope(wGrp, wChannels, pDssContext.getFrameRatioHint());
        drawSpectrumAnalyser(wGrp, wChannels, pDssContext.getFrameRatioHint());
    }

    private void resolveBandDescriptions(Band[] pBandTable) {
        DecimalFormat wDf = new DecimalFormat("###.#");
        for (Band wBand : pBandTable) {
            if (wBand.frequency >= 1000.0f) {
                wBand.description = wDf.format(wBand.frequency / 1000.0f) + "k";
            } else {
                wBand.description = wDf.format(wBand.frequency);
            }
        }
    }

    //sets the numbers of bands rendered by the spectrum analyser.
    public void setSpectrumAnalyserBandCount(int pCount) {
        mSpectrumAnalyzer_BandCount = pCount;
        computeBandTables();
    }

    //Sets the FFT sample size and rate to be just for calculating the spectrum
    //analyser values.
    private synchronized void setSpectrumAnalyserSampleSizeAndRate(int pSize,float pRate) {
        saFFTSampleSize = pSize;
        saFFTSampleRate = pRate;
        mBaseMusic_FastFourierTransform = new BaseMusic_FastFourierTransform(saFFTSampleSize);
        mCurrentFFTData = new float[mSpectrumAnalyzer_BandCount];
        computeBandTables();
    }

    //Merges two audio channels into one.
    private float[] channelMerge(float[][] pChannels) {
        for (int a = 0; a < pChannels[0].length; a++) {
            float wMcd = 0;
            for (int b = 0; b < pChannels.length; b++) {
                wMcd += pChannels[b][a];
            }
            pChannels[0][a] = wMcd / (float) pChannels.length;
        }
        return pChannels[0];
    }

    private void updateSpectrumAnalyserBandWidth() {
        saBandWidth = (float)( DEFAULT_WIDTH - 32 ) / (float)mSpectrumAnalyzer_BandCount;
    }

//---

    /**
     * Interface for band distribution types. Band distribution refers to
     * combining band data into groups therefore reducing the number of visible
     * bands. For example, a traditional 10 band spectrum analyzer contains only
     * 10 visible frequency bands sampled from a potentially more than hundreds
     * or more frequency bands. In order to distribute the bands into only 10,
     * several different distributions can be typically be used such as log or
     * simply linear distribution.
     */
    public static interface BandDistribution {

        /**
         * @param pBandCount The desired number of visible bands.
         * @param pFFT The FFT instance used for the spectrum analyser.
         * @param pSampleRate The sample rate of the data to process.
         * 
         * @return A band distribution table.
         */
        Band[] create(int pBandCount, BaseMusic_FastFourierTransform pFFT,float pSampleRate);

    }

//---

    // Linear based band distribution class
    public static class LinearBandDistribution implements BandDistribution {
        public Band[] create(int pBandCount,BaseMusic_FastFourierTransform pFFT, float pFFTSampleRate) {
            //We actually only use half of the available data because the higher bands are not audible by humans.
            int wOss = pFFT.getOutputSampleSize();
            int r = (int) ((double) wOss / (double) pBandCount);
            //create a frequency table.
            float[] wFqt = pFFT.calculateFrequencyTable(pFFTSampleRate);
            float wLfq = 0.0f;
            Band[] wSabdTable = new Band[pBandCount];
            int wBand = 0;
            for (double a = r; a <= wOss && wBand < pBandCount; a += r) {
                //build band instance with distribution, frequency range, and gain info.
                wSabdTable[wBand] = new Band((int) a, wLfq, wFqt[(int) a - r]);
                wLfq = wFqt[(int) a - r];
                wBand++;
            }
            return wSabdTable;
        }
    }

//---

    // Log based band distribution class.
    public static class LogBandDistribution implements BandDistribution {

        private double lso;
        private int sso;

        /**
         * Create a log band distribution instance supplying a sub-sonic offset
         * and a log scale offset. The sub-sonic offset allows the first 'n'
         * most bands to be combined into the first band group, while the
         * remaining bands will follow the log distribution curve. The log scale
         * offset refers to at what point in the log scale to use for
         * distribution calculations. The lower the number, the few bands per
         * group for the first few band groups.
         * 
         * @param pSubSonicOffset Groups the first 'n' bands into the sub-sonic band group. (default: 5)
         * @param pLogScaleOffset Starting point on the log scale. (default: 20.0)
         */
        public LogBandDistribution(int pSubSonicOffset, double pLogScaleOffset) {
            sso = pSubSonicOffset;
            lso = pLogScaleOffset;
        }

        public Band[] create(int pBandCount, BaseMusic_FastFourierTransform pFFT, float pFFTSampleRate) {
            //check the output size from the FFT instance to build the band table.
            int wHss = pFFT.getOutputSampleSize() - sso;
            double o = Math.log(lso);
            double r = (double) (pBandCount - 1) / (Math.log(wHss + lso) - o);
            //create a frequency table.
            float[] wFqt = pFFT.calculateFrequencyTable(pFFTSampleRate);
            float wLfq = wFqt[sso];
            int wLcb = 1;
            List<Band> wBands = new ArrayList<Band>();
            //subsonic bands group.
            wBands.add(new Band(sso, 0, wLfq));
            //divid reset of bands using log.
            for (int b = 0; b < wHss; b++) {
                //calculate current band.
                double wCb = ((Math.log((double) b + lso) - o) * r) + 1.0;
                if (Math.round(wCb) != wLcb) {
                    wBands.add(new Band(b + sso, wLfq, wFqt[b + sso]));
                    wLfq = wFqt[b + sso];
                    wLcb = (int) Math.round(wCb);
                }
            }
            //fill in last entry if necessary.
            if (wBands.size() < pBandCount) {
                wBands.add(new Band((wHss - 1) + sso, wLfq, wFqt[(wHss - 1) + sso]));
            }
            return wBands.toArray(new Band[wBands.size()]);
        }
    }

//---	

    public static class Band {

        public int distribution;
        public float frequency;
        public float startFrequency;
        public float endFrequency;
        public String description;

        public Band(int pDistribution, float pStartFrequency, float pEndFrquency) {
            distribution = pDistribution;
            startFrequency = pStartFrequency;
            endFrequency = pEndFrquency;
            frequency = pStartFrequency + ((pEndFrquency - pStartFrequency) / 2.0f);
        }

    }

//---	

    /**
     * Interface for band gain types. Band gain refers to gain applied to each
     * band (or bin) of the frequency spectrum generated by FFT processing.
     * Typically lower frequencies generally have more power than high ones.
     * Therefore a graphical representation of a particular sequence of audio
     * will tend to show mostly lower fequency bands. The purpose of the
     * BandGain interface is to allow developers to control the level of gain
     * applied to each band to provide a most even looking spectrum output.
     * 
     * BainGain instances must return a table of gain values to apply to all
     * bands return from FFT processing. Table returned from the create method
     * must be no more or less than pFFT.getOutputSampleSize();
     * 
     */
    public static interface BandGain {

        /**
         * @param pFFT The FFT instance used for the spectrum analyser.
         * @param pSampleRate The sample rate of the data to process.
         * 
         * @return A band gain table.
         */
        float[] create(BaseMusic_FastFourierTransform pFFT, float pSampleRate);

    }

//---

    //Flat band gain. No extra gain is applied to bands extra for the master gain.
    public static class FlatBandGain implements BandGain {

        private float gain;

        public FlatBandGain(float pGain) {
            gain = pGain;
        }

        public float[] create(BaseMusic_FastFourierTransform pFFT, float pFFTSampleRate) {
            int wOss = pFFT.getOutputSampleSize();
            float[] wSabgTable = new float[wOss];
            for (int i = 0; i < wOss; i++) {
                wSabgTable[i] = gain;
            }
            return wSabgTable;
        }

    }

//---

    //frequency based band gain. More gain is applied as the band frequency increases.
    public static class FrequencyBandGain implements BandGain {

        private float bias;

        public FrequencyBandGain(float pBias) {
            //A level of bias to flaten out the gain curve. The high the number the less gain is applied to high frequencies.
            bias = pBias;
        }

        public float[] create(BaseMusic_FastFourierTransform pFFT, float pFFTSampleRate) {
            int wOss = pFFT.getOutputSampleSize();
            //create a frequency table.
            float[] wFqt = pFFT.calculateFrequencyTable(pFFTSampleRate);
            float[] wSabgTable = new float[wOss];
            for (int i = 0; i < wOss; i++) {
                wSabgTable[i] = (((wFqt[i] / bias) + 512.0f) / 512.0f) * (bias * 1.5f);
            }
            return wSabgTable;
        }

    }

}
