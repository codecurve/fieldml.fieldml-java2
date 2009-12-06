package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;

public class ContinuousParameters
    extends TableEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    public ContinuousParameters( String name, ContinuousDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    public void setValue( double value, int... keys )
    {
        setValue( valueDomain.makeValue( value ), keys );
    }


    public void setDefaultValue( double... values )
    {
        setDefaultValue( valueDomain.makeValue( values ) );
    }
}
