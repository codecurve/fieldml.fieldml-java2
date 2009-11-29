package fieldml.function;

import fieldml.annotations.SerializationAsString;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.MeshDomainValue;

public class LinearQuadraticBSpline
    extends ContinuousFunction
{
    @SerializationAsString
    public final ContinuousEvaluator dofs;


    private double evaluate( double[] params, double[] xi )
    {
        double p0 = 0.5 * ( 1 - xi[0] ) * ( 1 - xi[0] );
        double p1 = -( xi[0] * xi[0] ) + xi[0] + 0.5;
        double p2 = 0.5 * xi[0] * xi[0];

        return params[0] * p0 + params[1] * p1 + params[2] * p2;
    }


    public LinearQuadraticBSpline( String name, ContinuousEvaluator dofs )
    {
        super( name );

        this.dofs = dofs;
    }


    @Override
    public double evaluate( MeshDomainValue value )
    {
        ContinuousDomainValue params = dofs.evaluate( value.domain.elementDomain, value.indexValue );

        return evaluate( params.values, value.chartValues );
    }
}
