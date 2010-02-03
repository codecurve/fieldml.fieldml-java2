package fieldml.evaluator;

import fieldml.domain.ContinuousListDomain;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;

public class ContinuousListVariableEvaluator
    extends AbstractEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    public ContinuousListVariableEvaluator( String name, ContinuousListDomain valueDomain )
    {
        super( name, valueDomain ); 
    }


    @Override
    public ContinuousListDomainValue evaluate( DomainValues context )
    {
        ContinuousListEvaluator variable = context.getContinuousListVariable( name );

        return variable.evaluate( context );
    }
}
