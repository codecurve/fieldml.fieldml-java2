package purgatory.fieldml;
//TODO: Can this API be subdivided up into sub-modules?  It is already too "monolithic", and is likely to grow.

/**
 * In some far-off future, these could all be JNI calls to a FieldML library written in C/C++.
 * Because the API is to be called from Fortran as well, only primitive types can be used as parameters.
 * 
 * Domains and field ids are strictly non-negative, error codes are strictly negative.
 * 
 * Components are zero-indexed.
 * 
 * Mapped fields take a single component from their first parameter, which must be a discrete-domain value,
 * and use that as an index to their list of values.
 * 
 * At the moment, the only other type of fields are computed fields, which are nominally unsupported by
 * the first release of the API. 
 */
public interface FieldML
{
    //Error codes
    public static final int NO_ERROR = 0;
    public static final int ERR_GENERIC_ERROR = -1;
    public static final int ERR_NO_SUCH_OBJECT = -2;
    public static final int ERR_WRONG_OBJECT_TYPE = -3;
    public static final int ERR_BAD_PARAMETER = -4;
    public static final int ERR_INVALID_CALL = -5;
    
    //Value types
    public static final int PT_PARAMETER = 0;
    public static final int PT_DIRECT_VALUE = 1;
    public static final int PT_INDIRECT_VALUE = 2;
    
    //Domain methods
    /**
     * Create a continuous domain with the given name, and return an error-code.
     * 
     * Continuous domains have components with optional upper and lower bounds.
     * Components on a continuous domain are 64-bit doubles.
     */
    public int FieldML_BeginContinuousDomain( String name );

    /**
     * Create a discrete domain with the given name, and return an error-code.
     * 
     * Discrete domains have components with a fixed set of possible integer values.
     * Components on a discrete domain are 32-bit integers.
     */
    public int FieldML_BeginDiscreteDomain( String name );
    
    /**
     * End the definition of the current domain, and return a domain id on success,
     * or an error code otherwise.
     */
    public int FieldML_EndDomain();

    /**
     * Return the domain id for the given domain (if it exists) or an error code otherwise.
     */
    public int FieldML_GetDomainId( String originalDomainName );
    
    /**
     * Copy the name of the given domain into the given buffer.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     */
    public int FieldML_GetDomainName( int domainId, char[] name );

    /**
     * Add a component of the given name to the current continuous domain.
     * The component's extrema are defined by the given min and max parameters.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_WRONG_OBJECT_TYPE if the domain id does not correspond to a continuous domain.
     * ERR_BAD_PARAMETER if a component of the given name already exists.
     * ERR_BAD_PARAMETER if either of the extrema values is NaN.
     */
    public int FieldML_AddContinuousDomainComponent( String componentName, double min, double max );

    /**
     * Add a component of the given name to the current discrete domain.
     * The component's values are taken from the first count entries in the values array.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_WRONG_OBJECT_TYPE if the domain id does not correspond to a discrete domain.
     * ERR_BAD_PARAMETER if a component of the given name already exists.
     * ERR_BAD_PARAMETER if the count parameter is less than one.
     * 
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_AddDiscreteDomainComponent( String componentName, int[] values, int count );
    
    /**
     * Return the number of components that the given domain has. This number can be zero
     * if the domain has not yet been given any components.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     */
    public int FieldML_GetDomainComponentCount( int domainId );
    
    /**
     * Copy the name of the given domain component into the given buffer.
     * The index must non-negative, and less than the given domain's component count.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_BAD_PARAMETER if there is no domain component with the given index.
     * 
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_GetDomainComponentName( int domainId, int componentIndex, char[] name );
    
    /**
     * Return the number of discrete values that the given domain component has.
     * The return value will be at least one.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_WRONG_OBJECT_TYPE if the domain id does not correspond to a discrete domain.
     * ERR_BAD_PARAMETER if there is no domain component with the given index.
     * 
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_GetDiscreteDomainComponentValueCount( int domainId, int componentIndex );
    
    /**
     * Copies the discrete values of the given discrete domain component into
     * the given array, and return the number of values copied.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_WRONG_OBJECT_TYPE if the domain id does not correspond to a discrete domain.
     * ERR_BAD_PARAMETER if there is no domain component with the given index.
     * 
     * @see FieldML_GetDomainComponentCount
     * @see FieldML_GetDiscreteDomainComponentValueCount
     */
    public int FieldML_GetDiscreteDomainComponentValues( int domainId, int componentIndex, int[] values );
    
    /**
     * Copies the extrema values of the given continuous domain component into
     * the given array. The array should be large enough to hold two values.
     * 
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_WRONG_OBJECT_TYPE if the domain id does not correspond to a discrete domain.
     * ERR_BAD_PARAMETER if there is no domain component with the given index.
     * 
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_GetContinuousDomainComponentExtrema( int domainId, int componentIndex, double[] values );
    
    //Ordinary fields
    /**
     * Create a derived field with the given name and a value on the given
     * domain, and return an error-code.
     *
     * ERR_BAD_PARAMETER if there is no domain with the given id.
     */
    public int FieldML_BeginDerivedField( String name, int valueDomainId );
    
