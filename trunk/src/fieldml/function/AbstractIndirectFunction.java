package fieldml.function;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class AbstractIndirectFunction
    extends ContinuousFunction
{
    @SerializationAsString
    public final ContinuousDomain dofDomain;

    @SerializationAsString
    public final EnsembleEvaluator dofIndexes;

    private final EnsembleDomain iteratedDomain;


    public AbstractIndirectFunction( String name, ContinuousDomain dofDomain, EnsembleEvaluator dofIndexes, EnsembleDomain iteratedDomain )
    {
        super( name );
        // TODO Assert that dofIndexes's parameter domain has the right cardinality for the given interpolation.
        this.dofDomain = dofDomain;
        this.dofIndexes = dofIndexes;
        this.iteratedDomain = iteratedDomain;
    }


    protected abstract double evaluate( double[] params, double[] xi );


    @Override
    public double evaluate( DomainValues context, MeshDomainValue meshLocation,
        SimpleMap<ContinuousDomain, ContinuousEvaluator> dofEvaluators )
    {
        int parameterCount;
        final int elementIndex = meshLocation.indexValue;
        double[] params = new double[iteratedDomain.getValueCount()];
        ContinuousEvaluator dofs = dofEvaluators.get( dofDomain );

        context = new DomainValues( context );
        context.set( meshLocation.domain.elementDomain, elementIndex );

        parameterCount = 0;
        for( int localNodeIndex = 1; localNodeIndex <= iteratedDomain.getValueCount(); localNodeIndex++ )
        {
            context.set( iteratedDomain, localNodeIndex );
            final EnsembleDomainValue indexOfGlobalNode = dofIndexes.evaluate( context );
            if( ( indexOfGlobalNode == null ) || ( indexOfGlobalNode.indexValue == 0 ) )
            {
                continue;
            }
            context.set( indexOfGlobalNode );
            ContinuousDomainValue dofValue = dofs.evaluate( context );
            params[parameterCount++] = dofValue.values[0];
        }

        return evaluate( params, meshLocation.chartValues );
    }
}
