package fieldml.evaluator;

import java.util.Collection;

import fieldml.domain.Domain;

public interface Evaluator<D extends Domain>
{
    public String getName();


    public D getValueDomain();


    public Collection<? extends Evaluator<?>> getVariables();
}
