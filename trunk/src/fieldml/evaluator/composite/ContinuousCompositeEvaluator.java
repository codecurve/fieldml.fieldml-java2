package fieldml.evaluator.composite;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class ContinuousCompositeEvaluator
    extends CompositeEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    public ContinuousCompositeEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }


    public void importMappedField( ContinuousDomain valueDomain, ContinuousParameters sourceField, ContinuousEvaluator weightField,
        EnsembleDomain iteratedDomain )
    {
        // NOTE could extend this to iterate over multiple ensemble domains, or automatically detect which ensemble domains
        // to iterate over based on the domains over which weightField is declared.
        operations.add( new MappedImportOperation( valueDomain, sourceField, weightField, iteratedDomain ) );
    }


    public void importValue( EnsembleDomainValue value )
    {
        operations.add( new ValueOperation( value ) );
    }


    @Override
    protected ContinuousDomainValue onComposition( DomainValues localValues )
    {
        return localValues.get( valueDomain );
    }
}
