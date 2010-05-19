#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <ctype.h>

#include <libxml/SAX.h>
#include <libxml/globals.h>
#include <libxml/xmlerror.h>
#include <libxml/parser.h>
#include <libxml/xmlmemory.h>
#include <libxml/xmlschemas.h>

#include "string_table.h"
#include "int_stack.h"
#include "fieldml_parse.h"
#include "fieldml_sax.h"
#include "string_const.h"
#include "fieldml_structs.h"

//========================================================================
//
// Structs
//
//========================================================================

typedef enum _SaxState
{
    FML_ROOT,
    FML_FIELDML,
    FML_REGION,

    FML_ENSEMBLE_DOMAIN,
    FML_ENSEMBLE_DOMAIN_BOUNDS,
    
    FML_CONTINUOUS_DOMAIN,

    FML_MESH_DOMAIN,
    FML_MESH_SHAPES,
    FML_MESH_CONNECTIVITY,
    
    FML_CONTINUOUS_IMPORT,
    FML_ALIASES,

    FML_ENSEMBLE_PARAMETERS,
    FML_CONTINUOUS_PARAMETERS,

    FML_CONTINUOUS_VARIABLE,

    FML_CONTINUOUS_DEREFERENCE,

    FML_ENSEMBLE_VARIABLE,

    FML_SEMI_DENSE,
    FML_DENSE_INDEXES,
    FML_SPARSE_INDEXES,
    FML_INLINE_DATA,
    FML_SWIZZLE_DATA,
    FML_FILE_DATA,
    
    FML_CONTINUOUS_PIECEWISE,
    FML_ELEMENT_EVALUATORS,
    
    FML_CONTINUOUS_AGGREGATE,
    FML_SOURCE_FIELDS,
    
    FML_MARKUP,
}
SaxState;

typedef struct _SaxAttribute
{
    char *attribute;
    char *prefix;
    char *URI;
    char *value;
}
SaxAttribute;


typedef struct _SaxAttributes
{
    SaxAttribute *attributes;
    int count;
}
SaxAttributes;


typedef struct _SaxContext
{
    IntStack *state;

    FmlObjectHandle currentObject;
    
    int bufferLength;
    char *buffer;
    
    FieldmlParse *parse;
}
SaxContext;


//========================================================================
//
// Utils
//
//========================================================================

// strndup not available on all platforms.
static char *strdupN( const xmlChar *str, unsigned int n )
{
    char *dup;
    
    if( str == NULL )
    {
        return NULL;
    }

    if( strlen( str ) < n )
    {
        return strdup( str );
    }

    dup = malloc( n + 1 );
    memcpy( dup, str, n );
    dup[n] = 0;

    return dup;
}


static char *strdupS( const xmlChar *str )
{
    if( str == NULL )
    {
        return NULL;
    }
    
    return strdup( str );
}


static SaxAttributes *createAttributes( const int attributeCount, const xmlChar ** attributes )
{
    int i, attributeNumber;

    SaxAttributes *saxAttributes = calloc( 1, sizeof(SaxAttributes) );

    saxAttributes->count = attributeCount;
    saxAttributes->attributes = calloc( attributeCount, sizeof(SaxAttribute) );

    attributeNumber = 0;
    for( i = 0; i < attributeCount * 5; i += 5 )
    {
        saxAttributes->attributes[attributeNumber].attribute = strdupS( attributes[i + 0] );
        saxAttributes->attributes[attributeNumber].prefix = strdupS( attributes[i + 1] );
        saxAttributes->attributes[attributeNumber].URI = strdupS( attributes[i + 2] );
        saxAttributes->attributes[attributeNumber].value = strdupN( attributes[i + 3], attributes[i + 4] - attributes[i + 3] );
        attributeNumber++;
    }

    return saxAttributes;
}


static void destroyAttributes( SaxAttributes *saxAttributes )
{
    int i;

    for( i = 0; i < saxAttributes->count; i++ )
    {
        free( saxAttributes->attributes[i].attribute );
        free( saxAttributes->attributes[i].prefix );
        free( saxAttributes->attributes[i].URI );
        free( saxAttributes->attributes[i].value );
    }
    free( saxAttributes->attributes );

    free( saxAttributes );
}


