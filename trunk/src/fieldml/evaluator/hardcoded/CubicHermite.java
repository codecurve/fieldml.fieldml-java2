package fieldml.evaluator.hardcoded;

import fieldml.domain.ContinuousListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.region.Region;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;


public class CubicHermite
    extends AbstractEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    // NOTE Making this method public simplifies testing.
    public static double[] evaluateDirect( double x1 )
    {
        double[] value = new double[4];

        value[0] = ( 1 - 3 * x1 * x1 + 2 * x1 * x1 * x1 ); // psi01
        value[1] = x1 * ( x1 - 1 ) * ( x1 - 1 ); // psi11
        value[2] = x1 * x1 * ( 3 - 2 * x1 ); // psi02
        value[3] = x1 * x1 * ( x1 - 1 ); // psi12

        return value;
    }

    public final MeshDomain xiDomain;


    public CubicHermite( String name, MeshDomain xiDomain )
    {
        super( name, Region.getLibrary().getContinuousListDomain( "library.cubic_hermite.parameters" ) );

        this.xiDomain = xiDomain;
    }


    @Override
    public ContinuousListDomainValue evaluate( DomainValues context )
    {
        double xi[] = context.get( xiDomain ).chartValues;

        return valueDomain.makeValue( evaluateDirect( xi[0] ) );
    }
}
