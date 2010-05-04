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

typedef enum _DomainBoundsType
{
    BOUNDS_UNKNOWN,              // EnsembleDomain bounds not yet known.
    BOUNDS_DISCRETE_CONTIGUOUS,  // Contiguous bounds (i.e. 1 ... N)
    BOUNDS_DISCRETE_ARBITRARY,   // Arbitrary bounds (not yet supported)
}
DomainBoundsType;


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

    //These four are stand-in types used to allow forward-declaration during parsing.
    FHT_UNKNOWN_ENSEMBLE_DOMAIN,
    FHT_UNKNOWN_CONTINUOUS_DOMAIN,
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


/*
     Returns the handle of the given domain's component ensemble.
 */
FmlObjectHandle Fieldml_GetDomainComponentEnsemble( FmlParseHandle handle, FmlObjectHandle objectHandle );


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


/*
    Returns the bounds-type of the given domain.
    
    NOTE: Currently, only ensemble domains have explicit bounds.
    NOTE: Currently, only discrete contiguous bounds are supported.
 */
DomainBoundsType Fieldml_GetDomainBoundsType( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_GetEnsembleDomainElementCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


int Fieldml_GetEnsembleDomainElementNames( FmlParseHandle handle, FmlObjectHandle objectHandle, const int *array, int arrayLength );


int Fieldml_GetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the name of the given object.
 */
const char * Fieldml_GetObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle );
int Fieldml_CopyObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


/*
     Returns the value domain of the given evaluator.
 */
FmlObjectHandle Fieldml_GetValueDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the data description type of the given parameter evaluator.
 */
DataDescriptionType Fieldml_GetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle );


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


/*
     Returns the remote name of the given imported evaluator.
     
     NOTE: At some point, the name will be a URI-style string allowing the caller
     to identify the import's source fieldml region. 
 */
const char * Fieldml_GetImportRemoteName( FmlParseHandle handle, FmlObjectHandle objectHandle );
int Fieldml_CopyImportRemoteName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


/*
    Returns the number of aliases used by the given imported evaluator. 
 */
int Fieldml_GetImportAliasCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the local domain/evaulator used by the nth alias of the given imported evaluator. 
 */
FmlObjectHandle Fieldml_GetImportAliasLocalHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int aliasIndex );


/*
    Returns the remote domain used by the nth alias of the given imported evaluator. 
 */
FmlObjectHandle Fieldml_GetImportAliasRemoteHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int aliasIndex );


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

#endif // H_FIELDML_PARSE