const char * getAttribute( SaxAttributes *saxAttributes, const char *attribute )
{
    int i;
    for( i = 0; i < saxAttributes->count; i++ )
    {
        if( strcmp( attribute, saxAttributes->attributes[i].attribute ) == 0 )
        {
            return saxAttributes->attributes[i].value;
        }
    }

    return NULL;
}


int intParserCount( const char *buffer )
{
    const char *p = buffer;
    int digits = 0;
    int count = 0;
    char c;
    
    while( *p != 0 )
    {
        c = *p++;
        if( isdigit( c ) )
        {
            digits++;
            continue;
        }
        
        if( digits > 0 )
        {
            count++;
            digits = 0;
        }
    }
    
    return count;
}


const int *intParserInts( const char *buffer )
{
    int count = intParserCount( buffer );
    int *ints = calloc( count, sizeof( int ) );
    const char *p = buffer;
    int number = 0;
    int digits;
    char c;
    
    digits = 0;
    count = 0;
    while( *p != 0 )
    {
        c = *p++;
        if( isdigit( c ) )
        {
            number *= 10;
            number += ( c - '0' );
            digits++;
            continue;
        }
        
        if( digits > 0 )
        {
            ints[count++] = number;
            number = 0;
            digits = 0;
        }
    }
    
    return ints;
}


//========================================================================
//
// SAX -> FieldmlParse glue
//
//========================================================================

void startEnsembleDomain( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *componentEnsemble;
    FmlObjectHandle handle;
        
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        addError( context->parse, "EnsembleDomain has no name", NULL, NULL );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, COMPONENT_DOMAIN_ATTRIB );
    if( componentEnsemble != NULL )
    {
        handle = getOrCreateObjectHandle( context->parse, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
        handle = FML_INVALID_HANDLE;
    }

    context->currentObject = Fieldml_CreateEnsembleDomain( context->parse, name, handle );
}


