package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class MapEvaluator
    extends AbstractContinuousEvaluator
{
    @SerializationAsString
    public final EnsembleEvaluator valueIndexes;

    @SerializationAsString
    public final ContinuousEvaluator valueWeights;

    @SerializationAsString
    public final ContinuousEvaluator valueSource;


    public MapEvaluator( String name, ContinuousDomain valueDomain, EnsembleEvaluator valueIndexes, ContinuousEvaluator valueWeights,
        ContinuousEvaluator valueSource )
    {
        super( name, valueDomain );

        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;
        this.valueSource = valueSource;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        double[] weights = valueWeights.evaluate( context ).values;
        int[] indexes = valueIndexes.evaluate( context ).values;
        double[] values = new double[indexes.length];

        for( int i = 0; i < indexes.length; i++ )
        {
            context.set( valueIndexes.getValueDomain().baseDomain, indexes[i] );
            values[i] = valueSource.evaluate( context ).values[0];
        }

        double finalValue = DotProductEvaluator.dotProduct( values, weights );

        return valueDomain.makeValue( finalValue );
    }
}
