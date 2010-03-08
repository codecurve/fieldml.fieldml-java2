package fieldml.evaluator;

import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.function.ContinuousFunction;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class FunctionEvaluator
    extends AbstractContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousFunction function;

    @SerializationAsString
    public final ContinuousEvaluator inputSource;


    public FunctionEvaluator( String name, ContinuousDomain outputDomain, ContinuousEvaluator inputSource, ContinuousFunction function )
    {
        // TODO For now, this class only supports evaluation on a mesh.
        super( name, outputDomain );

        this.function = function;
        this.inputSource = inputSource;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        double[] args = inputSource.evaluate( context ).values;
        return valueDomain.makeValue( function.evaluate( args ) );
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        return inputSource.getVariables();
    }
}
