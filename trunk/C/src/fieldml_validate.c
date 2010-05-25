#include <string.h>

#include "fieldml_api.h"
#include "fieldml_structs.h"

int validateEnsembleDomain( FieldmlRegion *region, FieldmlObject *object )
{
    if( object->object.ensembleDomain->boundsType == BOUNDS_UNKNOWN )
    {
        logError( region, "EnsembleDomain has no bounds", object->name, NULL );
        return FML_ERR_INCOMPLETE_OBJECT;
    }
    else if( object->object.ensembleDomain->boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
    {
        if( object->object.ensembleDomain->bounds.contiguous.count <= 0 )
        {
            return FML_ERR_MISCONFIGURED_OBJECT;
        }
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
