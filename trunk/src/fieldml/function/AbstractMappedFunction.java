package fieldml.function;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousMap;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public abstract class AbstractMappedFunction
    extends ContinuousFunction
{
    @SerializationAsString
    public final ContinuousDomain dofsDomain;

    @SerializationAsString
    public final ContinuousMap dofsMap;


    public abstract double evaluate( double[] params, double[] xi );


    public AbstractMappedFunction( String name, ContinuousDomain dofsDomain )
    {
        this( name, dofsDomain, null );
    }


    public AbstractMappedFunction( String name, ContinuousDomain dofsDomain, ContinuousMap dofsMap )
    {
        super( name );

        this.dofsDomain = dofsDomain;
        this.dofsMap = dofsMap;
    }


    @Override
    public double evaluate( DomainValues context, MeshDomainValue meshLocation,
        SimpleMap<ContinuousDomain, ContinuousEvaluator> dofEvaluators )
    {
        ContinuousEvaluator dofEvaluator = dofEvaluators.get( dofsDomain );
        double[] params;
        
        if( dofsMap == null )
        {
            ContinuousDomainValue dofs = dofEvaluator.evaluate( context );
            params = dofs.values;
        }
        else
        {
            params = dofsMap.evaluate( context, dofEvaluator );
        }

        return evaluate( params, meshLocation.chartValues );
    }
}
