#include <stdlib.h>
#include <stdio.h>

#include "int_table.h"
#include "string_table.h"
#include "fieldml_api.h"
#include "fieldml_parse.h"
#include "fieldml_sax.h"
#include "fieldml_structs.h"


//========================================================================
//
// Utility
//
//========================================================================

static FieldmlParse *handleToParse( FmlParseHandle handle )
{
    return (FieldmlParse*)(handle<<2);
}


static FmlParseHandle parseToHandle( FieldmlParse *parse )
{
    return ((int)parse)>>2;
}


static int getTotal( FieldmlParse *parse, FieldmlHandleType type )
{
    int count, i, total;
    FieldmlObject *object;

    total = 0;
    count = getSimpleListCount( parse->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
        if( object->type == type )
        {
            total++;
        }
    }

    return total;
}


static int getNthHandle( FieldmlParse *parse, FieldmlHandleType type, int index )
{
    int count, i;
    FieldmlObject *object;

    if( index <= 0 )
    {
        return FML_INVALID_HANDLE;
    }

    count = getSimpleListCount( parse->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
        if( object->type != type )
        {
            continue;
        }
        
        index--;
        if( index == 0 )
        {
            return i;
        }
    }

    return FML_INVALID_HANDLE;
}


static FieldmlObject *getNthObject( FieldmlParse *parse, FieldmlHandleType type, int index )
{
    int handle = getNthHandle( parse, type, index );

    return getSimpleListEntry( parse->objects, handle );
}


static IntTable *getEntryIntTable( FieldmlObject *object )
{
    if( object == NULL )
    {
        return NULL;
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        return object->object.aggregate->evaluators;
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return object->object.piecewise->evaluators;
    }

    return NULL;
}


static FmlObjectHandle hackToHandle( void *hack )
{
    if( hack == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    return (int)( hack ) - 1;
}


static int cappedCopy( char *source, char *buffer, int bufferLength )
{
    int length;
    
    if( bufferLength <= 0 )
    {
        return 0;
    }
    
    length = strlen( source );
    
    if( length >= bufferLength )
    {
        length = ( bufferLength - 1 );
    }
    
    memcpy( buffer, source, length );
    buffer[length] = 0;
    
    return length;
}
//========================================================================
//
// API
//
//========================================================================

FmlParseHandle fmlParseFile( char *filename )
{
    FieldmlParse *parse = parseFieldmlFile( filename );

    return parseToHandle( parse );
}


void fmlDestroyParse( FmlParseHandle handle )
{
    FieldmlParse *parse = handleToParse( handle );
    
    destroyFieldmlParse( parse );
}


int fmlGetErrorCount( FmlParseHandle handle )
{
    FieldmlParse *parse = handleToParse( handle );

    return getSimpleListCount( parse->errors );
}


char *fmlGetError( FmlParseHandle handle, int index )
{
    FieldmlParse *parse = handleToParse( handle );

    return (char *)getSimpleListEntry( parse->errors, index - 1 );
}


int fmlCopyError( FmlParseHandle handle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( fmlGetError( handle, index ), buffer, bufferLength );
}


int fmlGetObjectCount( FmlParseHandle handle, FieldmlHandleType type )
{
    FieldmlParse *parse = handleToParse( handle );

    if( type == FHT_UNKNOWN )
    {
        return -1;
    }

    return getTotal( parse, type );
}


FmlObjectHandle fmlGetObjectHandle( FmlParseHandle handle, FieldmlHandleType type, int index )
{
    FieldmlParse *parse = handleToParse( handle );

    return getNthHandle( parse, type, index );
}


FieldmlHandleType fmlGetObjectType( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FHT_UNKNOWN;
    }
    
    return object->type;
}

int fmlGetMarkupCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return -1;
    }
    
    return getStringTableCount( object->markup );
}


char *fmlGetMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }
    
    return getStringTableEntryName( object->markup, index - 1 );
}


int fmlCopyMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( fmlGetMarkupAttribute( handle, objectHandle, index ), buffer, bufferLength );
}


char *fmlGetMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }
    
    return (char*)getStringTableEntryData( object->markup, index - 1 );
}


int fmlCopyMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( fmlGetMarkupValue( handle, objectHandle, index ), buffer, bufferLength );
}


FmlObjectHandle fmlGetDomainComponentEnsemble( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    switch( object->type )
    {
    case FHT_ENSEMBLE_DOMAIN:
        return object->object.ensembleDomain->componentDomain;
    case FHT_CONTINUOUS_DOMAIN:
        return object->object.continuousDomain->componentDomain;
    default:
        break;
    }

    return FML_INVALID_HANDLE;
}


DomainBoundsType fmlGetDomainBoundsType( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_ENSEMBLE_DOMAIN ) )
    {
        return BOUNDS_UNKNOWN;
    }

    return object->object.ensembleDomain->boundsType;
}


int fmlGetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_ENSEMBLE_DOMAIN ) )
    {
        return -1;
    }

    if( object->object.ensembleDomain->boundsType != BOUNDS_DISCRETE_CONTIGUOUS )
    {
        return -1;
    }

    return object->object.ensembleDomain->bounds.contiguous.count;
}


FmlObjectHandle fmlGetMeshElementDomain( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return FML_INVALID_HANDLE;
    }

    return object->object.meshDomain->elementDomain;
}


char *fmlGetMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return NULL;
    }

    return (char*)getIntTableEntry( object->object.meshDomain->shapes, elementNumber );
}


int fmlCopyMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber, char *buffer, int bufferLength )
{
    return cappedCopy( fmlGetMeshElementShape( handle, objectHandle, elementNumber ), buffer, bufferLength );
}


int fmlGetMeshConnectivityCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return -1;
    }

    return getIntTableCount( object->object.meshDomain->connectivity );
}


