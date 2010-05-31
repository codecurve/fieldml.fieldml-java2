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
#include <stdio.h>

#include "int_table.h"
#include "string_table.h"
#include "fieldml_api.h"
#include "fieldml_sax.h"
#include "fieldml_structs.h"
#include "fieldml_write.h"
#include "fieldml_validate.h"
#include "fieldml_io.h"

typedef struct _SemidenseStream
{
    int indexCount;
    int valueCount;
    const int *swizzle;
}
SemidenseStream;


typedef enum _ParameterStreamType
{
    SEMIDENSE_STREAM,
}
ParameterStreamType;


typedef struct _ParameterReader
{
    ParameterStreamType type;
    union
    {
        SemidenseStream semidenseStream;
    }
    reader;

    FmlInputStream stream;
    DataFileType streamFormat;
}
ParameterReader;


typedef struct _ParameterWriter
{
    ParameterStreamType type;
    union
    {
        SemidenseStream semidenseStream;
    }
    writer;

    FmlOutputStream stream;
    DataFileType streamFormat;
}
ParameterWriter;


static ParameterReader *createFileReader( FmlHandle handle, FmlObjectHandle parameters )
{
    ParameterReader *reader;
    int i, offset;
    FmlInputStream streamHandle;
    
    if( Fieldml_GetParameterDataDescription( handle, parameters ) == DESCRIPTION_SEMIDENSE )
    {
        int indexCount = Fieldml_GetSemidenseIndexCount( handle, parameters, 1 );
        int firstIndex = Fieldml_GetSemidenseIndex( handle, parameters, 1, 0 );
        int valueCount = Fieldml_GetEnsembleDomainElementCount( handle, firstIndex );
        int swizzleCount = Fieldml_GetSwizzleCount( handle, parameters );
        
        if( ( indexCount < 0 ) ||
            ( firstIndex == FML_INVALID_HANDLE ) ||
            ( valueCount < 1 ) )
        {
            setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
            return NULL;
        }
        
        if( ( swizzleCount > 0 ) && ( swizzleCount != valueCount ) )
        {
            setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
            return NULL;
        }
        
        streamHandle = FmlCreateFileInputStream( Fieldml_GetParameterDataFilename( handle, parameters ) );
        if( streamHandle == NULL )
        {
            setError( handle, FML_ERR_FILE_READ );
            return NULL;
        }
        
        reader = calloc( 1, sizeof( ParameterReader ) );
        reader->type = SEMIDENSE_STREAM;
        reader->streamFormat = Fieldml_GetParameterDataFileType( handle, parameters );
        
        reader->reader.semidenseStream.indexCount = indexCount;
        reader->reader.semidenseStream.valueCount = valueCount;
        reader->stream = streamHandle;
        
        if( swizzleCount == 0 )
        {
            reader->reader.semidenseStream.swizzle = NULL;
        }
        else
        {
            reader->reader.semidenseStream.swizzle = Fieldml_GetSwizzleData( handle, parameters );
        }
        
        setError( handle, FML_ERR_NO_ERROR );
    }
    else
    {
        setError( handle, FML_ERR_UNSUPPORTED );
        return NULL;
    }
    
    offset = Fieldml_GetParameterDataOffset( handle, parameters );
    if( reader->streamFormat == TYPE_LINES )
    {
        for( i = 0; i < offset; i++ )
        {
            FmlInputStreamSkipLine( streamHandle );
        }
    }
    
    return reader;
}


static int readIntSlice( ParameterReader *reader, int *indexBuffer, int *valueBuffer )
{
    if( reader->type == SEMIDENSE_STREAM )
    {
        int i;
        int *buffer = malloc( reader->reader.semidenseStream.valueCount * sizeof(int) );
        
        for( i = 0; i < reader->reader.semidenseStream.indexCount; i++ )
        {
            indexBuffer[i] = FmlInputStreamReadInt( reader->stream );
        }
        
        for( i = 0; i < reader->reader.semidenseStream.valueCount; i++ )
        {
            buffer[i] = FmlInputStreamReadInt( reader->stream );
        }
        
        for( i = 0; i < reader->reader.semidenseStream.valueCount; i++ )
        {
            if( reader->reader.semidenseStream.swizzle != NULL )
            {
                valueBuffer[i] = buffer[reader->reader.semidenseStream.swizzle[i] - 1];
            }
            else
            {
                valueBuffer[i] = buffer[i];
            }
        }
        
        free( buffer );
        
        if( reader->streamFormat == TYPE_LINES )
        {
            FmlInputStreamSkipLine( reader->stream );
        }
        
        return FmlInputStreamIsEof( reader->stream );
    }
    
    return 0;
}


static int readDoubleSlice( ParameterReader *reader, int *indexBuffer, double *valueBuffer )
{
    if( reader->type == SEMIDENSE_STREAM )
    {
        int i;
        double *buffer = malloc( reader->reader.semidenseStream.valueCount * sizeof(double) );
        
        for( i = 0; i < reader->reader.semidenseStream.indexCount; i++ )
        {
            indexBuffer[i] = FmlInputStreamReadInt( reader->stream );
        }
        
        for( i = 0; i < reader->reader.semidenseStream.valueCount; i++ )
        {
            buffer[i] = FmlInputStreamReadDouble( reader->stream );
        }
        
        for( i = 0; i < reader->reader.semidenseStream.valueCount; i++ )
        {
            if( reader->reader.semidenseStream.swizzle != NULL )
            {
                valueBuffer[i] = buffer[reader->reader.semidenseStream.swizzle[i] - 1];
            }
            else
            {
                valueBuffer[i] = buffer[i];
            }
        }
        
        free( buffer );

        if( reader->streamFormat == TYPE_LINES )
        {
            FmlInputStreamSkipLine( reader->stream );
        }

        return FmlInputStreamIsEof( reader->stream );
    }
    
    return 0;
}


static void destroyReader( ParameterReader *reader )
{
    if( reader->type == SEMIDENSE_STREAM )
    {
        FmlInputStreamDestroy( reader->stream );
    }
    
    free( reader );
}


