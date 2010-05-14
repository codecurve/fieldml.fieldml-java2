#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "int_table.h"
#include "string_table.h"
#include "fieldml_api.h"
#include "fieldml_parse.h"
#include "fieldml_sax.h"
#include "fieldml_structs.h"
#include "fieldml_write.h"
#include "fieldml_validate.h"


//========================================================================
//
// Utility
//
//========================================================================

static FieldmlParse *handleToParse( FmlParseHandle handle )
{
    return (FieldmlParse*)handle;
}


static FmlParseHandle parseToHandle( FieldmlParse *parse )
{
    return (FmlParseHandle)parse;
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


static int getNamedHandle( FieldmlParse *parse, const char *name )
{
    int count, i;
    FieldmlObject *object;

    count = getSimpleListCount( parse->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
        if( strcmp( object->name, name ) == 0 )
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


static int cappedCopy( const char *source, char *buffer, int bufferLength )
{
    int length;
    
    if( ( bufferLength <= 0 ) || ( source == NULL ) )
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

FmlParseHandle Fieldml_ParseFile( const char *filename )
{
    FieldmlParse *parse = parseFieldmlFile( filename );

    return parseToHandle( parse );
}


FmlParseHandle Fieldml_Create()
{
    FieldmlParse *parse = createFieldmlParse();

    return parseToHandle( parse );
}


int Fieldml_WriteFile( FmlParseHandle handle, const char *filename )
{
    FieldmlParse *parse = handleToParse( handle );

    return writeFieldmlFile( parse, filename );
}


void Fieldml_DestroyParse( FmlParseHandle handle )
{
    FieldmlParse *parse = handleToParse( handle );
    
    destroyFieldmlParse( parse );
}


int Fieldml_GetErrorCount( FmlParseHandle handle )
{
    FieldmlParse *parse = handleToParse( handle );

    return getSimpleListCount( parse->errors );
}


const char * Fieldml_GetError( FmlParseHandle handle, int index )
{
    FieldmlParse *parse = handleToParse( handle );

    return (const char *)getSimpleListEntry( parse->errors, index - 1 );
}


int Fieldml_CopyError( FmlParseHandle handle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetError( handle, index ), buffer, bufferLength );
}


int Fieldml_GetObjectCount( FmlParseHandle handle, FieldmlHandleType type )
{
    FieldmlParse *parse = handleToParse( handle );

    if( type == FHT_UNKNOWN )
    {
        return -1;
    }

    return getTotal( parse, type );
}


FmlObjectHandle Fieldml_GetObjectHandle( FmlParseHandle handle, FieldmlHandleType type, int index )
{
    FieldmlParse *parse = handleToParse( handle );

    return getNthHandle( parse, type, index );
}


FmlObjectHandle Fieldml_GetNamedObjectHandle( FmlParseHandle handle, const char * name )
{
    FieldmlParse *parse = handleToParse( handle );

    return getNamedHandle( parse, name );
}


FieldmlHandleType Fieldml_GetObjectType( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FHT_UNKNOWN;
    }
    
    return object->type;
}


int Fieldml_SetMarkup(  FmlParseHandle handle, FmlObjectHandle objectHandle, const char * attribute, const char * value )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    
    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }
    
    setStringTableEntry( object->markup, attribute, _strdup( value ), free );

    return FML_ERR_NO_ERROR;
}


int Fieldml_GetMarkupCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return -1;
    }
    
    return getStringTableCount( object->markup );
}


int Fieldml_ValidateObject( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_ERR_NO_ERROR;
    }
    
    return validateFieldmlObject( parse, object );
}


const char * Fieldml_GetMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }
    
    return getStringTableEntryName( object->markup, index - 1 );
}


int Fieldml_CopyMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMarkupAttribute( handle, objectHandle, index ), buffer, bufferLength );
}


const char * Fieldml_GetMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }
    
    return (const char*)getStringTableEntryData( object->markup, index - 1 );
}


