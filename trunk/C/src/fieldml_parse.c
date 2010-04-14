#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libxml/sax.h>

#include "fieldml_parse.h"
#include "int_table.h"
#include "string_table.h"
#include "simple_list.h"
#include "fieldml_sax.h"

//========================================================================
//
// Structs
//
//========================================================================

typedef enum _FieldmlParseState
{
	FLP_IDLE,
	FLP_ENSEMBLE_DOMAIN,
	FLP_CONTINUOUS_DOMAIN,
	FLP_ENSEMBLE_PARAMETERS,
	FLP_CONTINUOUS_PARAMETERS,
	FLP_CONTINUOUS_PIECEWISE,
	FLP_CONTINUOUS_AGGREGATE,
	FLP_CONTINUOUS_IMPORT,
	FLP_VARIABLE,
}
FieldmlParseState;

struct _FieldmlContext
{
	void *currentObject;
	void *currentObject2;
	
	FieldmlParseState state;
	
	FieldmlParse *parse;
};

struct _FieldmlParse
{
    StringTable *ensembleDomains;
    StringTable *continuousDomains;
    StringTable *contiguousBounds;
    StringTable *continuousImports;
    StringTable *ensembleParameters;
    StringTable *continuousParameters;
    StringTable *continuousPiecewise;
    StringTable *continuousAggregate;
    StringTable *semidenseData;
    StringTable *variables;
};

typedef enum _EnsembleBoundsType
{
    BOUNDS_UNKNOWN,     // EnsembleDomain bounds not yet known.
    BOUNDS_CONTIGUOUS,  // Contiguous bounds (i.e. 1 ... N)
    BOUNDS_ARBITRARY,   // Arbitrary bounds (not yet supported)
}
EnsembleBoundsType;


typedef enum _ParameterStorageType
{
    STORAGE_UNKNOWN,
    STORAGE_SEMIDENSE,
}
ParameterStorageType;


typedef struct _EnsembleDomain
{
    char *name;
    char *componentEnsemble;

    EnsembleBoundsType boundsType;
}
EnsembleDomain;


typedef struct _ContinuousDomain
{
    char *name;
    char *baseName;
    char *componentEnsemble;
}
ContinuousDomain;


typedef struct _ContiguousBounds
{
    int count;
}
ContiguousBounds;


typedef struct _ContinuousImport
{
    char *name;
    char *remoteName;
    char *valueDomain;

    StringTable *aliases;
}
ContinuousImport;


typedef struct _EnsembleParameters
{
    char *name;
    char *valueDomain;

    ParameterStorageType storageType;
}
EnsembleParameters;


typedef struct _ContinuousParameters
{
    char *name;
    char *valueDomain;

    ParameterStorageType storageType;
}
ContinuousParameters;


typedef struct _ContinuousPiecewise
{
	char *name;
    char *valueDomain;
    char *indexDomain;
    
    IntTable *evaluators;
}
ContinuousPiecewise;


typedef struct _ContinuousAggregate
{
	char *name;
	char *valueDomain;
	
	IntTable *evaluators;
}
ContinuousAggregate;

typedef enum _DataLocation
{
    LOC_UNKNOWN,
    LOC_INLINE,
    LOC_FILE,
}
DataLocation;


typedef struct _StringDataSource
{
    char *string;
    int length;
}
StringDataSource;


typedef struct _FileDataSource
{
    char *filename;
    int offset;
    int isText;
}
FileDataSource;


typedef struct _DataSource
{
    DataLocation location;
    union TaggedDataSource
    {
        StringDataSource stringData;
        FileDataSource fileData;
    }
    data;
}
DataSource;


typedef struct _SemidenseData
{
    SimpleList *sparseIndexes;
    SimpleList *denseIndexes;

    DataSource dataSource;
}
SemidenseData;


typedef struct _Variable
{
    char *name;
    char *valueDomain;
    SimpleList *parameters;
}
Variable;

//========================================================================
//
// Creators
//
//========================================================================


