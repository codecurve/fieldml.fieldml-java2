package purgatory.fieldml.field.component;

import purgatory.fieldml.field.FieldValues;

public class IndexValueNamedComponent
    extends IndexComponent
{
    private final int valueIndex;

    private final int nameValueIndex;

    private final int nameValueComponentIndex;


    public IndexValueNamedComponent( int valueIndex, int nameValueIndex, int nameValueComponentIndex )
    {
        this.valueIndex = valueIndex;
        this.nameValueIndex = nameValueIndex;
        this.nameValueComponentIndex = nameValueComponentIndex;
    }


    @Override
    public int evaluate( FieldValues parameters )
    {
        int componentIndex = parameters.values.get( nameValueIndex ).indexValues[nameValueComponentIndex];
        
        return parameters.values.get( valueIndex ).indexValues[componentIndex];
    }
}