int Fieldml_CopyMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMarkupValue( handle, objectHandle, index ), buffer, bufferLength );
}


const char * Fieldml_GetMarkupAttributeValue( FmlParseHandle handle, FmlObjectHandle objectHandle, const char * attribute )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }
    
    return (const char*)getStringTableEntry( object->markup, attribute );
}


int Fieldml_CopyMarkupAttributeValue( FmlParseHandle handle, FmlObjectHandle objectHandle, const char * attribute, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMarkupAttributeValue( handle, objectHandle, attribute ), buffer, bufferLength );
}


FmlObjectHandle Fieldml_GetDomainComponentEnsemble( FmlParseHandle handle, FmlObjectHandle objectHandle )
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


DomainBoundsType Fieldml_GetDomainBoundsType( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_ENSEMBLE_DOMAIN ) )
    {
        return BOUNDS_UNKNOWN;
    }

    return object->object.ensembleDomain->boundsType;
}


int Fieldml_GetEnsembleDomainElementCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_ENSEMBLE_DOMAIN ) )
    {
        return -1;
    }

    if( object->object.ensembleDomain->boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
    {
        return object->object.ensembleDomain->bounds.contiguous.count;
    }
    
    return -1;
}


int Fieldml_GetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
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


int Fieldml_SetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle, int count )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object->regionHandle != FILE_REGION_HANDLE )
    {
        return FML_ERR_ACCESS_VIOLATION;
    }
    
    if( object->type == FHT_ENSEMBLE_DOMAIN )
    {
        object->object.ensembleDomain->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
        object->object.ensembleDomain->bounds.contiguous.count = count;
        
        return FML_ERR_NO_ERROR;
    }
    else if( object->type == FHT_MESH_DOMAIN )
    {
        FieldmlObject *subObject;
        
        subObject = getSimpleListEntry( parse->objects, object->object.meshDomain->elementDomain );
        
        if( ( subObject == NULL ) || ( subObject->type != FHT_ENSEMBLE_DOMAIN ) )
        {
            return FML_ERR_INCOMPLETE_OBJECT;
        }
        
        subObject->object.ensembleDomain->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
        subObject->object.ensembleDomain->bounds.contiguous.count = count;
        
        return FML_ERR_NO_ERROR;
    }

    return FML_ERR_INVALID_OBJECT;
}


FmlObjectHandle Fieldml_GetMeshElementDomain( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return FML_INVALID_HANDLE;
    }

    return object->object.meshDomain->elementDomain;
}


const char * Fieldml_GetMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return NULL;
    }

    return (char*)getIntTableEntry( object->object.meshDomain->shapes, elementNumber );
}


int Fieldml_CopyMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMeshElementShape( handle, objectHandle, elementNumber ), buffer, bufferLength );
}


int Fieldml_GetMeshConnectivityCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return -1;
    }

    return getIntTableCount( object->object.meshDomain->connectivity );
}


FmlObjectHandle Fieldml_GetMeshConnectivityDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return FML_INVALID_HANDLE;
    }

    return getIntTableEntryName( object->object.meshDomain->connectivity, index - 1 );
}


FmlObjectHandle Fieldml_GetMeshConnectivitySource( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return FML_INVALID_HANDLE;
    }

    return hackToHandle( getIntTableEntryData( object->object.meshDomain->connectivity, index - 1 ) );
}


FmlObjectHandle Fieldml_GetMeshXiDomain( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( ( object == NULL ) || ( object->type != FHT_MESH_DOMAIN ) )
    {
        return -1;
    }

    return object->object.meshDomain->xiDomain;
}


const char * Fieldml_GetObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return NULL;
    }

    return object->name;
}


int Fieldml_CopyObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetObjectName( handle, objectHandle ), buffer, bufferLength );
}


FmlObjectHandle Fieldml_GetValueDomain( FmlParseHandle handle, FmlObjectHandle objectHandle )
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