void endEnsembleDomain( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startContiguousBounds( SaxContext *context, SaxAttributes *attributes )
{
    const char *count;
    
    count = getAttribute( attributes, VALUE_COUNT_ATTRIB );
    if( count == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Contiguous bounds has no value count", name, NULL );
        return;
    }
    
    Fieldml_SetContiguousBoundsCount( context->parse, context->currentObject, atoi( count ) );
}


void startContinuousDomain( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *componentEnsemble;
    FmlObjectHandle handle;
        
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        addError( context->parse, "ContinuousDomain has no name", name, NULL );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, COMPONENT_DOMAIN_ATTRIB );
    if( componentEnsemble != NULL )
    {
        handle = getOrCreateObjectHandle( context->parse, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
        handle = FML_INVALID_HANDLE;
    }

    context->currentObject = Fieldml_CreateContinuousDomain( context->parse, name, handle );
}


void endContinuousDomain( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startMeshDomain( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *xiEnsemble;
    FmlObjectHandle xiHandle;
    char *subName;
    
    name = getAttribute( attributes, NAME_ATTRIB );
    xiEnsemble = getAttribute( attributes, XI_COMPONENT_DOMAIN_ATTRIB );
    
    if( name == NULL )
    {
        addError( context->parse, "MeshDomain has no name", NULL, NULL );
        return;
    }
    
    if( xiEnsemble == NULL )
    {
        addError( context->parse, "MeshDomain has no xi components", name, NULL );
        return;
    }

    subName = calloc( 1, strlen( name ) + 12 );
    
    xiHandle = getOrCreateObjectHandle( context->parse, xiEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    context->currentObject = Fieldml_CreateMeshDomain( context->parse, name, xiHandle );
}


void onMeshShape( SaxContext *context, SaxAttributes *attributes )
{
    const char *element = getAttribute( attributes, KEY_ATTRIB );
    const char *shape = getAttribute( attributes, VALUE_ATTRIB );

    if( ( element == NULL ) || ( shape == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "MeshDomain has malformed shape entry", name, NULL );
        return;
    }
    
    Fieldml_SetMeshElementShape( context->parse, context->currentObject, atoi( element ), shape );
}


void onMeshConnectivity( SaxContext *context, SaxAttributes *attributes )
{
    const char *type = getAttribute( attributes, KEY_ATTRIB );
    const char *field = getAttribute( attributes, VALUE_ATTRIB );
    FmlObjectHandle fieldHandle, domainHandle;

    if( ( type == NULL ) || ( field == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "MeshDomain has malformed connectivity entry", name, NULL );
        return;
    }
    
    domainHandle = getOrCreateObjectHandle( context->parse, type, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    fieldHandle = getOrCreateObjectHandle( context->parse, field, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    
    Fieldml_SetMeshConnectivity( context->parse, context->currentObject, domainHandle, fieldHandle );
}


void endMeshDomain( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startContinuousImport( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *remoteName;
    const char *valueDomain;
    FmlObjectHandle handle, remoteHandle;
        
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        addError( context->parse, "ImportedContinuousEvaluator has no name", NULL, NULL );
        return;
    }
    
    remoteName = getAttribute( attributes, EVALUATOR_ATTRIB );
    if( remoteName == NULL )
    {
        addError( context->parse, "ImportedContinuousEvaluator has no remote name", name, NULL );
        return;
    }
    
    valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    if( valueDomain == NULL )
    {
        addError( context->parse, "ImportedContinuousEvaluator has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    remoteHandle = getOrCreateObjectHandle( context->parse, remoteName, FHT_UNKNOWN_CONTINUOUS_EVALUATOR );

    context->currentObject = Fieldml_CreateContinuousImport( context->parse, name, remoteHandle, handle );
}


void onAlias( SaxContext *context, SaxAttributes *attributes )
{
    const char *remote = getAttribute( attributes, KEY_ATTRIB );
    const char *local = getAttribute( attributes, VALUE_ATTRIB );
    FmlObjectHandle localHandle, remoteHandle;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Evaluator has malformed alias", name, NULL );
        return;
    }

    localHandle = getOrCreateObjectHandle( context->parse, local, FHT_UNKNOWN_CONTINUOUS_SOURCE );

    remoteHandle = getOrCreateObjectHandle( context->parse, remote, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    
    Fieldml_SetAlias( context->parse, context->currentObject, remoteHandle, localHandle );
}


void endContinuousImport( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startContinuousDereference( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    const char *valueIndexes = getAttribute( attributes, VALUE_INDEXES_ATTRIB );
    const char *valueSource = getAttribute( attributes, VALUE_SOURCE_ATTRIB );
    FmlObjectHandle valueHandle, indexHandle, sourceHandle;

    if( name == NULL )
    {
        addError( context->parse, "ContinuousDereference has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousDereference has no value domain", name, NULL );
        return;
    }
    if( valueIndexes == NULL )
    {
        addError( context->parse, "ContinuousDereference has no value indexes", name, NULL );
        return;
    }
    if( valueSource == NULL )
    {
        addError( context->parse, "ContinuousDereference has no value source", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    indexHandle = getOrCreateObjectHandle( context->parse, valueIndexes, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    sourceHandle = getOrCreateObjectHandle( context->parse, valueSource, FHT_UNKNOWN_CONTINUOUS_SOURCE );

    context->currentObject = Fieldml_CreateContinuousDereference( context->parse, name, indexHandle, sourceHandle, valueHandle );
}


void endContinuousDereference( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startEnsembleParameters( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle handle;

    if( name == NULL )
    {
        addError( context->parse, "EnsembleParameters has no name", NULL, NULL );
        return;
    }

    if( valueDomain == NULL )
    {
        addError( context->parse, "EnsembleParameters has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    context->currentObject = Fieldml_CreateEnsembleParameters( context->parse, name, handle );
}


void endEnsembleParameters( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startContinuousParameters( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle handle;

    if( name == NULL )
    {
        addError( context->parse, "ContinuousParameters has no name", NULL, NULL );
        return;
    }

    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousParameters has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    context->currentObject = Fieldml_CreateContinuousParameters( context->parse, name, handle );
}


void endContinuousParameters( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startInlineData( SaxContext *context, SaxAttributes *attributes )
{
    Fieldml_SetParameterDataLocation( context->parse, context->currentObject, LOCATION_INLINE );
}


void onInlineData( SaxContext *context, const char *const characters, const int length )
{
    Fieldml_AddInlineParameterData( context->parse, context->currentObject, characters, length );
}


void onFileData( SaxContext *context, SaxAttributes *attributes )
{
    const char *file = getAttribute( attributes, FILE_ATTRIB );
    const char *type = getAttribute( attributes, TYPE_ATTRIB );
    const char *offset = getAttribute( attributes, OFFSET_ATTRIB );
    DataFileType fileType;
    int offsetAmount;
    
    if( file == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Parameters file data for must have a file name", name, NULL );
        return;
    }
    
    if( type == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Parameters file data for must have a file type", name, NULL );
        return;
    }
    else if( strcmp( type, STRING_TYPE_TEXT ) == 0 )
    {
        fileType = TYPE_TEXT;
    }
    else if( strcmp( type, STRING_TYPE_LINES ) == 0 )
    {
        fileType = TYPE_LINES;
    }
    else 
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Parameters file data for must have a known file type", name, NULL );
        return;
    }
    
    if( offset == NULL )
    {
        offsetAmount = 0;
    }
    else
    {
        offsetAmount = atoi( offset );
    }
    
    Fieldml_SetParameterDataLocation( context->parse, context->currentObject, LOCATION_FILE );
    Fieldml_SetParameterFileData( context->parse, context->currentObject, file, fileType, offsetAmount );
}


void startSwizzleData( SaxContext *context, SaxAttributes *attributes )
{
}


void onSwizzleData( SaxContext *context, const char *const characters, const int length )
{
    char *newString;
    
    newString = malloc( context->bufferLength + length + 1 );
    if( context->buffer != NULL )
    {
        memcpy( newString, context->buffer, context->bufferLength );
    }
    memcpy( newString + context->bufferLength, characters, length );
    context->bufferLength += length;
    newString[ context->bufferLength ] = 0;
    free( context->buffer );
    context->buffer = newString;
}


void endSwizzleData( SaxContext *context )
{
    int intCount;
    const int *ints;
    
    intCount = intParserCount( context->buffer );
    ints = intParserInts( context->buffer );
    
    Fieldml_SetSwizzle( context->parse, context->currentObject, ints, intCount );
    
    free( (int*)ints );
    free( context->buffer );
    context->buffer = NULL;
    context->bufferLength = 0;
}


void startSemidenseData( SaxContext *context, SaxAttributes *attributes )
{
    Fieldml_SetParameterDataDescription( context->parse, context->currentObject, DESCRIPTION_SEMIDENSE );
}


void onSemidenseSparseIndex( SaxContext *context, SaxAttributes *attributes )
{
    const char *index;
    FmlObjectHandle handle;
    
    index = getAttribute( attributes, VALUE_ATTRIB );
    if( index == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Missing index in semi dense data", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    Fieldml_AddSemidenseIndex( context->parse, context->currentObject, handle, 1 );
}


void onSemidenseDenseIndex( SaxContext *context, SaxAttributes *attributes )
{
    const char *index;
    FmlObjectHandle handle;
    
    index = getAttribute( attributes, VALUE_ATTRIB );
    if( index == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Missing index in semi dense data", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    Fieldml_AddSemidenseIndex( context->parse, context->currentObject, handle, 0 );
}


void endSemidenseData( SaxContext *context )
{
}


void startContinuousPiecewise( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *valueDomain;
    const char *indexDomain;
    FmlObjectHandle valueHandle, indexHandle;
    
    name = getAttribute( attributes, NAME_ATTRIB );
    valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    indexDomain = getAttribute( attributes, INDEX_DOMAIN_ATTRIB );
    
    if( name == NULL )
    {
        addError( context->parse, "ContinuousPiecewise has no name", NULL, NULL );
        return;
    }
    
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousPiecewise has no value domain", name, NULL );
        return;
    }
    
    if( indexDomain == NULL )
    {
        addError( context->parse, "ContinuousPiecewise has no index domain", name, NULL );
        return;
    }
    
    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    indexHandle = getOrCreateObjectHandle( context->parse, indexDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    context->currentObject = Fieldml_CreateContinuousPiecewise( context->parse, name, indexHandle, valueHandle );
}


void onContinuousPiecewiseEntry( SaxContext *context, SaxAttributes *attributes )
{
    const char *key;
    const char *value;
    FmlObjectHandle handle;
    
    key = getAttribute( attributes, KEY_ATTRIB );
    value = getAttribute( attributes, VALUE_ATTRIB );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Malformed element evaluator for ContinuousPiecewise", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
    
    Fieldml_SetEvaluator( context->parse, context->currentObject, atoi( key ), handle );
}


void endContinuousPiecewise( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startContinuousAggregate( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *valueDomain;
    FmlObjectHandle valueHandle;
    
    name = getAttribute( attributes, NAME_ATTRIB );
    valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    
    if( name == NULL )
    {
        addError( context->parse, "ContinuousAggregate has no name", NULL, NULL );
        return;
    }
    
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousAggregate has no value domain", name, NULL );
        return;
    }
    
    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    
    context->currentObject = Fieldml_CreateContinuousAggregate( context->parse, name, valueHandle );
}


void onContinuousAggregateEntry( SaxContext *context, SaxAttributes *attributes )
{
    const char *key;
    const char *value;
    FmlObjectHandle handle;
    
    key = getAttribute( attributes, KEY_ATTRIB );
    value = getAttribute( attributes, VALUE_ATTRIB );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Malformed element evaluator for ContinuousAggregate", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
    
    Fieldml_SetEvaluator( context->parse, context->currentObject, atoi( key ), handle );
}


void endContinuousAggregate( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void startContinuousVariable( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle valueHandle;

    if( name == NULL )
    {
        addError( context->parse, "ContinuousVariable has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousVariable has no value domain", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    context->currentObject = Fieldml_CreateContinuousVariable( context->parse, name, valueHandle );
}


void startEnsembleVariable( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle valueHandle;
    
    if( name == NULL )
    {
        addError( context->parse, "EnsembleVariable has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        addError( context->parse, "EnsembleVariable has no value domain", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    context->currentObject = Fieldml_CreateEnsembleVariable( context->parse, name, valueHandle );
}


void endVariable( SaxContext *context )
{
    Fieldml_ValidateObject( context->parse, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


void onMarkupEntry( SaxContext *context, SaxAttributes *attributes )
{
    const char *key;
    const char *value;
    
    key = getAttribute( attributes, KEY_ATTRIB );
    value = getAttribute( attributes, VALUE_ATTRIB );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->parse, context->currentObject );
        addError( context->parse, "Malformed markup", name, NULL );
        return;
    }
    
    Fieldml_SetMarkup( context->parse, context->currentObject, key, value );
}

//========================================================================
//
// SAX handlers
//
//========================================================================

int isStandalone( void *context )
{
    return 0;
}


int hasInternalSubset( void *context )
{
    return 0;
}


int hasExternalSubset( void *context )
{
    return 0;
}


void onInternalSubset( void *context, const xmlChar *name, const xmlChar *externalID, const xmlChar *systemID )
{
}


void externalSubset( void *context, const xmlChar *name, const xmlChar *externalID, const xmlChar *systemID )
{
}


xmlParserInputPtr resolveEntity( void *context, const xmlChar *publicId, const xmlChar *systemId )
{
    return NULL;
}


xmlEntityPtr getEntity( void *context, const xmlChar *name )
{
    return NULL;
}


xmlEntityPtr getParameterEntity( void *context, const xmlChar *name )
{
    return NULL;
}


void onEntityDecl( void *context, const xmlChar *name, int type, const xmlChar *publicId, const xmlChar *systemId, xmlChar *content )
{
}


static void onAttributeDecl( void *context, const xmlChar * elem, const xmlChar * name, int type, int def, const xmlChar * defaultValue, xmlEnumerationPtr tree )
{
    xmlFreeEnumeration( tree );
}


static void onElementDecl( void *context, const xmlChar *name, int type, xmlElementContentPtr content )
{
}


static void onNotationDecl( void *context, const xmlChar *name, const xmlChar *publicId, const xmlChar *systemId )
{
}

static void onUnparsedEntityDecl( void *context, const xmlChar *name, const xmlChar *publicId, const xmlChar *systemId, const xmlChar *notationName )
{
}


void setDocumentLocator( void *context, xmlSAXLocatorPtr loc )
{
    /*
     At the moment (libxml 2.7.2), this is worse than useless.
     The locator only wraps functions which require the library's internal
     parsing context, which is only passed into this function if you pass
     in 'NULL' as the user-data which initiating the SAX parse...
     which is exactly what we don't want to do.
     */
}


static void onStartDocument( void *context )
{
}


static void onEndDocument( void *context )
{
}


static void onCharacters( void *context, const xmlChar *ch, int len )
{
    SaxContext *saxContext = (SaxContext*)context;

    switch( intStackPeek( saxContext->state ) )
    {
    case FML_INLINE_DATA:
        onInlineData( saxContext, ch, len );
        break;
    case FML_SWIZZLE_DATA:
        onSwizzleData( saxContext, ch, len );
        break;
    default:
        break;
    }
}


static void onReference( void *context, const xmlChar *name )
{
}


static void onIgnorableWhitespace( void *context, const xmlChar *ch, int len )
{
}


static void onProcessingInstruction( void *context, const xmlChar *target, const xmlChar *data )
{
}


static void onCdataBlock( void *context, const xmlChar *value, int len )
{
}


void comment( void *context, const xmlChar *value )
{
}


static void XMLCDECL warning( void *context, const char *msg, ... )
{
    va_list args;

    va_start( args, msg );
    fprintf( stdout, "SAX.warning: " );
    vfprintf( stdout, msg, args );
    va_end( args );
}


static void XMLCDECL error( void *context, const char *msg, ... )
{
    va_list args;

    va_start( args, msg );
    fprintf( stdout, "SAX.error: " );
    vfprintf( stdout, msg, args );
    va_end( args );
}


static void XMLCDECL fatalError( void *context, const char *msg, ... )
{
    va_list args;

    va_start( args, msg );
    fprintf( stdout, "SAX.fatalError: " );
    vfprintf( stdout, msg, args );
    va_end( args );
}


static void onStartElementNs( void *context, const xmlChar *name, const xmlChar *prefix, const xmlChar *URI, int nb_namespaces, const xmlChar **namespaces,
    int nb_attributes, int nb_defaulted, const xmlChar **attributes )
{
    SaxAttributes *saxAttributes = createAttributes( nb_attributes, attributes );
    SaxContext *saxContext = (SaxContext*)context;

    int state;

    state = intStackPeek( saxContext->state );

    if( ( state != FML_ROOT ) && ( state != FML_FIELDML ) && ( state != FML_REGION ) )
    {
        if( strcmp( name, MARKUP_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_MARKUP );
            destroyAttributes( saxAttributes );
            return;
        }
    }

    switch( state )
    {
    case FML_ROOT:
        if( strcmp( name, FIELDML_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_FIELDML );
        }
        break;
    case FML_FIELDML:
        if( strcmp( name, REGION_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_REGION );
        }
        break;
    case FML_ENSEMBLE_DOMAIN:
        if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            startContiguousBounds( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_ENSEMBLE_DOMAIN_BOUNDS );
        }
        break;
    case FML_MESH_DOMAIN:
        if( strcmp( name, MESH_SHAPES_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_MESH_SHAPES );
        }
        else if( strcmp( name, MESH_CONNECTIVITY_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_MESH_CONNECTIVITY );
        }
        else if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            startContiguousBounds( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_ENSEMBLE_DOMAIN_BOUNDS );
        }
        break;
    case FML_MESH_SHAPES:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            onMeshShape( saxContext, saxAttributes );
        }
        break;
    case FML_MESH_CONNECTIVITY:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            onMeshConnectivity( saxContext, saxAttributes );
        }
        break;
    case FML_CONTINUOUS_IMPORT:
        if( strcmp( name, ALIASES_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_ALIASES );
        }
        break;
    case FML_ENSEMBLE_PARAMETERS:
    case FML_CONTINUOUS_PARAMETERS:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            startSemidenseData( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_SEMI_DENSE );
        }
        break;
    case FML_CONTINUOUS_PIECEWISE:
        if( strcmp( name, ELEMENT_EVALUATORS_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_ELEMENT_EVALUATORS );
        }
        else if( strcmp( name, ALIASES_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_ALIASES );
        }
        break;
    case FML_CONTINUOUS_AGGREGATE:
        if( strcmp( name, SOURCE_FIELDS_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_SOURCE_FIELDS );
        }
        else if( strcmp( name, ALIASES_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_ALIASES );
        }
        break;
    case FML_MARKUP:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            onMarkupEntry( saxContext, saxAttributes );
        }
        break;
    case FML_SOURCE_FIELDS:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            onContinuousAggregateEntry( saxContext, saxAttributes );
        }
        break;
    case FML_ELEMENT_EVALUATORS:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            onContinuousPiecewiseEntry( saxContext, saxAttributes );
        }
        break;
    case FML_SEMI_DENSE:
        if( strcmp( name, DENSE_INDEXES_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_DENSE_INDEXES );
        }
        else if( strcmp( name, SPARSE_INDEXES_TAG ) == 0 )
        {
            intStackPush( saxContext->state, FML_SPARSE_INDEXES );
        }
        else if( strcmp( name, INLINE_DATA_TAG ) == 0 )
        {
            startInlineData( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_INLINE_DATA );
        }
        else if( strcmp( name, FILE_DATA_TAG ) == 0 )
        {
            onFileData( saxContext, saxAttributes );
        }
        else if( strcmp( name, SWIZZLE_TAG ) == 0 )
        {
            startSwizzleData( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_SWIZZLE_DATA );
        }
        break;
    case FML_DENSE_INDEXES:
        if( strcmp( name, ENTRY_TAG ) == 0 )
        {
            onSemidenseDenseIndex( saxContext, saxAttributes );
        }
        break;
    case FML_SPARSE_INDEXES:
        if( strcmp( name, ENTRY_TAG ) == 0 )
        {
            onSemidenseSparseIndex( saxContext, saxAttributes );
        }
        break;
    case FML_ALIASES:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            onAlias( saxContext, saxAttributes );
        }
        break;
    case FML_REGION:
        if( strcmp( name, ENSEMBLE_DOMAIN_TAG ) == 0 )
        {
            startEnsembleDomain( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_ENSEMBLE_DOMAIN );
        }
        else if( strcmp( name, CONTINUOUS_DOMAIN_TAG ) == 0 )
        {
            startContinuousDomain( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_DOMAIN );
        }
        else if( strcmp( name, MESH_DOMAIN_TAG ) == 0 )
        {
            startMeshDomain( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_MESH_DOMAIN );
        }
        else if( strcmp( name, IMPORTED_CONTINUOUS_TAG ) == 0 )
        {
            startContinuousImport( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_IMPORT );
        }
        else if( strcmp( name, ENSEMBLE_PARAMETERS_TAG ) == 0 )
        {
            startEnsembleParameters( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_ENSEMBLE_PARAMETERS );
        }
        else if( strcmp( name, CONTINUOUS_PARAMETERS_TAG ) == 0 )
        {
            startContinuousParameters( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_PARAMETERS );
        }
        else if( strcmp( name, CONTINUOUS_PIECEWISE_TAG ) == 0 )
        {
            startContinuousPiecewise( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_PIECEWISE );
        }
        else if( strcmp( name, CONTINUOUS_VARIABLE_TAG ) == 0 )
        {
            startContinuousVariable( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_VARIABLE );
        }
        else if( strcmp( name, CONTINUOUS_AGGREGATE_TAG ) == 0 )
        {
            startContinuousAggregate( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_AGGREGATE );
        }
        else if( strcmp( name, ENSEMBLE_VARIABLE_TAG ) == 0 )
        {
            startEnsembleVariable( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_ENSEMBLE_VARIABLE );
        }
        else if( strcmp( name, CONTINUOUS_DEREFERENCE_TAG ) == 0 )
        {
            startContinuousDereference( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_DEREFERENCE );
        }
        break;
        //FALLTHROUGH
    default:
        break;
    }

    destroyAttributes( saxAttributes );
}


static void onEndElementNs( void *context, const xmlChar *name, const xmlChar *prefix, const xmlChar *URI )
{
    SaxContext *saxContext = (SaxContext*)context;

    switch( intStackPeek( saxContext->state ) )
    {
    case FML_FIELDML:
        if( strcmp( name, FIELDML_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_DOMAIN_BOUNDS:
        if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_MESH_DOMAIN:
        if( strcmp( name, MESH_DOMAIN_TAG ) == 0 )
        {
            endMeshDomain( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_MESH_SHAPES:
        if( strcmp( name, MESH_SHAPES_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_MESH_CONNECTIVITY:
        if( strcmp( name, MESH_CONNECTIVITY_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_DOMAIN:
        if( strcmp( name, ENSEMBLE_DOMAIN_TAG ) == 0 )
        {
            endEnsembleDomain( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_DOMAIN:
        if( strcmp( name, CONTINUOUS_DOMAIN_TAG ) == 0 )
        {
            endContinuousDomain( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_ALIASES:
        if( strcmp( name, ALIASES_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_DEREFERENCE:
        if( strcmp( name, CONTINUOUS_DEREFERENCE_TAG ) == 0 )
        {
            endContinuousDereference( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_IMPORT:
        if( strcmp( name, IMPORTED_CONTINUOUS_TAG ) == 0 )
        {
            endContinuousImport( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_PARAMETERS:
        if( strcmp( name, ENSEMBLE_PARAMETERS_TAG ) == 0 )
        {
            endEnsembleParameters( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_PIECEWISE:
        if( strcmp( name, CONTINUOUS_PIECEWISE_TAG ) == 0 )
        {
            endContinuousPiecewise( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_SOURCE_FIELDS:
        if( strcmp( name, SOURCE_FIELDS_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_MARKUP:
        if( strcmp( name, MARKUP_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_AGGREGATE:
        if( strcmp( name, CONTINUOUS_AGGREGATE_TAG ) == 0 )
        {
            endContinuousAggregate( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_PARAMETERS:
        if( strcmp( name, CONTINUOUS_PARAMETERS_TAG ) == 0 )
        {
            endContinuousParameters( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_SEMI_DENSE:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            endSemidenseData( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_ELEMENT_EVALUATORS:
        if( strcmp( name, ELEMENT_EVALUATORS_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_SPARSE_INDEXES:
        if( strcmp( name, SPARSE_INDEXES_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_DENSE_INDEXES:
        if( strcmp( name, DENSE_INDEXES_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_INLINE_DATA:
        if( strcmp( name, INLINE_DATA_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_SWIZZLE_DATA:
        if( strcmp( name, SWIZZLE_TAG ) == 0 )
        {
            endSwizzleData( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_FILE_DATA:
        if( strcmp( name, FILE_DATA_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_VARIABLE:
        if( strcmp( name, CONTINUOUS_VARIABLE_TAG ) == 0 )
        {
            endVariable( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_VARIABLE:
        if( strcmp( name, ENSEMBLE_VARIABLE_TAG ) == 0 )
        {
            endVariable( saxContext );
            intStackPop( saxContext->state );
        }
        break;
    case FML_REGION:
        if( strcmp( name, REGION_TAG ) == 0 )
        {
            intStackPop( saxContext->state );
        }
        //FALLTHROUGH
    default:
        break;
    }
}


static xmlSAXHandler SAX2HandlerStruct =
{
    onInternalSubset,
    isStandalone,
    hasInternalSubset,
    hasExternalSubset,
    resolveEntity,
    getEntity,
    onEntityDecl,
    onNotationDecl,
    onAttributeDecl,
    onElementDecl,
    onUnparsedEntityDecl,
    setDocumentLocator,
    onStartDocument,
    onEndDocument,
    NULL,
    NULL,
    onReference,
    onCharacters,
    onIgnorableWhitespace,
    onProcessingInstruction,
    comment,
    warning,
    error,
    fatalError,
    getParameterEntity,
    onCdataBlock,
    externalSubset,
    XML_SAX2_MAGIC,
    NULL,
    onStartElementNs,
    onEndElementNs,
    NULL
};

static xmlSAXHandlerPtr SAX2Handler = &SAX2HandlerStruct;

//========================================================================
//
// Main
//
//========================================================================

FieldmlParse *parseFieldmlFile( const char *filename )
{
    int res, state;
    SaxContext context;
    FieldmlParse * parse;

    parse = Fieldml_Create();

    context.state = createIntStack();
    context.parse = parse;
    context.currentObject = FML_INVALID_HANDLE;
    context.bufferLength = 0;
    context.buffer = NULL;

    intStackPush( context.state, FML_ROOT );

    LIBXML_TEST_VERSION

    xmlSubstituteEntitiesDefault( 1 );

    res = xmlSAXUserParseFile( SAX2Handler, &context, filename );
    if( res != 0 )
    {
        addError( parse, "xmlSAXUserParseFile returned error", NULL, NULL );
    }

    state = intStackPeek( context.state );
    if( state != FML_ROOT )
    {
        addError( parse, "Parser state not empty", NULL, NULL );
    }

    xmlCleanupParser();
    xmlMemoryDump();

    finalizeFieldmlParse( parse );

    return parse;
}
