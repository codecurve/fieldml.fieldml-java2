package purgatory.fieldml.exception;

import purgatory.fieldml.FieldML;

public class WrongFieldmlObjectTypeException
    extends FieldmlException
{
    public WrongFieldmlObjectTypeException()
    {
        super( "Wrong FieldML object type", FieldML.ERR_WRONG_OBJECT_TYPE );
    }
}