FmlObjectHandle Fieldml_CreateContinuousDereference( FmlParseHandle handle, const char * name, FmlObjectHandle indexes, FmlObjectHandle values, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createContinuousDereference( name, FILE_REGION_HANDLE, indexes, values, valueDomain );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_GetDereferenceIndexes( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->type != FHT_CONTINUOUS_DEREFERENCE )
    {
        return FML_INVALID_HANDLE;
    }
    
    return object->object.dereference->valueIndexes;
}


FmlObjectHandle Fieldml_GetDereferenceSource( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->type != FHT_CONTINUOUS_DEREFERENCE )
    {
        return FML_INVALID_HANDLE;
    }
    
    return object->object.dereference->valueSource;
}


FmlObjectHandle Fieldml_CreateEnsembleVariable( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createEnsembleVariable( name, FILE_REGION_HANDLE, valueDomain );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_CreateContinuousVariable( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createContinuousVariable( name, FILE_REGION_HANDLE, valueDomain );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_CreateEnsembleParameters( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createEnsembleParameters( name, FILE_REGION_HANDLE, valueDomain );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_CreateContinuousParameters( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createContinuousParameters( name, FILE_REGION_HANDLE, valueDomain );
    
    return addFieldmlObject( parse, object );
}


int Fieldml_SetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle, DataDescriptionType description )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType != DESCRIPTION_UNKNOWN )
        {
            return FML_ERR_ACCESS_VIOLATION;
        }

        object->object.parameters->descriptionType = description;
        
        if( description == DESCRIPTION_SEMIDENSE )
        {
            object->object.parameters->dataDescription.semidense = createSemidenseData();
        }
        
        return FML_ERR_NO_ERROR;  
    }

    return FML_ERR_INVALID_OBJECT;
}

DataDescriptionType Fieldml_GetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle )
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


DataLocationType Fieldml_GetParameterDataLocation( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return LOCATION_UNKNOWN;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            return object->object.parameters->dataDescription.semidense->locationType;
        }
    }

    return LOCATION_UNKNOWN;
}


int Fieldml_SetParameterDataLocation( FmlParseHandle handle, FmlObjectHandle objectHandle, DataLocationType location )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            if( object->object.parameters->dataDescription.semidense->locationType != LOCATION_UNKNOWN )
            {
                return FML_ERR_ACCESS_VIOLATION;
            }
            
            object->object.parameters->dataDescription.semidense->locationType = location;
            return FML_ERR_NO_ERROR;
        }
    }

    return FML_ERR_INVALID_OBJECT;
}


int Fieldml_AddInlineParameterData( FmlParseHandle handle, FmlObjectHandle objectHandle, const char *data, int length )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    Parameters *parameters;
    StringDataSource *source;
    char *newString;
    
    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }
    
    if( ( object->type != FHT_CONTINUOUS_PARAMETERS ) && ( object->type != FHT_ENSEMBLE_PARAMETERS ) )
    {
        return FML_ERR_INVALID_OBJECT;
    }
    
    parameters = object->object.parameters;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType != LOCATION_INLINE )
        {
            return FML_ERR_INVALID_OBJECT;
        }
        source = &(parameters->dataDescription.semidense->dataLocation.stringData);
    }

    newString = malloc( source->length + length + 1 );
    memcpy( newString, source->string, source->length );
    memcpy( newString + source->length, data, length );
    source->length += length;
    newString[ source->length ] = 0;
    free( source->string );
    source->string = newString;
    
    return FML_ERR_NO_ERROR;
}


int Fieldml_SetParameterFileData( FmlParseHandle handle, FmlObjectHandle objectHandle, const char * filename, DataFileType type, int offset )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    Parameters *parameters;
    FileDataSource *source;
    
    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }
    
    if( ( object->type != FHT_CONTINUOUS_PARAMETERS ) && ( object->type != FHT_ENSEMBLE_PARAMETERS ) )
    {
        return FML_ERR_INVALID_OBJECT;
    }
    
    parameters = object->object.parameters;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType != LOCATION_FILE )
        {
            return FML_ERR_INVALID_OBJECT;
        }
        source = &(parameters->dataDescription.semidense->dataLocation.fileData);
    }
    
    if( source->filename != NULL )
    {
        free( source->filename );
    }
    
    source->filename = _strdup( filename );
    source->fileType = type;
    source->offset = offset;
    
    return FML_ERR_NO_ERROR;
}


