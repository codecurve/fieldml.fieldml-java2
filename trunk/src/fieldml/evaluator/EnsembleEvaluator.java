package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public abstract class EnsembleEvaluator
    extends AbstractEvaluator<EnsembleDomain, EnsembleDomainValue>
{
    public EnsembleEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );
    }


    protected final int[] evaluateAll( DomainValues context, EnsembleDomain spannedDomain )
    {
        int[] values = new int[spannedDomain.getValueCount()];

        for( int i = 1; i <= spannedDomain.getValueCount(); i++ )
        {
            context.set( spannedDomain, i );
            values[i - 1] = evaluate( context ).values[0];
        }

        return values;
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues context, EnsembleDomain domain )
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
            EnsembleDomainValue v = evaluate( context );

            return domain.makeValue( v.values[context.get( valueDomain.componentDomain ).values[0]] );
        }

        return null;
    }
}
