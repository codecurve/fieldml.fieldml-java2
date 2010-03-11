package fieldml.evaluator;

import fieldml.domain.MeshDomain;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public abstract class MeshEvaluator
    extends AbstractEvaluator<MeshDomain, MeshDomainValue>
{
    public MeshEvaluator( String name, MeshDomain valueDomain )
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
