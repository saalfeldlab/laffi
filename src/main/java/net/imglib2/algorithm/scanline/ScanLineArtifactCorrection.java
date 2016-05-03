package net.imglib2.algorithm.scanline;

import java.util.Arrays;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.ScanLineXCorrectionRealTransform;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Pair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


/**
 * Corrects scan line artifacts.
 * 
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 *
 */
public class ScanLineArtifactCorrection
{

	public static void main( String[] args )
	{

		String f = "/groups/flyfuncconn/flyfuncconn-public/forJohn/r3_ss2483lc10_frup65/20160301_r3_ss2483lc10_frup65_rnai_flyb_00002_regression/20160301_r3_ss2483lc10_frup65_rnai_flyb_00002_baseline.tif";
		String fout = "/groups/saalfeld/home/bogovicj/tmp/scanline/corrected_b_0002.tif";

		int maxOffset = 3;

		ImagePlus ip = IJ.openImage( f );
		Img<UnsignedShortType> img = ImageJFunctions.wrap( ip );

		RealRandomAccessible< UnsignedShortType > imgr = 
				Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< UnsignedShortType >() );

		int startOffset = -maxOffset;
		int endOffset   =  maxOffset;

		int numOffsets = endOffset - startOffset + 1;
		
		int i = 0;
		double[] ssdList = new double[ numOffsets ];
		double[] offsetList = new double[ numOffsets ];
		for( int offset = startOffset; offset <= endOffset; offset += 1 )
		{
			
			System.out.println( "offset: " + offset );
			double ssd = Double.MAX_VALUE;
			ScanLineXCorrectionRealTransform xfm = new ScanLineXCorrectionRealTransform( 3, offset, 0 );
			IntervalView< UnsignedShortType > rraCor = Views.interval( Views.raster( RealViews.transform( imgr, xfm )), img );
			ssd = measureSSDByLines( rraCor );

			offsetList[ i ] = offset;
			ssdList[ i++ ] = ssd;
		}

		double[] evenOddOffsets = estimateSubPixelOffsets( ssdList, offsetList );
		//System.out.println( "evenOddOffsets: " + evenOddOffsets[ 0 ] + " " + evenOddOffsets[ 1 ]  );
		
		ScanLineXCorrectionRealTransform xfm = new ScanLineXCorrectionRealTransform( 3, evenOddOffsets[0], evenOddOffsets[1] );
		IntervalView< UnsignedShortType > rraCor = Views.interval( Views.raster( RealViews.transform( imgr, xfm )), img );

		ImagePlus ipCorrected = ImageJFunctions.wrap( rraCor, "corrected" );
		IJ.save( ipCorrected, fout );
	}
	
	public static double[] estimateSubPixelOffsets( double[] ssds, double[] offsets )
	{
		// fit a quadratic to these data points and find the minimum
		int rows = ssds.length;
		DenseMatrix64F A = new DenseMatrix64F( rows, 3 );
		DenseMatrix64F b = new DenseMatrix64F( rows, 1 );
		DenseMatrix64F x = new DenseMatrix64F( 3, 1 );
		for( int r = 0; r < rows; r++ )
		{
			A.set( r, 0, 1.0 );
			A.set( r, 1, offsets[ r ] );
			A.set( r, 2, offsets[ r ] * offsets[ r ] );

			b.set( r, 0, ssds[ r ] );
		}
		CommonOps.solve( A, b, x );
		
		// now x stores our estimate of a quadratic: find the minimum
		double min = -x.get( 1 ) / ( 2 * x.get( 2 ));
		System.out.println( "best offset: " + min );
		double[] out = new double[]{ min/2, -min/2 };
		return out;
	}

	public static <T extends RealType<T>> double measureSSDByLines( RandomAccessibleInterval<T> rai )
	{
		int nd = rai.numDimensions();
		
		long[] steps = new long[ nd ];
		Arrays.fill( steps, 1 );
		steps[ 1 ] = 2;
		
		long[] min = new long[ nd ];
		long[] max = new long[ nd ];
		long[] maxt = new long[ nd ];
		for( int d = 0; d < nd; d++ )
		{
			if( d == 1 )
			{
				min[ d ] = 1;
				max[ d ] = rai.dimension( d ) - 2;
				maxt[ d ] = ( rai.dimension( d ) / 2) - 1;
			}
			else
			{
				min[ d ] = 0;
				max[ d ] = rai.dimension( d );
				maxt[ d ] = rai.dimension( d );
			}
		}

		FinalInterval subInterval = new FinalInterval( maxt );
		IntervalView< T > evenLines = Views.interval( Views.subsample( rai, steps ), subInterval );
		IntervalView< T > oddLines =  Views.interval( Views.subsample( Views.interval( rai, min, max ), steps ), subInterval );

		RandomAccessible< Pair< T, T >> pairra = Views.pair( evenLines, oddLines );

		// Compute the sum of squared differences across adjacent even and odd lines 
		double ssd = 0.0;
		// TODO using oddLines as interval may not be correct in all cases
		Cursor< Pair< T, T >> c = Views.interval( pairra, oddLines ).cursor();
		while( c.hasNext() )
		{
			Pair< T, T > p = c.next();
			double diff = p.getA().getRealDouble() - p.getB().getRealDouble();
			ssd += ( diff * diff );
		}

		return ssd;
	}
	
	/**
	 * For debugging purposes.  Prints all x to System.out for a given y and z.
	 * 
	 * @param rai the RandomAccessibleInterval
	 * @param y the fixed y
	 * @param z the fixed z
	 */
	public static  <T extends RealType<T>> void printLine( RandomAccessibleInterval<T> rai, int y, int z )
	{
		RandomAccess< T > ra = rai.randomAccess();
		ra.setPosition( y, 1 );
		ra.setPosition( z, 2 );
		for( int x = (int)rai.min( 0 ); x <= rai.max( 0 ); x++ )
		{
			ra.setPosition( x, 0 );
			System.out.println( ra.get().getRealDouble() );
		}
	}

}
