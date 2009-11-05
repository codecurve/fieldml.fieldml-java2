package purgatory.fieldml.field;

import purgatory.fieldml.FieldML;
import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.FieldmlException;

public class ParameterEvaluator
    extends Evaluator
{
    private final int inputValueIndex;


    public ParameterEvaluator( String name, Domain domain, int inputValueIndex )
    {
        super( name, domain );

        this.inputValueIndex = inputValueIndex;
    }


    @Override
    public void evaluate( FieldValues inputValues, int[] argumentIndexes, FieldValues localValues )
        throws FieldmlException
    {
        localValues.values.get( inputValueIndex ).assign( inputValues.values.get( argumentIndexes[inputValueIndex] ) );
    }


    @Override
    public int getType()
    {
        return FieldML.PT_PARAMETER;
    }
}
