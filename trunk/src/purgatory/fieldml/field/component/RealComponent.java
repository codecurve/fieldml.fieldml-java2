package purgatory.fieldml.field.component;

import purgatory.fieldml.field.FieldValues;

public abstract class RealComponent
    extends Component
{
    public abstract double evaluate( FieldValues parameters );
}