static ParameterWriter *createFileWriter( FmlHandle handle, FmlObjectHandle parameters, int append )
{
    ParameterWriter *writer;
    FmlOutputStream streamHandle;
    
    if( Fieldml_GetParameterDataDescription( handle, parameters ) == DESCRIPTION_SEMIDENSE )
    {
        int indexCount = Fieldml_GetSemidenseIndexCount( handle, parameters, 1 );
        int firstIndex = Fieldml_GetSemidenseIndex( handle, parameters, 1, 0 );
        int valueCount = Fieldml_GetEnsembleDomainElementCount( handle, firstIndex );
        int swizzleCount = Fieldml_GetSwizzleCount( handle, parameters );
        
        if( ( indexCount < 0 ) ||
            ( firstIndex == FML_INVALID_HANDLE ) ||
            ( valueCount < 1 )
            )
        {
            setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
            return NULL;
        }
        
        if( swizzleCount != 0 )
        {
            setError( handle, FML_ERR_UNSUPPORTED );
            return NULL;
        }
        
        if( ( ( Fieldml_GetParameterDataOffset( handle, parameters ) != 0 ) && !append ) )
        {
            setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
            return NULL;
        }
        
        streamHandle = FmlCreateFileOutputStream( Fieldml_GetParameterDataFilename( handle, parameters ), append );
        if( streamHandle == NULL )
        {
            setError( handle, FML_ERR_FILE_WRITE );
            return NULL;
        }
        
        writer = calloc( 1, sizeof( ParameterWriter ) );
        writer->type = SEMIDENSE_STREAM;
        writer->streamFormat = Fieldml_GetParameterDataFileType( handle, parameters );
        
        writer->writer.semidenseStream.indexCount = indexCount;
        writer->writer.semidenseStream.valueCount = valueCount;
        writer->stream = streamHandle;
        
        if( swizzleCount == 0 )
        {
            writer->writer.semidenseStream.swizzle = NULL;
        }
        else
        {
            writer->writer.semidenseStream.swizzle = Fieldml_GetSwizzleData( handle, parameters );
        }
        
        setError( handle, FML_ERR_NO_ERROR );
    }
    else
    {
        setError( handle, FML_ERR_UNSUPPORTED );
        return NULL;
    }
    
    return writer;
}


static int writeIntSlice( ParameterWriter *writer, int *indexBuffer, int *valueBuffer )
{
    if( writer->type == SEMIDENSE_STREAM )
    {
        int i, err;
        
        for( i = 0; i < writer->writer.semidenseStream.indexCount; i++ )
        {
            err = FmlOutputStreamWriteInt( writer->stream, indexBuffer[i] );
        }
        
        for( i = 0; i < writer->writer.semidenseStream.valueCount; i++ )
        {
            err = FmlOutputStreamWriteInt( writer->stream, valueBuffer[i] );
        }

        if( writer->streamFormat == TYPE_LINES )
        {
            FmlOutputStreamWriteNewline( writer->stream );
        }

        return err;
    }
    
    return FML_ERR_INVALID_OBJECT;
}


static int writeDoubleSlice( ParameterWriter *writer, int *indexBuffer, double *valueBuffer )
{
    if( writer->type == SEMIDENSE_STREAM )
    {
        int i, err;
        
        for( i = 0; i < writer->writer.semidenseStream.indexCount; i++ )
        {
            err = FmlOutputStreamWriteInt( writer->stream, indexBuffer[i] );
        }
        
        for( i = 0; i < writer->writer.semidenseStream.valueCount; i++ )
        {
            err = FmlOutputStreamWriteDouble( writer->stream, valueBuffer[i] );
        }

        if( writer->streamFormat == TYPE_LINES )
        {
            FmlOutputStreamWriteNewline( writer->stream );
        }

        return err;
    }

    return FML_ERR_INVALID_OBJECT;
}


static void destroyWriter( ParameterWriter *writer )
{
    if( writer->type == SEMIDENSE_STREAM )
    {
        FmlOutputStreamDestroy( writer->stream );
    }
    
    free( writer );
}


//========================================================================
//
// Utility
//
//========================================================================

static int getTotal( FieldmlRegion *region, FieldmlHandleType type )
{
    int count, i, total;
    FieldmlObject *object;

    total = 0;
    count = getSimpleListCount( region->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( region->objects, i );
        if( object->type == type )
        {
            total++;
        }
    }

    return total;
}


