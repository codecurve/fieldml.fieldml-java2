package fieldml.evaluator.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
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

    @SerializationAsString
    public final ContinuousDomain valueDomain;


    public MappedImportOperation( ContinuousDomain valueDomain, ContinuousParameters sourceField, ContinuousEvaluator weightings,
        EnsembleDomain iteratedDomain )
    {
        this.sourceField = sourceField;
        this.weightings = weightings;
        this.iteratedDomain = iteratedDomain;
        this.valueDomain = valueDomain;
    }


    @Override
    public void perform( DomainValues values )
    {
        // TODO If both the weight and the value lookups are multi-dimension, we need to figure out what to do.
        int dimensions = weightings.getValueDomain().dimensions;
        double value;
        double[] finalValue = new double[dimensions];
        double[] weights;

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            values.set( iteratedDomain, i );
            weights = weightings.evaluate( values ).values;
            value = sourceField.evaluate( values ).values[0];
            for( int j = 0; j < dimensions; j++ )
            {

                finalValue[j] += ( weights[j] * value );
            }
        }

        values.set( valueDomain, finalValue );
    }
}
