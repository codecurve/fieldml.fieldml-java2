package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.map.IndirectMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class MapEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final IndirectMap map;

    @SerializationAsString
    public final ContinuousEvaluator indexedValues;

    @SerializationAsString
    public final EnsembleDomain spannedDomain;


    public MapEvaluator( String name, ContinuousDomain valueDomain, IndirectMap map, ContinuousEvaluator indexedValues,
        EnsembleDomain spannedDomain )
    {
        super( name, valueDomain );

        this.map = map;
        this.indexedValues = indexedValues;
        this.spannedDomain = spannedDomain;
    }


    public MapEvaluator( String name, ContinuousDomain valueDomain, IndirectMap map, ContinuousEvaluator indexedValues )
    {
        this( name, valueDomain, map, indexedValues, null );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        double[] values;
        
        if( spannedDomain != null )
        {
            values = new double[spannedDomain.getValueCount()];
            
            DomainValues spanContext = new DomainValues( context );
            
            for( int i = 1; i <= spannedDomain.getValueCount(); i++ )
            {
                spanContext.set( spannedDomain, i );
                values[i] = map.evaluate( spanContext, indexedValues );
            }
        }
        else
        {
            values = new double[1];
            values[0] = map.evaluate( context, indexedValues );
        }

        return valueDomain.makeValue( values );
    }
}