static int getNthHandle( FieldmlRegion *region, FieldmlHandleType type, int index )
{
    int count, i;
    FieldmlObject *object;

    if( index <= 0 )
    {
        return FML_INVALID_HANDLE;
    }

    count = getSimpleListCount( region->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( region->objects, i );
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


static int getNamedHandle( FieldmlRegion *region, const char *name )
{
    int count, i;
    FieldmlObject *object;

    setError( region, FML_ERR_NO_ERROR );  

    count = getSimpleListCount( region->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( region->objects, i );
        if( strcmp( object->name, name ) == 0 )
        {
            return i;
        }
    }

    return FML_INVALID_HANDLE;
}


static IntTable *getEvaluatorTable( FieldmlObject *object )
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


static IntTable *getAliasTable( FieldmlObject *object )
{
    if( object == NULL )
    {
        return NULL;
    }
    else if( object->type == FHT_CONTINUOUS_AGGREGATE )
    {
        return object->object.aggregate->aliases;
    }
    else if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        return object->object.piecewise->aliases;
    }
    else if( object->type == FHT_CONTINUOUS_REFERENCE )
    {
        return object->object.continuousReference->aliases;
    }

    return NULL;
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

FmlHandle Fieldml_CreateFromFile( const char *filename )
{
    return parseFieldmlFile( filename );
}


FmlHandle Fieldml_Create()
{
    return createFieldmlRegion( "" );
}


int Fieldml_SetDebug( FmlHandle handle, int debug )
{
    handle->debug = debug;
    
    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_WriteFile( FmlHandle handle, const char *filename )
{
    return writeFieldmlFile( handle, filename );
}


void Fieldml_Destroy( FmlHandle handle )
{
    destroyFieldmlRegion( handle );
}


int Fieldml_GetErrorCount( FmlHandle handle )
{
    return getSimpleListCount( handle->errors );
}


const char * Fieldml_GetError( FmlHandle handle, int index )
{
    return (const char *)getSimpleListEntry( handle->errors, index - 1 );
}


int Fieldml_CopyError( FmlHandle handle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetError( handle, index ), buffer, bufferLength );
}


int Fieldml_GetObjectCount( FmlHandle handle, FieldmlHandleType type )
{
    if( type == FHT_UNKNOWN )
    {
        return -1;
    }

    return getTotal( handle, type );
}


FmlObjectHandle Fieldml_GetObject( FmlHandle handle, FieldmlHandleType type, int index )
{
    FmlObjectHandle object = getNthHandle( handle, type, index );
    
    if( object == FML_INVALID_HANDLE )
    {
        setError( handle, FML_ERR_INVALID_PARAMETER_3 );  
    }
    else
    {
        setError( handle, FML_ERR_NO_ERROR );  
    }
    
    return object;
}


FmlObjectHandle Fieldml_GetNamedObject( FmlHandle handle, const char * name )
{
    FmlObjectHandle object;
    
    object = getNamedHandle( handle, name );
    
    return object;
}


FieldmlHandleType Fieldml_GetObjectType( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return FHT_UNKNOWN;
    }
    
    return object->type;
}


int Fieldml_SetMarkup( FmlHandle handle, FmlObjectHandle objectHandle, const char * attribute, const char * value )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    
    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }
    
    setStringTableEntry( object->markup, attribute, strdup( value ), free );

    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_GetMarkupCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return -1;
    }
    
    setError( handle, FML_ERR_NO_ERROR );  
    return getStringTableCount( object->markup );
}


int Fieldml_ValidateObject( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_NO_ERROR );
    }
    
    return validateFieldmlObject( handle, object );
}


const char * Fieldml_GetMarkupAttribute( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return NULL;
    }
    
    return getStringTableEntryName( object->markup, index - 1 );
}


int Fieldml_CopyMarkupAttribute( FmlHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMarkupAttribute( handle, objectHandle, index ), buffer, bufferLength );
}


const char * Fieldml_GetMarkupValue( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return NULL;
    }
    
    setError( handle, FML_ERR_NO_ERROR );  
    return (const char*)getStringTableEntryData( object->markup, index - 1 );
}


int Fieldml_CopyMarkupValue( FmlHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMarkupValue( handle, objectHandle, index ), buffer, bufferLength );
}


const char * Fieldml_GetMarkupAttributeValue( FmlHandle handle, FmlObjectHandle objectHandle, const char * attribute )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return NULL;
    }
    
    setError( handle, FML_ERR_NO_ERROR );  
    return (const char*)getStringTableEntry( object->markup, attribute );
}


int Fieldml_CopyMarkupAttributeValue( FmlHandle handle, FmlObjectHandle objectHandle, const char * attribute, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMarkupAttributeValue( handle, objectHandle, attribute ), buffer, bufferLength );
}


FmlObjectHandle Fieldml_GetDomainComponentEnsemble( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return FML_INVALID_HANDLE;
    }

    setError( handle, FML_ERR_NO_ERROR );  
    switch( object->type )
    {
    case FHT_ENSEMBLE_DOMAIN:
        return object->object.ensembleDomain->componentDomain;
    case FHT_CONTINUOUS_DOMAIN:
        return object->object.continuousDomain->componentDomain;
    default:
        break;
    }

    setError( handle, FML_ERR_INVALID_OBJECT );  
    return FML_INVALID_HANDLE;
}


DomainBoundsType Fieldml_GetDomainBoundsType( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL ) 
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }
    if( object->type == FHT_ENSEMBLE_DOMAIN )
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return object->object.ensembleDomain->boundsType;
    }
    
    setError( handle, FML_ERR_INVALID_OBJECT );  
    return BOUNDS_UNKNOWN;
}


int Fieldml_GetEnsembleDomainElementCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL ) 
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }
    if( object->type == FHT_ENSEMBLE_DOMAIN )
    {
        if( object->object.ensembleDomain->boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            setError( handle, FML_ERR_NO_ERROR );  
            return object->object.ensembleDomain->bounds.contiguous.count;
        }
        
        setError( handle, FML_ERR_UNSUPPORTED );  
        return -1;
    }

    setError( handle, FML_ERR_INVALID_OBJECT );  
    return -1;
}


int Fieldml_GetContiguousBoundsCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }
    
    if( object->type == FHT_ENSEMBLE_DOMAIN )
    {
        if( object->object.ensembleDomain->boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            setError( handle, FML_ERR_NO_ERROR );  
            return object->object.ensembleDomain->bounds.contiguous.count;
        }
        
        setError( handle, FML_ERR_INVALID_OBJECT );  
        return -1;
    }
    else if( object->type == FHT_MESH_DOMAIN )
    {
        FieldmlObject *subObject;
        
        subObject = getSimpleListEntry( handle->objects, object->object.meshDomain->elementDomain );
        
        if( ( subObject == NULL ) || ( subObject->type != FHT_ENSEMBLE_DOMAIN ) )
        {
            return setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
        }
        
        return Fieldml_GetContiguousBoundsCount( handle, object->object.meshDomain->elementDomain );
    }
    else
    {
        setError( handle, FML_ERR_INVALID_OBJECT );  
        return -1;
    }


}


