package purgatory.fieldml.field.component.evaluated;

import purgatory.fieldml.field.FieldValues;

public class EvaluateComponent
    implements Evaluator
{
    private final int parameterIndex;

    private final int componentIndex;


    public EvaluateComponent( int parameterIndex, int componentIndex )
    {
        this.parameterIndex = parameterIndex;
        this.componentIndex = componentIndex;
    }


    @Override
    public double evaluate( FieldValues values )
    {
        //Evaluators are only used with real-valued domains
        return values.values.get( parameterIndex ).realValues[componentIndex];
    }
}
