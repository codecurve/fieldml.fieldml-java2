package purgatory.fieldml.field.component;

import purgatory.fieldml.field.FieldValues;

public class IndexValueComponent
    extends IndexComponent
{
    private final int valueIndex;

    private final int valueComponentIndex;


    public IndexValueComponent( int valueIndex, int valueComponentIndex )
    {
        this.valueIndex = valueIndex;
        this.valueComponentIndex = valueComponentIndex;
    }


    @Override
    public int evaluate( FieldValues parameters )
    {
        return parameters.values.get( valueIndex ).indexValues[valueComponentIndex];
    }
}
