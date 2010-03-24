package fieldmlx.evaluator;

import fieldml.domain.Domain;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class ValueOperation
    implements CompositionOperation
{
    public final Domain domain;

    public final DomainValue<?> value;


    public ValueOperation( Domain domain, DomainValue<?> value )
    {
        this.domain = domain;
        this.value = value;
    }


    @Override
    public void perform( DomainValues context )
    {
        context.set( domain, value );
    }
}
