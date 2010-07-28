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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libxml/encoding.h>
#include <libxml/xmlwriter.h>

#include "string_const.h"
#include "string_table.h"
#include "fieldml_write.h"
#include "fieldml_structs.h"
#include "fieldml_api.h"

const char * MY_ENCODING = "ISO-8859-1";


static void writeObjectName( xmlTextWriterPtr writer, FieldmlRegion *region, const char *attribute, int handle )
{
    xmlTextWriterWriteAttribute( writer, attribute, Fieldml_GetObjectName( region, handle ) );
}


static void writeIntArray( xmlTextWriterPtr writer, const char *tag, int count, const int *array )
{
    int i;

    xmlTextWriterStartElement( writer, tag );
    
    for( i = 0; i < count; i++ )
    {
        xmlTextWriterWriteFormatString( writer, "%d ", array[i] );
    }

    xmlTextWriterEndElement( writer );
}


static void writeStringTableEntry( xmlTextWriterPtr writer, const char *key, const char *value )
{
    xmlTextWriterStartElement( writer, MAP_ENTRY_TAG );
    xmlTextWriterWriteAttribute( writer, KEY_ATTRIB, key );
    xmlTextWriterWriteAttribute( writer, VALUE_ATTRIB, value );
    xmlTextWriterEndElement( writer );
}


static void writeListEntry( xmlTextWriterPtr writer, const char *value )
{
    xmlTextWriterStartElement( writer, ENTRY_TAG );
    xmlTextWriterWriteAttribute( writer, VALUE_ATTRIB, value );
    xmlTextWriterEndElement( writer );
}


static void writeStringTable( xmlTextWriterPtr writer, const char *name, StringTable *table )
{
    int i, count;
    void *defaultValue;
    
    count = getStringTableCount( table );
    if( ( count == 0 ) && ( getStringTableDefault( table ) == NULL ) )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );
    
    defaultValue = getStringTableDefault( table );
    if( defaultValue != NULL )
    {
        xmlTextWriterWriteFormatAttribute( writer, DEFAULT_ATTRIB, "%s", defaultValue );
    }
    
    for( i = 0; i < count; i++ )
    {
        writeStringTableEntry( writer, getStringTableEntryName( table, i ), getStringTableEntryData( table, i ) );
    }
    
    xmlTextWriterEndElement( writer );
}


static void writeIntTableEntry( xmlTextWriterPtr writer, int key, const char *value )
{
    xmlTextWriterStartElement( writer, MAP_ENTRY_TAG );
    xmlTextWriterWriteFormatAttribute( writer, KEY_ATTRIB, "%d", key );
    xmlTextWriterWriteAttribute( writer, VALUE_ATTRIB, value );
    xmlTextWriterEndElement( writer );
}


static void writeIntTable( xmlTextWriterPtr writer, const char *name, IntTable *table )
{
    int i, count;
    void *defaultValue;
    
    count = getIntTableCount( table );
    if( ( count == 0 ) && ( getIntTableDefault( table ) == NULL ) )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );
    defaultValue = getIntTableDefault( table );
    if( defaultValue != NULL )
    {
        xmlTextWriterWriteFormatAttribute( writer, DEFAULT_ATTRIB, "%s", defaultValue );
    }
    
    for( i = 0; i < count; i++ )
    {
        writeIntTableEntry( writer, getIntTableEntryName( table, i ), (char*)getIntTableEntryData( table, i ) );
    }
    
    xmlTextWriterEndElement( writer );
}


