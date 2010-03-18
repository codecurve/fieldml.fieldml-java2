package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.EnsembleValueSource;

public abstract class EnsembleEvaluator
    extends AbstractEvaluator<EnsembleDomain, EnsembleDomainValue>
    implements EnsembleValueSource
{
    public EnsembleEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );
    }


    protected final int[] evaluateAll( DomainValues context, EnsembleDomain spannedDomain, EnsembleDomain indexDomain )
    {
        int[] values = new int[spannedDomain.getValueCount()];

        if( indexDomain == null )
        {
            for( int i = 1; i <= spannedDomain.getValueCount(); i++ )
            {
                context.set( spannedDomain, i );
                values[i - 1] = getValue( context ).values[0];
            }
        }
        else
        {
            int[] indexes = context.get( indexDomain ).values;

            for( int i = 0; i < indexes.length; i++ )
            {
                context.set( spannedDomain, indexes[i] );
                values[i - 1] = getValue( context ).values[0];
            }
        }

        return values;
    }


    @Override
    public EnsembleDomainValue getValue( DomainValues context, EnsembleDomain domain, EnsembleDomain indexDomain )
    {
        if( domain == valueDomain )
        {
            // Desired domain matches native domain.
            return getValue( context );
        }
        else if( ( valueDomain.componentDomain == null ) && ( domain.componentDomain != null ) )
        {
            // Native domain is scalar, desired domain is not.
            // MUSTDO Check native vs. desired bounds.

            return domain.makeValue( evaluateAll( context, domain.componentDomain, indexDomain ) );
        }
        else if( ( valueDomain.componentDomain != null ) && ( domain.componentDomain == null ) )
        {
            // MUSTDO Check native vs. desired bounds.
            EnsembleDomainValue v = getValue( context );

            return domain.makeValue( v.values[context.get( valueDomain.componentDomain ).values[0] - 1] );
        }

        return null;
    }
}
