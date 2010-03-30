package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.ContinuousValueSource;
import fieldml.value.EnsembleValueSource;
import fieldml.value.MeshValueSource;

public interface DelegatingEvaluator
{
    public void alias( ContinuousValueSource local, ContinuousDomain remote );


    public void alias( EnsembleValueSource local, EnsembleDomain remote );


    public void alias( MeshValueSource local, MeshDomain remote );
}
