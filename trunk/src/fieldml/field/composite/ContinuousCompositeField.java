package fieldml.field.composite;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousCompositeField
    extends CompositeField<ContinuousDomain, ContinuousDomainValue>
{
    public ContinuousCompositeField( String name, ContinuousDomain valueDomain, Domain... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues input )
    {
        DomainValues localValues = new DomainValues( input );

        apply( localValues );

        return localValues.get( valueDomain );
    }
}