const char *Fieldml_GetParameterDataFilename( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    FileDataSource *source;

    if( object == NULL )
    {
        return 0;
    }

    source = NULL;
    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            if( object->object.parameters->dataDescription.semidense->locationType == LOCATION_FILE )
            {
                source = &object->object.parameters->dataDescription.semidense->dataLocation.fileData;
            }
        }
    }
    
    return source->filename;
}


int Fieldml_CopyParameterDataFilename( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetParameterDataFilename( handle, objectHandle ), buffer, bufferLength );
}


int Fieldml_GetParameterDataOffset( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    FileDataSource *source;

    if( object == NULL )
    {
        return 0;
    }

    source = NULL;
    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            if( object->object.parameters->dataDescription.semidense->locationType == LOCATION_FILE )
            {
                source = &object->object.parameters->dataDescription.semidense->dataLocation.fileData;
            }
        }
    }
    
    if( source != NULL )
    {
        return source->offset;
    }

    return 0;
}


DataFileType Fieldml_GetParameterDataFileType( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    FileDataSource *source;

    if( object == NULL )
    {
        return TYPE_UNKNOWN;
    }

    source = NULL;
    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            if( object->object.parameters->dataDescription.semidense->locationType == LOCATION_FILE )
            {
                source = &object->object.parameters->dataDescription.semidense->dataLocation.fileData;
            }
        }
    }
    
    if( source != NULL )
    {
        return source->fileType;
    }

    return TYPE_UNKNOWN;
}


int Fieldml_AddSemidenseIndex( FmlParseHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle indexHandle, int isSparse )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return FML_ERR_INVALID_OBJECT;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return FML_ERR_INVALID_OBJECT;
    }

    if( isSparse )
    {
        addSimpleListEntry( object->object.parameters->dataDescription.semidense->sparseIndexes, (void*)(indexHandle + 1) );
    }
    else
    {
        addSimpleListEntry( object->object.parameters->dataDescription.semidense->denseIndexes, (void*)(indexHandle + 1) );
    }
    
    return FML_ERR_NO_ERROR;
}


int Fieldml_GetSemidenseIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle, int isSparse )
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


FmlObjectHandle Fieldml_GetSemidenseIndex( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, int isSparse )
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


int Fieldml_SetSwizzle( FmlParseHandle handle, FmlObjectHandle objectHandle, const int *buffer, int count )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    int *ints;
    
    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return FML_INVALID_HANDLE;
    }
    
    if( object->object.parameters->dataDescription.semidense->swizzle != NULL )
    {
        free( (int*)object->object.parameters->dataDescription.semidense->swizzle );
    }
    
    ints = malloc( count * sizeof( int ) );
    memcpy( ints, buffer, sizeof( int ) * count );
    
    object->object.parameters->dataDescription.semidense->swizzleCount = count;
    object->object.parameters->dataDescription.semidense->swizzle = ints;
    
    return FML_ERR_NO_ERROR;
}


int Fieldml_GetSwizzleCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    
    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return FML_INVALID_HANDLE;
    }
    
    return object->object.parameters->dataDescription.semidense->swizzleCount;
}


const int *Fieldml_GetSwizzleData( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    
    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return NULL;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return NULL;
    }
    
    return object->object.parameters->dataDescription.semidense->swizzle;
}


