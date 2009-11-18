package fieldml.field;

import fieldml.domain.EnsembleDomain;
import fieldml.value.EnsembleDomainValue;

public class EnsembleParameters
    extends MappingField<EnsembleDomain, EnsembleDomainValue>
{
    public EnsembleParameters( String name, EnsembleDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    public void setValue( int value, int... keys )
    {
        setValue( EnsembleDomainValue.makeValue( valueDomain, value ), keys );
    }


    public void setDefaultValue( int value )
    {
        setValue( EnsembleDomainValue.makeValue( valueDomain, value ) );
    }
}
