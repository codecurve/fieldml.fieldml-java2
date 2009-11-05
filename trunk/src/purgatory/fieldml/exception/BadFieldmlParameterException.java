package purgatory.fieldml.exception;

import purgatory.fieldml.FieldML;

public class BadFieldmlParameterException
    extends FieldmlException
{
    public BadFieldmlParameterException()
    {
        super( "Bad parameter", FieldML.ERR_BAD_PARAMETER );
    }
}
