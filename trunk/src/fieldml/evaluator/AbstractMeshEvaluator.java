package fieldml.evaluator;

import fieldml.domain.MeshDomain;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public abstract class AbstractMeshEvaluator
    extends AbstractEvaluator<MeshDomain, MeshDomainValue>
    implements MeshEvaluator
{
    public AbstractMeshEvaluator( String name, MeshDomain valueDomain )
    {
        super( name, valueDomain );
    }


    @Override
    public abstract MeshDomainValue evaluate( DomainValues context );


    @Override
    public MeshDomainValue evaluate( DomainValues context, MeshDomain domain )
    {
        if( domain == valueDomain )
        {
            // Desired domain matches native domain.
            return evaluate( context );
        }

        return null;
    }
}
