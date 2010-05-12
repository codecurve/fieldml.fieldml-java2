#ifndef H_FIELDML_API
#define H_FIELDML_API

/*
     API notes:
     
     If a function returns a FmlParseHandle or FmlObjectHandle, it will return
     FML_INVALID handle on error.
     
     All FieldML objects are referred to only by their integer handle.
     
     All handles are internally type-checked. If an inappropriate handle is
     passed to a function, the function will return -1, NULL or
     FML_INVALID_HANDLE as applicable.
 */

#define FML_INVALID_HANDLE -1

#define FML_ERR_NO_ERROR            0
#define FML_ERR_UNKNOWN_OBJECT      1001
#define FML_ERR_INCOMPLETE_OBJECT   1002
#define FML_ERR_INVALID_OBJECT      1003
#define FML_ERR_ACCESS_VIOLATION    1004


typedef enum _DomainBoundsType
{
    BOUNDS_UNKNOWN,              // EnsembleDomain bounds not yet known.
    BOUNDS_DISCRETE_CONTIGUOUS,  // Contiguous bounds (i.e. 1 ... N)
    BOUNDS_DISCRETE_ARBITRARY,   // Arbitrary bounds (not yet supported)
}
DomainBoundsType;


typedef enum _DataFileType
{
    TYPE_UNKNOWN,
    TYPE_TEXT,                  // Text file with CSV/space delimited numbers. Offset is numbers.
    TYPE_LINES,                 // Formatted text file. Offset is lines. CSV/space delimited numbers expected at offset.
}
DataFileType;


typedef enum _DataDescriptionType
{
    DESCRIPTION_UNKNOWN,
    DESCRIPTION_SEMIDENSE,
}
DataDescriptionType;


typedef enum _DataLocationType
{
    LOCATION_UNKNOWN,
    LOCATION_INLINE,
    LOCATION_FILE,
}
DataLocationType;


typedef enum _FieldmlHandleType
{
    FHT_UNKNOWN,
    
    FHT_ENSEMBLE_DOMAIN,
    FHT_CONTINUOUS_DOMAIN,
    FHT_MESH_DOMAIN,
    FHT_CONTINUOUS_IMPORT,
    FHT_ENSEMBLE_PARAMETERS,
    FHT_CONTINUOUS_PARAMETERS,
    FHT_CONTINUOUS_PIECEWISE,
    FHT_CONTINUOUS_AGGREGATE,
    FHT_CONTINUOUS_DEREFERENCE,
    FHT_CONTINUOUS_VARIABLE,
    FHT_ENSEMBLE_VARIABLE,
    FHT_REMOTE_ENSEMBLE_DOMAIN,
    FHT_REMOTE_CONTINUOUS_DOMAIN,
    FHT_REMOTE_ENSEMBLE_EVALUATOR,
    FHT_REMOTE_CONTINUOUS_EVALUATOR,
    
    //These four are stand-in types used to allow forward-declaration during parsing.
    FHT_UNKNOWN_ENSEMBLE_DOMAIN,
    FHT_UNKNOWN_CONTINUOUS_DOMAIN,
    FHT_UNKNOWN_ENSEMBLE_EVALUATOR,
    FHT_UNKNOWN_CONTINUOUS_EVALUATOR,
    FHT_UNKNOWN_ENSEMBLE_SOURCE,
    FHT_UNKNOWN_CONTINUOUS_SOURCE,
}
FieldmlHandleType;


typedef void * FmlParseHandle;

typedef int FmlObjectHandle;


/*
     Parses the given file, and returns a handle to the parsed data. This
     handle is then used for all subsequent API calls. 
 */
FmlParseHandle Fieldml_ParseFile( const char *filename );

FmlParseHandle Fieldml_Create();


int Fieldml_WriteFile( FmlParseHandle parse, const char *filename );


/*
     Frees all resources associated with the given parse.
     
     HANDLE SHOULD NOT BE USED AFTER THIS CALL.
 */
void Fieldml_DestroyParse( FmlParseHandle handle );


/*
     Returns the number of errors encountered by the given parse.
 */
int Fieldml_GetErrorCount( FmlParseHandle handle );


/*
     Returns the nth error string for the given parse.
 */
const char * Fieldml_GetError( FmlParseHandle handle, int errorIndex );
int Fieldml_CopyError( FmlParseHandle handle, int errorIndex, char *buffer, int bufferLength );


/*
     Returns the number of objects of the given type, or zero if there are none.
 */
int Fieldml_GetObjectCount( FmlParseHandle handle, FieldmlHandleType type );


