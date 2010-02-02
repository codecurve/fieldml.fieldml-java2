package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class DotProductEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousListEvaluator source1;

    @SerializationAsString
    public final ContinuousListEvaluator source2;


    public DotProductEvaluator( String name, ContinuousDomain valueDomain, ContinuousListEvaluator valueSource, ContinuousListEvaluator valueWeights )
    {
        super( name, valueDomain );
        this.source1 = valueSource;
        this.source2 = valueWeights;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        // TODO Currently assumes that indexedValues is scalar.

        double[] weights = source2.evaluate( context ).values;
        double[] values = source1.evaluate( context ).values;
        double finalValue = 0;

        for( int i = 0; i < values.length; i++ )
        {
            finalValue += weights[i] * values[i];
        }

        return valueDomain.makeValue( finalValue );
    }
}
