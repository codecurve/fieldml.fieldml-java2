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
    BOUNDS_UNKNOWN,     		 // EnsembleDomain bounds not yet known.
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


/*
 * This is a subset of FieldmlHandleType from fieldml_structs.h, as some FieldmlHandleTypes 
 * are for internal use only.
 */ 
typedef enum _FieldmlObjectType
{
	FML_ENSEMBLE_DOMAIN,
	FML_CONTINUOUS_DOMAIN,
	FML_MESH_DOMAIN,
	FML_CONTINUOUS_PARAMETERS,
	FML_ENSEMBLE_PARAMETERS,
	FML_CONTINUOUS_IMPORT,
	FML_CONTINUOUS_AGGREGATE,
	FML_CONTINUOUS_PIECEWISE,
	FML_CONTINUOUS_VARIABLE,
	FML_ENSEMBLE_VARIABLE,
	FML_CONTINUOUS_DEREFERENCE,
}
FieldmlObjectType;


typedef int FmlParseHandle;

typedef int FmlObjectHandle;


/*
	 Parses the given file, and returns a handle to the parsed data. This
	 handle is then used for all subsequent API calls. 
 */
FmlParseHandle fmlParseFile( char *filename );


/*
 	Returns the number of objects of the given type, or zero if there are none.
 */
int fmlGetObjectCount( FmlParseHandle handle, FieldmlObjectType type );


/*
 	Returns a handle to the nth object of the given type.
 */
FmlObjectHandle fmlGetObjectHandle( FmlParseHandle handle, FieldmlObjectType type, int index );


/*
 	Returns the handle of the given domain's component ensemble.
 */
FmlObjectHandle fmlGetDomainComponentEnsemble( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
	Returns the bounds-type of the given domain.
	
	NOTE: Currently, only ensemble domains have explicit bounds.
	NOTE: Currently, only discrete contiguous bounds are supported.
 */
DomainBoundsType fmlGetDomainBoundsType( FmlParseHandle handle, FmlObjectHandle objectHandle );


/*
	 Returns the name of the given object.
 */
char *fmlGetObjectName( FmlParseHandle handle, FmlObjectHandle objectHandle );


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

#endif // H_FIELDML_PARSE
