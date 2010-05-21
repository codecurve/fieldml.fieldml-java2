#ifndef H_FIELDML_API
#define H_FIELDML_API

/*
     API notes:
     
     If a function returns a FmlHandle or FmlObjectHandle, it will return
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
#define FML_ERR_FILE_READ_ERROR     1005


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


typedef struct _ParameterReader *FmlReaderHandle;

typedef struct _ParameterWriter *FmlWriterHandle;

typedef struct _FieldmlRegion *FmlHandle;

typedef int FmlObjectHandle;


/*
     Parses the given file, and returns a handle to the parsed data. This
     handle is then used for all subsequent API calls. 
 */
FmlHandle Fieldml_CreateFromFile( const char *filename );

FmlHandle Fieldml_Create();


int Fieldml_WriteFile( FmlHandle handle, const char *filename );


/*
     Frees all resources associated with the given handle.
     
     HANDLE SHOULD NOT BE USED AFTER THIS CALL.
 */
void Fieldml_Destroy( FmlHandle handle );


/*
     Returns the number of errors encountered by the given handle.
 */
int Fieldml_GetErrorCount( FmlHandle handle );


/*
     Returns the nth error string for the given handle.
 */
const char * Fieldml_GetError( FmlHandle handle, int errorIndex );
int Fieldml_CopyError( FmlHandle handle, int errorIndex, char *buffer, int bufferLength );


/*
     Returns the number of objects of the given type, or zero if there are none.
 */
int Fieldml_GetObjectCount( FmlHandle handle, FieldmlHandleType type );


/*
     Returns a handle to the nth object of the given type.
 */
FmlObjectHandle Fieldml_GetObjectHandle( FmlHandle handle, FieldmlHandleType type, int objectIndex );


FieldmlHandleType Fieldml_GetObjectType( FmlHandle handle, FmlObjectHandle object );


FmlObjectHandle Fieldml_GetNamedObjectHandle( FmlHandle handle, const char * name );

/*
     Returns the number of markup entries (attribute/value pairs) for the given object.
 */
int Fieldml_GetMarkupCount( FmlHandle handle, FmlObjectHandle objectHandle );


int Fieldml_ValidateObject( FmlHandle handle, FmlObjectHandle objectHandle );

/*
     Returns the attribute string of the nth markup entry for the given object.
 */
const char * Fieldml_GetMarkupAttribute( FmlHandle handle, FmlObjectHandle objectHandle, int markupIndex );
int Fieldml_CopyMarkupAttribute( FmlHandle handle, FmlObjectHandle objectHandle, int markupIndex, char *buffer, int bufferLength );


/*
     Returns the value string of the nth markup entry for the given object.
 */
const char * Fieldml_GetMarkupValue( FmlHandle handle, FmlObjectHandle objectHandle, int markupIndex );
int Fieldml_CopyMarkupValue( FmlHandle handle, FmlObjectHandle objectHandle, int markupIndex, char *buffer, int bufferLength );


const char * Fieldml_GetMarkupAttributeValue( FmlHandle handle, FmlObjectHandle objectHandle, const char * attribute );
int Fieldml_CopyMarkupAttributeValue( FmlHandle handle, FmlObjectHandle objectHandle, const char * attribute, char *buffer, int bufferLength );


int Fieldml_SetMarkup(  FmlHandle handle, FmlObjectHandle objectHandle, const char * attribute, const char * value );

/*
     Returns the handle of the given domain's component ensemble.
 */
FmlObjectHandle Fieldml_GetDomainComponentEnsemble( FmlHandle handle, FmlObjectHandle objectHandle );


FmlObjectHandle Fieldml_CreateEnsembleDomain( FmlHandle handle, const char * name, FmlObjectHandle componentHandle );

FmlObjectHandle Fieldml_CreateContinuousDomain( FmlHandle handle, const char * name, FmlObjectHandle componentHandle );

FmlObjectHandle Fieldml_CreateMeshDomain( FmlHandle handle, const char * name, FmlObjectHandle xiEnsemble );


/*
     Returns the handle of the given mesh domain's xi domain.
 */
