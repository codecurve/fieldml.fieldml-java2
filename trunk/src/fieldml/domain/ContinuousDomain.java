package fieldml.domain;


public class ContinuousDomain
    extends Domain
{
    public final int dimensions;


    public ContinuousDomain( String name, int dimensions )
    {
        super( name );

        this.dimensions = dimensions;
    }
}
