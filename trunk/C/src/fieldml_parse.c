#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libxml/sax.h>

#include "fieldml_parse.h"
#include "int_table.h"
#include "string_table.h"
#include "simple_list.h"
#include "fieldml_structs.h"
#include "fieldml_sax.h"

//========================================================================
//
// Structs
//
//========================================================================

struct _FieldmlContext
{
	FieldmlObject *currentObject;
	
	FieldmlParse *parse;
};


//========================================================================
//
// Creators
//
//========================================================================


FieldmlContext *createFieldmlContext( FieldmlParse *parse )
{
	FieldmlContext *context = calloc( 1, sizeof( FieldmlContext ) );
	
	context->parse = parse;
	
	return context;
}


FieldmlParse *createFieldmlParse()
{
    FieldmlParse *parse;

    parse = calloc( 1, sizeof( FieldmlParse ) );

    parse->objects = createSimpleList();
    
    return parse;
}


EnsembleDomain *createEnsembleDomain( int componentDomain )
{
	EnsembleDomain *domain = calloc( 1, sizeof( EnsembleDomain ) );
	domain->componentDomain = componentDomain;
	domain->boundsType = BOUNDS_UNKNOWN;
	
	return domain;
}


ContinuousDomain *createContinuousDomain( int componentDomain )
{
	ContinuousDomain *domain = calloc( 1, sizeof( ContinuousDomain ) );
	domain->componentDomain = componentDomain;
	
	return domain;
}


MeshDomain *createMeshDomain( int xiEnsemble )
{
	MeshDomain *domain;
	
	domain = calloc( 1, sizeof( MeshDomain ) );
	domain->xiEnsemble = xiEnsemble;
	domain->shapes = createIntTable();
	domain->connectivity = createStringTable();
	
	return domain;
}

ContinuousImport *createContinuousImport( char *remoteName, int valueDomain )
{
    ContinuousImport *import;

    import = calloc( 1, sizeof( ContinuousImport ) );
    import->remoteName = _strdup( remoteName );
    import->valueDomain = valueDomain;
    import->aliases = createIntTable();

    return import;
}


Parameters *createParameters( int valueDomain )
{
    Parameters *parameters;

    parameters = calloc( 1, sizeof( Parameters ) );
    parameters->valueDomain = valueDomain;
    parameters->descriptionType = DESCRIPTION_UNKNOWN;
    parameters->locationType = LOCATION_UNKNOWN;

    return parameters;
}


ContinuousPiecewise *createContinuousPiecewise( int valueDomain, int indexDomain )
{
	ContinuousPiecewise *piecewise;
	
	piecewise = calloc( 1, sizeof( ContinuousPiecewise ) );
	piecewise->valueDomain = valueDomain;
	piecewise->indexDomain = indexDomain;
	
	piecewise->evaluators = createIntTable();
	
	return piecewise;
}


ContinuousAggregate *createContinuousAggregate( int valueDomain )
{
	ContinuousAggregate *aggregate;
	
	aggregate = calloc( 1, sizeof( ContinuousAggregate ) );
	aggregate->valueDomain = valueDomain;
	
	aggregate->markup = createStringTable();
	aggregate->evaluators = createIntTable();
	
	return aggregate;
}


ContinuousDereference *createContinuousDereference( int valueDomain, int valueIndexes, int valueSource )
{
	ContinuousDereference *dereference;
	
	dereference = calloc( 1, sizeof( ContinuousDereference ) );
	dereference->valueDomain = valueDomain;
	dereference->valueIndexes = valueIndexes;
	dereference->valueSource = valueSource;
	
	return dereference;
}


SemidenseData *createSemidenseData()
{
    SemidenseData *data;
    data = calloc( 1, sizeof( SemidenseData ) );
    data->denseIndexes = createSimpleList();
    data->sparseIndexes = createSimpleList();

    return data;
}


Variable *createVariable( int valueDomain )
{
    Variable *variable;

    variable = calloc( 1, sizeof( Variable ) );
    variable->valueDomain = valueDomain;
    variable->parameters = createSimpleList();

    return variable;
}


