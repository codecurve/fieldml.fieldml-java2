package fieldml.evaluator.composite;

import java.util.ArrayList;
import java.util.List;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public abstract class CompositeEvaluator<D extends Domain, V extends DomainValue<D>>
    extends AbstractEvaluator<D, V>
{
    public final List<CompositeOperation> operations;


    public CompositeEvaluator( String name, D valueDomain )
    {
        super( name, valueDomain );

        operations = new ArrayList<CompositeOperation>();
    }


    protected abstract V onComposition( DomainValues localValues );


    @Override
    public V evaluate( DomainValues input )
    {
        DomainValues localValues = new DomainValues( input );

        for( CompositeOperation o : operations )
        {
            o.perform( localValues );
        }

        return onComposition( localValues );
    }


    public void importField( AbstractEvaluator<?, ?> field )
    {
        operations.add( new ImportOperation( field ) );
    }


    public void importThrough( ContinuousParameters parameters, EnsembleParameters iteratedParameters, EnsembleDomain iteratedDomain,
        ContinuousDomain valueDomain )
    {
        operations.add( new ImportThroughOperation( parameters, iteratedParameters, iteratedDomain, valueDomain ) );
    }
}
