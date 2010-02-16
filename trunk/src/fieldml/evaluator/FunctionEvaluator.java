package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.MeshDomain;
import fieldml.function.ContinuousFunction;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class FunctionEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousFunction function;

    @SerializationAsString
    public final MeshDomain functionDomain;


    public FunctionEvaluator( String name, ContinuousDomain valueDomain, MeshDomain functionDomain, ContinuousFunction function )
    {
        //TODO For now, this class only supports evaluation on a mesh.
        super( name, valueDomain );

        this.function = function;
        this.functionDomain = functionDomain;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        MeshDomainValue value = context.get( functionDomain );

        return valueDomain.makeValue( function.evaluate( value.chartValues ) );
    }

}
