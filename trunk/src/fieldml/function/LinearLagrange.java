package fieldml.function;

import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;

public class LinearLagrange
    extends AbstractIndirectFunction
{
    @Override
    protected double evaluate( double[] params, double[] xi )
    {
        double x1_1 = ( 1 - xi[0] );
        double x1_2 = ( xi[0] );

        return params[0] * x1_1 + params[1] * x1_2;
    }


    public LinearLagrange( String name, ContinuousEvaluator dofs, EnsembleEvaluator dofIndexes,
        EnsembleDomain iteratedDomain )
    {
        super( name, dofs, dofIndexes, iteratedDomain );
    }
}
