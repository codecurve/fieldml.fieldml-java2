package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.field.MappingField;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public class BilinearSimplexEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final MappingField<ContinuousDomain, ContinuousDomainValue> dofs;

    @SerializationAsString
    public final MappingField<EnsembleDomain, EnsembleDomainValue> dofIndexes;


    private static double SimplexBilinear( double[] params, double[] xi )
    {
        double p0 = ( 1 - ( xi[0] + xi[1] ) );
        double p1 = xi[0];
        double p2 = xi[1];

        return params[0] * p0 + params[1] * p1 + params[2] * p2;
    }


    public BilinearSimplexEvaluator( String name, MappingField<ContinuousDomain, ContinuousDomainValue> dofs,
        MappingField<EnsembleDomain, EnsembleDomainValue> dofIndexes )
    {
        super( name );

        // TODO Assert that dofIndexes value domain is dofs only parameter domain
        // TODO Assert that dofIndexes's parameter domain has the right cardinality for the given interpolation.
        this.dofs = dofs;
        this.dofIndexes = dofIndexes;
    }


    @Override
    public double evaluate( MeshDomainValue value )
    {
        final int elementIndex = value.indexValue;
        double[] params = new double[3];

        for( int i = 0; i < 3; i++ )
        {
            final int localNodeIndex = i + 1;
            final EnsembleDomainValue indexOfGlobalNode = dofIndexes.evaluate( elementIndex, localNodeIndex );
            ContinuousDomainValue dofValue = dofs.evaluate( indexOfGlobalNode );
            params[i] = dofValue.chartValues[0];
        }

        return SimplexBilinear( params, value.chartValues );
    }
}
