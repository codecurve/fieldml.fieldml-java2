package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class MapEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final EnsembleDomain valueIndexes;

    @SerializationAsString
    public final ContinuousDomain valueWeights;

    @SerializationAsString
    public final ContinuousEvaluator valueSource;


    public MapEvaluator( String name, ContinuousDomain valueDomain, EnsembleDomain valueIndexes, ContinuousDomain valueWeights,
        ContinuousEvaluator valueSource )
    {
        super( name, valueDomain );

        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;
        this.valueSource = valueSource;
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        double[] weights = context.get( valueWeights ).values;
        int[] indexes = context.get( valueIndexes ).values;
        double[] values = new double[indexes.length];

        for( int i = 0; i < indexes.length; i++ )
        {
            context.set( valueIndexes.baseDomain, indexes[i] );
            values[i] = valueSource.getValue( context ).values[0];
        }

        double finalValue = DotProductEvaluator.dotProduct( values, weights );

        return valueDomain.makeValue( finalValue );
    }
}
