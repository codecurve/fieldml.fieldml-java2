package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
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


    public double[] evaluate( DomainValues context, ContinuousEvaluator values )
    {
        // TODO If both the weight and the value lookups are multi-dimension, we need to figure out what to do.
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

        double[] finalValue = new double[outputDimensions];

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            context.set( iteratedDomain, i );
            spanned = spannedEvaluator.evaluate( context ).values;
            fixed = fixedEvaluator.evaluate( context ).values[0];
            for( int j = 0; j < outputDimensions; j++ )
            {
                finalValue[j] += ( spanned[j] * fixed );
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
