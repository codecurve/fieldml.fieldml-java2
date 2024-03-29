package fieldmlx.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.annotations.SerializationAsString;
import fieldmlx.function.ContinuousFunction;

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
    public ContinuousDomainValue getValue( DomainValues context )
    {
        return valueDomain.makeValue( function.evaluate( context.get( inputDomain ).values ) );
    }

}
