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
	count = getSimpleListSize( parse->objects );
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
	
	count = getSimpleListSize( parse->objects );
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


static int objectTypeToHandleType( FieldmlObjectType type )
{
	switch( type )
	{
	case FML_ENSEMBLE_DOMAIN:
		return FHT_ENSEMBLE_DOMAIN;
	case FML_CONTINUOUS_DOMAIN:
		return FHT_CONTINUOUS_DOMAIN;
	case FML_MESH_DOMAIN:
		return FHT_MESH_DOMAIN;
	case FML_CONTINUOUS_PARAMETERS:
		return FHT_CONTINUOUS_PARAMETERS;
	case FML_ENSEMBLE_PARAMETERS:
		return FHT_ENSEMBLE_PARAMETERS;
	case FML_CONTINUOUS_IMPORT:
		return FHT_CONTINUOUS_IMPORT;
	case FML_CONTINUOUS_AGGREGATE:
		return FHT_CONTINUOUS_AGGREGATE;
	case FML_CONTINUOUS_PIECEWISE:
		return FHT_CONTINUOUS_PIECEWISE;
	case FML_CONTINUOUS_VARIABLE:
		return FHT_CONTINUOUS_VARIABLE;
	case FML_ENSEMBLE_VARIABLE:
		return FHT_ENSEMBLE_VARIABLE;
	case FML_CONTINUOUS_DEREFERENCE:
		return FHT_CONTINUOUS_DEREFERENCE;
	default:
		return FHT_UNKNOWN;
	}
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


int fmlGetObjectCount( FmlParseHandle handle, FieldmlObjectType type )
{
	FieldmlParse *parse = handleToParse( handle );
	FieldmlHandleType hType = objectTypeToHandleType( type );
	
	if( hType == FHT_UNKNOWN )
	{
		return -1;
	}
	
	return getTotal( parse, hType );
}


FmlObjectHandle fmlGetObjectHandle( FmlParseHandle handle, FieldmlObjectType type, int index )
{
	FieldmlParse *parse = handleToParse( handle );
	FieldmlHandleType hType = objectTypeToHandleType( type );
	
	return getNthHandle( parse, hType, index );
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
		return getSimpleListSize( object->object.parameters->dataDescription.semidense->sparseIndexes );
	}
	else
	{
		return getSimpleListSize( object->object.parameters->dataDescription.semidense->denseIndexes );
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
	
	if( hack == NULL )
	{
		return FML_INVALID_HANDLE;
	}
	
	return (int)(hack) - 1;
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
	void *hack;
	
	if( table == NULL )
	{
		return FML_INVALID_HANDLE;
	}
	
	hack = getIntTableEntryData( table, index - 1 );
	if( hack == NULL )
	{
		return FML_INVALID_HANDLE;
	}
	
	return (int)(hack) - 1;
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

    count = fmlGetObjectCount( handle, FML_CONTINUOUS_DOMAIN );
	fprintf( stdout, "ContinuousDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_DOMAIN, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
    			fmlGetObjectName( handle, fmlGetDomainComponentEnsemble( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FML_ENSEMBLE_DOMAIN );
	fprintf( stdout, "EnsembleDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_ENSEMBLE_DOMAIN, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
    			fmlGetObjectName( handle, fmlGetDomainComponentEnsemble( handle, oHandle ) ) );
    	
    	boundsType = fmlGetDomainBoundsType( handle, oHandle );
    	if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
    	{
    		fprintf( stdout, "    1...%d\n", fmlGetContiguousBoundsCount( handle, oHandle ) );
    	}
    }
    
    count = fmlGetObjectCount( handle, FML_MESH_DOMAIN );
	fprintf( stdout, "MeshDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_MESH_DOMAIN, i );
    	
    	fprintf( stdout, "  %d: %s\n", i, fmlGetObjectName( handle, oHandle ) );
    }
    
    count = fmlGetObjectCount( handle, FML_CONTINUOUS_PARAMETERS );
	fprintf( stdout, "ContinuousParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_PARAMETERS, i );
    	
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

    count = fmlGetObjectCount( handle, FML_ENSEMBLE_PARAMETERS );
	fprintf( stdout, "EnsembleParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_ENSEMBLE_PARAMETERS, i );
    	
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

    count = fmlGetObjectCount( handle, FML_CONTINUOUS_IMPORT );
	fprintf( stdout, "ContinuousImports: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_IMPORT, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
   			fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FML_CONTINUOUS_PIECEWISE );
	fprintf( stdout, "ContinuousPiecewise: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_PIECEWISE, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
   			fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    	count2 = fmlGetEvaluatorCount( handle, oHandle );
    	for( j = 1; j <= count2; j++ )
    	{
    		fprintf( stdout, "   entry %d: %d -> %s\n", j, fmlGetEvaluatorElement( handle, oHandle, j ),
    				fmlGetObjectName( handle, fmlGetEvaluatorHandle( handle, oHandle, j ) ) );
    	}
    }

    count = fmlGetObjectCount( handle, FML_CONTINUOUS_AGGREGATE );
	fprintf( stdout, "ContinuousAggregate: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_AGGREGATE, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
   			fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    	count2 = fmlGetEvaluatorCount( handle, oHandle );
    	for( j = 1; j <= count2; j++ )
    	{
    		fprintf( stdout, "   entry %d: %d -> %s\n", j, fmlGetEvaluatorElement( handle, oHandle, j ),
    				fmlGetObjectName( handle, fmlGetEvaluatorHandle( handle, oHandle, j ) ) );
    	}
    }

    count = fmlGetObjectCount( handle, FML_CONTINUOUS_VARIABLE );
	fprintf( stdout, "ContinuousVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_VARIABLE, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
   			fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FML_ENSEMBLE_VARIABLE );
	fprintf( stdout, "EnsembleVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_ENSEMBLE_VARIABLE, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
   			fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }
    
    count = fmlGetObjectCount( handle, FML_CONTINUOUS_DEREFERENCE );
	fprintf( stdout, "ContinuousDereference: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
    	oHandle = fmlGetObjectHandle( handle, FML_CONTINUOUS_DEREFERENCE, i );
    	
    	fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
   			fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }
    
    return 0;
}

