package fieldml.field;

import fieldml.domain.EnsembleDomain;
import fieldml.value.EnsembleDomainValue;

public class EnsembleMappingField
    extends MappingField<EnsembleDomain, EnsembleDomainValue>
{
    public EnsembleMappingField( String name, EnsembleDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain );
    }


    public void setValue( int value, int... keys )
    {
        setValue( EnsembleDomainValue.makeValue( valueDomain, value ), keys );
    }
}
