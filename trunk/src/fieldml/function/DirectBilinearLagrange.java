package fieldml.function;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class DirectBilinearLagrange
    extends ContinuousFunction
{
    @SerializationAsString
    public final ContinuousDomain dofsDomain;


    protected double evaluate( double[] params, double[] xi )
    {
        double x1_1 = ( 1 - xi[0] );
        double x1_2 = ( xi[0] );
        double x2_1 = ( 1 - xi[1] );
        double x2_2 = ( xi[1] );

        return params[0] * x1_1 * x2_1 + params[1] * x1_2 * x2_1 + params[2] * x1_1 * x2_2 + params[3] * x1_2 * x2_2;
    }


    public DirectBilinearLagrange( String name, ContinuousDomain dofsDomain )
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
