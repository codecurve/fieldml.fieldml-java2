package fieldml.field;

import java.util.HashMap;
import java.util.Map;

import fieldml.annotations.SerializeToString;
import fieldml.domain.Domain;
import fieldml.value.DomainValue;

public abstract class Field<D extends DomainValue>
{
    public static final Map<String, Field<?>> fields = new HashMap<String, Field<?>>();

    public final String name;

    @SerializeToString
    public final Domain valueDomain;


    public Field( String name, Domain valueDomain )
    {
        this.name = name;
        this.valueDomain = valueDomain;

        fields.put( name, this );
    }


    public String toString()
    {
        return name;
    }


    public abstract D evaluate( DomainValue... input );
}
