package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleValueSource;

public class MapEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final EnsembleValueSource valueIndexes;

    @SerializationAsString
    public final ContinuousValueSource valueWeights;

    @SerializationAsString
    public final ContinuousValueSource valueSource;


    public MapEvaluator( String name, ContinuousDomain valueDomain, EnsembleValueSource valueIndexes, ContinuousValueSource valueWeights,
        ContinuousValueSource valueSource )
    {
        super( name, valueDomain );

        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;
        this.valueSource = valueSource;
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        double[] weights = valueWeights.getValue( context ).values;
        int[] indexes = valueIndexes.getValue( context ).values;
        double[] values = new double[indexes.length];

        for( int i = 0; i < indexes.length; i++ )
        {
            context.set( valueIndexes.getValueDomain().baseDomain, indexes[i] );
            values[i] = valueSource.getValue( context ).values[0];
        }

        double finalValue = DotProductEvaluator.dotProduct( values, weights );

        return valueDomain.makeValue( finalValue );
    }
}
