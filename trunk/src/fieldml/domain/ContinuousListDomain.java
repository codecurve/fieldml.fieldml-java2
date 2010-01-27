package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.value.ContinuousListDomainValue;

public class ContinuousListDomain
    extends Domain
{
    @SerializationAsString
    public final ContinuousDomain elementDomain;
    
    public ContinuousListDomain( String name, ContinuousDomain elementDomain )
    {
        super( name );
        
        this.elementDomain = elementDomain;
    }



    public ContinuousListDomainValue makeValue( double ... values )
    {
        return new ContinuousListDomainValue( this, values );
    }
}
