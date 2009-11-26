package fieldml.function;

import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class EnsembleFunction
{
    public EnsembleFunction()
    {
    }


    public abstract EnsembleDomainValue evaluate( MeshDomainValue value );
}