FieldmlContext *createFieldmlContext( FieldmlParse *parse )
{
	FieldmlContext *context = calloc( 1, sizeof( FieldmlContext ) );
	
	context->parse = parse;
	context->state = FLP_IDLE;
	
	return context;
}


FieldmlParse *createFieldmlParse()
{
    FieldmlParse *parse;

    parse = calloc( 1, sizeof( FieldmlParse ) );
    parse->ensembleDomains = createStringTable();
    parse->continuousDomains = createStringTable();
    parse->contiguousBounds = createStringTable();
    parse->continuousImports = createStringTable();
    parse->ensembleParameters = createStringTable();
    parse->continuousParameters = createStringTable();
    parse->continuousPiecewise = createStringTable();
    parse->continuousAggregate = createStringTable();
    parse->semidenseData = createStringTable();
    parse->variables = createStringTable();

    return parse;
}


EnsembleDomain *createEnsembleDomain( char *name, char *componentEnsemble )
{
    EnsembleDomain *domain;

    domain = calloc( 1, sizeof( EnsembleDomain ) );
    domain->name = _strdup( name );
    domain->componentEnsemble = _strdup( componentEnsemble );
    domain->boundsType = BOUNDS_UNKNOWN;

    return domain;
}


ContiguousBounds *createContiguousBounds( char *countString )
{
    ContiguousBounds *bounds;

    bounds = calloc( 1, sizeof( ContiguousBounds ) );
    bounds->count = atoi( countString );

    return bounds;
}


ContinuousDomain *createContinuousDomain( char *name, char *baseDomain, char *componentEnsemble )
{
    ContinuousDomain *domain;

    domain = calloc( 1, sizeof( ContinuousDomain ) );
    domain->name = _strdup( name );
    domain->baseName = _strdup( baseDomain );
    domain->componentEnsemble = _strdup( componentEnsemble );

    return domain;
}


ContinuousImport *createContinuousImport( char *name, char *remoteName, char *valueDomain )
{
    ContinuousImport *import;

    import = calloc( 1, sizeof( ContinuousImport ) );
    import->name = _strdup( name );
    import->remoteName = _strdup( remoteName );
    import->valueDomain = _strdup( valueDomain );
    import->aliases = createStringTable();

    return import;
}


EnsembleParameters *createEnsembleParameters( char *name, char *valueDomain )
{
    EnsembleParameters *parameters;

    parameters = calloc( 1, sizeof( EnsembleParameters ) );
    parameters->name = _strdup( name );
    parameters->valueDomain = _strdup( valueDomain );
    parameters->storageType = STORAGE_UNKNOWN;

    return parameters;
}


ContinuousParameters *createContinuousParameters( char *name, char *valueDomain )
{
    ContinuousParameters *parameters;

    parameters = calloc( 1, sizeof( ContinuousParameters ) );
    parameters->name = _strdup( name );
    parameters->valueDomain = _strdup( valueDomain );
    parameters->storageType = STORAGE_UNKNOWN;

    return parameters;
}


ContinuousPiecewise *createContinuousPiecewise( char *name, char *valueDomain, char *indexDomain )
{
	ContinuousPiecewise *piecewise;
	
	piecewise = calloc( 1, sizeof( ContinuousPiecewise ) );
	piecewise->name = _strdup( name );
	piecewise->valueDomain = _strdup( valueDomain );
	piecewise->indexDomain = _strdup( indexDomain );
	
	piecewise->evaluators = createIntTable();
	
	return piecewise;
}


ContinuousAggregate *createContinuousAggregate( char *name, char *valueDomain )
{
	ContinuousAggregate *aggregate;
	
	aggregate = calloc( 1, sizeof( ContinuousAggregate ) );
	aggregate->name = _strdup( name );
	aggregate->valueDomain = _strdup( valueDomain );
	
	aggregate->evaluators = createIntTable();
	
	return aggregate;
}


