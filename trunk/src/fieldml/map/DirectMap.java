package fieldml.map;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class DirectMap
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousListEvaluator valueSource;

    @SerializationAsString
    public final ContinuousListEvaluator valueWeights;


    public DirectMap( String name, ContinuousDomain valueDomain, ContinuousListEvaluator valueSource, ContinuousListEvaluator valueWeights )
    {
        super( name, valueDomain );
        this.valueSource = valueSource;
        this.valueWeights = valueWeights;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        // TODO Currently assumes that indexedValues is scalar.

        double[] weights = valueWeights.evaluate( context ).values;
        double[] values = valueSource.evaluate( context ).values;
        double finalValue = 0;

        for( int i = 0; i < values.length; i++ )
        {
            finalValue += weights[i] * values[i];
        }

        return valueDomain.makeValue( finalValue );
    }
}
