package fieldml.evaluator.composite;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousMap;
import fieldml.evaluator.ContinuousParameters;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class MappedImportOperation
    implements CompositeOperation
{
    @SerializationAsString
    public final ContinuousParameters sourceField;

    @SerializationAsString
    public final ContinuousDomain valueDomain;
    
    
    @SerializationAsString
    public final ContinuousMap map;


    public MappedImportOperation( ContinuousDomain valueDomain, ContinuousParameters sourceField, ContinuousMap map )
    {
        this.valueDomain = valueDomain;
        this.sourceField = sourceField;
        this.map = map;
    }


    @Override
    public void perform( DomainValues context )
    {
        ContinuousDomainValue value = map.evaluate( valueDomain, context, sourceField );
        context.set( value );
    }
}
