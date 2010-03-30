package fieldmlx.function;

import java.util.Arrays;

public class TensorBasis
    extends ContinuousFunction
{
    private final ContinuousFunction[] bases;

    private final String description;

    private final int componentCount;
    

    public TensorBasis( String description )
    {
        this.description = description;

        String[] parts = description.split( "\\*" );

        bases = new ContinuousFunction[parts.length];

        int components = 1;
        for( int i = 0; i < parts.length; i++ )
        {
            if( parts[i].equals( "l.lagrange" ) )
            {
                components *= 2;
                bases[i] = new LinearLagrange();
            }
            else if( parts[i].equals( "q.lagrange" ) )
            {
                components *= 3;
                bases[i] = new QuadraticLagrange();
            }
            else if( parts[i].equals( "c.lagrange" ) )
            {
                components *= 4;
                bases[i] = new CubicLagrange();
            }
            else if( parts[i].equals( "c.bspline" ) )
            {
                components *= 3;
                bases[i] = new QuadraticBSpline();
            }
            else if( parts[i].equals( "c.hermite" ) )
            {
                components *= 4;
                bases[i] = new CubicHermite();
            }
        }

        this.componentCount = components;
    }


    @Override
    public double[] evaluate( double... args )
    {
        double[] value = new double[componentCount];
        Arrays.fill( value, 1 );

        int divisor = 1;
        for( int i = 0; i < bases.length; i++ )
        {
            double[] v = bases[i].evaluate( args[i] );
            for( int j = 0; j < componentCount; j++ )
            {
                value[j] *= v[( j / divisor ) % v.length];
            }
            divisor *= v.length;
        }
        return value;
    }


    @Override
    public String toString()
    {
        return description;
    }
    
    
    public int getDimensions()
    {
        //NOTE This will not be correct when simplex bases are added.
        return bases.length;
    }
}
