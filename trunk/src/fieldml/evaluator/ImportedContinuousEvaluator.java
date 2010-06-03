package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleValueSource;
import fieldml.value.MeshValueSource;
import fieldmlx.annotations.SerializationAsString;
import fieldmlx.util.SimpleMap;
import fieldmlx.util.SimpleMapEntry;

public class ImportedContinuousEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousEvaluator evaluator;

    public final SimpleMap<ContinuousDomain, ContinuousValueSource> continuousAliases;

    public final SimpleMap<EnsembleDomain, EnsembleValueSource> ensembleAliases;

    public final SimpleMap<MeshDomain, MeshValueSource> meshAliases;


    public ImportedContinuousEvaluator( String localName, ContinuousEvaluator evaluator )
    {
        super( localName, evaluator.getValueDomain() );

        continuousAliases = new SimpleMap<ContinuousDomain, ContinuousValueSource>();
        ensembleAliases = new SimpleMap<EnsembleDomain, EnsembleValueSource>();
        meshAliases = new SimpleMap<MeshDomain, MeshValueSource>();

        this.evaluator = evaluator;
    }


    public void alias( ContinuousValueSource local, ContinuousDomain remote )
    {
        continuousAliases.put( remote, local );
    }


    public void alias( EnsembleValueSource local, EnsembleDomain remote )
    {
        ensembleAliases.put( remote, local );
    }


    public void alias( MeshValueSource local, MeshDomain remote )
    {
        meshAliases.put( remote, local );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<ContinuousDomain, ContinuousValueSource> e : continuousAliases )
        {
            localContext.alias( e.value, e.key );
        }
        for( SimpleMapEntry<EnsembleDomain, EnsembleValueSource> e : ensembleAliases )
        {
            localContext.alias( e.value, e.key );
        }
        for( SimpleMapEntry<MeshDomain, MeshValueSource> e : meshAliases )
        {
            localContext.alias( e.value, e.key );
        }

        return evaluator.getValue( localContext );
    }

}
