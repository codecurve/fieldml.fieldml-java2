package purgatory.junit;

import purgatory.fieldml.FieldML;
import purgatory.fieldml.util.general.StringUtils;

public class FieldMLJavaFieldTest
    extends FieldMLTest
{
    private int discreteDomainId;

    private int continuousDomainId;


    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        discreteDomainId = helperCreateDiscreteDomain( testDomainName1, testComponentName1, discrete1 );
        assertTrue( discreteDomainId > 0 );

        continuousDomainId = helperCreateContinuousDomain( testDomainName2, testComponentName1, low1, high1 );
        assertTrue( discreteDomainId > 0 );
    }


    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }


    public void testFieldML_CreateField()
    {
        // Create a field on a continuous domain.
        int fieldId1 = helperCreateField( testFieldName1, continuousDomainId );
        assertTrue( fieldId1 > 0 );

        // Create a field with the same name as an existing field.
        int err = helperCreateField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Create a field on a discrete domain.
        int fieldId2 = helperCreateField( testFieldName2, discreteDomainId );
        assertTrue( fieldId2 > 0 );

        int fieldId3 = fieldml.FieldML_GetFieldId( testFieldName1 );
        assertEquals( fieldId1, fieldId3 );

        int domainId = fieldml.FieldML_GetValueDomain( fieldId1 );
        assertEquals( domainId, continuousDomainId );
    }


    public void testFieldML_AddParameter()
    {
        // Create a field on a continuous domain.
        int err = fieldml.FieldML_BeginDerivedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );

        // Add an input parameter.
        int index = fieldml.FieldML_AddParameter( testParameterName1, discreteDomainId );
        assertEquals( 0, index );

        // Add an input parameter with an unknown domain.
        err = fieldml.FieldML_AddParameter( testParameterName2, discreteDomainId + 1000 );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );

        // Add an input parameter with a duplicate name.
        err = fieldml.FieldML_AddParameter( testParameterName1, discreteDomainId );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Add a second input parameter.
        int index2 = fieldml.FieldML_AddParameter( testParameterName2, discreteDomainId );
        assertEquals( 1, index2 );
        
        int fieldId = fieldml.FieldML_EndField();
        assertTrue( fieldId > 0 );
    }


    public void testFieldML_AddFieldValue()
    {
        // Create a field on a discrete domain.
        int err = fieldml.FieldML_BeginDerivedField( testFieldName2, discreteDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_AddParameter( testParameterName1, continuousDomainId );
        assertTrue( err >= 0 );
        err = fieldml.FieldML_AddParameter( testParameterName2, continuousDomainId );
        assertTrue( err >= 0 );
        int fieldId2 = fieldml.FieldML_EndField();
        assertTrue( fieldId2 > 0 );

        // Create a field on a continuous domain.
        err = fieldml.FieldML_BeginDerivedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        int index1 = fieldml.FieldML_AddParameter( testParameterName1, discreteDomainId );
        assertTrue( index1 >= 0 );
        int index2 = fieldml.FieldML_AddParameter( testParameterName2, continuousDomainId );
        assertTrue( index2 >= 0 );

        int[] indexes = new int[64];

        // Add a derived parameter with mismatched parameter domains for the
        // field.
        indexes[0] = 0;
        indexes[1] = 0;
        err = fieldml.FieldML_AddFieldValue( testParameterName2, fieldId2, indexes );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Add a derived parameter with invalid indexes.
        indexes[0] = 0;
        indexes[1] = 5;
        err = fieldml.FieldML_AddFieldValue( testParameterName2, fieldId2, indexes );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Add a derived parameter with an invalid field id.
        indexes[0] = 1;
        indexes[1] = 1;
        err = fieldml.FieldML_AddFieldValue( testParameterName2, fieldId2 + 1000, indexes );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );

        // Add a derived parameter with a duplicate name.
        indexes[0] = 1;
        indexes[1] = 1;
        err = fieldml.FieldML_AddFieldValue( testParameterName1, fieldId2, indexes );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Add a derived parameter with correct parameters for the field.
        indexes[0] = 1;
        indexes[1] = 1;
        err = fieldml.FieldML_AddFieldValue( testParameterName3, fieldId2, indexes );
        assertEquals( 2, err );

        int fieldId1 = fieldml.FieldML_EndField();
        assertTrue( fieldId1 > 0 );
    }


    public void testFieldML_GetFieldValue()
    {
        // Create a second field on a discrete domain.
        int err = fieldml.FieldML_BeginDerivedField( testFieldName2, discreteDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_AddParameter( testParameterName1, continuousDomainId );
        assertTrue( err >= 0 );
        err = fieldml.FieldML_AddParameter( testParameterName2, continuousDomainId );
        assertTrue( err >= 0 );
        int fieldId2 = fieldml.FieldML_EndField();
        assertTrue( fieldId2 > 0 );

        // Create a field on a continuous domain with derived fields.
        err = fieldml.FieldML_BeginDerivedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        int index1 = fieldml.FieldML_AddParameter( testParameterName1, continuousDomainId );
        assertTrue( index1 >= 0 );

        int[] indexes = new int[64];

        // Add two derived parameters.
        indexes[0] = index1;
        indexes[1] = index1;
        int index2 = fieldml.FieldML_AddFieldValue( testParameterName2, fieldId2, indexes );
        assertTrue( index2 >= 0 );

        int fieldId1 = fieldml.FieldML_EndField();
        assertTrue( fieldId1 > 0 );

        // Get the field for an input parameter.
        err = fieldml.FieldML_GetFieldValueField( fieldId1, index1 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Get the field for a parameter of a non-existant field.
        err = fieldml.FieldML_GetFieldValueField( fieldId1 + 1000, index2 );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );

        // Get the field for a non-existant parameter.
        err = fieldml.FieldML_GetFieldValueField( fieldId1, index1 + 1000 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Get the field for a derived parameter.
        int id = fieldml.FieldML_GetFieldValueField( fieldId1, index2 );
        assertEquals( fieldId2, id );

        // Get the indexes for an input parameter.
        err = fieldml.FieldML_GetFieldValueArguments( fieldId1, index1, indexes );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Get the indexes for a parameter of a non-existant field.
        err = fieldml.FieldML_GetFieldValueArguments( fieldId1 + 1000, index2, indexes );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );

        // Get the indexes for a non-existant parameter.
        err = fieldml.FieldML_GetFieldValueArguments( fieldId1, index1 + 1000, indexes );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Get the indexes for a derived parameter.
        indexes[0] = index1 + 255;
        indexes[1] = index1 + 255;
        err = fieldml.FieldML_GetFieldValueArguments( fieldId1, index2, indexes );
        assertEquals( 2, err );
        assertEquals( index1, indexes[0] );
        assertEquals( index1, indexes[1] );
    }


    public void testFieldML_CreateMappedField()
    {
        // Create a mapped field on a continuous domain.
        int err = fieldml.FieldML_BeginMappedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        int fieldId1 = fieldml.FieldML_EndField();
        assertTrue( fieldId1 > 0 );

        // Create a mapped field with the same name as an existing field.
        err = fieldml.FieldML_BeginMappedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Create a mapped field on a discrete domain.
        err = fieldml.FieldML_BeginMappedField( testFieldName2, discreteDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        int fieldId2 = fieldml.FieldML_EndField();
        assertTrue( fieldId2 > 0 );

        int fieldId3 = fieldml.FieldML_GetFieldId( testFieldName1 );
        assertEquals( fieldId1, fieldId3 );
    }


    public void testFieldML_SetMappingParameter()
    {
        // Create a mapped field on a continuous domain.
        int err = fieldml.FieldML_BeginMappedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );

        // Set a mapping parameter with a continuous domain.
        err = fieldml.FieldML_SetMappingParameter( continuousDomainId, 0 );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Set a mapping parameter with an invalid component index.
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 1000 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Set a mapping parameter correctly.
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 0 );
        assertEquals( FieldML.NO_ERROR, err );
        
        int fieldId1 = fieldml.FieldML_EndField();
        assertTrue( fieldId1 > 0 );

        // Set a mapping parameter for a non-mapped field.
        err = fieldml.FieldML_BeginDerivedField( testFieldName2, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 0 );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );
        int fieldId2 = fieldml.FieldML_EndField();
        assertTrue( fieldId2 > 0 );

        // Test the relevant getters.
        err = fieldml.FieldML_GetMappingParameterComponentIndex( fieldId2 );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        err = fieldml.FieldML_GetMappingParameterComponentIndex( fieldId1 );
        assertEquals( 0, err );

        err = fieldml.FieldML_GetMappingParameterDomain( fieldId2 );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        err = fieldml.FieldML_GetMappingParameterDomain( fieldId1 );
        assertEquals( discreteDomainId, err );
    }


    public void testFieldML_AssignComponentValues()
    {
        int[] iValues =
        { 2, 4, 6, 8 };
        double[] dValues =
        { 1.5, 2.5, 3.5 };

        // Create a mapped field on a continuous domain.
        int err = fieldml.FieldML_BeginMappedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 0 );
        assertEquals( FieldML.NO_ERROR, err );

        // Assign index values to a real-valued field.
        err = fieldml.FieldML_AssignDiscreteComponentValues( 1, iValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Assign real values.
        err = fieldml.FieldML_AssignContinuousComponentValues( 1, dValues );
        assertEquals( FieldML.NO_ERROR, err );

        int fieldId1 = fieldml.FieldML_EndField();
        assertTrue( fieldId1 > 0 );

        // Create a mapped field on a discrete domain.
        err = fieldml.FieldML_BeginMappedField( testFieldName2, discreteDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 0 );
        assertEquals( FieldML.NO_ERROR, err );

        // Assign real values to an index-valued field.
        err = fieldml.FieldML_AssignContinuousComponentValues( 1, dValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Assign index values.
        err = fieldml.FieldML_AssignDiscreteComponentValues( 1, iValues );
        assertEquals( FieldML.NO_ERROR, err );

        int fieldId2 = fieldml.FieldML_EndField();
        assertTrue( fieldId2 > 0 );

        // Create a non-mapped field.
        err = fieldml.FieldML_BeginDerivedField( testFieldName3, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );

        // Assign index values to a non-mapped field.
        err = fieldml.FieldML_AssignDiscreteComponentValues( 1, iValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Assign real values to a non-mapped field.
        err = fieldml.FieldML_AssignContinuousComponentValues( 1, dValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        int fieldId3 = fieldml.FieldML_EndField();
        assertTrue( fieldId3 > 0 );
    }


    public void testFieldML_GetComponentValues()
    {
        int[] iValues =
        { 2, 4, 6, 8 };
        double[] dValues =
        { 1.5, 2.5, 3.5 };

        int[] testIValues = new int[10];
        double[] testDValues = new double[10];

        // Create a mapped field on a continuous domain.
        int err = fieldml.FieldML_BeginMappedField( testFieldName1, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 0 );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_AssignContinuousComponentValues( 1, dValues );
        assertEquals( FieldML.NO_ERROR, err );
        int fieldId1 = fieldml.FieldML_EndField();
        assertTrue( fieldId1 > 0 );

        // Create a mapped field on a discrete domain.
        err = fieldml.FieldML_BeginMappedField( testFieldName2, discreteDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_SetMappingParameter( discreteDomainId, 0 );
        assertEquals( FieldML.NO_ERROR, err );
        err = fieldml.FieldML_AssignDiscreteComponentValues( 1, iValues );
        assertEquals( FieldML.NO_ERROR, err );
        int fieldId2 = fieldml.FieldML_EndField();
        assertTrue( fieldId2 > 0 );

        // Create a non-mapped field.
        err = fieldml.FieldML_BeginDerivedField( testFieldName3, continuousDomainId );
        assertEquals( FieldML.NO_ERROR, err );
        int fieldId3 = fieldml.FieldML_EndField();
        assertTrue( fieldId3 > 0 );

        // Get index values from a real-valued field.
        err = fieldml.FieldML_GetDiscreteComponentValues( fieldId1, 1, testIValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Get index values from a non-existant field.
        err = fieldml.FieldML_GetDiscreteComponentValues( fieldId1 + 1000, 1, testIValues );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );

        // Get index values from a non-mapped field.
        err = fieldml.FieldML_GetDiscreteComponentValues( fieldId3, 1, testIValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Get real values from an index-valued field.
        err = fieldml.FieldML_GetContinuousComponentValues( fieldId2, 1, testDValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Get real values from a non-existant field.
        err = fieldml.FieldML_GetContinuousComponentValues( fieldId1 + 1000, 1, testDValues );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );

        // Get real values from a non-mapped field.
        err = fieldml.FieldML_GetContinuousComponentValues( fieldId3, 1, testDValues );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Get real values.
        err = fieldml.FieldML_GetContinuousComponentValues( fieldId1, 1, testDValues );
        assertEquals( FieldML.NO_ERROR, err );
        for( int i = 0; i < fieldml.FieldML_GetDomainComponentCount( continuousDomainId ); i++ )
        {
            assertEquals( dValues[i], testDValues[i] );
        }

        // Get index values.
        err = fieldml.FieldML_GetDiscreteComponentValues( fieldId2, 1, testIValues );
        assertEquals( FieldML.NO_ERROR, err );
        for( int i = 0; i < fieldml.FieldML_GetDomainComponentCount( discreteDomainId ); i++ )
        {
            assertEquals( iValues[i], testIValues[i] );
        }
    }


    public void testFieldML_GetDomainName()
    {
        // TODO: Tests for fieldNames, parameterNames and DomainComponentNames
        int length = 5;
        char[] name = new char[length];
        fieldml.FieldML_GetDomainName( discreteDomainId, name );
        int expectedLength = Math.min( name.length, testDomainName1.length() );
        int actualLength = getLengthTillZeroChar( name );
        assertEquals( expectedLength, actualLength );
        String actualStringTillZeroChar = new String( name ).substring( 0, actualLength );
        assertEquals( testDomainName1.substring( 0, expectedLength ), actualStringTillZeroChar );

        length = 50;
        name = new char[length];
        StringUtils.stringToChars( name, "123456789a123456789b123456789c123456789d" );
        fieldml.FieldML_GetDomainName( discreteDomainId, name );
        expectedLength = Math.min( name.length, testDomainName1.length() );
        actualLength = getLengthTillZeroChar( name );
        assertEquals( expectedLength, actualLength );
        actualStringTillZeroChar = new String( name ).substring( 0, actualLength );
        assertEquals( testDomainName1.substring( 0, expectedLength ), actualStringTillZeroChar );
    }


    private int getLengthTillZeroChar( char[] name )
    {
        String actualStringPadded = new String( name );
        int actualLength = actualStringPadded.indexOf( 0 );
        if( actualLength < 0 )
        {
            actualLength = actualStringPadded.length();
        }
        return actualLength;
    }
}
