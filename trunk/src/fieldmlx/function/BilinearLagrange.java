package fieldmlx.function;

public class BilinearLagrange
    extends ContinuousFunction
{
    // NOTE Making this method public simplifies testing.
    public static double[] evaluateDirect( double x1, double x2 )
    {
        double[] x1_v = LinearLagrange.evaluateDirect( x1 );
        double[] x2_v = LinearLagrange.evaluateDirect( x2 );

        double[] value = new double[4];

        value[0] = x1_v[0] * x2_v[0];
        value[1] = x1_v[1] * x2_v[0];
        value[2] = x1_v[0] * x2_v[1];
        value[3] = x1_v[1] * x2_v[1];

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0], args[1] );
    }
}
