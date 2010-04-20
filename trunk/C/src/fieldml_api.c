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

//========================================================================
//
// Main
//
//========================================================================

int main( int argc, char **argv )
{
    int i, j, count, count2;
    FmlObjectHandle oHandle;
    FmlParseHandle handle = fmlParseFile( argv[1] );
    DomainBoundsType boundsType;

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_DOMAIN );
    fprintf( stdout, "ContinuousDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetDomainComponentEnsemble( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_ENSEMBLE_DOMAIN );
    fprintf( stdout, "EnsembleDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetDomainComponentEnsemble( handle, oHandle ) ) );
        
        boundsType = fmlGetDomainBoundsType( handle, oHandle );
        if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            fprintf( stdout, "    1...%d\n", fmlGetContiguousBoundsCount( handle, oHandle ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_MESH_DOMAIN );
    fprintf( stdout, "MeshDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_MESH_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s, %s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetMeshElementDomain( handle, oHandle ) ),
            fmlGetObjectName( handle, fmlGetMeshXiDomain( handle, oHandle ) ) );
        boundsType = fmlGetDomainBoundsType( handle, fmlGetMeshElementDomain( handle, oHandle ) );
        if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            int bounds = fmlGetContiguousBoundsCount( handle, fmlGetMeshElementDomain( handle, oHandle ) );
            fprintf( stdout, "    1...%d\n", bounds );
            for( j = 1; j <= bounds; j++ )
            {
                fprintf( stdout, "    %d: %s\n", j, fmlGetMeshElementShape( handle, oHandle, j ) );
            }
        }
        
        count2 = fmlGetMeshConnectivityCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s: %s\n",
                fmlGetObjectName( handle, fmlGetMeshConnectivityDomain( handle, oHandle, j ) ),
                fmlGetObjectName( handle, fmlGetMeshConnectivitySource( handle, oHandle, j ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_PARAMETERS );
    fprintf( stdout, "ContinuousParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_PARAMETERS, i );
        
        fprintf( stdout, "  %d: %d %s (%s)\n", i, fmlGetParameterDataDescription( handle, oHandle ),
            fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 1 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   sparse: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 1 ) ) );
        }
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 0 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    dense: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 0 ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_ENSEMBLE_PARAMETERS );
    fprintf( stdout, "EnsembleParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_ENSEMBLE_PARAMETERS, i );
        
        fprintf( stdout, "  %d: %d %s (%s)\n", i, fmlGetParameterDataDescription( handle, oHandle ),
            fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 1 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   sparse: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 1 ) ) );
        }
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 0 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    dense: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 0 ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_IMPORT );
    fprintf( stdout, "ContinuousImports: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_IMPORT, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        fprintf( stdout, "    Remote name: %s\n", fmlGetImportRemoteName( handle, oHandle ) );
        
        count2 = fmlGetImportAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                fmlGetObjectName( handle, fmlGetImportAliasLocalHandle( handle, oHandle, j ) ),
                fmlGetObjectName( handle, fmlGetImportAliasRemoteHandle( handle, oHandle, j ) ) ); 
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_PIECEWISE );
    fprintf( stdout, "ContinuousPiecewise: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_PIECEWISE, i );
        
        fprintf( stdout, "  %d: %s over %s (%s)\n", i,
            fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetIndexDomain( handle, oHandle, 1 ) ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, fmlGetEvaluatorElement( handle, oHandle, j ),
                fmlGetObjectName( handle, fmlGetEvaluatorHandle( handle, oHandle, j ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_AGGREGATE );
    fprintf( stdout, "ContinuousAggregate: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_AGGREGATE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, fmlGetEvaluatorElement( handle, oHandle, j ),
                fmlGetObjectName( handle, fmlGetEvaluatorHandle( handle, oHandle, j ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_VARIABLE );
    fprintf( stdout, "ContinuousVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_ENSEMBLE_VARIABLE );
    fprintf( stdout, "EnsembleVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_ENSEMBLE_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_DEREFERENCE );
    fprintf( stdout, "ContinuousDereference: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_DEREFERENCE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_REMOTE_ENSEMBLE_DOMAIN );
    fprintf( stdout, "External ensemble domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_REMOTE_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, fmlGetObjectName( handle, oHandle ) );
    }

    count = fmlGetObjectCount( handle, FHT_REMOTE_CONTINUOUS_DOMAIN );
    fprintf( stdout, "External continuous domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_REMOTE_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, fmlGetObjectName( handle, oHandle ) );
    }

    return 0;
}

