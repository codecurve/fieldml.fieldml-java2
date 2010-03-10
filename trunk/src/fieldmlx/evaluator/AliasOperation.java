package fieldmlx.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.value.DomainValues;

public class AliasOperation
    implements CompositionOperation
{
    @SerializationAsString
    public final Domain source;

    @SerializationAsString
    public final Domain destination;


    public AliasOperation( Domain source, Domain destination )
    {
        this.source = source;
        this.destination = destination;
    }


    @Override
    public void perform( DomainValues context )
    {
        context.copy( source, destination );
    }
}
