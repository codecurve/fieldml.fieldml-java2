package purgatory.junit;

import junit.framework.TestCase;
import purgatory.fieldml.FieldML;
import purgatory.fieldml.implementation.FieldMLJava;

public abstract class FieldMLTest
    extends TestCase
{
    static final String testFieldName1 = "field 1";
    static final String testFieldName2 = "field 2";
    static final String testFieldName3 = "field 3";

    static final String testParameterName1 = "parameter 1";
    static final String testParameterName2 = "parameter 2";
    static final String testParameterName3 = "parameter 3";

    static final String testDomainName1 = "domain 1";
    static final String testDomainName2 = "domain 2";

    static final String testComponentName1 = "component 1";
    static final String testComponentName2 = "component 2";

    static final double low1 = 0;
    static final double high1 = 1;

    static final double low2 = -Math.PI;
    static final double high2 = Math.PI;

    static final int[] discrete1 =
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    static final int[] discrete2 =
    { 2, 4, 8, 16, 32, 64 };

    FieldML fieldml;


    int helperCreateContinuousDomain( String name, String componentName, double min, double max )
    {
        int err;

        err = fieldml.FieldML_BeginContinuousDomain( name );
        if( err != FieldML.NO_ERROR )
        {
            return err;
        }
        err = fieldml.FieldML_AddContinuousDomainComponent( componentName, min, max );
        if( err != FieldML.NO_ERROR )
        {
            return err;
        }
        return fieldml.FieldML_EndDomain();
    }


    int helperCreateDiscreteDomain( String name, String componentName, int[] values )
    {
        int err;

        err = fieldml.FieldML_BeginDiscreteDomain( name );
        if( err != FieldML.NO_ERROR )
        {
            return err;
        }
        err = fieldml.FieldML_AddDiscreteDomainComponent( componentName, values, values.length );
        if( err != FieldML.NO_ERROR )
        {
            return err;
        }
        return fieldml.FieldML_EndDomain();
    }

    
    int helperCreateField( String name, int domainId )
    {
        int err;
        
        err = fieldml.FieldML_BeginDerivedField( name, domainId );
        if( err != FieldML.NO_ERROR )
        {
            return err;
        }
        
        return fieldml.FieldML_EndField();
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        fieldml = new FieldMLJava();
    }


    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }
}