SemidenseData *createSemidenseData()
{
    SemidenseData *data;
    data = calloc( 1, sizeof( SemidenseData ) );
    data->denseIndexes = createSimpleList();
    data->sparseIndexes = createSimpleList();
    data->dataSource.location = LOC_UNKNOWN;

    return data;
}


Variable *createVariable( char *name, char *valueDomain )
{
    Variable *variable;

    variable = calloc( 1, sizeof( Variable ) );
    variable->name = _strdup( name );
    variable->valueDomain = _strdup( valueDomain );
    variable->parameters = createSimpleList();

    return variable;
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
    free( domain->name );
    free( domain->componentEnsemble );
    free( domain );
}


void destroyContinuousDomain( ContinuousDomain *domain )
{
    free( domain->name );
    free( domain->baseName );
    free( domain->componentEnsemble );
    free( domain );
}


void destroyContinuousImport( ContinuousImport *import )
{
    free( import->name );
    free( import->remoteName );
    free( import->valueDomain );
    destroyStringTable( import->aliases, free );
    free( import );
}


void destroyEnsembleParameters( EnsembleParameters *parameters )
{
    free( parameters->name );
    free( parameters->valueDomain );

    switch( parameters->storageType )
    {
    case STORAGE_SEMIDENSE:
        break;
    default:
        break;
    }

    free( parameters );
}


void destroyContinuousParameters( ContinuousParameters *parameters )
{
    free( parameters->name );
    free( parameters->valueDomain );

    switch( parameters->storageType )
    {
    case STORAGE_SEMIDENSE:
        break;
    default:
        break;
    }

    free( parameters );
}


void destroyContinuousPiecewise( ContinuousPiecewise *piecewise )
{
	free( piecewise->name );
	free( piecewise->valueDomain );
	free( piecewise->indexDomain );
	destroyIntTable( piecewise->evaluators, free );
	free( piecewise );
}


void destroyContinuousAggregate( ContinuousAggregate *aggregate )
{
	free( aggregate->name );
	free( aggregate->valueDomain );
	destroyIntTable( aggregate->evaluators, free );
	free( aggregate );
}


void destroySemidenseData( SemidenseData *data )
{
    destroySimpleList( data->sparseIndexes, free );
    destroySimpleList( data->denseIndexes, free );
    
    if( data->dataSource.location == LOC_INLINE )
    {
    	free( data->dataSource.data.stringData.string );
    }
    else if( data->dataSource.location == LOC_FILE )
    {
    	free( data->dataSource.data.fileData.filename );
    }
    
    free( data );
}


void destroyVariable( Variable *variable )
{
    free( variable->name );
    free( variable->valueDomain );
    destroySimpleList( variable->parameters, free );

    free( variable );
}


void destroyFieldmlParse( FieldmlParse *parse )
{
    destroyStringTable( parse->ensembleDomains, destroyEnsembleDomain );
    destroyStringTable( parse->contiguousBounds, free );
    destroyStringTable( parse->continuousDomains, destroyContinuousDomain );
    destroyStringTable( parse->continuousImports, destroyContinuousImport );
    destroyStringTable( parse->continuousParameters, destroyContinuousParameters );
    destroyStringTable( parse->continuousPiecewise, destroyContinuousPiecewise );
    destroyStringTable( parse->continuousAggregate, destroyContinuousAggregate );
    destroyStringTable( parse->ensembleParameters, destroyEnsembleParameters );
    destroyStringTable( parse->semidenseData, destroySemidenseData );
    destroyStringTable( parse->variables, destroyVariable );

    free( parse );
}


//========================================================================
//
// Utility
//
//========================================================================


