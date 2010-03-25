package fieldml.evaluator;

import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldmlx.annotations.SerializationAsString;
import fieldmlx.evaluator.Markup;

public abstract class AbstractEvaluator<D extends Domain, V extends DomainValue<D>>
    implements MarkedUp
{
    public final String name;

    @SerializationAsString
    public final D valueDomain;

    public final Markup markup;


    public AbstractEvaluator( String name, D valueDomain )
    {
        this.name = name;
        this.valueDomain = valueDomain;

        markup = new Markup();
    }


    @Override
    public void set( String attribute, String value )
    {
        markup.put( attribute, value );
    }


    @Override
    public String get( String attribute )
    {
        return markup.get( attribute );
    }


    @Override
    public boolean has( String attribute, String value )
    {
        return markup.has( attribute, value );
    }


    @Override
    public String toString()
    {
        return name;
    }


    public final D getValueDomain()
    {
        return valueDomain;
    }


    public final String getName()
    {
        return name;
    }


    public abstract V getValue( DomainValues context, D domain, EnsembleDomain indexDomain );


    public abstract V getValue( DomainValues context );


    public final V getValue( DomainValues context, D domain )
    {
        return getValue( context, domain, null );
    }
}
