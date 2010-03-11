package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public abstract class ContinuousEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
{
    public ContinuousEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }


    @Override
    public abstract ContinuousDomainValue evaluate( DomainValues context );


    protected final double[] evaluateAll( DomainValues context, EnsembleDomain spannedDomain, EnsembleDomain indexDomain )
    {
        double[] values;

        if( indexDomain == null )
        {
            values = new double[spannedDomain.getValueCount()];
            for( int i = 1; i <= spannedDomain.getValueCount(); i++ )
            {
                context.set( spannedDomain, i );
                values[i - 1] = evaluate( context ).values[0];
            }
        }
        else
        {
            int[] indexes = context.get( indexDomain ).values;

            values = new double[indexes.length];
            for( int i = 1; i <= indexes.length; i++ )
            {
                context.set( indexDomain.baseDomain, indexes[i - 1] );
                values[i - 1] = evaluate( context ).values[0];
            }
        }

        return values;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context, ContinuousDomain domain, EnsembleDomain indexDomain )
    {
        if( domain == valueDomain )
        {
            // Desired domain matches native domain.
            return evaluate( context );
        }
        else if( ( valueDomain.componentCount == 1 ) && ( domain.componentCount != 1 ) )
        {
            // Native domain is scalar, desired domain is not.
            // MUSTDO Check native vs. desired bounds.

            return domain.makeValue( evaluateAll( context, domain.componentDomain, indexDomain ) );
        }
        else if( ( valueDomain.componentCount != 1 ) && ( domain.componentCount == 1 ) )
        {
            // MUSTDO Check native vs. desired bounds.
            ContinuousDomainValue value = evaluate( context );

            EnsembleDomainValue componentIndex = context.get( valueDomain.componentDomain );

            return domain.makeValue( value.values[componentIndex.values[0] - 1] );
        }

        assert false : "Domain mismatch: cannot get from " + valueDomain + " to " + domain;

        return null;
    }
}
