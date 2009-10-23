package purgatory.fieldml.field.component;

import purgatory.fieldml.field.FieldValues;

public abstract class IndexComponent
    extends Component
{
    public abstract int evaluate( FieldValues parameters );
}
