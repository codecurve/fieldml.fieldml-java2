package fieldml.field.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousParameters;
import fieldml.value.DomainValues;

public class FieldMappedImport
    implements FieldOperation
{
    @SerializationAsString
    public final ContinuousParameters sourceField;

    @SerializationAsString
    public final ContinuousParameters weightings;

    @SerializationAsString
    public final EnsembleDomain iteratedDomain;


    public FieldMappedImport( ContinuousParameters sourceField, ContinuousParameters weightings, EnsembleDomain iteratedDomain )
    {
        this.sourceField = sourceField;
        this.weightings = weightings;
        this.iteratedDomain = iteratedDomain;
    }


    @Override
    public void perform( DomainValues values )
    {
        int dimensions = sourceField.valueDomain.dimensions;
        double weight;
        double value;
        double[] finalValue = new double[dimensions];

        for( int i = 1; i <= iteratedDomain.getValueCount(); i++ )
        {
            values.set( iteratedDomain, i );
            for( int j = 0; j < dimensions; j++ )
            {
                weight = weightings.evaluate( values ).values[j];
                value = sourceField.evaluate( values ).values[j];

                finalValue[j] += ( weight * value );
            }
        }

        values.set( sourceField.valueDomain, finalValue );
    }
}
