package purgatory.fieldml.field;

import java.util.Arrays;

import purgatory.fieldml.FieldML;
import purgatory.fieldml.exception.FieldmlException;


public class FieldEvaluator
    extends Evaluator
{
    private final Field field;

    private final int[] fieldParameterIndexes;

    private final int destinationIndex;


    public FieldEvaluator( String name, Field field, int[] fieldParameterIndexes, int destinationIndex )
    {
        super( name, field.valueDomain );

        this.field = field;
        this.fieldParameterIndexes = Arrays.copyOf( fieldParameterIndexes, field.getParameterCount() );
        this.destinationIndex = destinationIndex;
    }


    @Override
    public void evaluate( FieldValues inputValues, int[] inputParameterIndexes, FieldValues localValues )
        throws FieldmlException
    {
        field.evaluate( localValues, fieldParameterIndexes, localValues.values.get( destinationIndex ) );
    }


    public Field getField()
    {
        return field;
    }


    public int[] getIndexes()
    {
        return fieldParameterIndexes;
    }


    @Override
    public int getType()
    {
        return FieldML.PT_DIRECT_VALUE;
    }
}
