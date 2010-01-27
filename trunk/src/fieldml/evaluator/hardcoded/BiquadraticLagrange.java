package fieldml.evaluator.hardcoded;

import fieldml.domain.ContinuousListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.region.Region;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;

public class BiquadraticLagrange
    extends AbstractEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    // NOTE Making this method public simplifies testing.
    public static double[] evaluateDirect( double x1, double x2 )
    {
        double[] v1 = QuadraticLagrange.evaluateDirect( x1 );
        double[] v2 = QuadraticLagrange.evaluateDirect( x2 );
        
        double value[] = new double[9];
        
        value[0] = v1[0] * v2[0];
        value[1] = v1[1] * v2[0];
        value[2] = v1[2] * v2[0];
        value[3] = v1[0] * v2[1];
        value[4] = v1[1] * v2[1];
        value[5] = v1[2] * v2[1];
        value[6] = v1[0] * v2[2];
        value[7] = v1[1] * v2[2];
        value[8] = v1[2] * v2[2];
        
        return value;
    }

    public final MeshDomain xiDomain;


    public BiquadraticLagrange( String name, MeshDomain xiDomain )
    {
        super( name, Region.getLibrary().getContinuousListDomain( "library.biquadratic_lagrange.parameters" ) );
        
        this.xiDomain = xiDomain;
    }


    @Override
    public ContinuousListDomainValue evaluate( DomainValues context )
    {
        double xi[] = context.get( xiDomain ).chartValues;
        
        return valueDomain.makeValue( evaluateDirect( xi[0], xi[1] ) );
    }
}
