package fieldmlx.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public class ImportOperation
    implements CompositionOperation
{
    @SerializationAsString
    public final AbstractEvaluator<?,?> field;


    public ImportOperation( AbstractEvaluator<?,?> field )
    {
        this.field = field;
    }


    @Override
    public void perform( DomainValues context )
    {
        DomainValue<?> v = field.evaluate( context );
        if( v != null )
        {
            context.set( v );
        }
    }
}
