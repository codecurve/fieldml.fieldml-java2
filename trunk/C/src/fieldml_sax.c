/* \file
 * $Id$
 * \author Caton Little
 * \brief 
 *
 * \section LICENSE
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is FieldML
 *
 * The Initial Developer of the Original Code is Auckland Uniservices Ltd,
 * Auckland, New Zealand. Portions created by the Initial Developer are
 * Copyright (C) 2010 the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

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
#include "fieldml_sax.h"
#include "string_const.h"
#include "fieldml_structs.h"
#include "fieldml_api.h"

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
    
    FML_CONTINUOUS_REFERENCE,
    FML_ALIASES,

    FML_ENSEMBLE_PARAMETERS,
    FML_CONTINUOUS_PARAMETERS,

    FML_CONTINUOUS_VARIABLE,

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
    const char *source;
    
    FieldmlRegion *region;
}
SaxContext;


//========================================================================
//
// Utils
//
//========================================================================


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


static const char * getAttribute( SaxAttributes *saxAttributes, const char *attribute )
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


static int intParserCount( const char *buffer )
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


static const int *intParserInts( const char *buffer )
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
// SAX -> FieldmlRegion glue
//
//========================================================================


static FmlObjectHandle getOrCreateObjectHandle( FieldmlRegion *region, const char *name, FieldmlHandleType type )
{
    FmlObjectHandle handle = Fieldml_GetNamedObject( region, name );

    if( handle == FML_INVALID_HANDLE )
    {
        handle = addFieldmlObject( region, createFieldmlObject( name, type, VIRTUAL_REGION_HANDLE ) );
    }
    
    return handle;
}


static void finalizeFieldmlRegion( FieldmlRegion *region )
{
    FieldmlObject *object;
    int i, count;
    
    count = getSimpleListCount( region->objects );
    
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( region->objects, i );
        
        if( ( object->type == FHT_UNKNOWN_CONTINUOUS_DOMAIN ) || ( object->type == FHT_UNKNOWN_CONTINUOUS_SOURCE ) )
        {
            object->type = FHT_REMOTE_CONTINUOUS_DOMAIN;
        }
        else if( ( object->type == FHT_UNKNOWN_ENSEMBLE_DOMAIN ) || ( object->type == FHT_UNKNOWN_ENSEMBLE_SOURCE ) )
        {
            object->type = FHT_REMOTE_ENSEMBLE_DOMAIN;
        }
        else if( object->type == FHT_UNKNOWN_CONTINUOUS_EVALUATOR )
        {
            object->type = FHT_REMOTE_CONTINUOUS_EVALUATOR;
        }
        else if( object->type == FHT_UNKNOWN_ENSEMBLE_EVALUATOR )
        {
            object->type = FHT_REMOTE_ENSEMBLE_EVALUATOR;
        }
    }
}


static void startRegion( SaxContext *context, SaxAttributes *attributes )
{
    const char *location;
    const char *name;
    
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        //HACK Allow nameless regions for now.
        name = "";
    }

    location = strdupDir( context->source );
    context->region = Fieldml_Create( location, name );
    free( location );
}


static void startEnsembleDomain( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *componentEnsemble;
    const char *isComponentEnsemble;
    FmlObjectHandle handle;
        
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        logError( context->region, "EnsembleDomain has no name", NULL, NULL );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, COMPONENT_DOMAIN_ATTRIB );
    if( componentEnsemble != NULL )
    {
        handle = getOrCreateObjectHandle( context->region, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
        handle = FML_INVALID_HANDLE;
    }
    
    isComponentEnsemble = getAttribute( attributes, IS_COMPONENT_DOMAIN_ATTRIB );
    if( ( isComponentEnsemble != NULL ) && ( strcmp( isComponentEnsemble, STRING_TRUE ) == 0 ) )
    {
        if( handle != FML_INVALID_HANDLE )
        {
            logError( context->region, "Component EnsembleDomain cannot be multi-component itself", name, NULL );
        }
        else
        {
            context->currentObject = Fieldml_CreateComponentEnsembleDomain( context->region, name );
        }
    }
    else
    {
        context->currentObject = Fieldml_CreateEnsembleDomain( context->region, name, handle );
    }
}


static void endEnsembleDomain( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startContiguousBounds( SaxContext *context, SaxAttributes *attributes )
{
    const char *count;
    
    count = getAttribute( attributes, VALUE_COUNT_ATTRIB );
    if( count == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Contiguous bounds has no value count", name, NULL );
        return;
    }
    
    Fieldml_SetContiguousBoundsCount( context->region, context->currentObject, atoi( count ) );
}


static void startContinuousDomain( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *componentEnsemble;
    FmlObjectHandle handle;
        
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        logError( context->region, "ContinuousDomain has no name", name, NULL );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, COMPONENT_DOMAIN_ATTRIB );
    if( componentEnsemble != NULL )
    {
        handle = getOrCreateObjectHandle( context->region, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
        handle = FML_INVALID_HANDLE;
    }

    context->currentObject = Fieldml_CreateContinuousDomain( context->region, name, handle );
}


static void endContinuousDomain( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startMeshDomain( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *xiEnsemble;
    FmlObjectHandle xiHandle;
    char *subName;
    
    name = getAttribute( attributes, NAME_ATTRIB );
    xiEnsemble = getAttribute( attributes, XI_COMPONENT_DOMAIN_ATTRIB );
    
    if( name == NULL )
    {
        logError( context->region, "MeshDomain has no name", NULL, NULL );
        return;
    }
    
    if( xiEnsemble == NULL )
    {
        logError( context->region, "MeshDomain has no xi components", name, NULL );
        return;
    }

    subName = calloc( 1, strlen( name ) + 12 );
    
    xiHandle = getOrCreateObjectHandle( context->region, xiEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    context->currentObject = Fieldml_CreateMeshDomain( context->region, name, xiHandle );
}


static void startMeshShapes( SaxContext *context, SaxAttributes *attributes )
{
    const char *defaultValue = getAttribute( attributes, DEFAULT_ATTRIB );

    if( defaultValue != NULL )
    {
        Fieldml_SetMeshDefaultShape( context->region, context->currentObject, defaultValue );
    }
}


static void onMeshShape( SaxContext *context, SaxAttributes *attributes )
{
    const char *element = getAttribute( attributes, KEY_ATTRIB );
    const char *shape = getAttribute( attributes, VALUE_ATTRIB );

    if( ( element == NULL ) || ( shape == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "MeshDomain has malformed shape entry", name, NULL );
        return;
    }
    
    Fieldml_SetMeshElementShape( context->region, context->currentObject, atoi( element ), shape );
}


static void onMeshConnectivity( SaxContext *context, SaxAttributes *attributes )
{
    const char *field = getAttribute( attributes, KEY_ATTRIB );
    const char *type = getAttribute( attributes, VALUE_ATTRIB );
    FmlObjectHandle fieldHandle, domainHandle;

    if( ( type == NULL ) || ( field == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "MeshDomain has malformed connectivity entry", name, NULL );
        return;
    }
    
    domainHandle = getOrCreateObjectHandle( context->region, type, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    fieldHandle = getOrCreateObjectHandle( context->region, field, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    
    Fieldml_SetMeshConnectivity( context->region, context->currentObject, fieldHandle, domainHandle );
}


static void endMeshDomain( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startContinuousReference( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *remoteName;
    const char *valueDomain;
    FmlObjectHandle handle, remoteHandle;
        
    name = getAttribute( attributes, NAME_ATTRIB );
    if( name == NULL )
    {
        logError( context->region, "ContinuousReferenceEvaluator has no name", NULL, NULL );
        return;
    }
    
    remoteName = getAttribute( attributes, EVALUATOR_ATTRIB );
    if( remoteName == NULL )
    {
        logError( context->region, "ContinuousReferenceEvaluator has no remote name", name, NULL );
        return;
    }
    
    valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    if( valueDomain == NULL )
    {
        logError( context->region, "ContinuousReferenceEvaluator has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    remoteHandle = getOrCreateObjectHandle( context->region, remoteName, FHT_UNKNOWN_CONTINUOUS_EVALUATOR );

    context->currentObject = Fieldml_CreateContinuousReference( context->region, name, remoteHandle, handle );
}


static void onAlias( SaxContext *context, SaxAttributes *attributes )
{
    const char *remote = getAttribute( attributes, KEY_ATTRIB );
    const char *local = getAttribute( attributes, VALUE_ATTRIB );
    FmlObjectHandle localHandle, remoteHandle;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Evaluator has malformed alias", name, NULL );
        return;
    }

    localHandle = getOrCreateObjectHandle( context->region, local, FHT_UNKNOWN_CONTINUOUS_SOURCE );

    remoteHandle = getOrCreateObjectHandle( context->region, remote, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    
    Fieldml_SetAlias( context->region, context->currentObject, remoteHandle, localHandle );
}


static void endContinuousReference( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startEnsembleParameters( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle handle;

    if( name == NULL )
    {
        logError( context->region, "EnsembleParameters has no name", NULL, NULL );
        return;
    }

    if( valueDomain == NULL )
    {
        logError( context->region, "EnsembleParameters has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    context->currentObject = Fieldml_CreateEnsembleParameters( context->region, name, handle );
}


static void endEnsembleParameters( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startContinuousParameters( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle handle;

    if( name == NULL )
    {
        logError( context->region, "ContinuousParameters has no name", NULL, NULL );
        return;
    }

    if( valueDomain == NULL )
    {
        logError( context->region, "ContinuousParameters has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    context->currentObject = Fieldml_CreateContinuousParameters( context->region, name, handle );
}


static void endContinuousParameters( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startInlineData( SaxContext *context, SaxAttributes *attributes )
{
    Fieldml_SetParameterDataLocation( context->region, context->currentObject, LOCATION_INLINE );
}


static void onInlineData( SaxContext *context, const char *const characters, const int length )
{
    Fieldml_AddParameterInlineData( context->region, context->currentObject, characters, length );
}


static void onFileData( SaxContext *context, SaxAttributes *attributes )
{
    const char *file = getAttribute( attributes, FILE_ATTRIB );
    const char *type = getAttribute( attributes, TYPE_ATTRIB );
    const char *offset = getAttribute( attributes, OFFSET_ATTRIB );
    DataFileType fileType;
    int offsetAmount;
    
    if( file == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Parameters file data for must have a file name", name, NULL );
        return;
    }
    
    if( type == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Parameters file data for must have a file type", name, NULL );
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
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Parameters file data for must have a known file type", name, NULL );
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
    
    Fieldml_SetParameterDataLocation( context->region, context->currentObject, LOCATION_FILE );
    Fieldml_SetParameterFileData( context->region, context->currentObject, file, fileType, offsetAmount );
}


static void startSwizzleData( SaxContext *context, SaxAttributes *attributes )
{
}


static void onSwizzleData( SaxContext *context, const char *const characters, const int length )
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


static void endSwizzleData( SaxContext *context )
{
    int intCount;
    const int *ints;
    
    intCount = intParserCount( context->buffer );
    ints = intParserInts( context->buffer );
    
    Fieldml_SetSwizzle( context->region, context->currentObject, ints, intCount );
    
    free( (int*)ints );
    free( context->buffer );
    context->buffer = NULL;
    context->bufferLength = 0;
}


static void startSemidenseData( SaxContext *context, SaxAttributes *attributes )
{
    Fieldml_SetParameterDataDescription( context->region, context->currentObject, DESCRIPTION_SEMIDENSE );
}


static void onSemidenseSparseIndex( SaxContext *context, SaxAttributes *attributes )
{
    const char *index;
    FmlObjectHandle handle;
    
    index = getAttribute( attributes, VALUE_ATTRIB );
    if( index == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Missing index in semi dense data", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    Fieldml_AddSemidenseIndex( context->region, context->currentObject, handle, 1 );
}


static void onSemidenseDenseIndex( SaxContext *context, SaxAttributes *attributes )
{
    const char *index;
    FmlObjectHandle handle;
    
    index = getAttribute( attributes, VALUE_ATTRIB );
    if( index == NULL )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Missing index in semi dense data", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    Fieldml_AddSemidenseIndex( context->region, context->currentObject, handle, 0 );
}


static void endSemidenseData( SaxContext *context )
{
}


static void startContinuousPiecewise( SaxContext *context, SaxAttributes *attributes )
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
        logError( context->region, "ContinuousPiecewise has no name", NULL, NULL );
        return;
    }
    
    if( valueDomain == NULL )
    {
        logError( context->region, "ContinuousPiecewise has no value domain", name, NULL );
        return;
    }
    
    if( indexDomain == NULL )
    {
        logError( context->region, "ContinuousPiecewise has no index domain", name, NULL );
        return;
    }
    
    valueHandle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    indexHandle = getOrCreateObjectHandle( context->region, indexDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    context->currentObject = Fieldml_CreateContinuousPiecewise( context->region, name, indexHandle, valueHandle );
}


static void onContinuousPiecewiseEvaluators( SaxContext *context, SaxAttributes *attributes )
{
    const char *defaultValue;
    
    defaultValue = getAttribute( attributes, DEFAULT_ATTRIB );
    if( defaultValue != NULL )
    {
        int defaultHandle = getOrCreateObjectHandle( context->region, defaultValue, FHT_UNKNOWN_CONTINUOUS_EVALUATOR );
        Fieldml_SetDefaultEvaluator( context->region, context->currentObject, defaultHandle );
    }
}

static void onContinuousPiecewiseEntry( SaxContext *context, SaxAttributes *attributes )
{
    const char *key;
    const char *value;
    FmlObjectHandle handle;
    
    key = getAttribute( attributes, KEY_ATTRIB );
    value = getAttribute( attributes, VALUE_ATTRIB );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Malformed element evaluator for ContinuousPiecewise", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
    
    Fieldml_SetEvaluator( context->region, context->currentObject, atoi( key ), handle );
}


static void endContinuousPiecewise( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startContinuousAggregate( SaxContext *context, SaxAttributes *attributes )
{
    const char *name;
    const char *valueDomain;
    FmlObjectHandle valueHandle;
    
    name = getAttribute( attributes, NAME_ATTRIB );
    valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    
    if( name == NULL )
    {
        logError( context->region, "ContinuousAggregate has no name", NULL, NULL );
        return;
    }
    
    if( valueDomain == NULL )
    {
        logError( context->region, "ContinuousAggregate has no value domain", name, NULL );
        return;
    }
    
    valueHandle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    
    context->currentObject = Fieldml_CreateContinuousAggregate( context->region, name, valueHandle );
}


static void onContinuousAggregateEntry( SaxContext *context, SaxAttributes *attributes )
{
    const char *key;
    const char *value;
    FmlObjectHandle handle;
    
    key = getAttribute( attributes, KEY_ATTRIB );
    value = getAttribute( attributes, VALUE_ATTRIB );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Malformed element evaluator for ContinuousAggregate", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->region, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
    
    Fieldml_SetEvaluator( context->region, context->currentObject, atoi( key ), handle );
}


static void endContinuousAggregate( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void startContinuousVariable( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle valueHandle;

    if( name == NULL )
    {
        logError( context->region, "ContinuousVariable has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        logError( context->region, "ContinuousVariable has no value domain", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    context->currentObject = Fieldml_CreateContinuousVariable( context->region, name, valueHandle );
}


static void startEnsembleVariable( SaxContext *context, SaxAttributes *attributes )
{
    const char *name = getAttribute( attributes, NAME_ATTRIB );
    const char *valueDomain = getAttribute( attributes, VALUE_DOMAIN_ATTRIB );
    FmlObjectHandle valueHandle;
    
    if( name == NULL )
    {
        logError( context->region, "EnsembleVariable has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        logError( context->region, "EnsembleVariable has no value domain", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->region, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    context->currentObject = Fieldml_CreateEnsembleVariable( context->region, name, valueHandle );
}


static void endVariable( SaxContext *context )
{
    Fieldml_ValidateObject( context->region, context->currentObject );
    
    context->currentObject = FML_INVALID_HANDLE;
}


static void onMarkupEntry( SaxContext *context, SaxAttributes *attributes )
{
    const char *key;
    const char *value;
    
    key = getAttribute( attributes, KEY_ATTRIB );
    value = getAttribute( attributes, VALUE_ATTRIB );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        const char * name =  Fieldml_GetObjectName( context->region, context->currentObject );
        logError( context->region, "Malformed markup", name, NULL );
        return;
    }
    
    Fieldml_SetMarkup( context->region, context->currentObject, key, value );
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
            startRegion( saxContext, saxAttributes );
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
            startMeshShapes( saxContext, saxAttributes );
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
        if( strcmp( name, MAP_ENTRY_TAG ) == 0 )
        {
            onMeshShape( saxContext, saxAttributes );
        }
        break;
    case FML_MESH_CONNECTIVITY:
        if( strcmp( name, MAP_ENTRY_TAG ) == 0 )
        {
            onMeshConnectivity( saxContext, saxAttributes );
        }
        break;
    case FML_CONTINUOUS_REFERENCE:
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
            onContinuousPiecewiseEvaluators( saxContext, saxAttributes );
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
        if( strcmp( name, MAP_ENTRY_TAG ) == 0 )
        {
            onMarkupEntry( saxContext, saxAttributes );
        }
        break;
    case FML_SOURCE_FIELDS:
        if( strcmp( name, MAP_ENTRY_TAG ) == 0 )
        {
            onContinuousAggregateEntry( saxContext, saxAttributes );
        }
        break;
    case FML_ELEMENT_EVALUATORS:
        if( strcmp( name, MAP_ENTRY_TAG ) == 0 )
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
        if( strcmp( name, MAP_ENTRY_TAG ) == 0 )
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
        else if( strcmp( name, CONTINUOUS_REFERENCE_TAG ) == 0 )
        {
            startContinuousReference( saxContext, saxAttributes );
            intStackPush( saxContext->state, FML_CONTINUOUS_REFERENCE );
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
    case FML_CONTINUOUS_REFERENCE:
        if( strcmp( name, CONTINUOUS_REFERENCE_TAG ) == 0 )
        {
            endContinuousReference( saxContext );
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

FieldmlRegion *parseFieldmlFile( const char *filename )
{
    int res, state;
    SaxContext context;

    context.state = createIntStack();
    context.region = NULL;
    context.currentObject = FML_INVALID_HANDLE;
    context.bufferLength = 0;
    context.buffer = NULL;
    context.source = filename;

    intStackPush( context.state, FML_ROOT );

    LIBXML_TEST_VERSION

    xmlSubstituteEntitiesDefault( 1 );
    
    res = xmlSAXUserParseFile( SAX2Handler, &context, filename );
    if( res != 0 )
    {
        logError( context.region, "xmlSAXUserParseFile returned error", NULL, NULL );
    }

    state = intStackPeek( context.state );
    if( state != FML_ROOT )
    {
        logError( context.region, "Parser state not empty", NULL, NULL );
    }

    xmlCleanupParser();
    xmlMemoryDump();
    
    destroyIntStack( context.state );

    finalizeFieldmlRegion( context.region );

    return context.region;
}
