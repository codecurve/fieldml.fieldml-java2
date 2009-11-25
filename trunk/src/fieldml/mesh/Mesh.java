package fieldml.mesh;

import fieldml.evaluator.AbstractEvaluator;

public class Mesh
{
    public int elementCount;
    
    public AbstractEvaluator<?, ?> elementShapes;
    
    public Object points;
}
