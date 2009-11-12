package purgatory.fieldml.field;

import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.FieldmlException;

public abstract class Evaluator
{
    private final String name;

    private final Domain domain;


    public Evaluator( String name, Domain domain )
    {
        this.name = name;
        this.domain = domain;
    }


    public String getName()
    {
        return name;
    }


    public Domain getDomain()
    {
        return domain;
    }


    public abstract void evaluate( FieldValues inputValues, int[] argumentIndexes, FieldValues localValues )
        throws FieldmlException;


    public abstract int getType();
}
