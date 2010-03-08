package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.domain.MeshDomain;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class MeshVariableEvaluator
    extends AbstractMeshEvaluator
{
    // This is only an advisory that the users of the variable may not work unless these dependencies exist.
    @SerializationAsString
    public Domain[] parameterDomains;


    public MeshVariableEvaluator( String name, MeshDomain valueDomain, Domain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;
    }


    @Override
    public MeshDomainValue evaluate( DomainValues context )
    {
        MeshEvaluator variable = context.getMeshVariable( name );

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
