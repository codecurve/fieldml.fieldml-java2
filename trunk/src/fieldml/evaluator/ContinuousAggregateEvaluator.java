package fieldml.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousAggregateEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final List<AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>> sourceFields;

    private int count;


    public ContinuousAggregateEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );

        count = valueDomain.dimensions;
        sourceFields = new ArrayList<AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>>();
        for( int i = 0; i < valueDomain.dimensions; i++ )
        {
            sourceFields.add( null );
        }
    }


    public void setSourceField( int destinationDimension, AbstractEvaluator<ContinuousDomain, ContinuousDomainValue> sourceField )
    {
        // TODO For simplicity, we're just taking the source field's first component.
        sourceFields.set( destinationDimension - 1, sourceField );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues input )
    {
        double[] value = new double[count];

        for( int i = 0; i < count; i++ )
        {
            value[i] = sourceFields.get( i ).evaluate( input ).values[0];
        }

        return ContinuousDomainValue.makeValue( valueDomain, value );
    }
}
