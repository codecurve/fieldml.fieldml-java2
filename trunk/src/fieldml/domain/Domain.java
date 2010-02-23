package fieldml.domain;

import fieldml.annotations.SerializationAsString;

public abstract class Domain
{
    public final String name;

    @SerializationAsString
    public final EnsembleDomain componentDomain;

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
