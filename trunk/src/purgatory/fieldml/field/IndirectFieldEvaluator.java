package purgatory.fieldml.field;

import java.util.Arrays;

import purgatory.fieldml.FieldML;
import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;


public class IndirectFieldEvaluator
    extends Evaluator
{
    private final int fieldValueIndex;

    private final int fieldValueComponentIndex;

    private final int[] parameterIndexes;
    
    private final int destinationIndex;
    
    private final FieldmlObjectManager<Field> manager;


    public IndirectFieldEvaluator( FieldmlObjectManager<Field> manager, String name, Domain domain, int fieldValueIndex, int fieldValueComponentIndex,
        int[] parameterIndexes, int destinationIndex )
    {
        super( name, domain );
        
        this.fieldValueIndex = fieldValueIndex;
        this.fieldValueComponentIndex = fieldValueComponentIndex;
        this.parameterIndexes = Arrays.copyOf( parameterIndexes, parameterIndexes.length );
        this.destinationIndex = destinationIndex;
        this.manager = manager;
    }


    @Override
    public void evaluate( FieldValues inputValues, int[] argumentIndexes, FieldValues localValues )
        throws FieldmlException
    {
        int fieldId = localValues.values.get( fieldValueIndex ).fieldIdValues[fieldValueComponentIndex];

        Field field = manager.get( fieldId );

        field.evaluate( localValues, parameterIndexes, localValues.values.get( destinationIndex ) );
    }


    public int[] getIndexes()
    {
        return parameterIndexes;
    }


    @Override
    public int getType()
    {
        return FieldML.PT_INDIRECT_VALUE;
    }
}
