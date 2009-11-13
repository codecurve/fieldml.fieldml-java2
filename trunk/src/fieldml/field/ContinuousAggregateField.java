package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValue;

public class ContinuousAggregateField
    extends Field<ContinuousDomain, ContinuousDomainValue>
{
    @SerializationAsString
    public final List<Field<ContinuousDomain, ContinuousDomainValue>> sourceFields;

    private int count;


    public ContinuousAggregateField( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );

        count = valueDomain.dimensions;
        sourceFields = new ArrayList<Field<ContinuousDomain, ContinuousDomainValue>>();
        for( int i = 0; i < valueDomain.dimensions; i++ )
        {
            sourceFields.add( null );
        }
    }


    public void setSourceField( int destinationDimension, Field<ContinuousDomain, ContinuousDomainValue> sourceField )
    {
        // TODO For simplicity, we're just taking the source field's first component.
        sourceFields.set( destinationDimension - 1, sourceField );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValue... input )
    {
        double[] value = new double[count];

        for( int i = 0; i < count; i++ )
        {
            value[i] = sourceFields.get( i ).evaluate( input ).values[0];
        }

        return ContinuousDomainValue.makeValue( valueDomain, value );
    }
}
