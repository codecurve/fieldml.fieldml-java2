package fieldml.evaluator;

import java.util.Arrays;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class MapEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final EnsembleListEvaluator valueIndexes;

    @SerializationAsString
    public final ContinuousListEvaluator valueWeights;

    @SerializationAsString
    public final ContinuousEvaluator valueSource;

    @SerializationAsString
    public final ContinuousListEvaluator valueScale;

    private final EnsembleDomain iteratedDomain;


    public MapEvaluator( String name, ContinuousDomain valueDomain, EnsembleListEvaluator valueIndexes, ContinuousListEvaluator valueWeights,
        ContinuousEvaluator valueSource )
    {
        this( name, valueDomain, valueIndexes, valueWeights, valueSource, null );
    }


    public MapEvaluator( String name, ContinuousDomain valueDomain, EnsembleListEvaluator valueIndexes, ContinuousListEvaluator valueWeights,
        ContinuousEvaluator valueSource, ContinuousListEvaluator valueScale )
    {
        super( name, valueDomain );

        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;
        this.valueSource = valueSource;
        this.valueScale = valueScale;

        iteratedDomain = valueIndexes.getValueDomain().elementDomain;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        int[] indexes = valueIndexes.evaluate( context ).values;
        double[] weights = valueWeights.evaluate( context ).values;
        double[] values;
        double[] scales;
        double finalValue = 0;

        int valueSize = valueSource.getValueDomain().dimensions;
        if( valueScale != null )
        {
            scales = valueScale.evaluate( context ).values;
        }
        else
        {
            scales = new double[weights.length];
            Arrays.fill( scales, 1.0 );
        }

        int weightIndex = 0;
        for( int i = 0; i < indexes.length; i++ )
        {
            context.set( iteratedDomain, indexes[i] );

            values = valueSource.evaluate( context ).values;
            for( int j = 0; j < valueSize; j++ )
            {
                finalValue += weights[weightIndex] * values[j] * scales[weightIndex];
                weightIndex++;
            }
        }

        return valueDomain.makeValue( finalValue );
    }
}
