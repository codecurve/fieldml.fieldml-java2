package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.EnsembleDomainValue;

public class EnsembleParameters
    extends TableEvaluator<EnsembleDomain, EnsembleDomainValue>
    implements EnsembleEvaluator
{
    public EnsembleParameters( String name, EnsembleDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    public void setValue( int value, int... keys )
    {
        setValue( valueDomain.makeValue( value ), keys );
    }


    public void setDefaultValue( int value )
    {
        setDefaultValue( valueDomain.makeValue( value ) );
    }
}