    /**
     * End the definition of the current field, and return a field id on success,
     * or an error code otherwise.
     */
    public int FieldML_EndField();

    /**
     * Return the field id for the given field (if it exists) or an error code otherwise.
     */
    public int FieldML_GetFieldId( String fieldName );

    /**
     * Copy the name of the given field into the given buffer.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     */
    public int FieldML_GetFieldName( int fieldId, char[] name );
    
    /**
     * Define a parameter of the given type and name to the current field.
     * Parameters must be passed in when evaluating a field, and are
     * analogous to function arguments.
     * Further parameters cannot be added once any field values
     * have been defined.
     * 
     * Returns the index of the new parameter.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a computed domain.
     * ERR_BAD_PARAMETER if there is already a parameter with the given name.
     */
    public int FieldML_AddParameter( String parameterName, int domainId );
    
    /**
     * Add a field value of the given name to the current field.
     * Like parameters, field values are used for evaluating other
     * field values, and final component values. However, field
     * values are obtained by evaluating a field. The arguments are passed
     * to the evaluated field as a set of indexes into this field's values
     * (both field values and parameters).
     * They are analogous to local variables.
     * 
     * The domain of the field value is equal to the domain of the field
     * used to provide it.
     * 
     * Further parameters cannot be added once any field values
     * have been defined.
     * 
     * Returns the index of the new parameter.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a computed domain.
     * ERR_BAD_PARAMETER if there is already a parameter with the given name.
     * 
     * @see FieldML_GetParameterCount
     */
    public int FieldML_AddFieldValue( String parameterName, int parameterFieldId, int[] argumentIndexes );
    public int FieldML_AddIndirectFieldValue( String parameterName, int fieldParameterIndex, int fieldParameterComponentIndex, int[] argumentIndexes );

    /**
     * Defines the component with the given index or the current field. 
     */
    public int FieldML_DefineComponent( int componentIndex, int valueIndex, int valueComponentIndex ); 
    public int FieldML_DefineNamedComponent( int componentIndex, int valueIndex, int nameValueIndex, int nameValueComponentIndex ); 
        
    /**
     * Returns the total number of values used by this field, including
     * parameters and field values. This number will be no smaller than the
     * number of parameters.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     */
    public int FieldML_GetValueCount( int fieldId );
    
    
    /**
     * Returns the type of the given parameter (if it exists), one of:
     * 
     * PT_PARAMETER
     * PT_DIRECT_VALUE
     * PT_INDIRECT_VALUE
     * 
     * ERR_BAD_PARAMETER if the indexed parameter does not exist.
     */
    public int FieldML_GetValueType( int fieldId, int parameterIndex );
    
    /**
     * Returns the domain id of the given field's value.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     */
    public int FieldML_GetValueDomain( int fieldId );
    
    /**
     * Returns the field id used to evaluate the given field value.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a computed domain.
     * ERR_BAD_PARAMETER if the indexed parameter is not a field value.
     * 
     * @see FieldML_GetValueCount
     */
    public int FieldML_GetFieldValueField( int fieldId, int valueIndex );
    
    /**
     * Returns the argument indexes used to evaluate the given field
     * value. The array's length should be at least enough to hold a
     * number of integers corresponding to the number of parameters
     * required by the field value's evaluating field.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a computed domain.
     * ERR_BAD_PARAMETER if the indexed parameter is not a field value.
     * 
     * @see FieldML_GetValueCount
     * @see FieldML_GetParameterCount
     */
    public int FieldML_GetFieldValueArguments( int fieldId, int fieldValueIndex, int[] argumentIndexes );
    
    //Mapped fields
    /**
     * Create a mapped field with the given name and a value on the given
     * domain, and return an error-code.
     * 
     * Mapped fields only take one parameter, have no field values,
     * and evaluate based on a single component of its parameter, which
     * must be discrete. As such, mapped fields can be thought of as having a
     * single, index-valued parameter.
     *
     * ERR_BAD_PARAMETER if there is no domain with the given id.
     */
    public int FieldML_BeginMappedField( String name, int valueDomainId );

    /**
     * Set the domain of the field's parameter, and which component of the
     * parameter to use when evaluating. This can only be done once per
     * mapped field.
     *
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_NO_SUCH_OBJECT if there is no domain with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     * ERR_WRONG_OBJECT_TYPE if the domain id does not correspond to a discrete domain.
     * ERR_BAD_PARAMETER if the componentIndex is invalid.
     * 
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_SetMappingParameter( int domainId, int componentIndex );

    /**
     * Return the domain id of this mapped field's parameter.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     */
    public int FieldML_GetMappingParameterDomain( int fieldId );
    
    /**
     * Return the component index of this mapped field's parameter.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     */
    public int FieldML_GetMappingParameterComponentIndex( int fieldId );
    
