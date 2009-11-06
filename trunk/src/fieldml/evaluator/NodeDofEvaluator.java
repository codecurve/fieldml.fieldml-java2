package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.field.MappingField;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public class NodeDofEvaluator
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final MappingField<ContinuousDomainValue> nodeDofs;

    @SerializationAsString
    public final MappingField<EnsembleDomainValue> elementNodes;

    public final String interpolation;


    private static double QuadBilinear( double[] params, double[] xi )
    {
        double p3 = xi[0] * xi[1];
        double p2 = ( 1 - xi[0] ) * xi[1];
        double p1 = xi[0] * ( 1 - xi[1] );
        double p0 = ( 1 - xi[0] ) * ( 1 - xi[1] );

        return params[0] * p0 + params[1] * p1 + params[2] * p2 + params[3] * p3;
    }


    //TODO Probably wrong.
    private static double SimplexBilinear( double[] params, double[] xi )
    {
        double p0 = ( 1 - ( xi[0] + xi[1] ) );
        double p1 = xi[0];
        double p2 = xi[1];

        return params[0] * p0 + params[1] * p1 + params[2] * p2;
    }


    public NodeDofEvaluator( String name, MappingField<ContinuousDomainValue> nodeDofs, MappingField<EnsembleDomainValue> elementNodes,
        String interpolation )
    {
        super( name );
        
        // TODO Assert that elementNode's value domain is nodeDof's node parameter domain
        // TODO Assert that elementNode's index domain has the right cardinality for the given interpolation.
        this.nodeDofs = nodeDofs;
        this.elementNodes = elementNodes;
        this.interpolation = interpolation;
    }


    @Override
    public double evaluate( MeshDomainValue value )
    {
        if( interpolation.equals( "library::quad_bilinear" ) )
        {
            double[] params = new double[4];
            for( int i = 0; i < 4; i++ )
            {
                ContinuousDomainValue v = nodeDofs.evaluate( elementNodes.evaluate( value.indexValue, i + 1 ) );
                params[i] = v.chartValues[0];
            }

            return QuadBilinear( params, value.chartValues );
        }
        else if( interpolation.equals( "library::triangle_bilinear" ) )
        {
            double[] params = new double[3];
            for( int i = 0; i < 3; i++ )
            {
                ContinuousDomainValue v = nodeDofs.evaluate( elementNodes.evaluate( value.indexValue, i + 1 ) );
                params[i] = v.chartValues[0];
            }

            return SimplexBilinear( params, value.chartValues );
        }

        // TODO HACK!
        return 0;
    }
}
