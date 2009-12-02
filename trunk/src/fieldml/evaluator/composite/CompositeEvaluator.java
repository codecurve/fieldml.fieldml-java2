package fieldml.evaluator.composite;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
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
    @SerializationAsString
    public final Domain[] parameterDomains;

    public final List<CompositeOperation> operations;


    public CompositeEvaluator( String name, D valueDomain, Domain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;

        operations = new ArrayList<CompositeOperation>();
    }


    protected void apply( DomainValues values )
    {
        for( CompositeOperation o : operations )
        {
            o.perform( values );
        }
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
