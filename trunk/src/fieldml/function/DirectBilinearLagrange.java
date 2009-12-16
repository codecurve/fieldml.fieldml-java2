package fieldml.function;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousMap;

public class DirectBilinearLagrange
    extends AbstractMappedFunction
{
    // NOTE Making this method public simplifies testing.
    public double evaluateDirect( double[] params, double[] xi )
    {
        double x1_1 = ( 1 - xi[0] );
        double x1_2 = ( xi[0] );
        double x2_1 = ( 1 - xi[1] );
        double x2_2 = ( xi[1] );

        return params[0] * x1_1 * x2_1 + params[1] * x1_2 * x2_1 + params[2] * x1_1 * x2_2 + params[3] * x1_2 * x2_2;
    }


    public DirectBilinearLagrange( String name, ContinuousDomain dofsDomain )
    {
        super( name, dofsDomain, null );
    }


    public DirectBilinearLagrange( String name, ContinuousDomain dofsDomain, ContinuousMap dofsMap )
    {
        super( name, dofsDomain, dofsMap );
    }


    @Override
    public double evaluate( double[] params, double[] xi )
    {
        return evaluateDirect( params, xi );
    }
}