static void writeObjectObjectTable( xmlTextWriterPtr writer, FieldmlRegion *region, const char *name, IntTable *table )
{
    int i, count, objectHandle;
    
    count = getIntTableCount( table );
    if( ( count == 0 ) && ( getIntTableDefault( table ) == NULL ) )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );
    objectHandle = getIntTableDefaultInt( table );
    if( objectHandle != FML_INVALID_HANDLE )
    {
        xmlTextWriterWriteFormatAttribute( writer, DEFAULT_ATTRIB, "%s", Fieldml_GetObjectName( region, objectHandle ) );
    }

    for( i = 0; i < count; i++ )
    {
        objectHandle = getIntTableEntryIntData( table, i );
        if( objectHandle == FML_INVALID_HANDLE )
        {
            continue;
        }
        
        writeStringTableEntry( writer,
            Fieldml_GetObjectName( region, getIntTableEntryName( table, i ) ),
            Fieldml_GetObjectName( region, objectHandle )
            );
    }

    xmlTextWriterEndElement( writer );
}


static void writeIntObjectTable( xmlTextWriterPtr writer, FieldmlRegion *region, const char *name, IntTable *table )
{
    int i, count, objectHandle;
    
    count = getIntTableCount( table );
    if( ( count == 0 ) && ( getIntTableDefault( table ) == NULL ) )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );
    objectHandle = getIntTableDefaultInt( table );
    if( objectHandle != FML_INVALID_HANDLE )
    {
        xmlTextWriterWriteFormatAttribute( writer, DEFAULT_ATTRIB, "%s", Fieldml_GetObjectName( region, objectHandle ) );
    }

    for( i = 0; i < count; i++ )
    {
        objectHandle = getIntTableEntryIntData( table, i );
        if( objectHandle == FML_INVALID_HANDLE )
        {
            continue;
        }
        
        writeIntTableEntry( writer,
            getIntTableEntryName( table, i ),
            Fieldml_GetObjectName( region, objectHandle )
            );
    }

    xmlTextWriterEndElement( writer );
}


static void writeObjectStack( xmlTextWriterPtr writer, FieldmlRegion *region, const char *name, IntStack *stack )
{
    int i, count;
    
    count = intStackGetCount( stack );
    if( count == 0 )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );

    for( i = 0; i < count; i++ )
    {
        writeListEntry( writer, Fieldml_GetObjectName( region, intStackGet( stack, i ) ) );
    }

    xmlTextWriterEndElement( writer );
}


