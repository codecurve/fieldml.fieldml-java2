package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.function.ContinuousFunction;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class FunctionEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousFunction function;

    @SerializationAsString
    public final ContinuousDomain inputDomain;


    public FunctionEvaluator( String name, ContinuousDomain outputDomain, ContinuousDomain inputDomain, ContinuousFunction function )
    {
        // TODO For now, this class only supports evaluation on a mesh.
        super( name, outputDomain );

        this.function = function;
        this.inputDomain = inputDomain;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        return valueDomain.makeValue( function.evaluate( context.get( inputDomain ).values ) );
    }

}
