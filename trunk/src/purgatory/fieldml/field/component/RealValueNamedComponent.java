package purgatory.fieldml.field.component;

import purgatory.fieldml.field.FieldValues;

public class RealValueNamedComponent
    extends RealComponent
{
    private final int valueIndex;

    private final int nameValueIndex;

    private final int nameValueComponentIndex;


    public RealValueNamedComponent( int valueIndex, int nameValueIndex, int nameValueComponentIndex )
    {
        this.valueIndex = valueIndex;
        this.nameValueIndex = nameValueIndex;
        this.nameValueComponentIndex = nameValueComponentIndex;
    }


    @Override
    public double evaluate( FieldValues parameters )
    {
        int componentIndex = parameters.values.get( nameValueIndex ).indexValues[nameValueComponentIndex];
        
        return parameters.values.get( valueIndex ).realValues[componentIndex];
    }
}
