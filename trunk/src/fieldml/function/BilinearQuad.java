package fieldml.function;

import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;

public class BilinearQuad
    extends AbstractIndirectFunction
{
    @Override
    protected double evaluate( double[] params, double[] xi )
    {
        double x1_1 = ( 1 - xi[0] );
        double x1_2 = ( xi[0] );
        double x2_1 = ( 1 - xi[1] );
        double x2_2 = ( xi[1] );

        return params[0] * x1_1 * x2_1 + params[1] * x1_2 * x2_1 + params[2] * x1_1 * x2_2 + params[3] * x1_2 * x2_2;
    }


    public BilinearQuad( String name, ContinuousEvaluator dofs, EnsembleEvaluator dofIndexes,
        EnsembleDomain iteratedDomain )
    {
        super( name, dofs, dofIndexes, iteratedDomain );
    }
}
