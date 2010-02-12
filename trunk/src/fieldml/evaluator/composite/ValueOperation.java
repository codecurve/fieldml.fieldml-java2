package fieldml.evaluator.composite;

import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class ValueOperation
    implements CompositeOperation
{
    public final DomainValue<?> value;


    public ValueOperation( DomainValue<?> value )
    {
        this.value = value;
    }


    @Override
    public void perform( DomainValues values )
    {
        values.set( value );
    }
}