FieldmlObject *createFieldmlObject( char *name, FieldmlHandleType type )
{
	FieldmlObject *object;
	
	object = calloc( 1, sizeof( FieldmlObject ) );
	object->name = _strdup( name );
	object->type = type;
	
	return object;
}

//========================================================================
//
// Destroyers
//
//========================================================================

void destroyFieldmlContext( FieldmlContext *context )
{
	free( context );
}


void destroyEnsembleDomain( EnsembleDomain *domain )
{
	free( domain );
}


void destroyContinuousDomain( ContinuousDomain *domain )
{
	free( domain );
}


void destroyMeshDomain( MeshDomain *domain )
{
	destroyIntTable( domain->shapes, free );
	destroyStringTable( domain->connectivity, free );
	free( domain );
}


void destroyContinuousImport( ContinuousImport *import )
{
    free( import->remoteName );
    destroyIntTable( import->aliases, free );
    free( import );
}


void destroySemidenseData( SemidenseData *data )
{
    destroySimpleList( data->sparseIndexes, NULL );
    destroySimpleList( data->denseIndexes, NULL );
    free( data );
}


void destroyParameters( Parameters *parameters )
{
    switch( parameters->descriptionType )
    {
    case DESCRIPTION_SEMIDENSE:
    	destroySemidenseData( parameters->dataDescription.semidense );
        break;
    default:
        break;
    }
    
    switch( parameters->locationType )
    {
    case LOCATION_FILE:
    	free( parameters->dataLocation.fileData.filename );
    	break;
  	case LOCATION_INLINE:
    	free( parameters->dataLocation.stringData.string );
    	break;
    default:
    	break;
    }

    free( parameters );
}


void destroyContinuousPiecewise( ContinuousPiecewise *piecewise )
{
	destroyIntTable( piecewise->evaluators, NULL );
	free( piecewise );
}


void destroyContinuousAggregate( ContinuousAggregate *aggregate )
{
	destroyStringTable( aggregate->markup, free );
	destroyIntTable( aggregate->evaluators, free );
	free( aggregate );
}


void destroyContinuousDereference( ContinuousDereference *dereference )
{
	free( dereference );
}


void destroyVariable( Variable *variable )
{
    destroySimpleList( variable->parameters, free );

    free( variable );
}


void destroyFieldmlObject( FieldmlObject *object )
{
	switch( object->type )
	{
	case FHT_ENSEMBLE_DOMAIN:
		destroyEnsembleDomain( object->object.ensembleDomain );
		break;
	case FHT_CONTINUOUS_DOMAIN:
		destroyContinuousDomain( object->object.continuousDomain );
		break;
	case FHT_MESH_DOMAIN:
		destroyMeshDomain( object->object.meshDomain );
		break;
	case FHT_CONTINUOUS_IMPORT:
		destroyContinuousImport( object->object.continuousImport );
		break;
	case FHT_CONTINUOUS_PARAMETERS:
	case FHT_ENSEMBLE_PARAMETERS:
		destroyParameters( object->object.parameters );
		break;
	case FHT_CONTINUOUS_PIECEWISE:
		destroyContinuousPiecewise( object->object.piecewise );
		break;
	case FHT_CONTINUOUS_AGGREGATE:
		destroyContinuousAggregate( object->object.aggregate );
		break;
	case FHT_CONTINUOUS_VARIABLE:
	case FHT_ENSEMBLE_VARIABLE:
		destroyVariable( object->object.variable );
		break;
	case FHT_CONTINUOUS_DEREFERENCE:
		destroyContinuousDereference( object->object.dereference );
		break;
	default:
		break;
	}
    free( object->name );
    free( object );
}


void destroyFieldmlParse( FieldmlParse *parse )
{
    destroySimpleList( parse->objects, NULL );

    free( parse );
}


//========================================================================
//
// Utility
//
//========================================================================


static int getObjectHandle( FieldmlParse *parse, char *name )
{
	int i, count;
	FieldmlObject *object;
	
	count = getSimpleListSize( parse->objects );
	for( i = 0; i < count; i++ )
	{
		object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
		if( strcmp( name, object->name ) == 0 )
		{
			return i;
		}
	}
	
	return FML_INVALID_HANDLE;
}


