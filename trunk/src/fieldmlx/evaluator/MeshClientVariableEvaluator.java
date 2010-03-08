package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractMeshEvaluator;
import fieldml.evaluator.Evaluator;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class MeshClientVariableEvaluator
    extends AbstractMeshEvaluator
{
    public MeshClientVariableEvaluator( String name, MeshDomain valueDomain )
    {
        super( name, valueDomain );
    }

    private MeshDomainValue value;


    public void setValue( int element, double... values )
    {
        value = valueDomain.makeValue( element, values );
    }


    @Override
    public MeshDomainValue evaluate( DomainValues context )
    {
        return value;
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        return new ArrayList<Evaluator<?>>();
    }
}
