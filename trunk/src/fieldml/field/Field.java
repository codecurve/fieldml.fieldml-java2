package fieldml.field;

import java.util.HashMap;
import java.util.Map;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.value.DomainValue;

public abstract class Field<D extends Domain, V extends DomainValue>
{
    public static final Map<String, Field<?, ?>> fields = new HashMap<String, Field<?, ?>>();

    public final String name;

    @SerializationAsString
    public final D valueDomain;


    public Field( String name, D valueDomain )
    {
        this.name = name;
        this.valueDomain = valueDomain;

        fields.put( name, this );
    }


    @Override
    public String toString()
    {
        return name;
    }


    public abstract V evaluate( DomainValue... input );
}
