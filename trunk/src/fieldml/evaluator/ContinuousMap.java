package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousMap
{
    public final String name;

    @SerializationAsString
    public final ContinuousEvaluator weights;

    @SerializationAsString
    public final EnsembleDomain iteratedDomain; // NOTE: We could have multiple such domains.


    public ContinuousMap( String name, ContinuousEvaluator weights, EnsembleDomain iteratedDomain )
    {
        this.name = name;
        this.weights = weights;
        this.iteratedDomain = iteratedDomain;
    }


    public ContinuousDomainValue evaluate( ContinuousDomain valueDomain, DomainValues context, ContinuousEvaluator values )
    {
        // TODO If both the weight and the value lookups are multi-dimension, we need to figure out what to do.
        int dimensions = valueDomain.dimensions;
        double[] finalValue = new double[dimensions];
        double fixed;
        double[] spanned;
        ContinuousEvaluator spannedEvaluator;
        ContinuousEvaluator fixedEvaluator;

        int outputDimensions = 0;
        if( weights.getValueDomain().dimensions == 1 )
        {
            spannedEvaluator = values;
            fixedEvaluator = weights;
            outputDimensions = values.getValueDomain().dimensions;
        }
        else if( values.getValueDomain().dimensions == 1 )
        {
            spannedEvaluator = weights;
            fixedEvaluator = values;
            outputDimensions = weights.getValueDomain().dimensions;
        }
        else
        {
            return null;
        }

        assert outputDimensions == valueDomain.dimensions;

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            context.set( iteratedDomain, i );
            spanned = spannedEvaluator.evaluate( context ).values;
            fixed = fixedEvaluator.evaluate( context ).values[0];
            for( int j = 0; j < dimensions; j++ )
            {
                finalValue[j] += ( spanned[j] * fixed );
            }
        }

        return valueDomain.makeValue( finalValue );
    }
}