FmlObjectHandle fmlGetMeshConnectivityDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return FML_INVALID_HANDLE;
    }

    return getIntTableEntryName( object->object.meshDomain->connectivity, index - 1 );
}


FmlObjectHandle fmlGetMeshConnectivitySource( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return FML_INVALID_HANDLE;
    }

    return hackToHandle( getIntTableEntryData( object->object.meshDomain->connectivity, index - 1 ) );
}


FmlObjectHandle fmlGetMeshXiDomain( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return -1;
    }

    return object->object.meshDomain->xiDomain;
}


char *fmlGetObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }

    return object->name;
}


int fmlCopyObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength )
{
    return cappedCopy( fmlGetObjectName( handle, objectHandle ), buffer, bufferLength );
}


FmlObjectHandle fmlGetValueDomain( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        return object->object.parameters->valueDomain;
    }
    else if( object->type == FHT_CONTINUOUS_IMPORT )
    {
        return object->object.continuousImport->valueDomain;
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        return object->object.aggregate->valueDomain;
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return object->object.piecewise->valueDomain;
    }
    else if( ( object->type == FHT_CONTINUOUS_VARIABLE ) || ( object->type == FHT_ENSEMBLE_VARIABLE ) )
    {
        return object->object.variable->valueDomain;
    }
    else if( object->type == FHT_CONTINUOUS_DEREFERENCE )
    {
        return object->object.dereference->valueDomain;
    }

    return FML_INVALID_HANDLE;
}


DataDescriptionType fmlGetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return DESCRIPTION_UNKNOWN;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        return object->object.parameters->descriptionType;
    }

    return DESCRIPTION_UNKNOWN;
}


int fmlGetSemidenseIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle, int isSparse )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return -1;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return -1;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return -1;
    }

    if( isSparse )
    {
        return getSimpleListCount( object->object.parameters->dataDescription.semidense->sparseIndexes );
    }
    else
    {
        return getSimpleListCount( object->object.parameters->dataDescription.semidense->denseIndexes );
    }
}


FmlObjectHandle fmlGetSemidenseIndex( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, int isSparse )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    void *hack;

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return FML_INVALID_HANDLE;
    }

    if( isSparse )
    {
        hack = getSimpleListEntry( object->object.parameters->dataDescription.semidense->sparseIndexes, index - 1 );
    }
    else
    {
        hack = getSimpleListEntry( object->object.parameters->dataDescription.semidense->denseIndexes, index - 1 );
    }

    return hackToHandle( hack );
}



int fmlGetEvaluatorCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    IntTable *table = getEntryIntTable( object );

    if( table == NULL )
    {
        return -1;
    }

    return getIntTableCount( table );
}


int fmlGetEvaluatorElement( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    IntTable *table = getEntryIntTable( object );

    if( table == NULL )
    {
        return -1;
    }

    return getIntTableEntryName( table, index - 1 );
}


FmlObjectHandle fmlGetEvaluatorHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    IntTable *table = getEntryIntTable( object );

    if( table == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    return hackToHandle( getIntTableEntryData( table, index - 1 ) );
}


char *fmlGetImportRemoteName( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }

    if( object->type != FHT_CONTINUOUS_IMPORT )
    {
        return NULL;
    }

    return object->object.continuousImport->remoteName;
}


int fmlCopyImportRemoteName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength )
{
    return cappedCopy( fmlGetImportRemoteName( handle, objectHandle ), buffer, bufferLength );
}


int fmlGetImportAliasCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return -1;
    }

    if( object->type != FHT_CONTINUOUS_IMPORT )
    {
        return -1;
    }

    return getIntTableCount( object->object.continuousImport->aliases );
}


FmlObjectHandle fmlGetImportAliasLocalHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->type != FHT_CONTINUOUS_IMPORT )
    {
        return FML_INVALID_HANDLE;
    }

    return getIntTableEntryName( object->object.continuousImport->aliases, index - 1 );
}


FmlObjectHandle fmlGetImportAliasRemoteHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->type != FHT_CONTINUOUS_IMPORT )
    {
        return FML_INVALID_HANDLE;
    }

    return hackToHandle( getIntTableEntryData( object->object.continuousImport->aliases, index - 1 ) );
}


int fmlGetIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return -1;
    }
    
    if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return 1;
    }
    else if( ( object->type == FHT_CONTINUOUS_PARAMETERS ) || ( object->type == FHT_ENSEMBLE_PARAMETERS ) )
    {
        int count1, count2;
        
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            count1 = getSimpleListCount( object->object.parameters->dataDescription.semidense->sparseIndexes );
            count2 = getSimpleListCount( object->object.parameters->dataDescription.semidense->denseIndexes );
            return count1 + count2;
        }
        
        return -1;
    }
    
    return -1;
}


FmlObjectHandle fmlGetIndexDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }
    
    if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        if( index == 1 )
        {
            return object->object.piecewise->indexDomain;
        }
        
        return FML_INVALID_HANDLE;
    }
    else if( ( object->type == FHT_CONTINUOUS_PARAMETERS ) || ( object->type == FHT_ENSEMBLE_PARAMETERS ) )
    {
        int count;
        
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            count = getSimpleListCount( object->object.parameters->dataDescription.semidense->sparseIndexes );
            
            if( index <= count )
            {
                return hackToHandle( getSimpleListEntry( object->object.parameters->dataDescription.semidense->sparseIndexes, index - 1 ) );
            }
            else
            {
                index -= count;
                return hackToHandle( getSimpleListEntry( object->object.parameters->dataDescription.semidense->denseIndexes, index - 1 ) );
            }
        }
        
        return FML_INVALID_HANDLE;
    }
    
    return FML_INVALID_HANDLE;
}
