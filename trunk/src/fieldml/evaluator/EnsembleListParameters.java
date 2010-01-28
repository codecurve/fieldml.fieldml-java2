package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
import fieldml.value.EnsembleListDomainValue;

/**
 * EnsembleListParameters is only defined over a single domain. To effect definition over multiple domains, use an
 * EnsembleListParameter object that interfaces multiple parameters to a single index into the corresponding
 * EnsembleListParameters object.
 */
public class EnsembleListParameters
    extends TableEvaluator<EnsembleListDomain, EnsembleListDomainValue>
    implements EnsembleListEvaluator
{
    public EnsembleListParameters( String name, EnsembleListDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    public void setValue( int index, int... values )
    {
        setValue( valueDomain.makeValue( values ), index );
    }


    public void setValue( int[] indexes, int... values )
    {
        setValue( valueDomain.makeValue( values ), indexes );
    }
}
