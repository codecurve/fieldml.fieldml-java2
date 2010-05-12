#include <stdio.h>
#include <stdlib.h>
#include <libxml/encoding.h>
#include <libxml/xmlwriter.h>

#include "string_const.h"
#include "string_table.h"
#include "fieldml_write.h"
#include "fieldml_structs.h"
#include "fieldml_api.h"


const char * MY_ENCODING = "ISO-8859-1";


static void writeObjectName( xmlTextWriterPtr writer, FieldmlParse *parse, const char *attribute, int handle )
{
    xmlTextWriterWriteAttribute( writer, attribute, Fieldml_GetObjectName( parse, handle ) );
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
    xmlTextWriterStartElement( writer, SIMPLE_MAP_ENTRY_TAG );
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
    
    count = getStringTableCount( table );
    if( count == 0 )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );
    
    for( i = 0; i < count; i++ )
    {
        writeStringTableEntry( writer, getStringTableEntryName( table, i ), getStringTableEntryData( table, i ) );
    }
    
    xmlTextWriterEndElement( writer );
}


static void writeIntTableEntry( xmlTextWriterPtr writer, int key, const char *value )
{
    xmlTextWriterStartElement( writer, SIMPLE_MAP_ENTRY_TAG );
    xmlTextWriterWriteFormatAttribute( writer, KEY_ATTRIB, "%d", key );
    xmlTextWriterWriteAttribute( writer, VALUE_ATTRIB, value );
    xmlTextWriterEndElement( writer );
}


static void writeIntTable( xmlTextWriterPtr writer, const char *name, IntTable *table )
{
    int i, count;
    
    count = getIntTableCount( table );
    if( count == 0 )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );
    
    for( i = 0; i < count; i++ )
    {
        writeIntTableEntry( writer, getIntTableEntryName( table, i ), (char*)getIntTableEntryData( table, i ) );
    }
    
    xmlTextWriterEndElement( writer );
}


static void writeObjectObjectTable( xmlTextWriterPtr writer, FieldmlParse *parse, const char *name, IntTable *table )
{
    int i, count;
    
    count = getIntTableCount( table );
    if( count == 0 )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );

    for( i = 0; i < count; i++ )
    {
        writeStringTableEntry( writer,
            Fieldml_GetObjectName( parse, getIntTableEntryName( table, i ) ),
            Fieldml_GetObjectName( parse, (int)getIntTableEntryData( table, i ) - 1 )
            );
    }

    xmlTextWriterEndElement( writer );
}


static void writeIntObjectTable( xmlTextWriterPtr writer, FieldmlParse *parse, const char *name, IntTable *table )
{
    int i, count;
    
    count = getIntTableCount( table );
    if( count == 0 )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );

    for( i = 0; i < count; i++ )
    {
        writeIntTableEntry( writer,
            getIntTableEntryName( table, i ),
            Fieldml_GetObjectName( parse, (int)getIntTableEntryData( table, i ) - 1 )
            );
    }

    xmlTextWriterEndElement( writer );
}


static void writeObjectList( xmlTextWriterPtr writer, FieldmlParse *parse, const char *name, SimpleList *list )
{
    int i, count;
    
    count = getSimpleListCount( list );
    if( count == 0 )
    {
        return;
    }

    xmlTextWriterStartElement( writer, name );

    for( i = 0; i < count; i++ )
    {
        writeListEntry( writer, Fieldml_GetObjectName( parse, (int)getSimpleListEntry( list, i ) - 1 ) );
    }

    xmlTextWriterEndElement( writer );
}


