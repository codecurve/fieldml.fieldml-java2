package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleValueSource;

public class CompositionEvaluator<D extends Domain, V extends DomainValue<D>>
{
    public final List<CompositionOperation> operations;


    public CompositionEvaluator()
    {
        operations = new ArrayList<CompositionOperation>();
    }


    public DomainValues evaluate( DomainValues context )
    {
        DomainValues localValues = new DomainValues( context );

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


    public <DD extends Domain, VV extends DomainValue<DD>> void importField( AbstractEvaluator<DD, VV> evaluator, DD domain,
        EnsembleDomain indexDomain )
    {
        operations.add( new ImportOperation<DD, VV>( evaluator, domain, indexDomain ) );
    }


    public void importValue( Domain domain, DomainValue<?> value )
    {
        operations.add( new ValueOperation( domain, value ) );
    }


    public void aliasValue( ContinuousValueSource source, ContinuousDomain destination )
    {
        operations.add( new ContinuousAliasOperation( source, destination ) );
    }


    public void aliasValue( EnsembleValueSource source, EnsembleDomain destination )
    {
        operations.add( new EnsembleAliasOperation( source, destination ) );
    }
}
