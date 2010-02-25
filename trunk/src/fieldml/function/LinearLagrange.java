package fieldml.function;

public class LinearLagrange
    extends ContinuousFunction
{
    // NOTE Making this method public static simplifies testing.
    public static double[] evaluateDirect( double x1 )
    {
        double[] value = new double[2];

        value[0] = ( 1 - x1 );
        value[1] = ( x1 );

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0] );
    }
}