static int addObject( FieldmlParse *parse, FieldmlObject *object )
{
	FieldmlObject *oldObject;
	int handle = getObjectHandle( parse, object->name );
	
	if( handle == FML_INVALID_HANDLE )
	{
		return addSimpleListEntry( parse->objects, object );
	}

	oldObject = (FieldmlObject*)getSimpleListEntry( parse->objects, handle );
	if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_DOMAIN )
	{
		if( object->type == FHT_ENSEMBLE_DOMAIN )
		{
			oldObject->type = object->type;
			oldObject->object = object->object;
			object->type = FHT_UNKNOWN_ENSEMBLE_DOMAIN;
			destroyFieldmlObject( object );
			
			return handle;
		}
	}
	else if( oldObject->type == FHT_UNKNOWN_CONTINUOUS_DOMAIN )
	{
		if( object->type == FHT_CONTINUOUS_DOMAIN )
		{
			oldObject->type = object->type;
			oldObject->object = object->object;
			object->type = FHT_UNKNOWN_CONTINUOUS_DOMAIN;
			destroyFieldmlObject( object );
			
			return handle;
		}
	}
	else if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_SOURCE )
	{
		if( ( object->type == FHT_ENSEMBLE_DOMAIN ) ||
		    ( object->type == FHT_ENSEMBLE_PARAMETERS ) ||
		    ( object->type == FHT_ENSEMBLE_VARIABLE ) )
		{
			oldObject->type = object->type;
			oldObject->object = object->object;
			object->type = FHT_UNKNOWN_ENSEMBLE_SOURCE;
			destroyFieldmlObject( object );
			
			return handle;
		}
	}
	else if( oldObject->type == FHT_UNKNOWN_CONTINUOUS_SOURCE )
	{
		if( ( object->type == FHT_CONTINUOUS_DOMAIN ) ||
			( object->type == FHT_CONTINUOUS_PIECEWISE ) ||
			( object->type == FHT_CONTINUOUS_IMPORT ) ||
			( object->type == FHT_CONTINUOUS_AGGREGATE ) ||
			( object->type == FHT_CONTINUOUS_DEREFERENCE ) ||
		    ( object->type == FHT_CONTINUOUS_PARAMETERS ) ||
		    ( object->type == FHT_CONTINUOUS_VARIABLE ) )
		{
			oldObject->type = object->type;
			oldObject->object = object->object;
			object->type = FHT_UNKNOWN_CONTINUOUS_SOURCE;
			destroyFieldmlObject( object );
			
			return handle;
		}
	}
	
	fprintf( stderr, "Handle collision: %d:%s cannot replace %d:%s\n",
			object->type, object->name,
			oldObject->type, oldObject->name );
	destroyFieldmlObject( object );
	
	return FML_INVALID_HANDLE;
}


int getOrCreateObjectHandle( FieldmlParse *parse, char *name, FieldmlHandleType type )
{
	int handle = getObjectHandle( parse, name );

	if( handle == FML_INVALID_HANDLE )
	{
		handle = addObject( parse, createFieldmlObject( name, type ) );
	}
	
	return handle;
}


void finalizeFieldmlParse( FieldmlParse *parse )
{
}


//========================================================================
//
// Event handlers
//
//========================================================================

void startEnsembleDomain( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *componentEnsemble;
    int handle;
    FieldmlObject *object;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "EnsembleDomain has no name\n" );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, "componentDomain" );
    if( componentEnsemble != NULL )
    {
    	handle = getOrCreateObjectHandle( context->parse, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
    	handle = FML_INVALID_HANDLE;
    }

    object = createFieldmlObject( name, FHT_ENSEMBLE_DOMAIN );
    object->object.ensembleDomain = createEnsembleDomain( handle );

    context->currentObject = object;
}


