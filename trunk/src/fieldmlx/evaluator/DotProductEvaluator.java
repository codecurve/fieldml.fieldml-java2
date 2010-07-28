package fieldmlx.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.annotations.SerializationAsString;

public class DotProductEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousDomain values;

    @SerializationAsString
    public final ContinuousDomain weights;

    @SerializationAsString
    public final ContinuousDomain scales;


    public static double dotProduct( double[] v1, double v2[] )
    {
        double finalValue = 0;

        for( int i = 0; i < v1.length; i++ )
        {
            finalValue += v1[i] * v2[i];
        }

        return finalValue;
    }


    public static double dotProduct( double[] v1, double v2[], double v3[] )
    {
        double finalValue = 0;

        for( int i = 0; i < v1.length; i++ )
        {
            finalValue += v1[i] * v2[i] * v3[i];
        }

        return finalValue;
    }


    public DotProductEvaluator( String name, ContinuousDomain valueDomain, ContinuousDomain valueSource, ContinuousDomain valueWeights,
        ContinuousDomain valueScale )
    {
        super( name, valueDomain );
        this.values = valueSource;
        this.weights = valueWeights;
        this.scales = valueScale;
    }


    public DotProductEvaluator( String name, ContinuousDomain valueDomain, ContinuousDomain valueSource, ContinuousDomain valueWeights 

)
    {
        this( name, valueDomain, valueSource, valueWeights, null );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        // TODO Currently assumes that values is scalar.

        double[] tWeights = context.get( weights ).values;
        double[] tValues = context.get( values ).values;

        if( scales == null )
        {
            return valueDomain.makeValue( dotProduct( tWeights, tValues ) );
        }
        else
        {
            double[] tScales = context.get( scales ).values;
            return valueDomain.makeValue( dotProduct( tWeights, tValues, tScales ) );
        }
    }
}
