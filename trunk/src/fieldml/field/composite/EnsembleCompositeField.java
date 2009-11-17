package fieldml.field.composite;

import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsembleCompositeField
    extends CompositeField<EnsembleDomain, EnsembleDomainValue>
{
    public EnsembleCompositeField( String name, EnsembleDomain valueDomain, Domain[] parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues input )
    {
        DomainValues localValues = new DomainValues( input );

        apply( localValues );

        return localValues.get( valueDomain );
    }
}
