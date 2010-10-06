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
 ** Fast Fourier Transformation class used for calculating the realtime spectrum analyzer.
 ** Slightly adapted, stripped down and modified ripoff from KJ-DSS project by Kristofer Fudalewski.
 ** Web: http://sirk.sytes.net - Original author email: sirk_sytes@hotmail.com 
 **
 **/

public class BaseMusic_FastFourierTransform {

    private float[] xre;
    private float[] xim;
    private float[] mag;
    private float[] fftSin;
    private float[] fftCos;
    private int[]   fftBr;
    private int ss, ss2, nu; 

    /**
     * @param pSampleSize The amount of the sample provided to the "calculate" method to use during
     *                    FFT calculations, this is used to prepare the calculation tables in advance.
     *                    This value is automatically rounded up to the nearest power of 2.
     */
    public BaseMusic_FastFourierTransform(int pSampleSize) {
        nu = (int)Math.ceil( Math.log( pSampleSize ) / Math.log( 2 ) );
        //calculate the nearest sample size to a power of 2
        ss = (int)Math.pow( 2, nu );
        ss2 = ss >> 1; 
        //allocate calculation buffers
        xre = new float[ ss ];
        xim = new float[ ss ];
        mag = new float[ ss2 ];
        //allocate FFT SIN/COS tables
        fftSin = new float[ nu * ss2 ];
        fftCos = new float[ nu * ss2 ];
        prepareTables();
    }

    //bit swapping method
    private int bitrev( int pJ, int pNu ) {
        int j1 = pJ;
        int j2;
        int k = 0;
        for( int i = 0; i < pNu; i++ ) {
            j2 = j1 >> 1;
            k  = ( k << 1 ) + j1 - ( j2 << 1 );
            j1 = j2;
        }
        return k;
    }

    /**
     * Converts sound data over time into pressure values. (FFT)
     * 
     * @param  pSample The sample to compute FFT values on.
     * @return         The results of the calculation, normalized between 0.0 and 1.0. 
     */
    public float[] calculate( float[] pSample ) {
        int n2 = ss2;
        //fill buffer
        for ( int a = 0; a < pSample.length; a++ ) {
            xre[ a ] = pSample[ a ];
            xim[ a ] = 0.0f;
        }
        //clear the remainder of the buffer
        for ( int a = pSample.length; a < ss; a++ ) {
            xre[ a ] = 0.0f;
            xim[ a ] = 0.0f;
        }
        float tr, ti, c, s;
        int   k, kn2, x = 0;
        for ( int l = 0; l < nu; l++ ) {
            k = 0;
            while ( k < ss ) {
                for ( int i = 0; i < n2; i++ ) {
                    //tabled sin/cos
                    c = fftCos[ x ]; 
                    s = fftSin[ x ]; 
                    kn2 = k + n2;
                    tr = xre[ kn2 ] * c + xim[ kn2 ] * s;
                    ti = xim[ kn2 ] * c - xre[ kn2 ] * s;
                    xre[ kn2 ] = xre[ k ] - tr;
                    xim[ kn2 ] = xim[ k ] - ti;
                    xre[ k ] += tr;
                    xim[ k ] += ti;
                    k++; 
                    x++;
                }
                k += n2;
            }
            n2 >>= 1; 
        }
        int r;
        //reorder output
        for( k = 0; k < ss; k++ ) {
            //use tabled BR values
            r = fftBr[ k ]; 
            if ( r > k ) {
                tr = xre[ k ];
                xre[ k ] = xre[ r ];
                xre[ r ] = tr;
                ti = xim[ k ];
                xim[ k ] = xim[ r ];
                xim[ r ] = ti;
            }
        }
        //calculate magnitude
        for ( int i = 0; i < ss2; i++ ) {
            mag[ i ] = Math.abs( ( (float)( Math.sqrt( ( xre[ i ] * xre[ i ] ) + ( xim[ i ] * xim[ i ] ) ) ) / ss ) );
        }
        return mag;
    }

    /**
     * Calculates a table of frequencies represented by the amplitude data returned by the 'calculate' method. 
     * Each element states the end of the frequency range of the corresponding FFT band (or bin). For example:
     * 
     * Range of band 0 =                 0.0 hz to frequencyTable[ 0 ] hz
     * Range of band 1 = frequencyTable[ 0 ] hz to frequencyTable[ 1 ] hz
     * Range of band 2 = frequencyTable[ 1 ] hz to frequencyTable[ 2 ] hz
     *   ... and so on.
     * 
     * Calculation uses the sample size rounded to the nearest power of 2 of the FFT instance and the sample rate parameter
     * to build this table.
     * 
     * @param  pSampleRate The sample rate used to calculate the frequency table. Usually the sample rate of the input
     *                     to the FFT calculate method.
     * @return             An array of frequency limits for each band. 
     */
    public float[] calculateFrequencyTable( float pSampleRate ) {
        float wFr = pSampleRate / 2.0f;
        //calculate band width. 
        float wBw = wFr / ss2;
        //store for frequency table
        float[] wFt = new float[ (int)ss2 ];
        //build band range table.
        int b = 0;
        for( float wFp = ( wBw / 2.0f ); wFp <= wFr; wFp += wBw ) {
            wFt[ b ] = wFp;
            b++;
        }
        return wFt;
    }

    /**
     * Returns the sample size this FFT instance uses for processing. It is automatically rounded to the nearest power of 2.
     * 
     * @return The sample size used by the calculate method. 
     */
    public int getInputSampleSize() {
        return ss;
    }

    /**
     * Returns the sample size this FFT instance returns after processing. It is automatically rounded to the nearest power of 2.
     * 
     * @return The sample size returned by the calculate method. 
     */
    public int getOutputSampleSize() {
        return ss2;
    }

    /**
     * Pre-calculates SIN/COS and bitrev tables in memory.
     */
    private void prepareTables() {
        int n2 = ss2;
        int nu1 = nu - 1;
        float p, arg;
        int   k = 0, x = 0;
        //prepare SIN/COS tables
        for ( int l = 0; l < nu; l++ ) {
            k = 0;
            while ( k < ss ) {
                for ( int i = 0; i < n2; i++ ) {
                    p = bitrev( k >> nu1, nu );
                    arg = 2 * (float)Math.PI * p / ss;
                    fftSin[ x ] = (float)Math.sin( arg );
                    fftCos[ x ] = (float)Math.cos( arg );
                    k++;
                    x++;
                }
                k += n2;
            }
            nu1--;
            n2 >>= 1;
        }
        //prepare bitrev table
        fftBr = new int[ ss ];
        for( k = 0; k < ss; k++ ) {
            fftBr[ k ] = bitrev( k, nu );
        }
    }
}
