package fieldml.evaluator;

import fieldml.domain.Domain;

public interface Evaluator<D extends Domain>
{
    public String getName();
    
    
    public D getValueDomain();
}
