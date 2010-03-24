package fieldmlx.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleValueSource;
import fieldmlx.annotations.SerializationAsString;

public class EnsembleAliasOperation
    implements CompositionOperation
{
    @SerializationAsString
    public final EnsembleValueSource source;

    @SerializationAsString
    public final EnsembleDomain destination;


    public EnsembleAliasOperation( EnsembleValueSource source, EnsembleDomain destination )
    {
        this.source = source;
        this.destination = destination;
    }


    @Override
    public void perform( DomainValues context )
    {
        context.alias( source, destination );
    }
}
