package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public abstract class AbstractEvaluator<D extends Domain, V extends DomainValue<D>>
{
    public final String name;

    @SerializationAsString
    public final D valueDomain;


    public AbstractEvaluator( String name, D valueDomain )
    {
        this.name = name;
        this.valueDomain = valueDomain;
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
