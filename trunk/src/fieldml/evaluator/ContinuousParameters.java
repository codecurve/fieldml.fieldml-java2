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


    public void setValue( int key, double ... values )
    {
        setValue( valueDomain.makeValue( values ), key );
    }


    public void setValue( int[] keys, double... values )
    {
        setValue( valueDomain.makeValue( values ), keys );
    }


    public void setDefaultValue( double... values )
    {
        setDefaultValue( valueDomain.makeValue( values ) );
    }
}