void dumpFieldmlParse( FieldmlParse *parse )
{
    int i, count;

    count = getStringTableCount( parse->continuousDomains );
    fprintf( stdout, "ContinuousDomains:\n" );
    for( i = 0; i < count; i++ )
    {
        fprintf( stdout, "    %s\n", ((ContinuousDomain*)getStringTableEntryData( parse->continuousDomains, i ))->name );
    }

    count = getStringTableCount( parse->ensembleDomains );
    fprintf( stdout, "EnsembleDomains:\n" );
    for( i = 0; i < count; i++ )
    {
        fprintf( stdout, "    %s\n", ((EnsembleDomain*)getStringTableEntryData( parse->ensembleDomains, i ))->name );
    }

    count = getStringTableCount( parse->continuousImports );
    fprintf( stdout, "Continuous Imports:\n" );
    for( i = 0; i < count; i++ )
    {
        int j, count2;
        ContinuousImport *import;

        import = (ContinuousImport*)getStringTableEntryData( parse->continuousImports, i );

        fprintf( stdout, "    %s\n", import->name );
        
        count2 = getStringTableCount( import->aliases );
        for( j = 0; j < count2; j++ )
        {
            fprintf( stdout, "        %s -> %s\n", getStringTableEntryName( import->aliases, j ), getStringTableEntryData( import->aliases, j ) );
        }
    }

    count = getStringTableCount( parse->continuousParameters );
    fprintf( stdout, "Continuous Parameters:\n" );
    for( i = 0; i < count; i++ )
    {
        ContinuousParameters *params;
        SemidenseData *data;

        params = (ContinuousParameters*)getStringTableEntryData( parse->continuousParameters, i );
        data = (SemidenseData*)getStringTableEntry( parse->semidenseData, params->name );

        fprintf( stdout, "    %s\n", params->name );
        
        if( data->dataSource.location == LOC_INLINE )
        {
			fprintf( stdout, "    *******************************\n");
			fprintf( stdout, "%s\n", data->dataSource.data.stringData.string );
			fprintf( stdout, "    *******************************\n");
        }
        else if( data->dataSource.location == LOC_FILE )
        {
        	fprintf( stdout, "    file = %s, offset = %d\n", data->dataSource.data.fileData.filename, data->dataSource.data.fileData.offset );
        }
    }

    count = getStringTableCount( parse->ensembleParameters );
    fprintf( stdout, "Ensemble Parameters:\n" );
    for( i = 0; i < count; i++ )
    {
        EnsembleParameters *params;
        SemidenseData *data;

        params = (EnsembleParameters*)getStringTableEntryData( parse->ensembleParameters, i );
        data = (SemidenseData*)getStringTableEntry( parse->semidenseData, params->name );

        fprintf( stdout, "    %s\n", params->name );

        if( data->dataSource.location == LOC_INLINE )
        {
			fprintf( stdout, "    *******************************\n");
			fprintf( stdout, "%s\n", data->dataSource.data.stringData.string );
			fprintf( stdout, "    *******************************\n");
        }
        else if( data->dataSource.location == LOC_FILE )
        {
        	fprintf( stdout, "    file = %s, offset = %d\n", data->dataSource.data.fileData.filename, data->dataSource.data.fileData.offset );
        }
    }

    count = getStringTableCount( parse->continuousPiecewise );
    fprintf( stdout, "Continuous Piecewise:\n" );
    for( i = 0; i < count; i++ )
    {
        ContinuousPiecewise *piecewise;
        int count2, j;

        piecewise = (ContinuousPiecewise*)getStringTableEntryData( parse->continuousPiecewise, i );

        fprintf( stdout, "    %s (over %s)\n", piecewise->name, piecewise->indexDomain );
        
        count2 = getIntTableCount( piecewise->evaluators );
        for( j = 0; j < count2; j++ )
        {
        	fprintf( stdout, "        %d -> %s\n", getIntTableEntryName( piecewise->evaluators, j ), getIntTableEntryData( piecewise->evaluators, j ) );
        }
    }

    count = getStringTableCount( parse->continuousAggregate );
    fprintf( stdout, "Continuous Aggregate:\n" );
    for( i = 0; i < count; i++ )
    {
        ContinuousAggregate *aggregate;
        int count2, j;

        aggregate = (ContinuousAggregate*)getStringTableEntryData( parse->continuousAggregate, i );

        fprintf( stdout, "    %s\n", aggregate->name );
        
        count2 = getIntTableCount( aggregate->evaluators );
        for( j = 0; j < count2; j++ )
        {
        	fprintf( stdout, "        %d -> %s\n", getIntTableEntryName( aggregate->evaluators, j ), getIntTableEntryData( aggregate->evaluators, j ) );
        }
    }
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
    EnsembleDomain *domain;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "EnsembleDomain has no name\n" );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, "componentDomain" );

    domain = createEnsembleDomain( name, componentEnsemble );

    context->currentObject = domain;
    context->state = FLP_ENSEMBLE_DOMAIN;
}


