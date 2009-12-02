package fieldml.value;

import fieldml.annotations.SerializationBlocked;
import fieldml.domain.Domain;

public abstract class DomainValue<D extends Domain>
{
    @SerializationBlocked
    public final D domain;


    DomainValue( D domain )
    {
        this.domain = domain;
    }
}
