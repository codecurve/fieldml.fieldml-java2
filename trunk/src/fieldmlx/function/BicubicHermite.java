package fieldmlx.function;

public class BicubicHermite
    extends ContinuousFunction
{
    public static double[] evaluateDirect( double x1, double x2 )
    {
        // Dof-vector is structured four consecutive sets of u, d/ds1, d/ds2, d2/ds1ds2 values.
        double x1_v[] = CubicHermite.evaluateDirect( x1 );
        double x2_v[] = CubicHermite.evaluateDirect( x2 );

        double[] value = new double[16];

        value[0] = x1_v[0] * x2_v[0];
        value[4] = x1_v[2] * x2_v[0];
        value[8] = x1_v[0] * x2_v[2];
        value[12] = x1_v[2] * x2_v[2];

        // du/ds1, at local nodes 1,2,3,4
        value[1] = x1_v[1] * x2_v[0];
        value[5] = x1_v[3] * x2_v[0];
        value[9] = x1_v[1] * x2_v[2];
        value[13] = x1_v[3] * x2_v[2];

        // du/ds2, at local nodes 1,2,3,4
        value[2] = x1_v[0] * x2_v[1];
        value[6] = x1_v[2] * x2_v[1];
        value[10] = x1_v[0] * x2_v[3];
        value[14] = x1_v[2] * x2_v[3];

        // d2u /ds1ds2, at local nodes 1,2,3,4
        value[3] = x1_v[1] * x2_v[1];
        value[7] = x1_v[3] * x2_v[1];
        value[11] = x1_v[1] * x2_v[3];
        value[15] = x1_v[3] * x2_v[3];

        return value;
    }


    @Override
    public double[] evaluate( double... args )
    {
        return evaluateDirect( args[0], args[1] );
    }
}