    /**
     * Assign values for the current index-valued mapped field to return for the
     * given parameter value. The given array must be hold at least a
     * number of integers equal to the number of components of this field's
     * value domain.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a discrete-valued field.
     * 
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_AssignDiscreteComponentValues( int parameterValue, int[] componentValues );
    
    /**
     * Assign values for the current real-valued mapped field to return for the
     * given parameter value. The given array must be hold at least a
     * number of doubles equal to the number of components of this field's
     * value domain.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a real-valued field.
     * 
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_AssignContinuousComponentValues( int parameterValue, double[] componentValues );
    
    /**
     * Return the values that the given index-valued mapped field returns for the
     * given parameter value. The given array must be able to hold a
     * number of integers equal to the number of components of this field's
     * value domain.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a discrete-valued field.
     * ERR_BAD_PARAMETER if the parameter value is not mapped.
     * 
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_GetDiscreteComponentValues( int fieldId, int parameterValue, int[] componentValues );
    
    /**
     * Return the values that the given real-valued mapped field returns for the
     * given parameter value. The given array must be able to hold a
     * number of doubles equal to the number of components of this field's
     * value domain.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a mapped domain.
     * ERR_WRONG_OBJECT_TYPE if the field id does not correspond to a discrete-valued field.
     * ERR_BAD_PARAMETER if the parameter value is not mapped.
     * 
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_GetContinuousComponentValues( int fieldId, int parameterValue, double[] componentValues );

    //Field methods
    /**
     * Return the total number of parameters the field requires.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     */
    public int FieldML_GetParameterCount( int fieldId );
    
    /**
     * Return the domain id for the given parameter.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_BAD_PARAMETER if the parameter index is invalid.
     * 
     * @see FieldML_GetValueCount
     */
    public int FieldML_GetValueDomain( int fieldId, int parameterIndex );
    
    /**
     * Return the domain ids for all parameters.
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_BAD_PARAMETER if the parameter index is invalid.
     * 
     * @see FieldML_GetParameterCount
     * @see FieldML_CreateCache
     */
    public int FieldML_GetParameterDomains( int fieldId, int[] domainIds );
    
    /**
     * Return the name of the given value (parameter or field value).
     * 
     * ERR_NO_SUCH_OBJECT if there is no field with the given id.
     * ERR_BAD_PARAMETER if the parameter index is invalid.
     * 
     * @see FieldML_GetValueCount
     */
    public int FieldML_GetValueName( int fieldId, int parameterIndex, char[] name );
    
    /**
     * Create a value-cache with the given domain ids. A value-cache holds a
     * set number of values from a set list of domains. Fields are evaluated
     * by using a cache to obtain its arguments. Returns a cache id.
     * Caches are analogous to varargs.
     * 
     * ERR_NO_SUCH_OBJECT if any of the given domain ids are invalid.
     * 
     * @see FieldML_GetParameterDomains
     */
    public int FieldML_CreateCache( int[] domainIds, int parameterCount );

    /**
     * Destroys the given cache, releasing the corresponding allocated
     * memory.
     * 
     * ERR_NO_SUCH_OBJECT if cache id is invalid.
     */
    public int FieldML_DestroyCache( int cacheId );
    
    /**
     * Assign values to the given parameter number. The parameter's domain must
     * be a continuous domain.
     * 
     * ERR_NO_SUCH_OBJECT if cache id is invalid.
     * ERR_WRONG_OBJECT_TYPE if the given parameter is not real-valued.
     * ERR_BAD_PARAMETER if the values array does not contain enough values.
     * 
     * @see FieldML_GetParameterCount
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_SetContinousCacheValues( int cacheId, int parameterNumber, double[] values );
    
    /**
     * Assign values to the given parameter number. The parameter's domain must
     * be a discrete domain.
     * 
     * ERR_NO_SUCH_OBJECT if cache id is invalid.
     * ERR_WRONG_OBJECT_TYPE if the given parameter is not index-valued.
     * ERR_BAD_PARAMETER if the values array does not contain enough values.
     * 
     * @see FieldML_GetParameterCount
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_SetDiscreteCacheValues( int cacheId, int parameterNumber, int[] values ); 
    
    //Field-evaluation methods
    /**
     * Evaluate the given index-value field using the given cache to obtain its
     * arguments. 
     * 
     * ERR_NO_SUCH_OBJECT if field id is invalid.
     * ERR_NO_SUCH_OBJECT if cache id is invalid.
     * ERR_WRONG_OBJECT_TYPE if the given field is not index-valued.
     * ERR_BAD_PARAMETER if the values array is not large enough.
     * ERR_BAD_PARAMETER if the given cache has the wrong domains.
     * 
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_EvaluateDiscreteField( int fieldId, int cacheId, int[] values );
    
    /**
     * Evaluate the given real-valued field using the given cache to obtain its
     * arguments. 
     * 
     * ERR_NO_SUCH_OBJECT if field id is invalid.
     * ERR_NO_SUCH_OBJECT if cache id is invalid.
     * ERR_WRONG_OBJECT_TYPE if the given field is not real-valued.
     * ERR_BAD_PARAMETER if the values array is not large enough.
     * ERR_BAD_PARAMETER if the given cache has the wrong domains.
     * 
     * @see FieldML_GetValueDomain
     * @see FieldML_GetDomainComponentCount
     */
    public int FieldML_EvaluateContinuousField( int fieldId, int cacheId, double[] values );
}
