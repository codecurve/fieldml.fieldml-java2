package fieldml.map;

import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.DomainValues;

public interface ContinuousMap
{
    public String getName();


    public double evaluate( DomainValues context, ContinuousEvaluator indexedValues );
}
