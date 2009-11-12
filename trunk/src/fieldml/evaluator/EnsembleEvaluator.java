package fieldml.evaluator;

import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class EnsembleEvaluator
{
    public EnsembleEvaluator()
    {
    }


    public abstract EnsembleDomainValue evaluate( MeshDomainValue value );
}
