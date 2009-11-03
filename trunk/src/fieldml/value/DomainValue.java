package fieldml.value;

import fieldml.annotations.SerializeToString;
import fieldml.domain.Domain;

public abstract class DomainValue
{
    @SerializeToString
    public final Domain domain;


    DomainValue( Domain domain )
    {
        this.domain = domain;
    }
}
