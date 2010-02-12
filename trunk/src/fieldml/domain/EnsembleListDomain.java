package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.value.EnsembleListDomainValue;

public class EnsembleListDomain
    extends Domain
{
    @SerializationAsString
    public final EnsembleDomain elementDomain;


    public EnsembleListDomain( String name, EnsembleDomain elementDomain )
    {
        super( name );

        this.elementDomain = elementDomain;
    }


    public EnsembleListDomainValue makeValue( int... indexValues )
    {
        return new EnsembleListDomainValue( this, indexValues );
    }
}