int Fieldml_CopySwizzleData( FmlParseHandle handle, FmlObjectHandle objectHandle, int *buffer, int bufferLength )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    int length, swizzleCount;
    const int *swizzle;
    
    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return 0;
    }

    swizzle = NULL;
    
    if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        swizzle = object->object.parameters->dataDescription.semidense->swizzle;
        swizzleCount = object->object.parameters->dataDescription.semidense->swizzleCount;
    }
    else
    {
        return 0;
    }
    
    length = swizzleCount;
    
    if( length > bufferLength )
    {
        length = bufferLength;
    }
    
    memcpy( buffer, swizzle, length * sizeof( int ) );
    
    return length;
}


FmlObjectHandle Fieldml_CreateContinuousPiecewise( FmlParseHandle handle, const char * name, FmlObjectHandle indexHandle, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createContinuousPiecewise( name, FILE_REGION_HANDLE, indexHandle, valueDomain );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_CreateContinuousAggregate( FmlParseHandle handle, const char * name, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createContinuousAggregate( name, FILE_REGION_HANDLE, valueDomain );
    
    return addFieldmlObject( parse, object );
}


int Fieldml_SetEvaluator( FmlParseHandle handle, FmlObjectHandle objectHandle, int element, FmlObjectHandle evaluator )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    IntTable *table = getEntryIntTable( object );
    
    if( object == NULL )
    {
        return FML_ERR_UNKNOWN_OBJECT;
    }
    if( table == NULL )
    {
        return FML_ERR_INVALID_OBJECT;
    }

    setIntTableEntry( table, element, (void*)(evaluator + 1), NULL ); //HACK!!
    
    return FML_ERR_NO_ERROR;
}


int Fieldml_GetEvaluatorCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
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


int Fieldml_GetEvaluatorElement( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
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


FmlObjectHandle Fieldml_GetEvaluatorHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
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


FmlObjectHandle Fieldml_CreateContinuousImport( FmlParseHandle handle, const char * name, FmlObjectHandle remoteEvaluator, FmlObjectHandle valueDomain )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;
    
    object = createContinuousImport( name, FILE_REGION_HANDLE, remoteEvaluator, valueDomain );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_GetImportRemoteEvaluator( FmlParseHandle handle, FmlObjectHandle objectHandle )
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

    return object->object.continuousImport->remoteEvaluator;
}


int Fieldml_GetAliasCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return -1;
    }

    if( object->type == FHT_CONTINUOUS_IMPORT )
    {
        return getIntTableCount( object->object.continuousImport->aliases );
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return getIntTableCount( object->object.piecewise->aliases );
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        return getIntTableCount( object->object.aggregate->aliases );
    }
    
    return -1;

}


FmlObjectHandle Fieldml_GetAliasLocalHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->type == FHT_CONTINUOUS_IMPORT )
    {
        return hackToHandle( getIntTableEntryData( object->object.continuousImport->aliases, index - 1 ) );
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return hackToHandle( getIntTableEntryData( object->object.piecewise->aliases, index - 1 ) );
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        return hackToHandle( getIntTableEntryData( object->object.aggregate->aliases, index - 1 ) );
    }

    return FML_INVALID_HANDLE; 
}


FmlObjectHandle Fieldml_GetAliasRemoteHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );

    if( object == NULL )
    {
        return FML_INVALID_HANDLE;
    }

    if( object->type == FHT_CONTINUOUS_IMPORT )
    {
        return getIntTableEntryName( object->object.continuousImport->aliases, index - 1 );
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return getIntTableEntryName( object->object.piecewise->aliases, index - 1 );
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        return getIntTableEntryName( object->object.aggregate->aliases, index - 1 );
    }

    return FML_INVALID_HANDLE;
}


