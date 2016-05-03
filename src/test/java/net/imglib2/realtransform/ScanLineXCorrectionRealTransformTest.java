package net.imglib2.realtransform;

import static org.junit.Assert.*;

import org.junit.Test;

public class ScanLineXCorrectionRealTransformTest
{

	@Test
	public void test()
	{
		ScanLineXCorrectionRealTransform xfm = new ScanLineXCorrectionRealTransform( 2, -1, 1 );

		double[] in  = new double[ 2 ];
		double[] res = new double[ 2 ];
		double[] resi = new double[ 2 ];
		
		// Test case for even line
		in[ 0 ] = 0.0;
		in[ 1 ] = 0.0;
		xfm.apply( in, res );
		xfm.applyInverse( resi, res );
		assertEquals( "move even line x",  -1.0, res[ 0 ], 0.001 );
		assertEquals( "move even line y ", in[ 1 ], res[ 1 ], 0.001 );
		assertEquals( "move even line inverse x",  in[ 0 ], resi[ 0 ], 0.001 );
		assertEquals( "move even line inverse y ", in[ 1 ], resi[ 1 ], 0.001 );
		
		// Test case for odd line
		in[ 1 ] = 1.0;
		xfm.apply( in, res );
		xfm.applyInverse( resi, res );
		assertEquals( "move odd line x",  1.0, res[ 0 ], 0.001 );
		assertEquals( "move odd line y ", in[ 1 ], res[ 1 ], 0.001 );
		assertEquals( "move odd line inverse x ",  in[ 0 ], resi[ 0 ], 0.001 );
		assertEquals( "move odd line inverse y ", in[ 1 ], resi[ 1 ], 0.001 );
		
		// Test case for intermediate lines		
		in[ 1 ] = 0.5;
		xfm.apply( in, res );
		xfm.applyInverse( resi, res );
		assertEquals( "move (0.5) line x",  0.0, res[ 0 ], 0.001 );
		assertEquals( "move odd line inverse x ",  in[ 0 ], resi[ 0 ], 0.001 );
		assertEquals( "move odd line inverse y ", in[ 1 ], resi[ 1 ], 0.001 );
		
		in[ 1 ] = 0.1;
		xfm.apply( in, res );
		xfm.applyInverse( resi, res );
		assertEquals( "move (0.1) line x",  -0.8, res[ 0 ], 0.001 );
		assertEquals( "move odd line inverse x ",  in[ 0 ], resi[ 0 ], 0.001 );
		assertEquals( "move odd line inverse y ", in[ 1 ], resi[ 1 ], 0.001 );
		
		in[ 1 ] = 0.9;
		xfm.apply( in, res );
		xfm.applyInverse( resi, res );
		assertEquals( "move (0.9) line x",  0.8, res[ 0 ], 0.001 );
		assertEquals( "move odd line inverse x ",  in[ 0 ], resi[ 0 ], 0.001 );
		assertEquals( "move odd line inverse y ", in[ 1 ], resi[ 1 ], 0.001 );

	}

}
