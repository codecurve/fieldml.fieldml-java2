package fieldml.evaluator.hardcoded;

import fieldml.domain.ContinuousListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.region.Region;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;

public class LinearLagrange
    extends AbstractEvaluator<ContinuousListDomain, ContinuousListDomainValue>
    implements ContinuousListEvaluator
{
    // NOTE Making this method public simplifies testing.
    public static double[] evaluateDirect( double x1 )
    {
        double[] value = new double[2];

        value[0] = ( 1- x1 );
        value[1] = ( x1 );

        return value;
    }

    public final MeshDomain xiDomain;


    public LinearLagrange( String name, MeshDomain xiDomain )
    {
        super( name, Region.getLibrary().getContinuousListDomain( "library.linear_lagrange.parameters" ) );
        
        this.xiDomain = xiDomain;
    }


    @Override
    public ContinuousListDomainValue evaluate( DomainValues context )
    {
        double xi[] = context.get( xiDomain ).chartValues;
        
        return valueDomain.makeValue( evaluateDirect( xi[0] ) );
    }
}