void endEnsembleDomain( FieldmlContext *context )
{
    EnsembleDomain *domain = (EnsembleDomain*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    if( domain->boundsType == BOUNDS_UNKNOWN )
    {
        fprintf( stderr, "EnsembleDomain %s has no bounds\n", domain->name );
        destroyEnsembleDomain( domain );
        return;
    }

    setStringTableEntry( context->parse->ensembleDomains, domain->name, domain, destroyEnsembleDomain );
}


void startContinuousDomain( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *baseDomain;
    char *componentEnsemble;
    ContinuousDomain *domain;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "ContinuousDomain has no name\n" );
        return;
    }
    
    baseDomain = getAttribute( attributes, "baseDomain" );
    if( baseDomain == NULL )
    {
        fprintf( stderr, "ContinuousDomain %s has no base domain\n", name );
        return;
    }

    componentEnsemble = getAttribute( attributes, "componentDomain" );

    domain = createContinuousDomain( name, baseDomain, componentEnsemble );

    context->currentObject = domain;
    context->state = FLP_CONTINUOUS_DOMAIN;
}


void endContinuousDomain( FieldmlContext *context )
{
    ContinuousDomain *domain = (ContinuousDomain*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    setStringTableEntry( context->parse->continuousDomains, domain->name, domain, destroyContinuousDomain );
}


void startContiguousBounds( FieldmlContext *context, SaxAttributes *attributes )
{
    char * count;
    EnsembleDomain *domain;
    ContiguousBounds *bounds;
        
    domain = (EnsembleDomain*)context->currentObject;

    count = getAttribute( attributes, "valueCount" );
    if( count == NULL )
    {
        fprintf( stderr, "ContiguousEnsembleBounds for %s has no value count\n", domain->name );
        return;
    }

    domain->boundsType = BOUNDS_CONTIGUOUS;

    bounds = createContiguousBounds( count );

    setStringTableEntry( context->parse->contiguousBounds, domain->name, bounds, free );
}


void startContinuousImport( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *remoteName;
    char *valueDomain;
    ContinuousImport *import;
        
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

    import = createContinuousImport( name, remoteName, valueDomain );

    context->currentObject = import;
    context->state = FLP_CONTINUOUS_IMPORT;
}

void continuousImportAlias( FieldmlContext *context, SaxAttributes *attributes )
{
    ContinuousImport *import;
    char *remote = getAttribute( attributes, "key" );
    char *local = getAttribute( attributes, "value" );

    import = (ContinuousImport*)context->currentObject;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has malformed alias\n", import->name );
        return;
    }

    setStringTableEntry( import->aliases, remote, _strdup( local ), free );
}


void endContinuousImport( FieldmlContext *context )
{
    ContinuousImport *import = (ContinuousImport*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    setStringTableEntry( context->parse->continuousImports, import->name, import, destroyContinuousImport );
}


void startEnsembleParameters( FieldmlContext *context, SaxAttributes *attributes )
{
    EnsembleParameters *parameters;

    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );

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

    parameters = createEnsembleParameters( name, valueDomain );

    context->currentObject = parameters;
    context->state = FLP_ENSEMBLE_PARAMETERS;
}


