package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class DotProductEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousEvaluator source1;

    @SerializationAsString
    public final ContinuousEvaluator source2;


    public static double dotProduct( double[] v1, double v2[] )
    {
        double finalValue = 0;

        for( int i = 0; i < v1.length; i++ )
        {
            finalValue += v1[i] * v2[i];
        }

        return finalValue;
    }


    public DotProductEvaluator( String name, ContinuousDomain valueDomain, ContinuousEvaluator valueSource, ContinuousEvaluator valueWeights )
    {
        super( name, valueDomain );
        this.source1 = valueSource;
        this.source2 = valueWeights;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        // TODO Currently assumes that values is scalar.

        double[] weights = source2.evaluate( context ).values;
        double[] values = source1.evaluate( context ).values;

        return valueDomain.makeValue( dotProduct( weights, values ) );
    }
}
