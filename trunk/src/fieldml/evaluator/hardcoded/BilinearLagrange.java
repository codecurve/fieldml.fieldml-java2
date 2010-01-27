package fieldml.evaluator.hardcoded;

import fieldml.domain.ContinuousListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.region.Region;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;

public class BilinearLagrange
    extends AbstractEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    // NOTE Making this method public simplifies testing.
    public static double[] evaluateDirect( double x1, double x2 )
    {
        double[] x1_v = LinearLagrange.evaluateDirect( x1 );
        double[] x2_v = LinearLagrange.evaluateDirect( x2 );

        double[] value = new double[4];

        value[0] = x1_v[0] * x2_v[0];
        value[1] = x1_v[1] * x2_v[0];
        value[2] = x1_v[0] * x2_v[1];
        value[3] = x1_v[1] * x2_v[1];

        return value;
    }

    public final MeshDomain xiDomain;


    public BilinearLagrange( String name, MeshDomain xiDomain )
    {
        super( name, Region.getLibrary().getContinuousListDomain( "library.bilinear_lagrange.parameters" ) );

        this.xiDomain = xiDomain;
    }


    @Override
    public ContinuousListDomainValue evaluate( DomainValues context )
    {
        double xi[] = context.get( xiDomain ).chartValues;

        return valueDomain.makeValue( evaluateDirect( xi[0], xi[1] ) );
    }
}
