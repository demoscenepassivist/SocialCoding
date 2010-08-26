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
 ** Utility methods precalculating various standard functions to be used as offset-tables.
 ** Functions provided are:
 ** 
 **	 -triangle wave	http://mathworld.wolfram.com/TriangleWave.html 
 **                 http://en.wikipedia.org/wiki/Triangle_wave
 **	 -sawtooth 	 	http://en.wikipedia.org/wiki/Sawtooth
 **	 -square wave 	http://en.wikipedia.org/wiki/Square_wave
 **
 ** Also multiplication, division, addition and subtraction methods for offset-tables are provided.
 **
 **/

import framework.base.*;

public class OffsetTableUtils {

    //on java and trigonometric functions ... -=#:-)
    //http://blogs.sun.com/jag/entry/transcendental_meditation
    //http://blogs.sun.com/jag/entry/transcendental_two

    public enum TRIGONOMETRIC_FUNCTION { SIN,COS }
    public enum TABLECOMBINE_OPERATION { ADD,SUB,MUL,DIV }

    public static double[] cosaque_DoublePrecision(int inNumberOfElements,double inMaxHeight,boolean inNonNegative,TRIGONOMETRIC_FUNCTION inTrigonometricFunction) {
        BaseLogging.getInstance().info("COSAQUE GENERATING SIN/COS-TABLE (DOUBLE PRECISION)... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double tNonNegativeOffset;
        if (inNonNegative) {
            tNonNegativeOffset = inMaxHeight;
        } else {
            tNonNegativeOffset = 0;
        }
        double tIteratorIncrease = 360.0d/(double)inNumberOfElements;	
        BaseLogging.getInstance().info("COSAQUE ITERATOR INCREASE="+tIteratorIncrease);
        double[] tSinCosTable = new double[inNumberOfElements];
        double tDegrees = 0;
        for (int i=0; i<inNumberOfElements; i++) {
            switch (inTrigonometricFunction) {
                case SIN: tSinCosTable[i]= ((Math.sin(Math.toRadians(tDegrees))*inMaxHeight)+tNonNegativeOffset)/2.0d;
                break;
                case COS: tSinCosTable[i]= ((Math.cos(Math.toRadians(tDegrees))*inMaxHeight)+tNonNegativeOffset)/2.0d;
                break;
            }
            tDegrees+=tIteratorIncrease;
        }
        BaseLogging.getInstance().info("FINISHED SIN/COS-TABLE GENERATION ...");
        return tSinCosTable;
    }

    public static float[] cosaque_SinglePrecision(int inNumberOfElements,float inMaxHeight,boolean inNonNegative,TRIGONOMETRIC_FUNCTION inTrigonometricFunction) {
        BaseLogging.getInstance().info("COSAQUE GENERATING SIN/COS-TABLE (SINGLE PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionSinCosTable = cosaque_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative,inTrigonometricFunction);
        float[] tSinglePrecisionSinCosTable = new float[tDoublePrecisionSinCosTable.length];
        for (int i=0; i<tDoublePrecisionSinCosTable.length; i++) {
            tSinglePrecisionSinCosTable[i] = (float)tDoublePrecisionSinCosTable[i];
        }
        return tSinglePrecisionSinCosTable;
    }

    public static int[] cosaque_IntegerPrecision(int inNumberOfElements,int inMaxHeight,boolean inNonNegative,TRIGONOMETRIC_FUNCTION inTrigonometricFunction) {
        BaseLogging.getInstance().info("COSAQUE GENERATING SIN/COS-TABLE (INTEGER PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionSinCosTable = cosaque_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative,inTrigonometricFunction);
        int[] tIntegerPrecisionSinCosTable = new int[tDoublePrecisionSinCosTable.length];
        for (int i=0; i<tIntegerPrecisionSinCosTable.length; i++) {
            tIntegerPrecisionSinCosTable[i] = (int)Math.round(tDoublePrecisionSinCosTable[i]);
        }
        return tIntegerPrecisionSinCosTable;
    }

    public static int[] cosaque_FixedPoint_SinglePrecision(int inNumberOfElements,int inMaxHeight,boolean inNonNegative,int inPrecisionInDigits,TRIGONOMETRIC_FUNCTION inTrigonometricFunction) {
        BaseLogging.getInstance().info("COSAQUE GENERATING SIN/COS-TABLE (FIXED POINT MATH SINGLE PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionSinCosTable = cosaque_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative,inTrigonometricFunction);
        int[] tFixedPointMathSinCosTable = new int[tDoublePrecisionSinCosTable.length];
        int tPrecisionMultiplier = getFixePointPrecisionMultiplierDivider(inPrecisionInDigits);
        for (int i=0; i<tDoublePrecisionSinCosTable.length; i++) {
            tFixedPointMathSinCosTable[i] = (int)Math.round(tDoublePrecisionSinCosTable[i]*tPrecisionMultiplier);
            //BaseLogging.getInstance().info("tFixedPointMathSinTable["+i+"]="+tFixedPointMathSinTable[i]);
        }
        return tFixedPointMathSinCosTable;
    }

    public static long[] cosaque_FixedPoint_DoublePrecision(int inNumberOfElements,int inMaxHeight,boolean inNonNegative,int inPrecisionInDigits,TRIGONOMETRIC_FUNCTION inTrigonometricFunction) {
        BaseLogging.getInstance().info("COSAQUE GENERATING SIN/COS-TABLE (FIXED POINT MATH DOUBLE PRECSION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionSinCosTable = cosaque_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative,inTrigonometricFunction);
        long[] tFixedPointMathSinCosTable = new long[tDoublePrecisionSinCosTable.length];
        int tPrecisionMultiplier = getFixePointPrecisionMultiplierDivider(inPrecisionInDigits);
        for (int i=0; i<tDoublePrecisionSinCosTable.length; i++) {
            tFixedPointMathSinCosTable[i] = (long)Math.round(tDoublePrecisionSinCosTable[i]*tPrecisionMultiplier);
        }
        return tFixedPointMathSinCosTable;
    }

    public static int getFixePointPrecisionMultiplierDivider(int inPrecisionInDigits) {
        int tPrecisionMultiplier = 1;
        for (int i=0; i<inPrecisionInDigits; i++) {
            tPrecisionMultiplier = tPrecisionMultiplier*10;	
        }
        return tPrecisionMultiplier;
    }

    public static int[] triangleWave_IntegerPrecision(int inNumberOfElements,int inMaxHeight,boolean inNonNegative) {
        BaseLogging.getInstance().info("GENERATING TRIANGLEWAVE-TABLE (INTEGER PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionTriangleWaveTable = triangleWave_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative);
        int[] tIntegerPrecisionTriangleWaveTable = new int[tDoublePrecisionTriangleWaveTable.length];
        for (int i=0; i<tIntegerPrecisionTriangleWaveTable.length; i++) {
            tIntegerPrecisionTriangleWaveTable[i] = (int)Math.round(tDoublePrecisionTriangleWaveTable[i]);
        }
        return tIntegerPrecisionTriangleWaveTable;
    }

    public static double[] triangleWave_DoublePrecision(int inNumberOfElements,double inMaxHeight,boolean inNonNegative) {
        BaseLogging.getInstance().info("GENERATING TRIANGLEWAVE-TABLE (DOUBLE PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double tIterationIncrement = (inMaxHeight*2)/(double)inNumberOfElements;
        double tIterationIncrementAccumulator;
        if (inNonNegative) {
            tIterationIncrementAccumulator = 0;
        } else {
            tIterationIncrementAccumulator = -1.0d*(inMaxHeight/2);
        }
        double[] tDoublePrecisionTriangleWaveTable = new double[inNumberOfElements];
        for (int i=0; i<inNumberOfElements; i++) {
            if (i<inNumberOfElements/2) {
                tIterationIncrementAccumulator += tIterationIncrement;
            } else {
                tIterationIncrementAccumulator -= tIterationIncrement;
            }
            tDoublePrecisionTriangleWaveTable[i] = tIterationIncrementAccumulator;
        }
        return tDoublePrecisionTriangleWaveTable;
    }

    public static int[] sawtoothWave_IntegerPrecision(int inNumberOfElements,int inMaxHeight,boolean inNonNegative) {
        BaseLogging.getInstance().info("GENERATING SAWTOOTHWAVE-TABLE (INTEGER PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionSawtoothWaveTable = sawtoothWave_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative);
        int[] tIntegerPrecisionSawtoothWaveTable = new int[tDoublePrecisionSawtoothWaveTable.length];
        for (int i=0; i<tIntegerPrecisionSawtoothWaveTable.length; i++) {
            tIntegerPrecisionSawtoothWaveTable[i] = (int)Math.round(tDoublePrecisionSawtoothWaveTable[i]);
        }
        return tIntegerPrecisionSawtoothWaveTable;
    }

    public static double[] sawtoothWave_DoublePrecision(int inNumberOfElements,double inMaxHeight, boolean inNonNegative) {
        BaseLogging.getInstance().info("GENERATING SAWTOOTHWAVE-TABLE (DOUBLE PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double tIterationIncrement = inMaxHeight/(double)inNumberOfElements;
        double tIterationIncrementAccumulator;
        if (inNonNegative) {
            tIterationIncrementAccumulator = 0;
        } else {
            tIterationIncrementAccumulator = -1.0d*(inMaxHeight/2);
        }
        double[] tDoublePrecisionSawtoothWaveTable = new double[inNumberOfElements];
        for (int i=0; i<inNumberOfElements; i++) {
            tIterationIncrementAccumulator += tIterationIncrement;
            tDoublePrecisionSawtoothWaveTable[i] = tIterationIncrementAccumulator;
        }
        return tDoublePrecisionSawtoothWaveTable;
    }

    public static int[] squareWave_IntegerPrecision(int inNumberOfElements,int inMaxHeight,boolean inNonNegative) {
        BaseLogging.getInstance().info("GENERATING SQUAREWAVE-TABLE (INTEGER PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double[] tDoublePrecisionSquareWaveTable = squareWave_DoublePrecision(inNumberOfElements,inMaxHeight,inNonNegative);
        int[] tIntegerPrecisionSquareWaveTable = new int[tDoublePrecisionSquareWaveTable.length];
        for (int i=0; i<tIntegerPrecisionSquareWaveTable.length; i++) {
            tIntegerPrecisionSquareWaveTable[i] = (int)Math.round(tDoublePrecisionSquareWaveTable[i]);
        }
        return tIntegerPrecisionSquareWaveTable;
    }

    public static double[] squareWave_DoublePrecision(int inNumberOfElements,double inMaxHeight,boolean inNonNegative) {
        BaseLogging.getInstance().info("GENERATING SQUAREWAVE-TABLE (DOUBLE PRECISION) ... ELEMENTS="+inNumberOfElements+" MAXHEIGHT="+inMaxHeight+" NONNEGATIVE="+inNonNegative);
        double tMinHeight;
        double tMaxHeight;
        if (inNonNegative) {
            tMinHeight = 0;
            tMaxHeight = inMaxHeight;
        } else {
            tMinHeight = -1.0d*(inMaxHeight/2);
            tMaxHeight = inMaxHeight/2;
        }
        double[] tDoublePrecisionSquareWaveTable = new double[inNumberOfElements];
        for (int i=0; i<inNumberOfElements; i++) {
            if (i<inNumberOfElements/2) {
                tDoublePrecisionSquareWaveTable[i] = tMaxHeight;
            } else {
                tDoublePrecisionSquareWaveTable[i] = tMinHeight;
            }
        }
        return tDoublePrecisionSquareWaveTable;
    }

    public static double[] combineTable_DoublePrecision(double[] inTable1,double[] inTable2,TABLECOMBINE_OPERATION inOperation) {
        double[] tIntermediateTable = new double[inTable1.length];
        for (int i=0; i<inTable1.length; i++) {
            switch (inOperation) {
                case ADD: tIntermediateTable[i] = inTable1[i]+inTable2[i];
                break;
                case SUB: tIntermediateTable[i] = inTable1[i]-inTable2[i];
                break;
                case MUL: tIntermediateTable[i] = inTable1[i]*inTable2[i];
                break;
                case DIV: tIntermediateTable[i] = inTable1[i]/inTable2[i];
                break;
            }
        }
        return tIntermediateTable;
    }

    public static int[] combineTable_IntegerPrecision(int[] inTable1,int[] inTable2,TABLECOMBINE_OPERATION inOperation) {
        int[] tIntermediateTable = new int[inTable1.length];
        for (int i=0; i<inTable1.length; i++) {
            switch (inOperation) {
                case ADD: tIntermediateTable[i] = inTable1[i]+inTable2[i];
                break;
                case SUB: tIntermediateTable[i] = inTable1[i]-inTable2[i];
                break;
                case MUL: tIntermediateTable[i] = inTable1[i]*inTable2[i];
                break;
                case DIV: tIntermediateTable[i] = inTable1[i]/inTable2[i];
                break;
            }
        }
        return tIntermediateTable;
    }

    public static int[] addOffset_IntegerPrecision(int[] inTable,int inOffset) {
        int[] tIntermediateTable = new int[inTable.length];
        for (int i=0; i<inTable.length; i++) {
            tIntermediateTable[i] = inTable[i]+inOffset;
        }
        return tIntermediateTable;
    }

    public static double[] addOffset_DoublePrecision(double[] inTable,double inOffset) {
        double[] tIntermediateTable = new double[inTable.length];
        for (int i=0; i<inTable.length; i++) {
            tIntermediateTable[i] = inTable[i]+inOffset;
        }
        return tIntermediateTable;
    }

}
