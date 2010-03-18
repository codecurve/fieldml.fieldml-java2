package fieldmlx.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.EnsembleValueSource;

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
        EnsembleDomainValue v = source.getValue( context, destination );
        context.set( destination, v.values );
    }
}
