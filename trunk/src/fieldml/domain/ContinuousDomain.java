package fieldml.domain;

import java.util.HashMap;
import java.util.Map;

public class ContinuousDomain
    extends Domain
{
    public static final Map<String, ContinuousDomain> domains = new HashMap<String, ContinuousDomain>();
    
    public final int dimensions;


    public ContinuousDomain( String name, int dimensions )
    {
        super( name );

        this.dimensions = dimensions;

        domains.put( name, this );
    }
}
