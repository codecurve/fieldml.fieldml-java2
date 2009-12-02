package fieldml.evaluator.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.value.DomainValues;

public class MappedImportOperation
    implements CompositeOperation
{
    @SerializationAsString
    public final ContinuousParameters sourceField;

    @SerializationAsString
    public final ContinuousEvaluator weightings;

    @SerializationAsString
    public final EnsembleDomain iteratedDomain;


    public MappedImportOperation( ContinuousParameters sourceField, ContinuousEvaluator weightings, EnsembleDomain iteratedDomain )
    {
        this.sourceField = sourceField;
        this.weightings = weightings;
        this.iteratedDomain = iteratedDomain;
    }


    @Override
    public void perform( DomainValues values )
    {
        //TODO If both the weight and the value lookups are multi-dimension, we need to figure out what to do.
        int dimensions = weightings.getValueDomain().dimensions;
        double weight;
        double value;
        double[] finalValue = new double[dimensions];

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            values.set( iteratedDomain, i );
            for( int j = 0; j < dimensions; j++ )
            {
                weight = weightings.evaluate( values ).values[j];
                value = sourceField.evaluate( values ).values[0];

                finalValue[j] += ( weight * value );
            }
        }

        values.set( sourceField.valueDomain, finalValue );
    }
}
