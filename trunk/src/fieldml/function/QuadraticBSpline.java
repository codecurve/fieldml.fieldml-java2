package fieldml.function;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousMap;

public class QuadraticBSpline
    extends AbstractMappedFunction
{
    // NOTE Making this method public simplifies testing.
    public static double evaluateDirect( double[] params, double[] xi )
    {
        double p0 = 0.5 * ( 1 - xi[0] ) * ( 1 - xi[0] );
        double p1 = -( xi[0] * xi[0] ) + xi[0] + 0.5;
        double p2 = 0.5 * xi[0] * xi[0];

        return params[0] * p0 + params[1] * p1 + params[2] * p2;
    }


    public QuadraticBSpline( String name, ContinuousDomain dofsDomain )
    {
        this( name, dofsDomain, null );
    }


    public QuadraticBSpline( String name, ContinuousDomain dofsDomain, ContinuousMap dofsMap )
    {
        super( name, dofsDomain, dofsMap );
    }


    @Override
    public double evaluate( double[] params, double[] xi )
    {
        return evaluateDirect( params, xi );
    }
}
