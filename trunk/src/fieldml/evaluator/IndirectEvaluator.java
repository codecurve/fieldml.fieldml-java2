package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousMappingField;
import fieldml.field.EnsembleMappingField;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class IndirectEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousMappingField dofs;

    @SerializationAsString
    public final EnsembleMappingField dofIndexes;

    private final EnsembleDomain iteratedDomain;


    public IndirectEvaluator( String name, ContinuousMappingField dofs, EnsembleMappingField dofIndexes,
        EnsembleDomain iteratedDomain )
    {
        super( name );

        // TODO Assert that dofIndexes value domain is dofs only parameter domain
        // TODO Assert that dofIndexes's parameter domain has the right cardinality for the given interpolation.
        this.dofs = dofs;
        this.dofIndexes = dofIndexes;
        this.iteratedDomain = iteratedDomain;
    }


    protected abstract double evaluate( double[] params, double[] xi );


    @Override
    public double evaluate( MeshDomainValue value )
    {
        final int elementIndex = value.indexValue;
        double[] params = new double[4];

        for( int i = 0; i < iteratedDomain.getValueCount(); i++ )
        {
            final int localNodeIndex = i + 1;
            final EnsembleDomainValue indexOfGlobalNode = dofIndexes.evaluate( elementIndex, localNodeIndex );
            ContinuousDomainValue dofValue = dofs.evaluate( indexOfGlobalNode );
            params[i] = dofValue.chartValues[0];
        }

        return evaluate( params, value.chartValues );
    }
}