void endEnsembleParameters( FieldmlContext *context )
{
    EnsembleParameters *parameters = (EnsembleParameters*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    if( parameters->storageType == STORAGE_UNKNOWN )
    {
        fprintf( stderr, "EnsembleParameters %s has no data\n", parameters->name );
        destroyEnsembleParameters( parameters );
        return;
    }

    setStringTableEntry( context->parse->ensembleParameters, parameters->name, parameters, destroyEnsembleParameters );
}


void startContinuousParameters( FieldmlContext *context, SaxAttributes *attributes )
{
    ContinuousParameters *parameters;

    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );

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

    parameters = createContinuousParameters( name, valueDomain );

    context->currentObject = parameters;
    context->state = FLP_CONTINUOUS_PARAMETERS;
}


void endContinuousParameters( FieldmlContext *context )
{
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    if( parameters->storageType == STORAGE_UNKNOWN )
    {
        fprintf( stderr, "ContinuousParameters %s has no data\n", parameters->name );
        destroyContinuousParameters( parameters );
        return;
    }

    setStringTableEntry( context->parse->continuousParameters, parameters->name, parameters, destroyContinuousParameters );
}


void startContinuousPiecewise( FieldmlContext *context, SaxAttributes *attributes )
{
	ContinuousPiecewise *piecewise;
	char *name;
	char *valueDomain;
	char *indexDomain;
	
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
	
	piecewise = createContinuousPiecewise( name, valueDomain, indexDomain );
	
	context->currentObject = piecewise;
    context->state = FLP_CONTINUOUS_PIECEWISE;
}


void onContinuousPiecewiseEntry( FieldmlContext *context, SaxAttributes *attributes )
{
	ContinuousPiecewise *piecewise = (ContinuousPiecewise*)context->currentObject;
	char *key;
	char *value;
	
	key = getAttribute( attributes, "key" );
	value = getAttribute( attributes, "value" );
	
	if( ( key == NULL ) || ( value == NULL ) )
	{
		fprintf( stderr, "Malformed element evaluator for ContinuousPiecewise %s\n", piecewise->name );
		return;
	}
	
	setIntTableEntry( piecewise->evaluators, atoi( key ),  _strdup( value ), free );
}


