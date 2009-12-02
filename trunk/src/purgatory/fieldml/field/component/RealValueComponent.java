package purgatory.fieldml.field.component;

import purgatory.fieldml.field.FieldValues;

public class RealValueComponent
    extends RealComponent
{
    private final int valueIndex;

    private final int valueComponentIndex;


    public RealValueComponent( int valueIndex, int valueComponentIndex )
    {
        this.valueIndex = valueIndex;
        this.valueComponentIndex = valueComponentIndex;
    }


    @Override
    public double evaluate( FieldValues parameters )
    {
        return parameters.values.get( valueIndex ).realValues[valueComponentIndex];
    }
}
