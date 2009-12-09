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

    private final boolean valueSpan;


    public MappedImportOperation( ContinuousDomain valueDomain, ContinuousParameters sourceField, ContinuousEvaluator weightings,
        EnsembleDomain iteratedDomain )
    {
        this.sourceField = sourceField;
        this.weightings = weightings;
        this.iteratedDomain = iteratedDomain;
        this.valueDomain = valueDomain;

        int outputDimensions = 0;
        if( weightings.getValueDomain().dimensions == 1 )
        {
            valueSpan = true;
            outputDimensions = sourceField.getValueDomain().dimensions;
        }
        else if( sourceField.getValueDomain().dimensions == 1 )
        {
            valueSpan = false;
            outputDimensions = weightings.getValueDomain().dimensions;
        }
        else
        {
            valueSpan = false;
        }

        assert outputDimensions == valueDomain.dimensions;
    }


    @Override
    public void perform( DomainValues values )
    {
        // TODO If both the weight and the value lookups are multi-dimension, we need to figure out what to do.
        int dimensions = valueDomain.dimensions;
        double[] finalValue = new double[dimensions];
        double fixed;
        double[] spanned;

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            values.set( iteratedDomain, i );
            if( valueSpan )
            {
                spanned = sourceField.evaluate( values ).values;
                fixed = weightings.evaluate( values ).values[0];
            }
            else
            {
                spanned = weightings.evaluate( values ).values;
                fixed = sourceField.evaluate( values ).values[0];
            }
            for( int j = 0; j < dimensions; j++ )
            {
                finalValue[j] += ( spanned[j] * fixed );
            }
        }

        values.set( valueDomain, finalValue );
    }
}
