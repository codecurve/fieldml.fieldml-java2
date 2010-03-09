package fieldmlx.test;

import junit.framework.TestCase;


import fieldml.function.BilinearLagrange;
import fieldml.function.BiquadraticLagrange;
import fieldml.function.ContinuousFunction;
import fieldml.function.TensorBasis;

public class TensorBasisTest
    extends TestCase
{
    private static final void assertArrayEquals( double[] expected, double[] actual, double tolerance )
    {
        assertEquals( expected.length, actual.length );
        for( int i = 0; i < expected.length; i++ )
        {
            assertEquals( "Mismatch at entry " + i, expected[i], actual[i], tolerance );
        }
    }
    
    public void testTensorBases()
    {
        ContinuousFunction basis;
        
        double[] xi = new double[2];
        double[] expected;
        double[] actual;
        
        xi[0] = 0.12;
        xi[1] = 0.73;
        
        basis = new TensorBasis( "l.lagrange*l.lagrange" );
        expected = BilinearLagrange.evaluateDirect( xi[0], xi[1] );
        actual = basis.evaluate( xi );
        
        assertArrayEquals( expected, actual, 0.000001 );

        basis = new TensorBasis( "q.lagrange*q.lagrange" );
        expected = BiquadraticLagrange.evaluateDirect( xi[0], xi[1] );
        actual = basis.evaluate( xi );

        assertArrayEquals( expected, actual, 0.000001 );
    }
}
