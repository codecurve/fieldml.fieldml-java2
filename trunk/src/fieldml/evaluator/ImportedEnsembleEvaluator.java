package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class ImportedEnsembleEvaluator
    extends EnsembleEvaluator
{
    @SerializationAsString
    public final EnsembleEvaluator evaluator;

    public final SimpleMap<ContinuousDomain, ContinuousValueSource> continuousAliases;


    public ImportedEnsembleEvaluator( String localName, EnsembleEvaluator evaluator )
    {
        super( localName, evaluator.getValueDomain() );

        continuousAliases = new SimpleMap<ContinuousDomain, ContinuousValueSource>();

        this.evaluator = evaluator;
    }


    public void alias( ContinuousValueSource local, ContinuousDomain remote )
    {
        continuousAliases.put( remote, local );
    }


    @Override
    public EnsembleDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<ContinuousDomain, ContinuousValueSource> e : continuousAliases )
        {
            localContext.alias( e.value, e.key );
        }

        return evaluator.getValue( localContext );
    }

}
