package fieldmlx.evaluator;

import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class ValueOperation
    implements CompositionOperation
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
