package fieldml.field.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.field.Field;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class FieldImport
    implements FieldOperation
{
    @SerializationAsString
    public final Field<?,?> field;


    public FieldImport( Field<?,?> field )
    {
        this.field = field;
    }


    @Override
    public void perform( DomainValues values )
    {
        DomainValue<?> v = field.evaluate( values );
        if( v != null )
        {
            values.set( v );
        }
    }
}
