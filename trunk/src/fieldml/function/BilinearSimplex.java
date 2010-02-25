package fieldml.function;

public class BilinearSimplex
    extends ContinuousFunction
{
    public static double[] evaluateDirect( double x1, double x2 )
    {
        double[] value = new double[3];

        value[0] = ( 1 - ( x1 + x2 ) );
        value[1] = x1;
        value[2] = x2;

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0], args[1] );
    }
}
