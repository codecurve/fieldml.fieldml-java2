package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;
import fieldml.value.MeshValueSource;

public abstract class MeshEvaluator
    extends AbstractEvaluator<MeshDomain, MeshDomainValue>
    implements MeshValueSource
{
    public MeshEvaluator( String name, MeshDomain valueDomain )
    {
        super( name, valueDomain );
    }


    @Override
    public abstract MeshDomainValue getValue( DomainValues context );


    @Override
    public MeshDomainValue getValue( DomainValues context, MeshDomain domain, EnsembleDomain indexDomain )
    {
        if( domain == valueDomain )
        {
            // Desired domain matches native domain.
            return getValue( context );
        }

        return null;
    }
}
