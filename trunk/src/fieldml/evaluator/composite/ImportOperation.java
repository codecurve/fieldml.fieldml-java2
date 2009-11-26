package fieldml.evaluator.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class ImportOperation
    implements CompositeOperation
{
    @SerializationAsString
    public final AbstractEvaluator<?,?> field;


    public ImportOperation( AbstractEvaluator<?,?> field )
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
