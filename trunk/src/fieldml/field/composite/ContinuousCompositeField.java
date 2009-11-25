package fieldml.field.composite;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousParameters;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

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


    public void importMappedField( ContinuousParameters sourceField, ContinuousParameters weightField, EnsembleDomain iteratedDomain )
    {
        //NOTE could extend this to iterate over multiple ensemble domains, or automatically detect which ensemble domains
        //to iterate over based on the domains over which weightField is declared.
        operations.add( new FieldMappedImport( sourceField, weightField, iteratedDomain ) );
    }


    public void importValue( EnsembleDomainValue value )
    {
        operations.add( new ValueImport( value ) );
    }
}
