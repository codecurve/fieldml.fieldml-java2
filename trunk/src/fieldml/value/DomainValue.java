package fieldml.value;

import fieldml.annotations.SerializationBlocked;
import fieldml.domain.Domain;

public abstract class DomainValue
{
    @SerializationBlocked
    public final Domain domain;


    DomainValue( Domain domain )
    {
        this.domain = domain;
    }
}
