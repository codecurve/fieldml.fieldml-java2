package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousMappingField;
import fieldml.field.EnsembleMappingField;

public class BilinearQuadEvaluator
    extends IndirectEvaluator
{
    protected double evaluate( double[] params, double[] xi )
    {
        double p3 = xi[0] * xi[1];
        double p2 = ( 1 - xi[0] ) * xi[1];
        double p1 = xi[0] * ( 1 - xi[1] );
        double p0 = ( 1 - xi[0] ) * ( 1 - xi[1] );

        return params[0] * p0 + params[1] * p1 + params[2] * p2 + params[3] * p3;
    }


    public BilinearQuadEvaluator( String name, ContinuousMappingField dofs, EnsembleMappingField dofIndexes,
        EnsembleDomain iteratedDomain )
    {
        super( name, dofs, dofIndexes, iteratedDomain );
    }
}
