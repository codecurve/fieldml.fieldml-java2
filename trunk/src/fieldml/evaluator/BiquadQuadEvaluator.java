package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousMappingField;
import fieldml.field.EnsembleMappingField;

public class BiquadQuadEvaluator
    extends IndirectEvaluator
{
    protected double evaluate( double[] params, double[] xi )
    {
        double x1_1 = 2 * ( xi[0] - 1 ) * ( xi[0] - 0.5 );
        double x1_2 = 4 * ( xi[0] ) * ( 1 - xi[0] );
        double x1_3 = 2 * ( xi[0] ) * ( xi[0] - 0.5 );

        double x2_1 = 2 * ( xi[1] - 1 ) * ( xi[1] - 0.5 );
        double x2_2 = 4 * ( xi[1] ) * ( 1 - xi[1] );
        double x2_3 = 2 * ( xi[1] ) * ( xi[1] - 0.5 );

        return 0 + //Hack for pretty auto-formatting :)
            params[0] * x1_1 * x2_1 + params[1] * x1_2 * x2_1 + params[2] * x1_3 * x2_1 + //
            params[3] * x1_1 * x2_2 + params[4] * x1_2 * x2_2 + params[5] * x1_3 * x2_2 + //
            params[6] * x1_1 * x2_3 + params[7] * x1_2 * x2_3 + params[8] * x1_3 * x2_3;
    }


    public BiquadQuadEvaluator( String name, ContinuousMappingField dofs, EnsembleMappingField dofIndexes,
        EnsembleDomain iteratedDomain )
    {
        super( name, dofs, dofIndexes, iteratedDomain );
    }
}
