package fieldml.function;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.EnsembleEvaluator;

public class QuadraticLagrange
    extends AbstractIndirectFunction
{
    public static double evaluateDirect( double[] params, double[] xi )
    {
        double x1_1 = 2 * ( xi[0] - 1 ) * ( xi[0] - 0.5 );
        double x1_2 = 4 * xi[0] * ( 1 - xi[0] );
        double x1_3 = 2 * xi[0] * ( xi[0] - 0.5 );

        return params[0] * x1_1 + params[1] * x1_2 + params[2] * x1_3;
    }


    @Override
    protected double evaluate( double[] params, double[] xi )
    {
        return evaluateDirect( params, xi );
    }


    public QuadraticLagrange( String name, ContinuousDomain dofDomain, EnsembleEvaluator dofIndexes, EnsembleDomain iteratedDomain )
    {
        super( name, dofDomain, dofIndexes, iteratedDomain );
    }
}