/*
     Returns a handle to the nth object of the given type.
 */
FmlObjectHandle Fieldml_GetObjectHandle( FmlParseHandle handle, FieldmlHandleType type, int objectIndex );


FieldmlHandleType Fieldml_GetObjectType( FmlParseHandle handle, FmlObjectHandle object );


FieldmlHandleType Fieldml_GetNamedObjectHandle( FmlParseHandle handle, const char * name );

/*
     Returns the number of markup entries (attribute/value pairs) for the given object.
 */
int Fieldml_GetMarkupCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_ValidateObject( FmlParseHandle handle, FmlObjectHandle objectHandle );

/*
     Returns the attribute string of the nth markup entry for the given object.
 */
const char * Fieldml_GetMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int markupIndex );
int Fieldml_CopyMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int markupIndex, char *buffer, int bufferLength );


/*
     Returns the value string of the nth markup entry for the given object.
 */
const char * Fieldml_GetMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int markupIndex );
int Fieldml_CopyMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int markupIndex, char *buffer, int bufferLength );


const char * Fieldml_GetMarkupAttributeValue( FmlParseHandle handle, FmlObjectHandle objectHandle, const char * attribute );
int Fieldml_CopyMarkupAttributeValue( FmlParseHandle handle, FmlObjectHandle objectHandle, const char * attribute, char *buffer, int bufferLength );


int Fieldml_SetMarkup(  FmlParseHandle handle, FmlObjectHandle objectHandle, const char * attribute, const char * value );

/*
     Returns the handle of the given domain's component ensemble.
 */
FmlObjectHandle Fieldml_GetDomainComponentEnsemble( FmlParseHandle handle, FmlObjectHandle objectHandle );


FmlObjectHandle Fieldml_CreateEnsembleDomain( FmlParseHandle handle, const char * name, FmlObjectHandle componentHandle );

FmlObjectHandle Fieldml_CreateContinuousDomain( FmlParseHandle handle, const char * name, FmlObjectHandle componentHandle );

FmlObjectHandle Fieldml_CreateMeshDomain( FmlParseHandle handle, const char * name, FmlObjectHandle xiEnsemble );


/*
     Returns the handle of the given mesh domain's xi domain.
 */
FmlObjectHandle Fieldml_GetMeshXiDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the handle of the given mesh domain's element domain.
 */
FmlObjectHandle Fieldml_GetMeshElementDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns a string describing the shape of the element in the given mesh.
 */
const char * Fieldml_GetMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber );
int Fieldml_CopyMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber, char *buffer, int bufferLength );

int Fieldml_SetMeshElementShape( FmlParseHandle handle, FmlObjectHandle mesh, int elementNumber, const char * shape );

/*
     Returns the number of connectivities specified for the given mesh domain.
 */
int Fieldml_GetMeshConnectivityCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the domain of the nth connectivity for the given mesh. 
 */
FmlObjectHandle Fieldml_GetMeshConnectivityDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int connectivityIndex );


/*
     Returns the source of the nth connectivity for the given mesh. 
 */
FmlObjectHandle Fieldml_GetMeshConnectivitySource( FmlParseHandle handle, FmlObjectHandle objectHandle, int connectivityIndex );


int Fieldml_SetMeshConnectivity( FmlParseHandle handle, FmlObjectHandle mesh, FmlObjectHandle pointDomain, FmlObjectHandle evaluator );

/*
    Returns the bounds-type of the given domain.
    
    NOTE: Currently, only ensemble domains have explicit bounds.
    NOTE: Currently, only discrete contiguous bounds are supported.
 */
DomainBoundsType Fieldml_GetDomainBoundsType( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_GetEnsembleDomainElementCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_GetEnsembleDomainElementNames( FmlParseHandle handle, FmlObjectHandle objectHandle, const int *array, int arrayLength );


int Fieldml_GetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle );

int Fieldml_SetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle, int count );


/*
     Returns the name of the given object.
 */
const char * Fieldml_GetObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle );
int Fieldml_CopyObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


/*
     Returns the value domain of the given evaluator.
 */
FmlObjectHandle Fieldml_GetValueDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


FmlObjectHandle Fieldml_CreateEnsembleVariable( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_CreateContinuousVariable( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain );


FmlObjectHandle Fieldml_CreateEnsembleParameters( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_CreateContinuousParameters( FmlParseHandle handle, const char *name, FmlObjectHandle valueDomain );



int Fieldml_SetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle, DataDescriptionType description );

/*
    Returns the data description type of the given parameter evaluator.
 */
DataDescriptionType Fieldml_GetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle );


