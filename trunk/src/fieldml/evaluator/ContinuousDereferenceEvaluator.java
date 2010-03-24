package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.EnsembleValueSource;

public class ContinuousDereferenceEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final EnsembleValueSource valueIndexes;

    @SerializationAsString
    public final ContinuousValueSource valueSource;

    private final boolean iterateIndexes;

    private final EnsembleDomain iteratedDomain;


    public ContinuousDereferenceEvaluator( String name, ContinuousDomain valueDomain, EnsembleValueSource valueIndexes,
        ContinuousValueSource valueSource )
    {
        super( name, valueDomain );

        this.valueIndexes = valueIndexes;
        this.valueSource = valueSource;
        this.iteratedDomain = valueDomain.componentDomain;

        EnsembleDomain indexDomain = valueIndexes.getValueDomain();

        assert valueSource.getValueDomain().componentCount == 1;
        
        iterateIndexes = indexDomain.componentDomain == null;
        if( indexDomain.componentDomain != null )
        {
            assert valueDomain.componentDomain == indexDomain.componentDomain;
        }
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        int[] indexes = new int[iteratedDomain.getValueCount()];
        double[] values = new double[indexes.length];

        if( !iterateIndexes )
        {
            indexes = valueIndexes.getValue( context ).values;
        }

        for( int i = 0; i < indexes.length; i++ )
        {
            if( iterateIndexes )
            {
                context.set( iteratedDomain, i + 1 );
                EnsembleDomainValue v = valueIndexes.getValue( context );
                if( v == null )
                {
                    return null;
                }
                indexes[i] = v.values[0];
            }
            context.set( valueIndexes.getValueDomain().baseDomain, indexes[i] );
            values[i] = valueSource.getValue( context ).values[0];
        }

        return valueDomain.makeValue( values );
    }
}
