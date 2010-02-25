package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.domain.Domain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class CompositionEvaluator<D extends Domain, V extends DomainValue<D>>
{
    public final List<CompositionOperation> operations;
    

    public CompositionEvaluator()
    {
        operations = new ArrayList<CompositionOperation>();
    }


    public DomainValues evaluate( DomainValues input )
    {
        DomainValues localValues = new DomainValues( input );

        for( CompositionOperation o : operations )
        {
            o.perform( localValues );
        }

        return localValues;
    }


    public <DD extends Domain, VV extends DomainValue<DD>> void importField( AbstractEvaluator<DD, VV> evaluator, DD domain )
    {
        operations.add( new ImportOperation<DD, VV>( evaluator, domain ) );
    }


    public void importValue( EnsembleDomainValue value )
    {
        operations.add( new ValueOperation( value ) );
    }
}
