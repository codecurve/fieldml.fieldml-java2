package fieldml.function;

public class CubicHermite
    extends ContinuousFunction
{
    public static double[] evaluateDirect( double x1 )
    {
        double[] value = new double[4];

        value[0] = ( 1 - 3 * x1 * x1 + 2 * x1 * x1 * x1 ); // psi01
        value[1] = x1 * ( x1 - 1 ) * ( x1 - 1 ); // psi11
        value[2] = x1 * x1 * ( 3 - 2 * x1 ); // psi02
        value[3] = x1 * x1 * ( x1 - 1 ); // psi12

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0] );
    }
}
