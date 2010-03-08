package fieldml.evaluator;

import fieldml.domain.MeshDomain;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public interface MeshEvaluator
    extends Evaluator<MeshDomain>
{
    public abstract MeshDomainValue evaluate( DomainValues context );


    public MeshDomainValue evaluate( DomainValues context, MeshDomain domain );
}