void startContiguousBounds( FieldmlContext *context, SaxAttributes *attributes )
{
    char *count;
    FieldmlObject *object = context->currentObject;
    EnsembleDomain *domain = object->object.ensembleDomain;
    
    if( domain->boundsType != BOUNDS_UNKNOWN )
    {
    	fprintf( stderr, "EnsembleDomain %s already has a bounds\n", object->name );
    	return;
    }

    count = getAttribute( attributes, "valueCount" );
    if( count == NULL )
    {
        fprintf( stderr, "ContiguousEnsembleBounds for %s has no value count\n", object->name );
        return;
    }

    domain->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
    domain->bounds.contiguous.count = atoi( count );
}


void endEnsembleDomain( FieldmlContext *context )
{
    EnsembleDomain *domain = context->currentObject->object.ensembleDomain;

    if( domain->boundsType == BOUNDS_UNKNOWN )
    {
        fprintf( stderr, "EnsembleDomain %s has no bounds\n", context->currentObject->name );
        destroyFieldmlObject( context->currentObject );
    }
    else
    {
        addObject( context->parse, context->currentObject );
    }
    
    context->currentObject = NULL;
}


void startContinuousDomain( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *componentEnsemble;
    int handle;
    FieldmlObject *object;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "ContinuousDomain has no name\n" );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, "componentDomain" );
    if( componentEnsemble != NULL )
    {
    	handle = getOrCreateObjectHandle( context->parse, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
    	handle = FML_INVALID_HANDLE;
    }

    object = createFieldmlObject( name, FHT_CONTINUOUS_DOMAIN );
    object->object.continuousDomain = createContinuousDomain( handle );

    context->currentObject = object;
}


void endContinuousDomain( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startMeshDomain( FieldmlContext *context, SaxAttributes *attributes )
{
	char *name;
	char *xiEnsemble;
	FieldmlObject *object;
	int handle;
	
	name = getAttribute( attributes, "name" );
	xiEnsemble = getAttribute( attributes, "xiComponentDomain" );
	
	if( name == NULL )
	{
        fprintf( stderr, "MeshDomain has no name\n" );
        return;
	}
	
	if( xiEnsemble == NULL )
	{
        fprintf( stderr, "MeshDomain %s has no xi components\n", name );
        return;
	}

	handle = getOrCreateObjectHandle( context->parse, xiEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

	object = createFieldmlObject( name, FHT_MESH_DOMAIN );
    object->object.meshDomain = createMeshDomain( handle );

    context->currentObject = object;
}


void onMeshShape( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
    MeshDomain *domain = object->object.meshDomain;
    char *element = getAttribute( attributes, "key" );
    char *shape = getAttribute( attributes, "value" );

    if( ( element == NULL ) || ( shape == NULL ) )
    {
        fprintf( stderr, "MeshDomain %s has malformed shape entry\n", object->name );
        return;
    }

    setIntTableEntry( domain->shapes, atoi( element ), _strdup( shape ), free );
}


void onMeshConnectivity( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
    MeshDomain *domain = object->object.meshDomain;
    char *type = getAttribute( attributes, "key" );
    char *field = getAttribute( attributes, "value" );

    if( ( type == NULL ) || ( field == NULL ) )
    {
        fprintf( stderr, "MeshDomain %s has malformed connectivity entry\n", object->name );
        return;
    }

    setStringTableEntry( domain->connectivity, type, _strdup( field ), free );
}


void endMeshDomain( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousImport( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *remoteName;
    char *valueDomain;
    int handle;
    FieldmlObject *object;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "ImportedContinuousEvaluator has no name\n" );
        return;
    }
    
    remoteName = getAttribute( attributes, "evaluator" );
    if( remoteName == NULL )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has no remote name\n", name );
        return;
    }
    
    valueDomain = getAttribute( attributes, "valueDomain" );
    if( valueDomain == NULL )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has no value domain\n", name );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

	object = createFieldmlObject( name, FHT_CONTINUOUS_IMPORT );
    object->object.continuousImport = createContinuousImport( remoteName, handle );

    context->currentObject = object;
}


