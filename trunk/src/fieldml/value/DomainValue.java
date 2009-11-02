package fieldml.value;

import fieldml.domain.Domain;

public abstract class DomainValue
{
    public final Domain domain;


    DomainValue( Domain domain )
    {
        this.domain = domain;
    }
}
