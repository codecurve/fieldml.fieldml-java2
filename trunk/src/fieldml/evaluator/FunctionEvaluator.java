package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousListDomain;
import fieldml.domain.MeshDomain;
import fieldml.function.ContinuousFunction;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class FunctionEvaluator
    extends AbstractEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    @SerializationAsString
    public final ContinuousFunction function;

    @SerializationAsString
    public final MeshDomain functionDomain;


    public FunctionEvaluator( String name, ContinuousListDomain valueDomain, MeshDomain functionDomain, ContinuousFunction function )
    {
        //TODO For now, this class only supports evaluation on a mesh.
        super( name, valueDomain );

        this.function = function;
        this.functionDomain = functionDomain;
    }


    @Override
    public ContinuousListDomainValue evaluate( DomainValues context )
    {
        MeshDomainValue value = context.get( functionDomain );

        return valueDomain.makeValue( function.evaluate( value.chartValues ) );
    }

}