void onContinuousImportAlias( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
    ContinuousImport *import = object->object.continuousImport;
    char *remote = getAttribute( attributes, "key" );
    char *local = getAttribute( attributes, "value" );
	int localHandle, remoteHandle;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has malformed alias\n", object->name );
        return;
    }

    localHandle = getOrCreateObjectHandle( context->parse, local, FHT_UNKNOWN_CONTINUOUS_SOURCE );

    remoteHandle = getOrCreateObjectHandle( context->parse, remote, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    setIntTableEntry( import->aliases, localHandle, (void*)(remoteHandle + 1), NULL );
}


void onEnsembleImportAlias( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
    ContinuousImport *import = object->object.continuousImport;
    char *remote = getAttribute( attributes, "key" );
    char *local = getAttribute( attributes, "value" );
	int localHandle, remoteHandle;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has malformed alias\n", object->name );
        return;
    }

    localHandle = getOrCreateObjectHandle( context->parse, local, FHT_UNKNOWN_ENSEMBLE_SOURCE );

    remoteHandle = getOrCreateObjectHandle( context->parse, remote, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    setIntTableEntry( import->aliases, localHandle, (void*)(remoteHandle + 1), NULL );
}


void endContinuousImport( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousDereference( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    char *valueIndexes = getAttribute( attributes, "valueIndexes" );
    char *valueSource = getAttribute( attributes, "valueSource" );
	FieldmlObject *object;
	int valueHandle, indexHandle, sourceHandle;

    if( name == NULL )
    {
        fprintf( stderr, "ContinuousDereference has no name\n" );
        return;
    }
    if( valueDomain == NULL )
    {
        fprintf( stderr, "ContinuousDereference %s has no value domain\n", name );
        return;
    }
    if( valueIndexes == NULL )
    {
        fprintf( stderr, "ContinuousDereference %s has no value indexes\n", name );
        return;
    }
    if( valueSource == NULL )
    {
        fprintf( stderr, "ContinuousDereference %s has no value source\n", name );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    indexHandle = getOrCreateObjectHandle( context->parse, valueIndexes, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    sourceHandle = getOrCreateObjectHandle( context->parse, valueSource, FHT_UNKNOWN_CONTINUOUS_SOURCE );

	object = createFieldmlObject( name, FHT_CONTINUOUS_DEREFERENCE );
    object->object.dereference = createContinuousDereference( valueHandle, indexHandle, sourceHandle );

    context->currentObject = object;
}


void endContinuousDereference( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startEnsembleParameters( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    FieldmlObject *object;
    int handle;

    if( name == NULL )
    {
        fprintf( stderr, "EnsembleParameters has no name\n" );
        return;
    }

    if( valueDomain == NULL )
    {
        fprintf( stderr, "EnsembleParameters %s has no value domain\n", name );
        return;
    }

    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    object = createFieldmlObject( name, FHT_ENSEMBLE_PARAMETERS );
    object->object.parameters = createParameters( handle );

    context->currentObject = object;
}


void endEnsembleParameters( FieldmlContext *context )
{
    Parameters *parameters = context->currentObject->object.parameters;

    if( ( parameters->descriptionType == DESCRIPTION_UNKNOWN ) || ( parameters->locationType == LOCATION_UNKNOWN ) )
    {
        fprintf( stderr, "EnsembleParameters %s has no data\n", context->currentObject->name );
        destroyFieldmlObject( context->currentObject );
    }
    else
    {
    	addObject( context->parse, context->currentObject );
    }
    
    context->currentObject = NULL;
}


void startContinuousParameters( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    FieldmlObject *object;
    int handle;

    if( name == NULL )
    {
        fprintf( stderr, "ContinuousParameters has no name\n" );
        return;
    }

    if( valueDomain == NULL )
    {
        fprintf( stderr, "ContinuousParameters %s has no value domain\n", name );
        return;
    }

    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    
    object = createFieldmlObject( name, FHT_CONTINUOUS_PARAMETERS );
    object->object.parameters = createParameters( handle );

    context->currentObject = object;
}


void endContinuousParameters( FieldmlContext *context )
{
    Parameters *parameters = context->currentObject->object.parameters;

    if( ( parameters->descriptionType == DESCRIPTION_UNKNOWN ) || ( parameters->locationType == LOCATION_UNKNOWN ) )
    {
        fprintf( stderr, "ContinuousParameters %s has no data\n", context->currentObject->name );
        destroyFieldmlObject( context->currentObject );
    }
    else
    {
        addObject( context->parse, context->currentObject );
    }
    
    context->currentObject = NULL;
}


void startInlineData( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	Parameters *parameters = object->object.parameters;
    
    if( parameters->locationType != LOCATION_UNKNOWN )
    {
        fprintf( stderr, "Parameters %s already has data\n", object->name );
        return;
    }
    
    parameters->locationType = LOCATION_INLINE;
}


void onInlineData( FieldmlContext *context, const char *const characters, const int length )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	Parameters *parameters = object->object.parameters;
    StringDataSource *source;
    char *newString;

    if( parameters->locationType != LOCATION_INLINE )
    {
        return;
    }
    
    source = &(parameters->dataLocation.stringData);

    newString = malloc( source->length + length + 1 );
    memcpy( newString, source->string, source->length );
    memcpy( newString + source->length, characters, length );
    source->length += length;
    newString[ source->length ] = 0;
    free( source->string );
    source->string = newString;
}


void onFileData( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	Parameters *parameters = object->object.parameters;
    char *file = getAttribute( attributes, "file" );
    char *type = getAttribute( attributes, "type" );
    char *offset = getAttribute( attributes, "offset" );
    FileDataSource *source;
    
    if( parameters->locationType != LOCATION_UNKNOWN )
    {
        fprintf( stderr, "Parameters %s already has data\n", object->name );
        return;
    }
    
    if( file == NULL )
    {
        fprintf( stderr, "Parameters file data for %s must have a file name\n", object->name );
        return;
    }
    if( type == NULL )
    {
        fprintf( stderr, "Parameters file data for %s must have a file type\n", object->name );
        return;
    }
    
    parameters->locationType = LOCATION_FILE;

    source = &(parameters->dataLocation.fileData);
    source->filename = _strdup( file );
    source->isText = ( strcmp( type, "text" ) != 0 );
    if( offset == NULL )
    {
    	source->offset = 0;
    }
    else
    {
    	source->offset = atoi( offset );
    }
}


void startSemidenseData( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	Parameters *parameters = object->object.parameters;
    
    if( parameters->descriptionType != DESCRIPTION_UNKNOWN )
    {
        fprintf( stderr, "Parameters %s already has data\n", object->name );
        return;
    }
    
    parameters->descriptionType = DESCRIPTION_SEMIDENSE;
    parameters->dataDescription.semidense = createSemidenseData();
}


void onSemidenseSparseIndex( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	Parameters *parameters = object->object.parameters;
	char *index;
	int handle;
	
    if( parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return;
    }

    index = getAttribute( attributes, "value" );
    if( index == NULL )
    {
        fprintf( stderr, "Missing index in semi dense data\n" );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

	addSimpleListEntry( parameters->dataDescription.semidense->sparseIndexes, (void*)(handle + 1) );//HACK!!
}


void onSemidenseDenseIndex( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	Parameters *parameters = object->object.parameters;
	char *index;
	int handle;

    if( parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return;
    }

    index = getAttribute( attributes, "value" );
    if( index == NULL )
    {
        fprintf( stderr, "Missing index in semi dense data\n" );
        return;
    }

    handle = getOrCreateObjectHandle( context->parse, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

	addSimpleListEntry( parameters->dataDescription.semidense->denseIndexes, (void*)(handle + 1) );//HACK!!
}


void endSemidenseData( FieldmlContext *context )
{
}


void startContinuousPiecewise( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object;
	char *name;
	char *valueDomain;
	char *indexDomain;
	int valueHandle, indexHandle;
	
	name = getAttribute( attributes, "name" );
	valueDomain = getAttribute( attributes, "valueDomain" );
	indexDomain = getAttribute( attributes, "indexDomain" );
	
	if( name == NULL )
	{
        fprintf( stderr, "ContinuousPiecewise has no name\n" );
        return;
	}
	
	if( valueDomain == NULL )
	{
		fprintf( stderr, "ContinuousPiecewise %s has no value domain\n", name );
		return;
	}
	
	if( indexDomain == NULL )
	{
		fprintf( stderr, "ContinuousPiecewise %s has no index domain\n", name );
		return;
	}
	
	valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
	indexHandle = getOrCreateObjectHandle( context->parse, indexDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_PIECEWISE );
    object->object.piecewise = createContinuousPiecewise( valueHandle, indexHandle );

    context->currentObject = object;
}


void onContinuousPiecewiseEntry( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	ContinuousPiecewise *piecewise = (ContinuousPiecewise*)object->object.piecewise;
	char *key;
	char *value;
	int handle;
	
	key = getAttribute( attributes, "key" );
	value = getAttribute( attributes, "value" );
	
	if( ( key == NULL ) || ( value == NULL ) )
	{
		fprintf( stderr, "Malformed element evaluator for ContinuousPiecewise %s\n", object->name );
		return;
	}
	
	handle = getOrCreateObjectHandle( context->parse, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
	
	setIntTableEntry( piecewise->evaluators, atoi( key ), (void*)(handle + 1), NULL ); //HACK!!
}


void endContinuousPiecewise( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousAggregate( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object;
	char *name;
	char *valueDomain;
	int valueHandle;
	
	name = getAttribute( attributes, "name" );
	valueDomain = getAttribute( attributes, "valueDomain" );
	
	if( name == NULL )
	{
        fprintf( stderr, "ContinuousAggregate has no name\n" );
        return;
	}
	
	if( valueDomain == NULL )
	{
		fprintf( stderr, "ContinuousAggregate %s has no value domain\n", name );
		return;
	}
	
	valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_AGGREGATE );
    object->object.aggregate = createContinuousAggregate( valueHandle );

    context->currentObject = object;
}


void onContinuousAggregateEntry( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	ContinuousAggregate *aggregate = (ContinuousAggregate*)object->object.aggregate;
	char *key;
	char *value;
	int handle;
	
	key = getAttribute( attributes, "key" );
	value = getAttribute( attributes, "value" );
	
	if( ( key == NULL ) || ( value == NULL ) )
	{
		fprintf( stderr, "Malformed element evaluator for ContinuousAggregate %s\n", object->name );
		return;
	}
	
	handle = getOrCreateObjectHandle( context->parse, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
	
	setIntTableEntry( aggregate->evaluators, atoi( key ), (void*)(handle + 1), NULL ); //HACK!!
}


void endContinuousAggregate( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousVariable( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object;
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    int valueHandle;

    if( name == NULL )
    {
        fprintf( stderr, "ContinuousVariable has no name\n" );
        return;
    }
    if( valueDomain == NULL )
    {
        fprintf( stderr, "ContinuousVariable %s has no value domain\n", name );
        return;
    }

	valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_VARIABLE );
    object->object.variable = createVariable( valueHandle );

    context->currentObject = object;
}


void startEnsembleVariable( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object;
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    int valueHandle;
    
    if( name == NULL )
    {
        fprintf( stderr, "EnsembleVariable has no name\n" );
        return;
    }
    if( valueDomain == NULL )
    {
        fprintf( stderr, "EnsembleVariable %s has no value domain\n", name );
        return;
    }

	valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    object = createFieldmlObject( name, FHT_ENSEMBLE_VARIABLE );
    object->object.variable = createVariable( valueHandle );

    context->currentObject = object;
}


void endVariable( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void onMarkupEntry( FieldmlContext *context, SaxAttributes *attributes )
{
	FieldmlObject *object = (FieldmlObject*)context->currentObject;
	StringTable *table;
	char *key;
	char *value;
	
	key = getAttribute( attributes, "key" );
	value = getAttribute( attributes, "value" );
	
	if( ( key == NULL ) || ( value == NULL ) )
	{
		fprintf( stderr, "Malformed markup\n" );
		return;
	}
	
	table = NULL;
	if( object->type == FHT_CONTINUOUS_AGGREGATE )
	{
		table = object->object.aggregate->markup;
	}
	
	if( table != NULL )
	{
		setStringTableEntry( table, key, _strdup( value ), free );
	}
}
