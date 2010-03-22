package fieldmlx.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;

public class ContinuousAliasOperation
    implements CompositionOperation
{
    @SerializationAsString
    public final ContinuousValueSource source;

    @SerializationAsString
    public final ContinuousDomain destination;


    public ContinuousAliasOperation( ContinuousValueSource source, ContinuousDomain destination )
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
