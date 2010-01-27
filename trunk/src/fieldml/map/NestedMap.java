package fieldml.map;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.EnsembleListEvaluator;
import fieldml.value.DomainValues;

public class NestedMap
    implements ContinuousMap
{
    public final String name;

    @SerializationAsString
    public final EnsembleListEvaluator valueIndexes;

    @SerializationAsString
    public final ContinuousListEvaluator valueWeights;

    @SerializationAsString
    public final ContinuousMap values;

    private final EnsembleDomain iteratedDomain;


    public NestedMap( String name, EnsembleListEvaluator valueIndexes, ContinuousListEvaluator valueWeights, ContinuousMap values )
    {
        this.name = name;
        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;
        this.values = values;

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
        double finalValue = 0;

        for( int i = 0; i < indexes.length; i++ )
        {
            context.set( iteratedDomain, indexes[i] );

            finalValue += weights[i] * values.evaluate( context, indexedValues );
        }

        return finalValue;
    }
    
    
    @Override
    public String toString()
    {
        return name;
    }
}
