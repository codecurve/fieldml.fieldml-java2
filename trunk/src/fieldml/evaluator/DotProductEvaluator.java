package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class DotProductEvaluator
    extends AbstractContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousEvaluator values;

    @SerializationAsString
    public final ContinuousEvaluator weights;

    @SerializationAsString
    public final ContinuousEvaluator scales;


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


    public DotProductEvaluator( String name, ContinuousDomain valueDomain, ContinuousEvaluator valueSource,
        ContinuousEvaluator valueWeights, ContinuousEvaluator valueScale )
    {
        super( name, valueDomain );
        this.values = valueSource;
        this.weights = valueWeights;
        this.scales = valueScale;
    }


    public DotProductEvaluator( String name, ContinuousDomain valueDomain, ContinuousEvaluator valueSource, ContinuousEvaluator valueWeights )
    {
        this( name, valueDomain, valueSource, valueWeights, null );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        // TODO Currently assumes that values is scalar.

        double[] tWeights = weights.evaluate( context ).values;
        double[] tValues = values.evaluate( context ).values;

        if( scales == null )
        {
            return valueDomain.makeValue( dotProduct( tWeights, tValues ) );
        }
        else
        {
            double[] tScales = scales.evaluate( context ).values;
            return valueDomain.makeValue( dotProduct( tWeights, tValues, tScales ) );
        }
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        ArrayList<Evaluator<?>> variables = new ArrayList<Evaluator<?>>();

        variables.addAll( weights.getVariables() );
        variables.addAll( values.getVariables() );
        if( scales != null )
        {
            variables.addAll( scales.getVariables() );
        }

        return variables;
    }
}