int Fieldml_SetContiguousBoundsCount( FmlHandle handle, FmlObjectHandle objectHandle, int count )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( object->regionHandle != FILE_REGION_HANDLE )
    {
        return setError( handle, FML_ERR_ACCESS_VIOLATION );
    }
    
    if( object->type == FHT_ENSEMBLE_DOMAIN )
    {
        object->object.ensembleDomain->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
        object->object.ensembleDomain->bounds.contiguous.count = count;
        
        return setError( handle, FML_ERR_NO_ERROR );
    }
    else if( object->type == FHT_MESH_DOMAIN )
    {
        FieldmlObject *subObject;
        
        subObject = getSimpleListEntry( handle->objects, object->object.meshDomain->elementDomain );
        
        if( ( subObject == NULL ) || ( subObject->type != FHT_ENSEMBLE_DOMAIN ) )
        {
            return setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
        }
        
        subObject->object.ensembleDomain->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
        subObject->object.ensembleDomain->bounds.contiguous.count = count;
        
        return setError( handle, FML_ERR_NO_ERROR );
    }

    return setError( handle, FML_ERR_INVALID_OBJECT );
}


FmlObjectHandle Fieldml_GetMeshElementDomain( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    if( object->type == FHT_MESH_DOMAIN ) 
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return object->object.meshDomain->elementDomain;
    }
    
    setError( handle, FML_ERR_INVALID_OBJECT );  
    return FML_INVALID_HANDLE;
}


const char * Fieldml_GetMeshElementShape( FmlHandle handle, FmlObjectHandle objectHandle, int elementNumber, int allowDefault )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return NULL;
    }
    if( object->type == FHT_MESH_DOMAIN ) 
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return (char*)getIntTableEntry( object->object.meshDomain->shapes, elementNumber, allowDefault );
    }

    setError( handle, FML_ERR_INVALID_OBJECT );
    return NULL;
}


int Fieldml_CopyMeshElementShape( FmlHandle handle, FmlObjectHandle objectHandle, int elementNumber, int allowDefault, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMeshElementShape( handle, objectHandle, elementNumber, allowDefault ), buffer, bufferLength );
}


int Fieldml_GetMeshConnectivityCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }
    if( object->type == FHT_MESH_DOMAIN ) 
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return getIntTableCount( object->object.meshDomain->connectivity );
    }

    setError( handle, FML_ERR_INVALID_OBJECT );
    return -1;
}


FmlObjectHandle Fieldml_GetMeshConnectivityDomain( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    if( object->type == FHT_MESH_DOMAIN ) 
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return getIntTableEntryIntData( object->object.meshDomain->connectivity, index - 1 );
    }

    setError( handle, FML_ERR_INVALID_OBJECT );
    return FML_INVALID_HANDLE;
}


FmlObjectHandle Fieldml_GetMeshConnectivitySource( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    if( object->type == FHT_MESH_DOMAIN ) 
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return getIntTableEntryName( object->object.meshDomain->connectivity, index - 1 );
    }

    setError( handle, FML_ERR_INVALID_OBJECT );
    return FML_INVALID_HANDLE;
}


FmlObjectHandle Fieldml_GetMeshXiDomain( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    if( object->type == FHT_MESH_DOMAIN ) 
    {
        setError( handle, FML_ERR_NO_ERROR );  
        return object->object.meshDomain->xiDomain;
    }
    
    setError( handle, FML_ERR_INVALID_OBJECT );  
    return FML_INVALID_HANDLE;
}


const char * Fieldml_GetObjectName( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return NULL;
    }

    setError( handle, FML_ERR_NO_ERROR );
    return object->name;
}


int Fieldml_CopyObjectName( FmlHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetObjectName( handle, objectHandle ), buffer, bufferLength );
}


FmlObjectHandle Fieldml_GetValueDomain( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );  
        return FML_INVALID_HANDLE;
    }

    setError( handle, FML_ERR_NO_ERROR );  
    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        return object->object.parameters->valueDomain;
    }
    else if( object->type == FHT_CONTINUOUS_REFERENCE )
    {
        return object->object.continuousReference->valueDomain;
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

    setError( handle, FML_ERR_INVALID_OBJECT );  
    return FML_INVALID_HANDLE;
}


FmlObjectHandle Fieldml_CreateEnsembleVariable( FmlHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;

    object = createEnsembleVariable( name, FILE_REGION_HANDLE, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_CreateContinuousVariable( FmlHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;

    object = createContinuousVariable( name, FILE_REGION_HANDLE, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_CreateEnsembleParameters( FmlHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;

    object = createEnsembleParameters( name, FILE_REGION_HANDLE, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_CreateContinuousParameters( FmlHandle handle, const char *name, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;

    object = createContinuousParameters( name, FILE_REGION_HANDLE, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


int Fieldml_SetParameterDataDescription( FmlHandle handle, FmlObjectHandle objectHandle, DataDescriptionType description )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType != DESCRIPTION_UNKNOWN )
        {
            return setError( handle, FML_ERR_ACCESS_VIOLATION );
        }

        object->object.parameters->descriptionType = description;
        
        if( description == DESCRIPTION_SEMIDENSE )
        {
            object->object.parameters->dataDescription.semidense = createSemidenseData();
            return setError( handle, FML_ERR_NO_ERROR );  
        }
        else
        {
            return setError( handle, FML_ERR_UNSUPPORTED );  
        }
    }

    return setError( handle, FML_ERR_INVALID_OBJECT );
}

DataDescriptionType Fieldml_GetParameterDataDescription( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return DESCRIPTION_UNKNOWN;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        setError( handle, FML_ERR_NO_ERROR );
        return object->object.parameters->descriptionType;
    }

    setError( handle, FML_ERR_INVALID_OBJECT );
    return DESCRIPTION_UNKNOWN;
}


DataLocationType Fieldml_GetParameterDataLocation( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return LOCATION_UNKNOWN;
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            setError( handle, FML_ERR_NO_ERROR );
            return object->object.parameters->dataDescription.semidense->locationType;
        }
        else
        {
            setError( handle, FML_ERR_UNSUPPORTED );
        }
    }
    else
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
    }

    return LOCATION_UNKNOWN;
}


int Fieldml_SetParameterDataLocation( FmlHandle handle, FmlObjectHandle objectHandle, DataLocationType location )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) || ( object->type == FHT_CONTINUOUS_PARAMETERS ) ) 
    {
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            if( object->object.parameters->dataDescription.semidense->locationType == LOCATION_UNKNOWN )
            {
                object->object.parameters->dataDescription.semidense->locationType = location;
                return setError( handle, FML_ERR_NO_ERROR );
            }
            else
            {
                return setError( handle, FML_ERR_ACCESS_VIOLATION );
            }
        }
        else
        {
            return setError( handle, FML_ERR_UNSUPPORTED );
        }
    }
    else
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }
}


