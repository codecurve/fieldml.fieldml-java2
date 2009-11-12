package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;

public class BilinearSimplexEvaluator
    extends IndirectEvaluator
{
    protected double evaluate( double[] params, double[] xi )
    {
        double p0 = ( 1 - ( xi[0] + xi[1] ) );
        double p1 = xi[0];
        double p2 = xi[1];

        return params[0] * p0 + params[1] * p1 + params[2] * p2;
    }


    public BilinearSimplexEvaluator( String name, ContinuousParameters dofs, EnsembleParameters dofIndexes,
        EnsembleDomain iteratedDomain )
    {
        super( name, dofs, dofIndexes, iteratedDomain );
    }
}
