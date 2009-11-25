package fieldml.field.composite;

import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class ValueImport
    implements FieldOperation
{
    public final DomainValue<?> value;


    public ValueImport( DomainValue<?> value )
    {
        this.value = value;
    }


    @Override
    public void perform( DomainValues values )
    {
        values.set( value );
    }
}
