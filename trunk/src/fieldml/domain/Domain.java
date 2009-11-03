package fieldml.domain;

import java.util.HashMap;
import java.util.Map;

public abstract class Domain
{
    public static final Map<String, Domain> domains = new HashMap<String, Domain>();

    public final String name;
    
    public Object units;


    Domain( String name )
    {
        this.name = name;

        domains.put( name, this );
    }
    
    
    public String toString()
    {
        return name;
    }
}
