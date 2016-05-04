package fiji;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.scanline.ScanLineArtifactCorrection;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Plugin_ScanLineArtifactCorrection implements PlugIn
{

	public static void main( final String[] args ) throws InterruptedException
	{
		new ImageJ();

		ImagePlus ip = IJ.openImage( "/groups/flyfuncconn/flyfuncconn-public/forJohn/r3_ss2483lc10_frup65/20160301_r3_ss2483lc10_frup65_rnai_flya_00001_regression/20160301_r3_ss2483lc10_frup65_rnai_flya_00001_baseline.tif" );
		ip.show();

		new Plugin_ScanLineArtifactCorrection().run( null );
	}
	
	final static int DEFAULT_RADIUS = 3;
	@SuppressWarnings("unchecked")
	@Override
	public void run( String arg )
	{
		ImagePlus ip = IJ.getImage();
		
		if( ip.getType() == ImagePlus.COLOR_RGB )
		{
			IJ.error( "Scanline correction currently does not work on color images." );
			return;
		}
		
		final GenericDialog gd = new GenericDialog( "Scanline correction" );
		gd.addMessage( ip.getTitle() );
		gd.addNumericField( "Search radius", DEFAULT_RADIUS, 0 );
		//gd.addCheckbox( "Virtual?", false );
		gd.showDialog();
		if (gd.wasCanceled()) return;

		ScanLineArtifactCorrection alg = new ScanLineArtifactCorrection( (int)gd.getNextNumber() );
		
		ImagePlus result = null;
		switch( ip.getType() )
		{
		case ImagePlus.GRAY8 : 
		{
			System.out.println("uint8");
			RandomAccessibleInterval< UnsignedByteType > out = 
					alg.process( (RandomAccessibleInterval<UnsignedByteType>) ImageJFunctions.wrap( ip ));
			result = ScanLineArtifactCorrection.copyToImageStack( out, out );
			break;
		}
		case ImagePlus.GRAY16 : 
		{
			System.out.println("uint16");
			RandomAccessibleInterval< UnsignedShortType > out = 
					alg.process( (RandomAccessibleInterval<UnsignedShortType>) ImageJFunctions.wrap( ip ));
			result = ScanLineArtifactCorrection.copyToImageStack( out, out );
			break;
		}
		case ImagePlus.GRAY32 : 
		{
			System.out.println("uint32");
			RandomAccessibleInterval< FloatType > out = 
					alg.process( (RandomAccessibleInterval<FloatType>) ImageJFunctions.wrap( ip ));
			result = ScanLineArtifactCorrection.copyToImageStack( out, out );
			break;
		}
		}

		if( result != null )
			result.show();

	}
}