static int writeContinuousDomain( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    ContinuousDomain *domain = object->object.continuousDomain;
    
    xmlTextWriterStartElement( writer, CONTINUOUS_DOMAIN_TAG );
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    
    if( domain->componentDomain != FML_INVALID_HANDLE )
    {
        writeObjectName( writer, region, COMPONENT_DOMAIN_ATTRIB, domain->componentDomain );
    }

    writeStringTable( writer, MARKUP_TAG, object->markup );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static void writeBounds( xmlTextWriterPtr writer, EnsembleDomain *domain )
{
    xmlTextWriterStartElement( writer, BOUNDS_TAG );
    if( domain->boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
    {
        xmlTextWriterStartElement( writer, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG );
        xmlTextWriterWriteFormatAttribute( writer, VALUE_COUNT_ATTRIB, "%d", domain->bounds.contiguous.count );
        xmlTextWriterEndElement( writer );
    }
    xmlTextWriterEndElement( writer );
}


static int writeEnsembleDomain( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    EnsembleDomain *domain = object->object.ensembleDomain;

    xmlTextWriterStartElement( writer, ENSEMBLE_DOMAIN_TAG );
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    if( object->object.ensembleDomain->isComponentDomain )
    {
        xmlTextWriterWriteAttribute( writer, IS_COMPONENT_DOMAIN_ATTRIB, STRING_TRUE );
    }
    
    if( domain->componentDomain != FML_INVALID_HANDLE )
    {
        writeObjectName( writer, region, COMPONENT_DOMAIN_ATTRIB, domain->componentDomain );
    }
    
    writeStringTable( writer, MARKUP_TAG, object->markup );
        
    writeBounds( writer, domain );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeMeshDomain( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    MeshDomain *domain = object->object.meshDomain;
    FieldmlObject *elements;
    FieldmlObject *xi;
    
    elements = (FieldmlObject*)getSimpleListEntry( region->objects, domain->elementDomain );
    xi = (FieldmlObject*)getSimpleListEntry( region->objects, domain->xiDomain );

    xmlTextWriterStartElement( writer, MESH_DOMAIN_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    if( xi->object.continuousDomain->componentDomain != FML_INVALID_HANDLE )
    {
        writeObjectName( writer, region, XI_COMPONENT_DOMAIN_ATTRIB, xi->object.continuousDomain->componentDomain );
    }
    
    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeBounds( writer, elements->object.ensembleDomain );
    
    writeIntTable( writer, MESH_SHAPES_TAG, domain->shapes );

    writeObjectObjectTable( writer, region, MESH_CONNECTIVITY_TAG, domain->connectivity );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeVariable( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    Variable *variable = object->object.variable;

    if( object->type == FHT_CONTINUOUS_VARIABLE )
    {
        xmlTextWriterStartElement( writer, CONTINUOUS_VARIABLE_TAG );
    }
    else
    {
        xmlTextWriterStartElement( writer, ENSEMBLE_VARIABLE_TAG );
    }
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, region, VALUE_DOMAIN_ATTRIB, variable->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static writeContinuousReference( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    ContinuousReference *reference = object->object.continuousReference;

    xmlTextWriterStartElement( writer, CONTINUOUS_REFERENCE_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, region, EVALUATOR_ATTRIB, reference->remoteEvaluator );
    writeObjectName( writer, region, VALUE_DOMAIN_ATTRIB, reference->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeObjectObjectTable( writer, region, ALIASES_TAG, reference->aliases );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeContinuousPiecewise( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    ContinuousPiecewise *piecewise = object->object.piecewise;

    xmlTextWriterStartElement( writer, CONTINUOUS_PIECEWISE_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, region, INDEX_DOMAIN_ATTRIB, piecewise->indexDomain );
    writeObjectName( writer, region, VALUE_DOMAIN_ATTRIB, piecewise->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeObjectObjectTable( writer, region, ALIASES_TAG, piecewise->aliases );

    writeIntObjectTable( writer, region, ELEMENT_EVALUATORS_TAG, piecewise->evaluators );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static void writeSemidenseData( xmlTextWriterPtr writer, FieldmlRegion *region, SemidenseData *data )
{
    xmlTextWriterStartElement( writer, SEMI_DENSE_DATA_TAG );

    writeObjectStack( writer, region, SPARSE_INDEXES_TAG, data->sparseIndexes );
    writeObjectStack( writer, region, DENSE_INDEXES_TAG, data->denseIndexes );
    
    if( data->swizzleCount > 0 )
    {
        writeIntArray( writer, SWIZZLE_TAG, data->swizzleCount, data->swizzle );
    }
    
    xmlTextWriterStartElement( writer, DATA_LOCATION_TAG );
    if( data->locationType == LOCATION_FILE )
    {
        xmlTextWriterStartElement( writer, FILE_DATA_TAG );
        xmlTextWriterWriteAttribute( writer, FILE_ATTRIB, data->dataLocation.fileData.filename );
        if( data->dataLocation.fileData.fileType == TYPE_TEXT )
        {
            xmlTextWriterWriteAttribute( writer, TYPE_ATTRIB, STRING_TYPE_TEXT );
        }
        else if( data->dataLocation.fileData.fileType == TYPE_LINES )
        {
            xmlTextWriterWriteAttribute( writer, TYPE_ATTRIB, STRING_TYPE_LINES );
        }

        xmlTextWriterWriteFormatAttribute( writer, OFFSET_ATTRIB, "%d", data->dataLocation.fileData.offset );
        xmlTextWriterEndElement( writer );
        
    }
    else
    {
        xmlTextWriterStartElement( writer, INLINE_DATA_TAG );
        xmlTextWriterWriteString( writer, data->dataLocation.stringData.string );
        xmlTextWriterEndElement( writer );
    }
    
    xmlTextWriterEndElement( writer );

    xmlTextWriterEndElement( writer );
}


static int writeParameters( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    Parameters *parameters = object->object.parameters;
    
    if( object->type == FHT_CONTINUOUS_PARAMETERS )
    {
        xmlTextWriterStartElement( writer, CONTINUOUS_PARAMETERS_TAG );
    }
    else
    {
        xmlTextWriterStartElement( writer, ENSEMBLE_PARAMETERS_TAG );
    }

    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, region, VALUE_DOMAIN_ATTRIB, parameters->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        writeSemidenseData( writer, region, parameters->dataDescription.semidense );
    }
    
    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeAggregate( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    ContinuousAggregate *aggregate = object->object.aggregate;
    
    xmlTextWriterStartElement( writer, CONTINUOUS_AGGREGATE_TAG );
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, region, VALUE_DOMAIN_ATTRIB, aggregate->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeObjectObjectTable( writer, region, ALIASES_TAG, aggregate->aliases );

    writeIntObjectTable( writer, region, SOURCE_FIELDS_TAG, aggregate->evaluators );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeFieldmlObject( xmlTextWriterPtr writer, FieldmlRegion *region, FieldmlObject *object )
{
    switch( object->type )
    {
    case FHT_CONTINUOUS_DOMAIN:
        return writeContinuousDomain( writer, region, object );
    case FHT_ENSEMBLE_DOMAIN:
        return writeEnsembleDomain( writer, region, object );
    case FHT_MESH_DOMAIN:
        return writeMeshDomain( writer, region, object );
    case FHT_CONTINUOUS_VARIABLE:
    case FHT_ENSEMBLE_VARIABLE:
        return writeVariable( writer, region, object );
    case FHT_CONTINUOUS_REFERENCE:
        return writeContinuousReference( writer, region, object );
    case FHT_CONTINUOUS_PIECEWISE:
        return writeContinuousPiecewise( writer, region, object );
    case FHT_CONTINUOUS_PARAMETERS:
    case FHT_ENSEMBLE_PARAMETERS:
        return writeParameters( writer, region, object );
    case FHT_CONTINUOUS_AGGREGATE:
        return writeAggregate( writer, region, object );
    default:
        break;
    }
    
    return 0;
}


int writeFieldmlFile( FieldmlRegion *region, const char *filename )
{
    FieldmlObject *object;
    int i, count;
    int rc = 0;
    xmlTextWriterPtr writer;

    free( region->root );
    region->root = strdupDir( filename );

    writer = xmlNewTextWriterFilename( filename, 0 );
    if( writer == NULL )
    {
        printf( "testXmlwriterFilename: Error creating the xml writer\n" );
        return 1;
    }

    xmlTextWriterSetIndent( writer, 1 );
    xmlTextWriterStartDocument( writer, NULL, MY_ENCODING, NULL );

    xmlTextWriterStartElement( writer, FIELDML_TAG );
    xmlTextWriterWriteAttribute( writer, VERSION_ATTRIB, FML_VERSION_STRING );
    xmlTextWriterWriteAttribute( writer, "xsi:noNamespaceSchemaLocation", "Fieldml_0.2.xsd" );
    xmlTextWriterWriteAttribute( writer, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );        
    xmlTextWriterStartElement( writer, REGION_TAG );
    if( ( region->name != NULL ) && ( strlen( region->name ) > 0 ) ) 
    {
        xmlTextWriterWriteAttribute( writer, "name", region->name );        
    }
    
    count = getSimpleListCount( region->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( region->objects, i );
        if( object->regionHandle == FILE_REGION_HANDLE )
        {
            writeFieldmlObject( writer, region, object );
        }
        else
        {
        }
    }

    rc = xmlTextWriterEndDocument( writer );
    if( rc < 0 )
    {
        printf( "testXmlwriterFilename: Error at xmlTextWriterEndDocument\n" );
        return 1;
    }

    xmlFreeTextWriter( writer );

    return 0;
}