int Fieldml_AddInlineParameterData( FmlHandle handle, FmlObjectHandle objectHandle, const char *data, int length )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    Parameters *parameters;
    StringDataSource *source;
    char *newString;
    
    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }
    
    if( ( object->type != FHT_CONTINUOUS_PARAMETERS ) && ( object->type != FHT_ENSEMBLE_PARAMETERS ) )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }
    
    parameters = object->object.parameters;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType == LOCATION_INLINE )
        {
            source = &(parameters->dataDescription.semidense->dataLocation.stringData);
        }
        else
        {
            return setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
        }
    }
    else
    {
        return setError( handle, FML_ERR_UNSUPPORTED );
    }

    newString = malloc( source->length + length + 1 );
    memcpy( newString, source->string, source->length );
    memcpy( newString + source->length, data, length );
    source->length += length;
    newString[ source->length ] = 0;
    free( source->string );
    source->string = newString;
    
    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_SetParameterFileData( FmlHandle handle, FmlObjectHandle objectHandle, const char * filename, DataFileType type, int offset )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    Parameters *parameters;
    FileDataSource *source;
    
    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }
    
    if( ( object->type != FHT_CONTINUOUS_PARAMETERS ) && ( object->type != FHT_ENSEMBLE_PARAMETERS ) )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }
    
    parameters = object->object.parameters;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType == LOCATION_FILE )
        {
            source = &(parameters->dataDescription.semidense->dataLocation.fileData);
        }
        else
        {
            return setError( handle, FML_ERR_MISCONFIGURED_OBJECT );
        }
    }
    else
    {
        return setError( handle, FML_ERR_UNSUPPORTED );
    }
    
    if( source->filename != NULL )
    {
        free( source->filename );
    }
    
    source->filename = strdup( filename );
    source->fileType = type;
    source->offset = offset;
    
    return setError( handle, FML_ERR_NO_ERROR );
}


const char *Fieldml_GetParameterDataFilename( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    FileDataSource *source;

    if( object == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return NULL;
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
            else
            {
                setError( handle, FML_ERR_UNSUPPORTED );
            }
        }
        else
        {
            setError( handle, FML_ERR_UNSUPPORTED );
        }
    }
    else
    {
        setError( handle, FML_ERR_INVALID_OBJECT);
    }
    
    if( source != NULL )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return source->filename;
    }
    
    return NULL;
}


int Fieldml_CopyParameterDataFilename( FmlHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetParameterDataFilename( handle, objectHandle ), buffer, bufferLength );
}


int Fieldml_GetParameterDataOffset( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    FileDataSource *source;

    if( object == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
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
            else
            {
                setError( handle, FML_ERR_UNSUPPORTED );
            }
        }
        else
        {
            setError( handle, FML_ERR_UNSUPPORTED );
        }
    }
    else
    {
        setError( handle, FML_ERR_INVALID_OBJECT);
    }
    
    if( source != NULL )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return source->offset;
    }

    return 0;
}


DataFileType Fieldml_GetParameterDataFileType( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    FileDataSource *source;

    if( object == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
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
            else
            {
                setError( handle, FML_ERR_UNSUPPORTED );
            }
        }
        else
        {
            setError( handle, FML_ERR_UNSUPPORTED );
        }
    }
    else
    {
        setError( handle, FML_ERR_INVALID_OBJECT);
    }
    
    if( source != NULL )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return source->fileType;
    }

    return TYPE_UNKNOWN;
}


int Fieldml_AddSemidenseIndex( FmlHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle indexHandle, int isSparse )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }

    if( isSparse )
    {
        intStackPush( object->object.parameters->dataDescription.semidense->sparseIndexes, indexHandle );
    }
    else
    {
        intStackPush( object->object.parameters->dataDescription.semidense->denseIndexes, indexHandle );
    }
    
    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_GetSemidenseIndexCount( FmlHandle handle, FmlObjectHandle objectHandle, int isSparse )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return -1;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        setError( handle, FML_ERR_UNSUPPORTED );
        return -1;
    }

    setError( handle, FML_ERR_NO_ERROR );
    if( isSparse )
    {
        return intStackGetCount( object->object.parameters->dataDescription.semidense->sparseIndexes );
    }
    else
    {
        return intStackGetCount( object->object.parameters->dataDescription.semidense->denseIndexes );
    }
}


FmlObjectHandle Fieldml_GetSemidenseIndex( FmlHandle handle, FmlObjectHandle objectHandle, int index, int isSparse )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return FML_INVALID_HANDLE;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        setError( handle, FML_ERR_UNSUPPORTED );
        return FML_INVALID_HANDLE;
    }

    setError( handle, FML_ERR_NO_ERROR );
    if( isSparse )
    {
        return intStackGet( object->object.parameters->dataDescription.semidense->sparseIndexes, index - 1 );
    }
    else
    {
        return intStackGet( object->object.parameters->dataDescription.semidense->denseIndexes, index - 1 );
    }
}


