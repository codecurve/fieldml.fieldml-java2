package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class IndirectEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousParameters dofs;

    @SerializationAsString
    public final EnsembleParameters dofIndexes;

    private final EnsembleDomain iteratedDomain;


    public IndirectEvaluator( String name, ContinuousParameters dofs, EnsembleParameters dofIndexes, EnsembleDomain iteratedDomain )
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
        int parameterCount;
        final int elementIndex = value.indexValue;
        double[] params = new double[iteratedDomain.getValueCount()];
        
        DomainValues input = new DomainValues();
        input.set( value.domain.elementDomain, elementIndex );

        parameterCount = 0;
        for( int localNodeIndex = 1; localNodeIndex <= iteratedDomain.getValueCount(); localNodeIndex++ )
        {
            input.set( iteratedDomain, localNodeIndex );
            final EnsembleDomainValue indexOfGlobalNode = dofIndexes.evaluate( input );
            if( ( indexOfGlobalNode == null ) || ( indexOfGlobalNode.indexValue == 0 ) )
            {
                continue;
            }
            input.set( indexOfGlobalNode );
            ContinuousDomainValue dofValue = dofs.evaluate( input );
            params[parameterCount++] = dofValue.values[0];
        }

        return evaluate( params, value.chartValues );
    }
}
