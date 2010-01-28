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


    public MapEvaluator( String name, ContinuousDomain valueDomain, IndirectMap map, ContinuousEvaluator indexedValues,
        EnsembleDomain spannedDomain )
    {
        super( name, valueDomain );

        this.map = map;
        this.indexedValues = indexedValues;
    }


    public MapEvaluator( String name, ContinuousDomain valueDomain, IndirectMap map, ContinuousEvaluator indexedValues )
    {
        this( name, valueDomain, map, indexedValues, null );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        double[] values;
        
        values = new double[1];
        values[0] = map.evaluate( context, indexedValues );

        return valueDomain.makeValue( values );
    }
}
