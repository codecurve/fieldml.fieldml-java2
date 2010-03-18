package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.region.Region;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;

public class ImportedContinuous
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousEvaluator evaluator;

    public final SimpleMap<ContinuousDomain, ContinuousValueSource> continuousAliases;


    public ImportedContinuous( String localName, Region library, String evaluatorName )
    {
        super( localName, library.getContinuousEvaluator( evaluatorName ).getValueDomain() );

        continuousAliases = new SimpleMap<ContinuousDomain, ContinuousValueSource>();

        evaluator = library.getContinuousEvaluator( evaluatorName );
    }


    public void alias( ContinuousValueSource local, ContinuousDomain remote )
    {
        continuousAliases.put( remote, local );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<ContinuousDomain, ContinuousValueSource> e : continuousAliases )
        {
            localContext.alias( e.value, e.key );
        }

        return evaluator.getValue( localContext );
    }

}
