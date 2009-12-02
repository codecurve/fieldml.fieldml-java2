package purgatory.fieldml.field.component.evaluated;

import purgatory.fieldml.field.FieldValues;

public class EvaluateAddition
    implements Evaluator
{
    private final Evaluator value1;

    private final Evaluator value2;

    private final double constValue1;

    private final double constValue2;


    // Although EvaluateConst could be implemented, it's a high overhead for a definitively trivial
    // operation. As such, const value functionality has been folded into each operation evaluator.
    public EvaluateAddition( Evaluator value1, Evaluator value2 )
    {
        this.value1 = value1;
        this.value2 = value2;
        this.constValue1 = 0;
        this.constValue2 = 0;
    }


    public EvaluateAddition( double constValue1, Evaluator value2 )
    {
        this.value1 = null;
        this.value2 = value2;
        this.constValue1 = constValue1;
        this.constValue2 = 0;
    }


    public EvaluateAddition( Evaluator value1, double constValue2 )
    {
        this.value1 = value1;
        this.value2 = null;
        this.constValue1 = 0;
        this.constValue2 = constValue2;
    }


    // Trivial case added for completeness. The API implementation should detect when this
    // happens, and optimize it out.
    public EvaluateAddition( double constValue1, double constValue2 )
    {
        this.value1 = null;
        this.value2 = null;
        this.constValue1 = constValue1;
        this.constValue2 = constValue2;
    }


    @Override
    public double evaluate( FieldValues values )
    {
        if( value1 == null )
        {
            if( value2 == null )
            {
                return constValue1 + constValue2;
            }
            else
            {
                return constValue1 + value2.evaluate( values );
            }
        }
        else
        {
            if( value2 == null )
            {
                return value1.evaluate( values ) + constValue2;
            }
            else
            {
                return value1.evaluate( values ) + value2.evaluate( values );
            }
        }
    }
}
