package fieldml.domain;

import fieldmlx.annotations.SerializationAsString;
import fieldmlx.annotations.SerializationBlocked;

public abstract class Domain
{
    public final String name;

    @SerializationAsString
    public final EnsembleDomain componentDomain;

    @SerializationBlocked
    public final int componentCount;

    public Object units;


    Domain( String name, EnsembleDomain componentDomain )
    {
        this.name = name;
        this.componentDomain = componentDomain;

        if( componentDomain == null )
        {
            componentCount = 1;
        }
        else
        {
            assert componentDomain.componentCount == 1;
            componentCount = componentDomain.getValueCount();
        }
    }


    @Override
    public String toString()
    {
        return name;
    }
}
