package purgatory.junit;

import purgatory.fieldml.FieldML;

public class FieldMLJavaDomainTest
    extends FieldMLTest
{
    public void testFieldML_CreateContinuousDomain()
    {
        // Create a continuous domain.
        int id = helperCreateContinuousDomain( testDomainName1, testComponentName1, low1, high1 );
        assertTrue( id > 0 );
        // assertEquals( "test name", fieldml.FieldML_GetDomainName( id ) );

        // Create a continous domain with the same name as an existing domain.
        int secondId = helperCreateContinuousDomain( testDomainName1, testComponentName1, low1, high1 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, secondId );

        // Create a discrete domain with the same name as an existing domain.
        int thirdId = helperCreateDiscreteDomain( testDomainName1, testComponentName1, discrete1 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, thirdId );

        // Create a second continuous domain.
        int fourthId = helperCreateContinuousDomain( testDomainName2, testComponentName2, low1, high1 );
        assertTrue( fourthId > 0 );

        int domainId2 = fieldml.FieldML_GetDomainId( testDomainName1 );
        assertEquals( id, domainId2 );
    }


    public void testFieldML_AddContinousDomainComponent()
    {
        // Create a continuous domain.
        int err = fieldml.FieldML_BeginContinuousDomain( testDomainName1 );
        assertEquals( FieldML.NO_ERROR, err );

        // Add a new component.
        int index1 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName1, low1, high1 );
        assertEquals( 0, index1 );

        // Add a new component with the same name as the first component.
        int index2 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName1, low1, high1 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, index2 );

        // Add a new component with a different name from the first component.
        int index3 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName2, low2, high2 );
        assertEquals( 1, index3 );

        int domainId1 = fieldml.FieldML_EndDomain();
        assertTrue( domainId1 > 0 );

        // Add a continous component to a discrete domain.
        err = fieldml.FieldML_BeginDiscreteDomain( testDomainName2 );
        assertEquals( FieldML.NO_ERROR, err );

        err = fieldml.FieldML_AddContinuousDomainComponent( testComponentName1, low1, high1 );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        int domainId2 = fieldml.FieldML_EndDomain();
        assertTrue( domainId2 > 0 );
    }


    public void testFieldML_GetContinuousDomainComponentExtrema()
    {
        // Create a continuous domain.
        int err = fieldml.FieldML_BeginContinuousDomain( testDomainName1 );
        assertEquals( FieldML.NO_ERROR, err );

        // Add a new component.
        int index1 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName1, low1, high1 );
        assertEquals( 0, index1 );

        // Add a new component, with a different range.
        int index2 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName2, low2, high2 );
        assertEquals( 1, index2 );

        int domainId1 = fieldml.FieldML_EndDomain();
        assertTrue( domainId1 > 0 );

        double[] values = new double[2];

        // Get the range for the first component.
        err = fieldml.FieldML_GetContinuousDomainComponentExtrema( domainId1, index1, values );
        assertEquals( FieldML.NO_ERROR, err );
        assertEquals( low1, values[0] );
        assertEquals( high1, values[1] );

        err = fieldml.FieldML_GetContinuousDomainComponentExtrema( domainId1, index1 + 100, values );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Get the range for the second component.
        err = fieldml.FieldML_GetContinuousDomainComponentExtrema( domainId1, index2, values );
        assertEquals( FieldML.NO_ERROR, err );
        assertEquals( low2, values[0] );
        assertEquals( high2, values[1] );

        // Get the range for a discrete domain component.
        int domainId2 = helperCreateDiscreteDomain( testDomainName2, testComponentName1, discrete1 );
        assertTrue( domainId2 > 0 );

        err = fieldml.FieldML_GetContinuousDomainComponentExtrema( domainId2, 0, values );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );

        // Get the extrema for a non-existant domain.
        err = fieldml.FieldML_GetContinuousDomainComponentExtrema( domainId2 + 1000, index1, values );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );
    }


    public void testFieldML_CreateDiscreteDomain()
    {
        // Create a discrete domain.
        int id = helperCreateDiscreteDomain( testDomainName1, testComponentName1, discrete1 );
        assertTrue( id > 0 );
        // assertEquals( "test name", fieldml.FieldML_GetDomainName( id ) );

        // Create a discrete domain with the same name as an existing domain.
        int secondId = helperCreateDiscreteDomain( testDomainName1, testComponentName2, discrete2 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, secondId );

        // Create a continuous domain with the same name as an existing domain.
        int thirdId = helperCreateContinuousDomain( testDomainName1, testComponentName1, low2, high2 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, thirdId );

        // Create a second discrete domain.
        int fourthId = helperCreateDiscreteDomain( testDomainName2, testComponentName2, discrete2 );
        assertTrue( fourthId > 0 );

        // Get the domain id for an existant domain.
        int domainId2 = fieldml.FieldML_GetDomainId( testDomainName1 );
        assertEquals( id, domainId2 );

        // Get the domain id for an existant domain.
        int domainId3 = fieldml.FieldML_GetDomainId( testDomainName2 );
        assertEquals( fourthId, domainId3 );
    }


    public void testFieldML_AddDiscreteDomainComponent()
    {
        // Create a continuous domain.
        int err = fieldml.FieldML_BeginDiscreteDomain( testDomainName1 );
        assertEquals( FieldML.NO_ERROR, err );

        // Add a new component.
        int index1 = fieldml.FieldML_AddDiscreteDomainComponent( testComponentName1, discrete1, discrete1.length );
        assertEquals( 0, index1 );

        // Add a new component with the same name as the first component.
        int index2 = fieldml.FieldML_AddDiscreteDomainComponent( testComponentName1, discrete1, discrete1.length );
        assertEquals( FieldML.ERR_BAD_PARAMETER, index2 );

        // Add a new component with a different name from the first component.
        int index3 = fieldml.FieldML_AddDiscreteDomainComponent( testComponentName2, discrete2, discrete2.length );
        assertEquals( 1, index3 );
        
        int domainId1 = fieldml.FieldML_EndDomain();
        assertTrue( domainId1 > 0 );

        // Add a discrete component to a continuous domain.
        err = fieldml.FieldML_BeginContinuousDomain( testDomainName2 );
        assertEquals( FieldML.NO_ERROR, err );

        err = fieldml.FieldML_AddDiscreteDomainComponent( testComponentName1, discrete1, discrete1.length );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, err );
        
        fieldml.FieldML_EndDomain();
    }


    public void testFieldML_GetDiscreteDomainComponentValues()
    {
        // Create a discrete domain.
        int err = fieldml.FieldML_BeginDiscreteDomain( testDomainName1 );
        assertEquals( FieldML.NO_ERROR, err );

        // Add a new component.
        int index1 = fieldml.FieldML_AddDiscreteDomainComponent( testComponentName1, discrete1, discrete1.length );
        assertEquals( 0, index1 );

        // Add a new component, with a different range.
        int index2 = fieldml.FieldML_AddDiscreteDomainComponent( testComponentName2, discrete2, discrete2.length );
        assertEquals( 1, index2 );
        
        int domainId1 = fieldml.FieldML_EndDomain();
        assertTrue( domainId1 > 0 );

        int[] values = new int[1024];

        // Get the values for the first component.
        int count = fieldml.FieldML_GetDiscreteDomainComponentValueCount( domainId1, index1 );
        assertEquals( discrete1.length, count );
        count = fieldml.FieldML_GetDiscreteDomainComponentValues( domainId1, index1, values );
        assertEquals( discrete1.length, count );
        for( int i = 0; i < count; i++ )
        {
            assertEquals( discrete1[i], values[i] );
        }

        // Get the values for the second component.
        count = fieldml.FieldML_GetDiscreteDomainComponentValueCount( domainId1, index2 );
        assertEquals( discrete2.length, count );
        count = fieldml.FieldML_GetDiscreteDomainComponentValues( domainId1, index2, values );
        assertEquals( discrete2.length, count );
        for( int i = 0; i < count; i++ )
        {
            assertEquals( discrete2[i], values[i] );
        }

        err = fieldml.FieldML_GetDiscreteDomainComponentValueCount( domainId1, index2 + 100 );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );
        err = fieldml.FieldML_GetDiscreteDomainComponentValues( domainId1, index2 + 100, values );
        assertEquals( FieldML.ERR_BAD_PARAMETER, err );

        // Get the values for a continous domain component.
        int domainId2 = helperCreateContinuousDomain( testDomainName2, testComponentName2, low1, high1 );
        assertTrue( domainId2 > 0 );

        count = fieldml.FieldML_GetDiscreteDomainComponentValues( domainId2, index1, values );
        assertEquals( FieldML.ERR_WRONG_OBJECT_TYPE, count );

        // Get the values for a component of a non-existant domain.
        err = fieldml.FieldML_GetDiscreteDomainComponentValues( domainId2 + 1000, index1, values );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, err );
    }


    public void testFieldML_GetDomainComponentCount()
    {
        // Create a continuous domain.
        int err = fieldml.FieldML_BeginContinuousDomain( testDomainName1 );
        assertEquals( FieldML.NO_ERROR, err );

        // Add a new component.
        int index1 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName1, low1, high1 );
        assertEquals( 0, index1 );

        // Add a new component, with a different range.
        int index2 = fieldml.FieldML_AddContinuousDomainComponent( testComponentName2, low2, high2 );
        assertEquals( 1, index2 );
        
        int domainId1 = fieldml.FieldML_EndDomain();
        assertTrue( domainId1 > 0 );
        
        int count = fieldml.FieldML_GetDomainComponentCount( domainId1 );
        assertEquals( 2, count );

        // Get the component count for a non-existant domain.
        count = fieldml.FieldML_GetDomainComponentCount( domainId1 + 1000 );
        assertEquals( FieldML.ERR_NO_SUCH_OBJECT, count );
    }
}