FmlObjectHandle Fieldml_GetMeshXiDomain( FmlHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the handle of the given mesh domain's element domain.
 */
FmlObjectHandle Fieldml_GetMeshElementDomain( FmlHandle handle, FmlObjectHandle objectHandle );


/*
     Returns a string describing the shape of the element in the given mesh.
 */
const char * Fieldml_GetMeshElementShape( FmlHandle handle, FmlObjectHandle objectHandle, int elementNumber );
int Fieldml_CopyMeshElementShape( FmlHandle handle, FmlObjectHandle objectHandle, int elementNumber, char *buffer, int bufferLength );

int Fieldml_SetMeshDefaultShape( FmlHandle handle, FmlObjectHandle mesh, const char * shape );

int Fieldml_SetMeshElementShape( FmlHandle handle, FmlObjectHandle mesh, int elementNumber, const char * shape );

/*
     Returns the number of connectivities specified for the given mesh domain.
 */
int Fieldml_GetMeshConnectivityCount( FmlHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the domain of the nth connectivity for the given mesh. 
 */
FmlObjectHandle Fieldml_GetMeshConnectivityDomain( FmlHandle handle, FmlObjectHandle objectHandle, int connectivityIndex );


/*
     Returns the source of the nth connectivity for the given mesh. 
 */
FmlObjectHandle Fieldml_GetMeshConnectivitySource( FmlHandle handle, FmlObjectHandle objectHandle, int connectivityIndex );


int Fieldml_SetMeshConnectivity( FmlHandle handle, FmlObjectHandle mesh, FmlObjectHandle pointDomain, FmlObjectHandle evaluator );

/*
    Returns the bounds-type of the given domain.
    
    NOTE: Currently, only ensemble domains have explicit bounds.
    NOTE: Currently, only discrete contiguous bounds are supported.
 */
DomainBoundsType Fieldml_GetDomainBoundsType( FmlHandle handle, FmlObjectHandle objectHandle );


int Fieldml_GetEnsembleDomainElementCount( FmlHandle handle, FmlObjectHandle objectHandle );


int Fieldml_GetEnsembleDomainElementNames( FmlHandle handle, FmlObjectHandle objectHandle, const int *array, int arrayLength );


int Fieldml_GetContiguousBoundsCount( FmlHandle handle, FmlObjectHandle objectHandle );

int Fieldml_SetContiguousBoundsCount( FmlHandle handle, FmlObjectHandle objectHandle, int count );


/*
     Returns the name of the given object.
 */
const char * Fieldml_GetObjectName( FmlHandle handle, FmlObjectHandle objectHandle );
int Fieldml_CopyObjectName( FmlHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


/*
     Returns the value domain of the given evaluator.
 */
FmlObjectHandle Fieldml_GetValueDomain( FmlHandle handle, FmlObjectHandle objectHandle );


FmlObjectHandle Fieldml_CreateEnsembleVariable( FmlHandle handle, const char *name, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_CreateContinuousVariable( FmlHandle handle, const char *name, FmlObjectHandle valueDomain );


FmlObjectHandle Fieldml_CreateEnsembleParameters( FmlHandle handle, const char *name, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_CreateContinuousParameters( FmlHandle handle, const char *name, FmlObjectHandle valueDomain );



int Fieldml_SetParameterDataDescription( FmlHandle handle, FmlObjectHandle objectHandle, DataDescriptionType description );

/*
    Returns the data description type of the given parameter evaluator.
 */
DataDescriptionType Fieldml_GetParameterDataDescription( FmlHandle handle, FmlObjectHandle objectHandle );


DataLocationType Fieldml_GetParameterDataLocation( FmlHandle handle, FmlObjectHandle objectHandle );

int Fieldml_SetParameterDataLocation( FmlHandle handle, FmlObjectHandle objectHandle, DataLocationType location );

int Fieldml_AddInlineParameterData( FmlHandle handle, FmlObjectHandle objectHandle, const char *data, int length );

int Fieldml_SetParameterFileData( FmlHandle handle, FmlObjectHandle objectHandle, const char * filename, DataFileType type, int offset );

const char *Fieldml_GetParameterDataFilename( FmlHandle handle, FmlObjectHandle objectHandle );


int Fieldml_CopyParameterDataFilename( FmlHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


int Fieldml_GetParameterDataOffset( FmlHandle handle, FmlObjectHandle objectHandle );


DataFileType Fieldml_GetParameterDataFileType( FmlHandle handle, FmlObjectHandle objectHandle );


int Fieldml_AddSemidenseIndex( FmlHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle indexHandle, int isSparse );

/*
     Returns the number of sparse or dense indexes of the semidense data store
     associated with the given parameter evaluator.
 */
int Fieldml_GetSemidenseIndexCount( FmlHandle handle, FmlObjectHandle objectHandle, int isSparse );


/*
     Returns the handle of the nth sparse or dense index of the semidense data
     store associated with the given parameter evaluator.
 */
FmlObjectHandle Fieldml_GetSemidenseIndex( FmlHandle handle, FmlObjectHandle objectHandle, int indexIndex, int isSparse );


int Fieldml_SetSwizzle( FmlHandle handle, FmlObjectHandle objectHandle, const int *buffer, int count );

int Fieldml_GetSwizzleCount( FmlHandle handle, FmlObjectHandle objectHandle );

const int *Fieldml_GetSwizzleData( FmlHandle handle, FmlObjectHandle objectHandle );

int Fieldml_CopySwizzleData( FmlHandle handle, FmlObjectHandle objectHandle, int *buffer, int bufferLength );


FmlObjectHandle Fieldml_CreateContinuousPiecewise( FmlHandle handle, const char * name, FmlObjectHandle indexHandle, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_CreateContinuousAggregate( FmlHandle handle, const char * name, FmlObjectHandle valueDomain );


int Fieldml_SetEvaluator( FmlHandle handle, FmlObjectHandle objectHandle, int element, FmlObjectHandle evaluator );

/*
    Returns the number of element->evaluator delegations for the given
    piecewise/aggregate evaluator.
 */
int Fieldml_GetEvaluatorCount( FmlHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the element number for the nth element->evaluator delegation in
    the given piecewise/aggregate evaluator.
 */
int Fieldml_GetEvaluatorElement( FmlHandle handle, FmlObjectHandle objectHandle, int evaluatorIndex );


/*
    Returns the evaluator handle for the nth element->evaluator delegation in
    the given piecewise/aggregate evaluator.
 */
FmlObjectHandle Fieldml_GetEvaluatorHandle( FmlHandle handle, FmlObjectHandle objectHandle, int evaluatorIndex );


FmlObjectHandle Fieldml_CreateContinuousImport( FmlHandle handle, const char * name, FmlObjectHandle remoteEvaluator, FmlObjectHandle valueDomain );

FmlObjectHandle Fieldml_GetImportRemoteEvaluator( FmlHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the number of aliases used by the given evaluator. 
 */
int Fieldml_GetAliasCount( FmlHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the local domain/evaulator used by the nth alias of the given evaluator. 
 */
FmlObjectHandle Fieldml_GetAliasLocalHandle( FmlHandle handle, FmlObjectHandle objectHandle, int aliasIndex );


/*
    Returns the remote domain used by the nth alias of the given evaluator. 
 */
FmlObjectHandle Fieldml_GetAliasRemoteHandle( FmlHandle handle, FmlObjectHandle objectHandle, int aliasIndex );

int Fieldml_SetAlias( FmlHandle handle, FmlObjectHandle objectHandle, FmlObjectHandle remoteDomain, FmlObjectHandle localSource );

/*
    Returns the number of indexes used by the given evaluator.
    
    NOTE: Only defined for piecewise and parameter evaluators.
    
    NOTE: For piecewise evalutors, this is always one. For parameter evaluators,
    it depends on the data store.
 */
int Fieldml_GetIndexCount( FmlHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the domain of the nth indexes used by the given evaluator.
    
    NOTE: Only defined for piecewise and parameter evaluators.
 */
FmlObjectHandle Fieldml_GetIndexDomain( FmlHandle handle, FmlObjectHandle objectHandle, int indexIndex );


FmlReaderHandle Fieldml_OpenReader( FmlHandle handle, FmlObjectHandle objectHandle );


int Fieldml_ReadIntSlice( FmlHandle handle, FmlReaderHandle reader, int *indexBuffer, int *valueBuffer );


int Fieldml_ReadDoubleSlice( FmlHandle handle, FmlReaderHandle reader, int *indexBuffer, double *valueBuffer );


int Fieldml_CloseReader( FmlHandle handle, FmlReaderHandle reader );


FmlWriterHandle Fieldml_OpenWriter( FmlHandle handle, FmlObjectHandle objectHandle, int append );


int Fieldml_WriteIntSlice( FmlHandle handle, FmlWriterHandle writer, int *indexBuffer, int *valueBuffer );


int Fieldml_WriteDoubleSlice( FmlHandle handle, FmlWriterHandle writer, int *indexBuffer, double *valueBuffer );


int Fieldml_CloseWriter( FmlHandle handle, FmlWriterHandle writer );

#endif // H_FIELDML_API
