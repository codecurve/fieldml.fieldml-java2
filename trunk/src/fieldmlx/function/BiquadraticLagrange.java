package fieldmlx.function;

public class BiquadraticLagrange
    extends ContinuousFunction
{
    public static double[] evaluateDirect( double x1, double x2 )
    {
        double[] v1 = QuadraticLagrange.evaluateDirect( x1 );
        double[] v2 = QuadraticLagrange.evaluateDirect( x2 );

        double value[] = new double[9];

        value[0] = v1[0] * v2[0];
        value[1] = v1[1] * v2[0];
        value[2] = v1[2] * v2[0];
        value[3] = v1[0] * v2[1];
        value[4] = v1[1] * v2[1];
        value[5] = v1[2] * v2[1];
        value[6] = v1[0] * v2[2];
        value[7] = v1[1] * v2[2];
        value[8] = v1[2] * v2[2];

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0], args[1] );
    }
}
