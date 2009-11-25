package purgatory.fieldml.exception;

import purgatory.fieldml.FieldML;

public class NoSuchFieldmlObjectException
    extends FieldmlException
{
    public NoSuchFieldmlObjectException( int id )
    {
        super( "No FieldML object exists with id " + id, FieldML.ERR_NO_SUCH_OBJECT );
    }
}
