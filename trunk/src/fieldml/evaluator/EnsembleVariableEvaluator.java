package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsembleVariableEvaluator
    extends AbstractEnsembleEvaluator
{
    // This is only an advisory that the users of the variable may not work unless these dependencies exist.
    @SerializationAsString
    public Domain[] parameterDomains;


    public EnsembleVariableEvaluator( String name, EnsembleDomain valueDomain, Domain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues context )
    {
        EnsembleEvaluator variable = context.getEnsembleVariable( name );

        assert variable != null : "Variable " + name + " is not set for " + getName();

        return variable.evaluate( context );
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        ArrayList<Evaluator<?>> list = new ArrayList<Evaluator<?>>();
        list.add( this );

        return list;
    }
}
