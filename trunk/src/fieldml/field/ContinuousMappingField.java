package fieldml.field;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;

public class ContinuousMappingField
    extends MappingField<ContinuousDomain, ContinuousDomainValue>
{
    public ContinuousMappingField( String name, ContinuousDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    public void setValue( double value, int... keys )
    {
        setValue( ContinuousDomainValue.makeValue( valueDomain, value ), keys );
    }
}
