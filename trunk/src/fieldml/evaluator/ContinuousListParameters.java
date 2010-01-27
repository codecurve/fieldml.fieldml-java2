package fieldml.evaluator;

import fieldml.domain.ContinuousListDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousListDomainValue;

public class ContinuousListParameters
    extends TableEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    public ContinuousListParameters( String name, ContinuousListDomain valueDomain, EnsembleDomain ... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    public void setValue( int index, double ... values )
    {
        setValue( valueDomain.makeValue( values ), index );
    }


    public void setValue( int[] indexes, double ... values )
    {
        setValue( valueDomain.makeValue( values ), indexes );
    }
    

    public void setDefaultValue( double... values )
    {
        setDefaultValue( valueDomain.makeValue( values ) );
    }
}
