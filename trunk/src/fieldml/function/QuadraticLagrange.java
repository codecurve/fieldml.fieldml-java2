package fieldml.function;

public class QuadraticLagrange
    extends ContinuousFunction
{
    public static double[] evaluateDirect( double x1 )
    {
        double value[] = new double[3];

        value[0] = 2 * ( x1 - 1 ) * ( x1 - 0.5 );
        value[1] = 4 * ( x1 ) * ( 1 - x1 );
        value[2] = 2 * ( x1 ) * ( x1 - 0.5 );

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0] );
    }
}