int Fieldml_SetAlias( FmlParseHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle remoteDomain, FmlObjectHandle localSource )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, objectHandle );
    IntTable *table = NULL;
    
    if( remoteDomain == FML_INVALID_HANDLE )
    {
        return FML_ERR_INVALID_OBJECT;
    }
    
    if( object->type == FHT_CONTINUOUS_IMPORT )
    {
        setIntTableEntry( object->object.continuousImport->aliases, remoteDomain, (void*)(localSource + 1), NULL );
        return FML_ERR_NO_ERROR;
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        setIntTableEntry( object->object.aggregate->aliases, remoteDomain, (void*)(localSource + 1), NULL );
        return FML_ERR_NO_ERROR;
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        setIntTableEntry( object->object.continuousImport->aliases, remoteDomain, (void*)(localSource + 1), NULL );
        return FML_ERR_NO_ERROR;
    }
    
    return FML_ERR_INVALID_OBJECT;
}



int Fieldml_GetIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle )
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


FmlObjectHandle Fieldml_GetIndexDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int index )
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


FmlObjectHandle Fieldml_CreateContinuousDomain( FmlParseHandle handle, const char * name, FmlObjectHandle componentHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createContinuousDomain( name, FILE_REGION_HANDLE, componentHandle );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_CreateEnsembleDomain( FmlParseHandle handle, const char * name, FmlObjectHandle componentHandle )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object;

    object = createEnsembleDomain( name, FILE_REGION_HANDLE, FML_INVALID_HANDLE );
    
    return addFieldmlObject( parse, object );
}


FmlObjectHandle Fieldml_CreateMeshDomain( FmlParseHandle handle, const char * name, FmlObjectHandle xiEnsemble )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object, *xiObject, *elementObject;
    FmlObjectHandle xiHandle, elementHandle;
    char *subName;

    if( Fieldml_GetNamedObjectHandle( handle, name ) != FML_INVALID_HANDLE )
    {
        return FML_INVALID_HANDLE;
    }
    
    subName = calloc( 1, strlen( name ) + 12 );

    strcpy( subName, name );
    strcat( subName, ".xi" );
    if( Fieldml_GetNamedObjectHandle( handle, subName ) != FML_INVALID_HANDLE )
    {
        return FML_INVALID_HANDLE;
    }

    strcpy( subName, name );
    strcat( subName, ".elements" );
    if( Fieldml_GetNamedObjectHandle( handle, subName ) != FML_INVALID_HANDLE )
    {
        return FML_INVALID_HANDLE;
    }
    
    strcpy( subName, name );
    strcat( subName, ".xi" );
    xiObject = createContinuousDomain( subName, VIRTUAL_REGION_HANDLE, xiEnsemble );
    xiHandle = addFieldmlObject( parse, xiObject );
    
    strcpy( subName, name );
    strcat( subName, ".elements" );
    elementObject = createEnsembleDomain( subName, VIRTUAL_REGION_HANDLE, FML_INVALID_HANDLE );
    elementHandle = addFieldmlObject( parse, elementObject );
    
    object = createMeshDomain( name, FILE_REGION_HANDLE, xiHandle, elementHandle );

    return addFieldmlObject( parse, object );
}


int Fieldml_SetMeshElementShape( FmlParseHandle handle, FmlObjectHandle mesh, int elementNumber, const char * shape )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, mesh );

    if( object->type == FHT_MESH_DOMAIN )
    {
        setIntTableEntry( object->object.meshDomain->shapes, elementNumber, _strdup( shape ), free );
        return FML_ERR_NO_ERROR;
    }
    
    return FML_ERR_INVALID_OBJECT;
}


int Fieldml_SetMeshConnectivity( FmlParseHandle handle, FmlObjectHandle mesh, FmlObjectHandle pointDomain, FmlObjectHandle evaluator )
{
    FieldmlParse *parse = handleToParse( handle );
    FieldmlObject *object = getSimpleListEntry( parse->objects, mesh );

    if( pointDomain == FML_INVALID_HANDLE )
    {
        return FML_ERR_INVALID_OBJECT;
    }
    
    if( object->type == FHT_MESH_DOMAIN )
    {
        setIntTableEntry( object->object.meshDomain->connectivity, pointDomain, (void*)(evaluator + 1), NULL );
        return FML_ERR_NO_ERROR;
    }
    
    return FML_ERR_INVALID_OBJECT;
}
