package fieldml.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousAggregateEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final List<ContinuousEvaluator> sourceFields;

    private int count;


    public ContinuousAggregateEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );

        count = valueDomain.componentCount;
        sourceFields = new ArrayList<ContinuousEvaluator>();
        for( int i = 0; i < valueDomain.componentCount; i++ )
        {
            sourceFields.add( null );
        }
    }


    public void setSourceField( int destinationDimension, ContinuousEvaluator sourceField )
    {
        // TODO For now, we're just taking the source field's first component.
        sourceFields.set( destinationDimension - 1, sourceField );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues input )
    {
        double[] value = new double[count];

        for( int i = 0; i < count; i++ )
        {
            value[i] = sourceFields.get( i ).getValue( input ).values[0];
        }

        return valueDomain.makeValue( value );
    }
}
