package fieldml.field;

import fieldml.annotations.SerializeToString;
import fieldml.domain.Domain;
import fieldml.domain.MeshDomain;

public class FEMField
    extends Field
{
    @SerializeToString
    public final MeshDomain domain;
    
    @SerializeToString
    public final Domain valueDomain;


    public FEMField( String name, Domain valueDomain, MeshDomain domain )
    {
        super( name );
        
        this.domain = domain;
        this.valueDomain = valueDomain;
    }
}