DataLocationType Fieldml_GetParameterDataLocation( FmlParseHandle handle, FmlObjectHandle objectHandle );

int Fieldml_SetParameterDataLocation( FmlParseHandle handle, FmlObjectHandle objectHandle, DataLocationType location );

int Fieldml_AddInlineParameterData( FmlParseHandle handle, FmlObjectHandle objectHandle, const char *data, int length );

int Fieldml_SetParameterFileData( FmlParseHandle handle, FmlObjectHandle objectHandle, const char * file, DataFileType type, int offset );

const char *Fieldml_GetParameterDataFilename( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_CopyParameterDataFilename( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


int Fieldml_GetParameterDataOffset( FmlParseHandle handle, FmlObjectHandle objectHandle );


DataFileType Fieldml_GetParameterDataFileType( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_AddSemidenseIndex( FmlParseHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle indexHandle, int isSparse );

/*
     Returns the number of sparse or dense indexes of the semidense data store
     associated with the given parameter evaluator.
 */
int Fieldml_GetSemidenseIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle, int isSparse );


/*
     Returns the handle of the nth sparse or dense index of the semidense data
     store associated with the given parameter evaluator.
 */
FmlObjectHandle Fieldml_GetSemidenseIndex( FmlParseHandle handle, FmlObjectHandle objectHandle, int indexIndex, int isSparse );


int Fieldml_SetSwizzle( FmlParseHandle handle, FmlObjectHandle objectHandle, const int *buffer, int count );

int Fieldml_GetSwizzleCount( FmlParseHandle handle, FmlObjectHandle objectHandle );

const int *Fieldml_GetSwizzleData( FmlParseHandle handle, FmlObjectHandle objectHandle );

int Fieldml_CopySwizzleData( FmlParseHandle handle, FmlObjectHandle objectHandle, int *buffer, int bufferLength );


FmlObjectHandle Fieldml_CreateContinuousPiecewise( FmlParseHandle handle, const char * name, FmlObjectHandle indexHandle, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_CreateContinuousAggregate( FmlParseHandle handle, const char * name, FmlObjectHandle valueDomain );


int Fieldml_SetEvaluator( FmlParseHandle handle, FmlObjectHandle objectHandle, int element, FmlObjectHandle evaluator );

/*
    Returns the number of element->evaluator delegations for the given
    piecewise/aggregate evaluator.
 */
int Fieldml_GetEvaluatorCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the element number for the nth element->evaluator delegation in
    the given piecewise/aggregate evaluator.
 */
int Fieldml_GetEvaluatorElement( FmlParseHandle handle, FmlObjectHandle objectHandle, int evaluatorIndex );


/*
    Returns the evaluator handle for the nth element->evaluator delegation in
    the given piecewise/aggregate evaluator.
 */
FmlObjectHandle Fieldml_GetEvaluatorHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int evaluatorIndex );


FmlObjectHandle Fieldml_CreateContinuousImport( FmlParseHandle handle, const char * name, FmlObjectHandle remoteEvaluator, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_GetImportRemoteEvaluator( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the number of aliases used by the given evaluator. 
 */
int Fieldml_GetAliasCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the local domain/evaulator used by the nth alias of the given evaluator. 
 */
FmlObjectHandle Fieldml_GetAliasLocalHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int aliasIndex );


/*
    Returns the remote domain used by the nth alias of the given evaluator. 
 */
FmlObjectHandle Fieldml_GetAliasRemoteHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int aliasIndex );

int Fieldml_SetAlias( FmlParseHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle remoteDomain, FmlObjectHandle localSource );

/*
    Returns the number of indexes used by the given evaluator.
    
    NOTE: Only defined for piecewise and parameter evaluators.
    
    NOTE: For piecewise evalutors, this is always one. For parameter evaluators,
    it depends on the data store.
 */
int Fieldml_GetIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the domain of the nth indexes used by the given evaluator.
    
    NOTE: Only defined for piecewise and parameter evaluators.
 */
FmlObjectHandle Fieldml_GetIndexDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int indexIndex );


FmlObjectHandle Fieldml_CreateContinuousDereference( FmlParseHandle handle, const char * name, FmlObjectHandle indexes, FmlObjectHandle values, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_GetDereferenceIndexes( FmlParseHandle handle, FmlObjectHandle objectHandle );


FmlObjectHandle Fieldml_GetDereferenceSource( FmlParseHandle handle, FmlObjectHandle objectHandle );


#endif // H_FIELDML_PARSE