int Fieldml_SetSwizzle( FmlHandle handle, FmlObjectHandle objectHandle, const int *buffer, int count )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    int *ints;
    int ensembleHandle, ensembleCount;
    
    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }
    
    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return setError( handle, FML_ERR_UNSUPPORTED );
    }
    
    ensembleHandle = Fieldml_GetSemidenseIndex( handle, objectHandle, 1, 0 );
    ensembleCount = Fieldml_GetEnsembleDomainElementCount( handle, ensembleHandle );
    
    if( ensembleCount != count )
    {
        return setError( handle, FML_ERR_INVALID_PARAMETER_4 );
    }
    
    if( object->object.parameters->dataDescription.semidense->swizzle != NULL )
    {
        free( (int*)object->object.parameters->dataDescription.semidense->swizzle );
    }
    
    ints = malloc( count * sizeof( int ) );
    memcpy( ints, buffer, sizeof( int ) * count );
    
    object->object.parameters->dataDescription.semidense->swizzleCount = count;
    object->object.parameters->dataDescription.semidense->swizzle = ints;
    
    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_GetSwizzleCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return -1;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        setError( handle, FML_ERR_UNSUPPORTED );
        return -1;
    }
    
    setError( handle, FML_ERR_NO_ERROR );
    return object->object.parameters->dataDescription.semidense->swizzleCount;
}


const int *Fieldml_GetSwizzleData( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return NULL;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return NULL;
    }

    if( object->object.parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        setError( handle, FML_ERR_UNSUPPORTED );
        return NULL;
    }
    
    setError( handle, FML_ERR_NO_ERROR );
    return object->object.parameters->dataDescription.semidense->swizzle;
}


int Fieldml_CopySwizzleData( FmlHandle handle, FmlObjectHandle objectHandle, int *buffer, int bufferLength )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    int length, swizzleCount;
    const int *swizzle;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return 0;
    }

    if( ( object->type != FHT_ENSEMBLE_PARAMETERS ) && ( object->type != FHT_CONTINUOUS_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
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
        setError( handle, FML_ERR_UNSUPPORTED );
        return 0;
    }
    
    length = swizzleCount;
    
    if( length > bufferLength )
    {
        length = bufferLength;
    }
    
    memcpy( buffer, swizzle, length * sizeof( int ) );
    
    setError( handle, FML_ERR_NO_ERROR );
    return length;
}


FmlObjectHandle Fieldml_CreateContinuousPiecewise( FmlHandle handle, const char * name, FmlObjectHandle indexHandle, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;

    object = createContinuousPiecewise( name, FILE_REGION_HANDLE, indexHandle, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_CreateContinuousAggregate( FmlHandle handle, const char * name, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;

    object = createContinuousAggregate( name, FILE_REGION_HANDLE, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


int Fieldml_SetDefaultEvaluator( FmlHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle evaluator )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }

    if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        setIntTableDefaultInt( table, evaluator );
        return setError( handle, FML_ERR_NO_ERROR );
    }

    return setError( handle, FML_ERR_INVALID_OBJECT );
}


int Fieldml_GetDefaultEvaluator( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return FML_INVALID_HANDLE;
    }

    if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return getIntTableDefaultInt( table );
    }

    setError( handle, FML_ERR_INVALID_OBJECT );
    return FML_INVALID_HANDLE;
}


int Fieldml_SetEvaluator( FmlHandle handle, FmlObjectHandle objectHandle, int element, FmlObjectHandle evaluator )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return -1;
    }

    setIntTableIntEntry( table, element, evaluator );
    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_GetEvaluatorCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return -1;
    }

    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableCount( table );
}


int Fieldml_GetEvaluatorElement( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return -1;
    }

    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableEntryName( table, index - 1 );
}


FmlObjectHandle Fieldml_GetEvaluator( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return FML_INVALID_HANDLE;
    }

    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableEntryIntData( table, index - 1 );
}


FmlObjectHandle Fieldml_GetElementEvaluator( FmlHandle handle, FmlObjectHandle objectHandle, int elementNumber, int allowDefault )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    
    table = getEvaluatorTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return FML_INVALID_HANDLE;
    }

    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableIntEntry( table, elementNumber, allowDefault );
}


FmlObjectHandle Fieldml_CreateContinuousReference( FmlHandle handle, const char * name, FmlObjectHandle remoteEvaluator, FmlObjectHandle valueDomain )
{
    FieldmlObject *object;
    
    object = createContinuousReference( name, FILE_REGION_HANDLE, remoteEvaluator, valueDomain );
    
    setError( handle, FML_ERR_NO_ERROR );
    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_GetReferenceRemoteEvaluator( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }

    if( object->type == FHT_CONTINUOUS_REFERENCE )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return object->object.continuousReference->remoteEvaluator;
    }
    
    
    setError( handle, FML_ERR_INVALID_OBJECT );
    return FML_INVALID_HANDLE;
}


int Fieldml_GetAliasCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }
    
    table = getAliasTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return -1;
    }
    
    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableCount( table );
}


FmlObjectHandle Fieldml_GetAliasLocal( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    
    table = getAliasTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return FML_INVALID_HANDLE;
    }
    
    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableEntryIntData( table, index - 1 );
}


FmlObjectHandle Fieldml_GetAliasRemote( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    
    table = getAliasTable( object );
    if( table == NULL )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return FML_INVALID_HANDLE;
    }
    
    setError( handle, FML_ERR_NO_ERROR );
    return getIntTableEntryName( table, index - 1 );
}


FmlObjectHandle Fieldml_GetAliasByRemote( FmlHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle remoteHandle )
{
    int count, i, aliasHandle;
    
    count = Fieldml_GetAliasCount( handle, objectHandle );
    if( count == -1 )
    {
        return FML_INVALID_HANDLE;
    }
    
    for( i = 1; i <= count; i++ )
    {
        aliasHandle = Fieldml_GetAliasRemote( handle, objectHandle, i );
        if( aliasHandle == remoteHandle )
        {
            return Fieldml_GetAliasLocal( handle, objectHandle, i );
        }
    }
    
    return FML_INVALID_HANDLE;
}


int Fieldml_SetAlias( FmlHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle remoteDomain, FmlObjectHandle localSource )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    IntTable *table;
    
    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }
    
    table = getAliasTable( object );
    if( table == NULL )
    {
        return setError( handle, FML_ERR_INVALID_OBJECT );
    }
    
    setIntTableIntEntry( table, remoteDomain, localSource );
    return setError( handle, FML_ERR_NO_ERROR );
}



