package fieldml.function;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class QuadraticBSpline
    extends ContinuousFunction
{
    @SerializationAsString
    public final ContinuousDomain dofsDomain;


    // NOTE Making this method public simplifies testing.
    public static double evaluate( double[] params, double[] xi )
    {
        double p0 = 0.5 * ( 1 - xi[0] ) * ( 1 - xi[0] );
        double p1 = -( xi[0] * xi[0] ) + xi[0] + 0.5;
        double p2 = 0.5 * xi[0] * xi[0];

        return params[0] * p0 + params[1] * p1 + params[2] * p2;
    }


    public QuadraticBSpline( String name, ContinuousDomain dofsDomain )
    {
        super( name );

        this.dofsDomain = dofsDomain;
    }


    @Override
    public double evaluate( DomainValues context, MeshDomainValue meshLocation,
        SimpleMap<ContinuousDomain, ContinuousEvaluator> dofEvaluators )
    {
        ContinuousEvaluator dofEvaluator = dofEvaluators.get( dofsDomain );
        ContinuousDomainValue dofs = dofEvaluator.evaluate( context );

        return evaluate( dofs.values, meshLocation.chartValues );
    }
}
