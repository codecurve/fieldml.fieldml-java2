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


typedef int FmlParseHandle;

typedef int FmlObjectHandle;


/*
     Parses the given file, and returns a handle to the parsed data. This
     handle is then used for all subsequent API calls. 
 */
FmlParseHandle fmlParseFile( char *filename );


/*
     Frees all resources associated with the given parse.
     
     HANDLE SHOULD NOT BE USED AFTER THIS CALL.
 */
void fmlDestroyParse( FmlParseHandle handle );


/*
     Returns the number of errors encountered by the given parse.
 */
int fmlGetErrorCount( FmlParseHandle handle );


/*
     Returns the nth error string for the given parse.
 */
char *fmlGetError( FmlParseHandle handle, int index );
int fmlCopyError( FmlParseHandle handle, int index, char *buffer, int bufferLength );


/*
     Returns the number of objects of the given type, or zero if there are none.
 */
int fmlGetObjectCount( FmlParseHandle handle, FieldmlHandleType type );


/*
     Returns a handle to the nth object of the given type.
 */
FmlObjectHandle fmlGetObjectHandle( FmlParseHandle handle, FieldmlHandleType type, int index );


/*
     Returns the number of markup entries (attribute/value pairs) for the given object.
 */
int fmlGetMarkupCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the attribute string of the nth markup entry for the given object.
 */
char *fmlGetMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );
int fmlCopyMarkupAttribute( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength );


/*
     Returns the value string of the nth markup entry for the given object.
 */
char *fmlGetMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );
int fmlCopyMarkupValue( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, char *buffer, int bufferLength );

/*
     Returns the handle of the given domain's component ensemble.
 */
FmlObjectHandle fmlGetDomainComponentEnsemble( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the handle of the given mesh domain's xi domain.
 */
FmlObjectHandle fmlGetMeshXiDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the handle of the given mesh domain's element domain.
 */
FmlObjectHandle fmlGetMeshElementDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns a string describing the shape of the element in the given mesh.
 */
char *fmlGetMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber );
int fmlCopyMeshElementShape( FmlParseHandle handle, FmlObjectHandle objectHandle, int elementNumber, char *buffer, int bufferLength );


/*
     Returns the number of connectivities specified for the given mesh domain.
 */
int fmlGetMeshConnectivityCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the domain of the nth connectivity for the given mesh. 
 */
FmlObjectHandle fmlGetMeshConnectivityDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );


/*
     Returns the source of the nth connectivity for the given mesh. 
 */
FmlObjectHandle fmlGetMeshConnectivitySource( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );


/*
    Returns the bounds-type of the given domain.
    
    NOTE: Currently, only ensemble domains have explicit bounds.
    NOTE: Currently, only discrete contiguous bounds are supported.
 */
DomainBoundsType fmlGetDomainBoundsType( FmlParseHandle handle, FmlObjectHandle objectHandle );


int fmlGetContiguousBoundsCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the name of the given object.
 */
char *fmlGetObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle );
int fmlCopyObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


/*
     Returns the value domain of the given evaluator.
 */
FmlObjectHandle fmlGetValueDomain( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the data description type of the given parameter evaluator.
 */
DataDescriptionType fmlGetParameterDataDescription( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
     Returns the number of sparse or dense indexes of the semidense data store
     associated with the given parameter evaluator.
 */
int fmlGetSemidenseIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle, int isSparse );


/*
     Returns the handle of the nth sparse or dense index of the semidense data
     store associated with the given parameter evaluator.
 */
FmlObjectHandle fmlGetSemidenseIndex( FmlParseHandle handle, FmlObjectHandle objectHandle, int index, int isSparse );


/*
    Returns the number of element->evaluator delegations for the given
    piecewise/aggregate evaluator.
 */
int fmlGetEvaluatorCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the element number for the nth element->evaluator delegation in
    the given piecewise/aggregate evaluator.
 */
int fmlGetEvaluatorElement( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );


/*
    Returns the evaluator handle for the nth element->evaluator delegation in
    the given piecewise/aggregate evaluator.
 */
FmlObjectHandle fmlGetEvaluatorHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );


/*
     Returns the remote name of the given imported evaluator.
     
     NOTE: At some point, the name will be a URI-style string allowing the caller
     to identify the import's source fieldml region. 
 */
char *fmlGetImportRemoteName( FmlParseHandle handle, FmlObjectHandle objectHandle );
int fmlCopyImportRemoteName( FmlParseHandle handle, FmlObjectHandle objectHandle, char *buffer, int bufferLength );


/*
    Returns the number of aliases used by the given imported evaluator. 
 */
int fmlGetImportAliasCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the local domain/evaulator used by the nth alias of the given imported evaluator. 
 */
FmlObjectHandle fmlGetImportAliasLocalHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );


/*
    Returns the remote domain used by the nth alias of the given imported evaluator. 
 */
FmlObjectHandle fmlGetImportAliasRemoteHandle( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );


/*
    Returns the number of indexes used by the given evaluator.
    
    NOTE: Only defined for piecewise and parameter evaluators.
    
    NOTE: For piecewise evalutors, this is always one. For parameter evaluators,
    it depends on the data store.
 */
int fmlGetIndexCount( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
    Returns the domain of the nth indexes used by the given evaluator.
    
    NOTE: Only defined for piecewise and parameter evaluators.
 */
FmlObjectHandle fmlGetIndexDomain( FmlParseHandle handle, FmlObjectHandle objectHandle, int index );

#endif // H_FIELDML_PARSE