int Fieldml_GetIndexCount( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return -1;
    }
    
    if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return 1;
    }
    else if( ( object->type == FHT_CONTINUOUS_PARAMETERS ) || ( object->type == FHT_ENSEMBLE_PARAMETERS ) )
    {
        int count1, count2;
        
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            count1 = intStackGetCount( object->object.parameters->dataDescription.semidense->sparseIndexes );
            count2 = intStackGetCount( object->object.parameters->dataDescription.semidense->denseIndexes );
            setError( handle, FML_ERR_NO_ERROR );
            return count1 + count2;
        }
        
        setError( handle, FML_ERR_UNSUPPORTED );
        return -1;
    }
    
    setError( handle, FML_ERR_INVALID_OBJECT );
    return -1;
}


FmlObjectHandle Fieldml_GetIndexDomain( FmlHandle handle, FmlObjectHandle objectHandle, int index )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return FML_INVALID_HANDLE;
    }
    
    if( index <= 0 )
    {
        setError( handle, FML_ERR_INVALID_PARAMETER_3 );
        return FML_INVALID_HANDLE;
    }
    
    if( object->type == FHT_CONTINUOUS_PIECEWISE )
    {
        if( index == 1 )
        {
            setError( handle, FML_ERR_NO_ERROR );
            return object->object.piecewise->indexDomain;
        }
        
        setError( handle, FML_ERR_INVALID_PARAMETER_3 );
        return FML_INVALID_HANDLE;
    }
    else if( ( object->type == FHT_CONTINUOUS_PARAMETERS ) || ( object->type == FHT_ENSEMBLE_PARAMETERS ) )
    {
        int count;
        
        if( object->object.parameters->descriptionType == DESCRIPTION_SEMIDENSE )
        {
            count = intStackGetCount( object->object.parameters->dataDescription.semidense->sparseIndexes );
            
            if( index <= count )
            {
                setError( handle, FML_ERR_NO_ERROR );
                return intStackGet( object->object.parameters->dataDescription.semidense->sparseIndexes, index - 1 );
            }

            index -= count;
            count = intStackGetCount( object->object.parameters->dataDescription.semidense->denseIndexes );

            if( index <= count )
            {
                setError( handle, FML_ERR_NO_ERROR );
                return intStackGet( object->object.parameters->dataDescription.semidense->denseIndexes, index - 1 );
            }
            
            setError( handle, FML_ERR_INVALID_PARAMETER_3 );
        }
        else
        {
            setError( handle, FML_ERR_UNSUPPORTED );
        }
    }
    
    setError( handle, FML_ERR_INVALID_OBJECT );

    return FML_INVALID_HANDLE;
}


FmlObjectHandle Fieldml_CreateContinuousDomain( FmlHandle handle, const char * name, FmlObjectHandle componentHandle )
{
    FieldmlObject *object;

    if( ( componentHandle != FML_INVALID_HANDLE ) &&
        ( Fieldml_GetObjectType( handle, componentHandle ) != FHT_ENSEMBLE_DOMAIN ) )
    {
        return setError( handle, FML_ERR_INVALID_PARAMETER_3 );
    }

    object = createContinuousDomain( name, FILE_REGION_HANDLE, componentHandle );
    
    setError( handle, FML_ERR_NO_ERROR );

    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_CreateEnsembleDomain( FmlHandle handle, const char * name, FmlObjectHandle componentHandle )
{
    FieldmlObject *object;

    if( ( componentHandle != FML_INVALID_HANDLE ) &&
        ( Fieldml_GetObjectType( handle, componentHandle ) != FHT_ENSEMBLE_DOMAIN ) )
    {
        return setError( handle, FML_ERR_INVALID_PARAMETER_3 );
    }

    object = createEnsembleDomain( name, FILE_REGION_HANDLE, componentHandle );
    
    setError( handle, FML_ERR_NO_ERROR );

    return addFieldmlObject( handle, object );
}


FmlObjectHandle Fieldml_CreateMeshDomain( FmlHandle handle, const char * name, FmlObjectHandle xiEnsemble )
{
    FieldmlObject *object, *xiObject, *elementObject;
    FmlObjectHandle xiHandle, elementHandle;
    char *subName;

    if( ( xiEnsemble == FML_INVALID_HANDLE ) ||
        ( Fieldml_GetObjectType( handle, xiEnsemble ) != FHT_ENSEMBLE_DOMAIN ) )
    {
        setError( handle, FML_ERR_INVALID_PARAMETER_3 );
        return FML_INVALID_HANDLE;
    }
    
    subName = calloc( 1, strlen( name ) + 12 );

    strcpy( subName, name );
    strcat( subName, ".xi" );
    if( Fieldml_GetNamedObject( handle, subName ) != FML_INVALID_HANDLE )
    {
        setError( handle, FML_ERR_INVALID_PARAMETER_2 );
        return FML_INVALID_HANDLE;
    }

    strcpy( subName, name );
    strcat( subName, ".elements" );
    if( Fieldml_GetNamedObject( handle, subName ) != FML_INVALID_HANDLE )
    {
        setError( handle, FML_ERR_INVALID_PARAMETER_2 );
        return FML_INVALID_HANDLE;
    }
    
    strcpy( subName, name );
    strcat( subName, ".xi" );
    xiObject = createContinuousDomain( subName, VIRTUAL_REGION_HANDLE, xiEnsemble );
    xiHandle = addFieldmlObject( handle, xiObject );
    
    strcpy( subName, name );
    strcat( subName, ".elements" );
    elementObject = createEnsembleDomain( subName, VIRTUAL_REGION_HANDLE, FML_INVALID_HANDLE );
    elementHandle = addFieldmlObject( handle, elementObject );
    
    object = createMeshDomain( name, FILE_REGION_HANDLE, xiHandle, elementHandle );

    setError( handle, FML_ERR_NO_ERROR );

    return addFieldmlObject( handle, object );
}


int Fieldml_SetMeshDefaultShape( FmlHandle handle, FmlObjectHandle mesh, const char * shape )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, mesh );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( object->type == FHT_MESH_DOMAIN )
    {
        setIntTableDefault( object->object.meshDomain->shapes, strdup( shape ), free );
        return setError( handle, FML_ERR_NO_ERROR );
    }
    
    return setError( handle, FML_ERR_INVALID_OBJECT );
}


