package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * Implements a real transform to correct scan line artifacts.
 * 
 * Points are shifted along the first dimension ("x") by an amount that varies
 * according to whether the points location in the second dimension ("y") is even or odd.
 * 
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 *
 */
public class ScanLineXCorrectionRealTransform implements InvertibleRealTransform
{
	protected final int ndims;

	protected final double shiftEven;
	protected final double shiftOdd;
	
	public ScanLineXCorrectionRealTransform( 
			final int ndims, 
			final double shiftEven, 
			final double shiftOdd )
	{
		this.ndims = ndims;
		this.shiftEven = shiftEven;
		this.shiftOdd = shiftOdd;
	}
	
	/**
	 * Returns a double representing the fraction of "even-ness" used for 
	 * interpolating this transform between non-integer lines.
	 * 
	 * Specifically, returns 1.0 if the input is an even integer, 0.0 if the input is 
	 * an odd integer, with values interpolated linearly for non integers.
	 * 
	 * Example:
	 * evenNess( 2.1 ) returns 0.9
	 * evenNess( 2.8 ) returns 0.2
	 * evenNess( 3.3 ) returns 0.3
	 * 
	 * @param x the input
	 * @return the evenness
	 */
	public static double evenNess( final double x )
	{
		return Math.abs( ( x % 2.0 ) - 1.0 ); 
	}
	
	/**
	 * 
	 * @param alpha alpha
	 * @param x x 
	 * @param y y 
	 * @return alpha*x + (1-alpha)*y
	 */
	public static double linearLinterpolate( final double alpha, final double x, final double y )
	{
		return (alpha * x) + ((1 - alpha) * y);
	}

	@Override
	public int numSourceDimensions()
	{
		return ndims;
	}

	@Override
	public int numTargetDimensions()
	{
		return ndims;
	}

	@Override
	public void apply( double[] source, double[] target )
	{
		double f = evenNess( source[ 1 ] );
		double offset = linearLinterpolate( f, shiftEven, shiftOdd );
		for ( int d = 0; d < ndims; d++ )
			if ( d == 0 )
				target[ d ] = source[ d ] + offset;
			else
				target[ d ] = source[ d ];
	}

	@Override
	public void apply( float[] source, float[] target )
	{
		double f = evenNess( source[ 1 ] );
		double offset = linearLinterpolate( f, shiftEven, shiftOdd );
		for ( int d = 0; d < ndims; d++ )
			if ( d == 0 )
				target[ d ] = (float) (source[ d ] + offset);
			else
				target[ d ] = source[ d ];
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		double f = evenNess( source.getDoublePosition( 1 ) );
		double offset = linearLinterpolate( f, shiftEven, shiftOdd );
		for ( int d = 0; d < ndims; d++ )
			if ( d == 0 )
				target.setPosition( source.getDoublePosition( d ) + offset, d );
			else
				target.setPosition( source.getDoublePosition( d ), d );
	}

	@Override
	public void applyInverse( double[] source, double[] target )
	{
		double f = evenNess( target[ 1 ] );
		double offset = linearLinterpolate( f, shiftEven, shiftOdd );
		for ( int d = 0; d < ndims; d++ )
			if ( d == 0 )
				source[ d ] = target[ d ] - offset;
			else
				source[ d ] = target[ d ];
	}

	@Override
	public void applyInverse( float[] source, float[] target )
	{
		double f = evenNess( target[ 1 ] );
		double offset = linearLinterpolate( f, shiftEven, shiftOdd );
		for ( int d = 0; d < ndims; d++ )
			if ( d == 0 )
				source[ d ] = (float) (target[ d ] - offset);
			else
				source[ d ] = target[ d ];
	}

	@Override
	public void applyInverse( RealPositionable source, RealLocalizable target )
	{
		double f = evenNess( target.getDoublePosition( 1 ) );
		double offset = linearLinterpolate( f, shiftEven, shiftOdd );
		for ( int d = 0; d < ndims; d++ )
			if ( d == 0 )
				source.setPosition( target.getDoublePosition( d ) - offset, d );
			else
				source.setPosition( target.getDoublePosition( d ), d );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new ScanLineXCorrectionRealTransform( ndims, -shiftEven, -shiftOdd );
	}

	@Override
	public InvertibleRealTransform copy()
	{
		// this class is immutable, so returning 'this' is safe
		return this;
	}

}
