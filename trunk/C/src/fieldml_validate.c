#include <string.h>

#include "fieldml_api.h"
#include "fieldml_parse.h"
#include "fieldml_structs.h"

int validateEnsembleDomain( FieldmlParse *parse, FieldmlObject *object )
{
    if( object->object.ensembleDomain->boundsType == BOUNDS_UNKNOWN )
    {
        addError( parse, "EnsembleDomain has no bounds", object->name, NULL );
        return FML_ERR_INCOMPLETE_OBJECT;
    }

    return FML_ERR_NO_ERROR;
}

int validateFieldmlObject( FieldmlParse *parse, FieldmlObject *object )
{
    switch( object->type )
    {
    case FHT_ENSEMBLE_DOMAIN:
        return validateEnsembleDomain( parse, object );
    default:
        return FML_ERR_NO_ERROR;
    }
}
