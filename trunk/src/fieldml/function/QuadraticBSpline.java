package fieldml.function;

public class QuadraticBSpline
    implements ContinuousFunction
{
    // NOTE Making this method public simplifies testing.
    public static double[] evaluateDirect( double x1 )
    {
        double[] value = new double[3];

        value[0] = 0.5 * ( 1 - x1 ) * ( 1 - x1 );
        value[1] = -( x1 * x1 ) + x1 + 0.5;
        value[2] = 0.5 * x1 * x1;

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0] );
    }
}
