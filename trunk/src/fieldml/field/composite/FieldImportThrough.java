package fieldml.field.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class FieldImportThrough
    implements FieldOperation
{
    @SerializationAsString
    public final ContinuousParameters parameters;

    @SerializationAsString
    public final EnsembleParameters iteratedParameters;

    @SerializationAsString
    public final EnsembleDomain iteratedDomain;
    
    @SerializationAsString
    public final ContinuousDomain valueDomain;


    public FieldImportThrough( ContinuousParameters parameters, EnsembleParameters iteratedParameters, EnsembleDomain iteratedDomain, ContinuousDomain valueDomain )
    {
        this.parameters = parameters;
        this.iteratedParameters = iteratedParameters;
        this.iteratedDomain = iteratedDomain;
        this.valueDomain = valueDomain;

        assert parameters.valueDomain.dimensions == 1;
        
        // VALIDATE Scan the whole parameters field and make sure that exactly valueDomain.dimensions entries are defined for each value in iteratedDomain
    }


    @Override
    public void perform( DomainValues values )
    {
        int parameterCount = 0;
        double[] value = new double[iteratedDomain.getValueCount()];
        EnsembleDomainValue v;

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            values.set( iteratedDomain, i );
            v = iteratedParameters.evaluate( values );
            if( v != null )
            {
                values.set( v );
                value[parameterCount++] = parameters.evaluate( values ).values[0];
            }
        }
        
        assert parameterCount == valueDomain.dimensions : "" + parameterCount + " != " + valueDomain.dimensions;

        values.set( valueDomain, value );
    }
}