const char *Fieldml_GetMeshDefaultShape( FmlHandle handle, FmlObjectHandle mesh )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, mesh );

    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return NULL;
    }

    if( object->type == FHT_MESH_DOMAIN )
    {
        setError( handle, FML_ERR_NO_ERROR );
        return (char*)getIntTableDefault( object->object.meshDomain->shapes );
    }
    
    setError( handle, FML_ERR_INVALID_OBJECT );
    return NULL;
}


int Fieldml_CopyMeshDefaultShape( FmlHandle handle, FmlObjectHandle mesh, char * buffer, int bufferLength )
{
    return cappedCopy( Fieldml_GetMeshDefaultShape( handle, mesh ), buffer, bufferLength );
}


int Fieldml_SetMeshElementShape( FmlHandle handle, FmlObjectHandle mesh, int elementNumber, const char * shape )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, mesh );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( object->type == FHT_MESH_DOMAIN )
    {
        setIntTableEntry( object->object.meshDomain->shapes, elementNumber, strdup( shape ), free );
        return setError( handle, FML_ERR_NO_ERROR );
    }
    
    return setError( handle, FML_ERR_INVALID_OBJECT );
}


int Fieldml_SetMeshConnectivity( FmlHandle handle, FmlObjectHandle mesh, FmlObjectHandle evaluator, FmlObjectHandle pointDomain )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, mesh );

    if( object == NULL )
    {
        return setError( handle, FML_ERR_UNKNOWN_OBJECT );
    }

    if( ( pointDomain == FML_INVALID_HANDLE ) || ( evaluator == FML_INVALID_HANDLE ) )
    {
        // This could be use to 'un-set' a connectivity.
        return setError( handle, FML_ERR_INVALID_PARAMETER_3 );
    }
    
    if( Fieldml_GetObjectType( handle, pointDomain ) != FHT_ENSEMBLE_DOMAIN )
    {
        return setError( handle, FML_ERR_INVALID_PARAMETER_3 );
    }
    
    if( object->type == FHT_MESH_DOMAIN )
    {
        setIntTableIntEntry( object->object.meshDomain->connectivity, evaluator, pointDomain );
        return setError( handle, FML_ERR_NO_ERROR );
    }
    
    return setError( handle, FML_ERR_INVALID_OBJECT );
}


FmlReaderHandle Fieldml_OpenReader( FmlHandle handle, FmlObjectHandle objectHandle )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    Parameters *parameters;
    ParameterReader *reader;
    
    reader = NULL;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return NULL;
    }
    
    if( ( object->type != FHT_CONTINUOUS_PARAMETERS ) && ( object->type != FHT_ENSEMBLE_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return NULL;
    }
    
    parameters = object->object.parameters;
    
    if( parameters->dataDescription.semidense->locationType == LOCATION_FILE )
    {
        reader = createFileReader( handle, objectHandle );
    }
    else
    {
        setError( handle, FML_ERR_UNSUPPORTED );
    }
    
    return reader;
}


int Fieldml_ReadIntSlice( FmlHandle handle, FmlReaderHandle reader, int *indexBuffer, int *valueBuffer )
{
    int err = readIntSlice( reader, indexBuffer, valueBuffer );
    
    return setError( handle, err );
}


int Fieldml_ReadDoubleSlice( FmlHandle handle, FmlReaderHandle reader, int *indexBuffer, double *valueBuffer )
{
    int err = readDoubleSlice( reader, indexBuffer, valueBuffer );
    
    return setError( handle, err );
}


int Fieldml_CloseReader( FmlHandle handle, FmlReaderHandle reader )
{
    destroyReader( reader );
    
    return setError( handle, FML_ERR_NO_ERROR );
}


FmlWriterHandle Fieldml_OpenWriter( FmlHandle handle, FmlObjectHandle objectHandle, int append )
{
    FieldmlObject *object = getSimpleListEntry( handle->objects, objectHandle );
    Parameters *parameters;
    ParameterWriter *writer;
    
    writer = NULL;
    
    if( object == NULL )
    {
        setError( handle, FML_ERR_UNKNOWN_OBJECT );
        return NULL;
    }
    
    if( ( object->type != FHT_CONTINUOUS_PARAMETERS ) && ( object->type != FHT_ENSEMBLE_PARAMETERS ) )
    {
        setError( handle, FML_ERR_INVALID_OBJECT );
        return NULL;
    }
    
    parameters = object->object.parameters;
    
    if( parameters->dataDescription.semidense->locationType == LOCATION_FILE )
    {
        writer = createFileWriter( handle, objectHandle, append );
    }
    else
    {
        setError( handle, FML_ERR_UNSUPPORTED );
    }
    
    return writer;
}


int Fieldml_WriteIntSlice( FmlHandle handle, FmlWriterHandle writer, int *indexBuffer, int *valueBuffer )
{
    int err = writeIntSlice( writer, indexBuffer, valueBuffer );
    
    return setError( handle, err );
}


int Fieldml_WriteDoubleSlice( FmlHandle handle, FmlWriterHandle writer, int *indexBuffer, double *valueBuffer )
{
    int err = writeDoubleSlice( writer, indexBuffer, valueBuffer );
    
    return setError( handle, err );
}


int Fieldml_CloseWriter( FmlHandle handle, FmlWriterHandle writer )
{
    destroyWriter( writer );
    
    return setError( handle, FML_ERR_NO_ERROR );
}


int Fieldml_GetLastError( FmlHandle handle )
{
    return getError( handle );
}
