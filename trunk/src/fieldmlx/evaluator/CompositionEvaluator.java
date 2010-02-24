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


    public void importField( AbstractEvaluator<?, ?> field, Domain domain )
    {
        operations.add( new ImportOperation( field, domain ) );
    }


    public void importValue( EnsembleDomainValue value )
    {
        operations.add( new ValueOperation( value ) );
    }
}
