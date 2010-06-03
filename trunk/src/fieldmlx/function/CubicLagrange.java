package fieldmlx.function;

public class CubicLagrange
    extends ContinuousFunction
{
    // NOTE Making this method public static simplifies testing.
    public static double[] evaluateDirect( double x1 )
    {
        double value[] = new double[4];

        value[0] = 0.5 * ( 3 * x1 - 1 ) * ( 3 * x1 - 2 ) * ( 1 - x1 );
        value[1] = 4.5 * x1 * ( 3 * x1 - 2 ) * ( x1 - 1 );
        value[2] = 4.5 * x1 * ( 3 * x1 - 1 ) * ( 1 - x1 );
        value[2] = 0.5 * ( 3 * x1 - 1 ) * ( 3 * x1 - 2 ) * x1;

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0] );
    }
}
