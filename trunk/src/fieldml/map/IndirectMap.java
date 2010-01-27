package fieldml.map;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.EnsembleListEvaluator;
import fieldml.value.DomainValues;

public class IndirectMap
    implements ContinuousMap
{
    public final String name;

    @SerializationAsString
    public final EnsembleListEvaluator valueIndexes;

    @SerializationAsString
    public final ContinuousListEvaluator valueWeights;

    private final EnsembleDomain iteratedDomain;


    public IndirectMap( String name, EnsembleListEvaluator valueIndexes, ContinuousListEvaluator valueWeights )
    {
        this.name = name;
        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;

        iteratedDomain = valueIndexes.getValueDomain().elementDomain;
    }


    public String getName()
    {
        return name;
    }


    public double evaluate( DomainValues context, ContinuousEvaluator indexedValues )
    {
        // TODO Currently assumes that indexedValues is scalar.

        int[] indexes = valueIndexes.evaluate( context ).values;
        double[] weights = valueWeights.evaluate( context ).values;
        double[] values;
        double finalValue = 0;
        int valueSize = indexedValues.getValueDomain().dimensions;

        int weightIndex = 0;
        for( int i = 0; i < indexes.length; i++ )
        {
            context.set( iteratedDomain, indexes[i] );

            values = indexedValues.evaluate( context ).values;
            for( int j = 0; j < valueSize; j++ )
            {
                finalValue += weights[weightIndex++] * values[j];
            }
        }

        return finalValue;
    }
    
    
    @Override
    public String toString()
    {
        return name;
    }
}