void endContinuousPiecewise( FieldmlContext *context )
{
	ContinuousPiecewise *piecewise = (ContinuousPiecewise*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    setStringTableEntry( context->parse->continuousPiecewise, piecewise->name, piecewise, destroyContinuousPiecewise );
}


void startContinuousAggregate( FieldmlContext *context, SaxAttributes *attributes )
{
	ContinuousAggregate *aggregate;
	char *name;
	char *valueDomain;
	
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
	
	aggregate = createContinuousAggregate( name, valueDomain );
	
	context->currentObject = aggregate;
    context->state = FLP_CONTINUOUS_AGGREGATE;
}


void onContinuousAggregateEntry( FieldmlContext *context, SaxAttributes *attributes )
{
	ContinuousAggregate *aggregate = (ContinuousAggregate*)context->currentObject;
	char *key;
	char *value;
	
	key = getAttribute( attributes, "key" );
	value = getAttribute( attributes, "value" );
	
	if( ( key == NULL ) || ( value == NULL ) )
	{
		fprintf( stderr, "Malformed element evaluator for ContinuousAggregate %s\n", aggregate->name );
		return;
	}
	
	setIntTableEntry( aggregate->evaluators, atoi( key ),  _strdup( value ), free );
}


void endContinuousAggregate( FieldmlContext *context )
{
	ContinuousAggregate *aggregate = (ContinuousAggregate*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    setStringTableEntry( context->parse->continuousAggregate, aggregate->name, aggregate, destroyContinuousAggregate );
}


void startSemidenseData( FieldmlContext *context, SaxAttributes *attributes )
{
    SemidenseData *data = createSemidenseData();

    context->currentObject2 = data;
}


void semidenseIndex( FieldmlContext *context, SaxAttributes *attributes, int isSparse )
{
    SemidenseData *data = (SemidenseData*)context->currentObject2;

    char *index = getAttribute( attributes, "value" );
    if( index == NULL )
    {
        fprintf( stderr, "Invalid index in semi dense data\n" );
        return;
    }

    if( isSparse )
    {
        addListEntry( data->sparseIndexes, _strdup( index ) );
    }
    else
    {
        addListEntry( data->denseIndexes, _strdup( index ) );
    }
}


void semidenseStartInlineData( FieldmlContext *context, SaxAttributes *attributes )
{
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    SemidenseData *data = (SemidenseData*)context->currentObject2;
    
    if( data->dataSource.location != LOC_UNKNOWN )
    {
        fprintf( stderr, "Semidense data for %s already has data\n", parameters->name );
        return;
    }
    
    data->dataSource.location = LOC_INLINE;
}


void semidenseFileData( FieldmlContext *context, SaxAttributes *attributes )
{
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    SemidenseData *data = (SemidenseData*)context->currentObject2;
    char *file = getAttribute( attributes, "file" );
    char *type = getAttribute( attributes, "type" );
    char *offset = getAttribute( attributes, "offset" );
    FileDataSource *source;
    
    if( data->dataSource.location != LOC_UNKNOWN )
    {
        fprintf( stderr, "Semidense data for %s already has data\n", parameters->name );
        return;
    }
    
    if( file == NULL )
    {
        fprintf( stderr, "Semidense file data for %s must have a file name\n", parameters->name );
        return;
    }
    if( type == NULL )
    {
        fprintf( stderr, "Semidense file data for %s must have a file type\n", parameters->name );
        return;
    }
    
    data->dataSource.location = LOC_FILE;

    source = &(data->dataSource.data.fileData);
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


void semidenseInlineData( FieldmlContext *context, const char *const characters, const int length )
{
    StringDataSource *source;
    char *newString;
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    SemidenseData *data = (SemidenseData*)context->currentObject2;

    if( data->dataSource.location != LOC_INLINE )
    {
        fprintf( stderr, "Semidense data for %s already has non-inline data\n", parameters->name );
        return;
    }

    source = &(data->dataSource.data.stringData);

    newString = malloc( source->length + length + 1 );
    memcpy( newString, source->string, source->length );
    memcpy( newString + source->length, characters, length );
    source->length += length;
    newString[ source->length ] = 0;
    free( source->string );
    source->string = newString;
}


void endSemidenseData( FieldmlContext *context )
{
    SemidenseData *data = (SemidenseData*)context->currentObject2;
    context->currentObject2 = NULL;

    if( context->state = FLP_ENSEMBLE_PARAMETERS )
    {
        EnsembleParameters *parameters = (EnsembleParameters*)context->currentObject;
        
        parameters->storageType = STORAGE_SEMIDENSE;
        setStringTableEntry( context->parse->semidenseData, parameters->name, data, destroySemidenseData );
    }
    else if( context->state = FLP_CONTINUOUS_PARAMETERS )
    {
        ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
        
        parameters->storageType = STORAGE_SEMIDENSE;
        setStringTableEntry( context->parse->semidenseData, parameters->name, data, destroySemidenseData );
    }
    else
    {
    	// We've fallen off the edge of reality. DON'T PANIC. 
    }
}


void startVariable( FieldmlContext *context, SaxAttributes *attributes )
{
    Variable *variable;

    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );

    if( name == NULL )
    {
        fprintf( stderr, "Variable has no name\n" );
        return;
    }
    if( valueDomain == NULL )
    {
        fprintf( stderr, "Variable %s has no value domain\n", name );
        return;
    }

    variable = createVariable( name, valueDomain );

    context->currentObject = variable;
    context->state = FLP_VARIABLE;
}


void endVariable( FieldmlContext *context )
{
    Variable *variable = (Variable*)context->currentObject;
    context->currentObject = NULL;
    context->state = FLP_IDLE;

    setStringTableEntry( context->parse->variables, variable->name, variable, destroyVariable );
}
