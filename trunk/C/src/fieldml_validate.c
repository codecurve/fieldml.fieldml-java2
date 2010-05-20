#include <string.h>

#include "fieldml_api.h"
#include "fieldml_structs.h"

int validateEnsembleDomain( FieldmlRegion *region, FieldmlObject *object )
{
    if( object->object.ensembleDomain->boundsType == BOUNDS_UNKNOWN )
    {
        addError( region, "EnsembleDomain has no bounds", object->name, NULL );
        return FML_ERR_INCOMPLETE_OBJECT;
    }

    return FML_ERR_NO_ERROR;
}

int validateFieldmlObject( FieldmlRegion *region, FieldmlObject *object )
{
    switch( object->type )
    {
    case FHT_ENSEMBLE_DOMAIN:
        return validateEnsembleDomain( region, object );
    default:
        return FML_ERR_NO_ERROR;
    }
}