static int writeContinuousDomain( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    ContinuousDomain *domain = object->object.continuousDomain;
    
    xmlTextWriterStartElement( writer, CONTINUOUS_DOMAIN_TAG );
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    
    if( domain->componentDomain != FML_INVALID_HANDLE )
    {
        writeObjectName( writer, parse, COMPONENT_DOMAIN_ATTRIB, domain->componentDomain );
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


static int writeEnsembleDomain( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    EnsembleDomain *domain = object->object.ensembleDomain;

    xmlTextWriterStartElement( writer, ENSEMBLE_DOMAIN_TAG );
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    
    if( domain->componentDomain != FML_INVALID_HANDLE )
    {
        writeObjectName( writer, parse, COMPONENT_DOMAIN_ATTRIB, domain->componentDomain );
    }
    
    writeStringTable( writer, MARKUP_TAG, object->markup );
        
    writeBounds( writer, domain );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeMeshDomain( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    MeshDomain *domain = object->object.meshDomain;
    FieldmlObject *elements;
    FieldmlObject *xi;
    
    elements = (FieldmlObject*)getSimpleListEntry( parse->objects, domain->elementDomain );
    xi = (FieldmlObject*)getSimpleListEntry( parse->objects, domain->xiDomain );

    xmlTextWriterStartElement( writer, MESH_DOMAIN_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    if( xi->object.continuousDomain->componentDomain != FML_INVALID_HANDLE )
    {
        writeObjectName( writer, parse, XI_COMPONENT_DOMAIN_ATTRIB, xi->object.continuousDomain->componentDomain );
    }
    
    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeBounds( writer, elements->object.ensembleDomain );
    
    writeIntTable( writer, MESH_SHAPES_TAG, domain->shapes );

    writeObjectObjectTable( writer, parse, MESH_CONNECTIVITY_TAG, domain->connectivity );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeVariable( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
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
    writeObjectName( writer, parse, VALUE_DOMAIN_ATTRIB, variable->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeContinuousDereference( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    ContinuousDereference *deref = object->object.dereference;

    xmlTextWriterStartElement( writer, CONTINUOUS_DEREFERENCE_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, parse, VALUE_SOURCE_ATTRIB, deref->valueSource );
    writeObjectName( writer, parse, VALUE_INDEXES_ATTRIB, deref->valueIndexes );
    writeObjectName( writer, parse, VALUE_DOMAIN_ATTRIB, deref->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static writeContinuousImport( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    ContinuousImport *import = object->object.continuousImport;

    xmlTextWriterStartElement( writer, IMPORTED_CONTINUOUS_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, parse, EVALUATOR_ATTRIB, import->remoteEvaluator );
    writeObjectName( writer, parse, VALUE_DOMAIN_ATTRIB, import->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeObjectObjectTable( writer, parse, ALIASES_TAG, import->aliases );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeContinuousPiecewise( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    ContinuousPiecewise *piecewise = object->object.piecewise;

    xmlTextWriterStartElement( writer, CONTINUOUS_PIECEWISE_TAG );
    
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, parse, INDEX_DOMAIN_ATTRIB, piecewise->indexDomain );
    writeObjectName( writer, parse, VALUE_DOMAIN_ATTRIB, piecewise->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeObjectObjectTable( writer, parse, ALIASES_TAG, piecewise->aliases );

    writeIntObjectTable( writer, parse, ELEMENT_EVALUATORS_TAG, piecewise->evaluators );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static void writeSemidenseData( xmlTextWriterPtr writer, FieldmlParse *parse, SemidenseData *data )
{
    xmlTextWriterStartElement( writer, SEMI_DENSE_DATA_TAG );

    writeObjectList( writer, parse, SPARSE_INDEXES_TAG, data->sparseIndexes );
    writeObjectList( writer, parse, DENSE_INDEXES_TAG, data->denseIndexes );
    
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
    xmlTextWriterEndElement( writer );

    xmlTextWriterEndElement( writer );
}


static int writeParameters( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
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
    writeObjectName( writer, parse, VALUE_DOMAIN_ATTRIB, parameters->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        writeSemidenseData( writer, parse, parameters->dataDescription.semidense );
    }
    
    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeAggregate( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    ContinuousAggregate *aggregate = object->object.aggregate;
    
    xmlTextWriterStartElement( writer, CONTINUOUS_AGGREGATE_TAG );
    xmlTextWriterWriteAttribute( writer, NAME_ATTRIB, object->name );
    writeObjectName( writer, parse, VALUE_DOMAIN_ATTRIB, aggregate->valueDomain );

    writeStringTable( writer, MARKUP_TAG, object->markup );
    
    writeObjectObjectTable( writer, parse, ALIASES_TAG, aggregate->aliases );

    writeIntObjectTable( writer, parse, SOURCE_FIELDS_TAG, aggregate->evaluators );

    xmlTextWriterEndElement( writer );
    
    return 0;
}


static int writeFieldmlObject( xmlTextWriterPtr writer, FieldmlParse *parse, FieldmlObject *object )
{
    switch( object->type )
    {
    case FHT_CONTINUOUS_DOMAIN:
        return writeContinuousDomain( writer, parse, object );
    case FHT_ENSEMBLE_DOMAIN:
        return writeEnsembleDomain( writer, parse, object );
    case FHT_MESH_DOMAIN:
        return writeMeshDomain( writer, parse, object );
    case FHT_CONTINUOUS_VARIABLE:
    case FHT_ENSEMBLE_VARIABLE:
        return writeVariable( writer, parse, object );
    case FHT_CONTINUOUS_DEREFERENCE:
        return writeContinuousDereference( writer, parse, object );
    case FHT_CONTINUOUS_IMPORT:
        return writeContinuousImport( writer, parse, object );
    case FHT_CONTINUOUS_PIECEWISE:
        return writeContinuousPiecewise( writer, parse, object );
    case FHT_CONTINUOUS_PARAMETERS:
    case FHT_ENSEMBLE_PARAMETERS:
        return writeParameters( writer, parse, object );
    case FHT_CONTINUOUS_AGGREGATE:
        return writeAggregate( writer, parse, object );
    default:
        break;
    }
    
    return 0;
}


int writeFieldmlFile( FieldmlParse *parse, const char *filename )
{
    FieldmlObject *object;
    int i, count;
    int rc = 0;
    xmlTextWriterPtr writer;

    writer = xmlNewTextWriterFilename( filename, 0 );
    if( writer == NULL )
    {
        printf( "testXmlwriterFilename: Error creating the xml writer\n" );
        return 1;
    }

    xmlTextWriterStartDocument( writer, NULL, MY_ENCODING, NULL );

    xmlTextWriterStartElement( writer, FIELDML_TAG );
    xmlTextWriterWriteAttribute( writer, "xsi:noNamespaceSchemaLocation", "Fieldml.xsd" );
    xmlTextWriterWriteAttribute( writer, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );        
    xmlTextWriterStartElement( writer, REGION_TAG );
    
    count = getSimpleListCount( parse->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
        if( object->regionHandle == FILE_REGION_HANDLE )
        {
            writeFieldmlObject( writer, parse, object );
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
