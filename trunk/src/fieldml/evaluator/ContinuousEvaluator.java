package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public abstract class ContinuousEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
{
    public ContinuousEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }


    @Override
    public abstract ContinuousDomainValue evaluate( DomainValues context );


    protected final double[] evaluateAll( DomainValues context, EnsembleDomain spannedDomain )
    {
        double[] values = new double[spannedDomain.getValueCount()];

        for( int i = 1; i <= spannedDomain.getValueCount(); i++ )
        {
            context.set( spannedDomain, i );
            values[i - 1] = evaluate( context ).values[0];
        }
        
        return values;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context, ContinuousDomain domain )
    {
        if( domain == valueDomain )
        {
            // Desired domain matches native domain.
            return evaluate( context );
        }
        else if( ( valueDomain.componentDomain == null ) && ( domain.componentDomain != null ) )
        {
            // Native domain is scalar, desired domain is not.
            // MUSTDO Check native vs. desired bounds.

            return domain.makeValue( evaluateAll( context, domain.componentDomain ) );
        }
        else if( ( valueDomain.componentDomain != null ) && ( domain.componentDomain == null ) )
        {
            // MUSTDO Check native vs. desired bounds.
            ContinuousDomainValue v = evaluate( context );

            return domain.makeValue( v.values[context.get( valueDomain.componentDomain ).values[0]] );
        }

        return null;
    }
}
