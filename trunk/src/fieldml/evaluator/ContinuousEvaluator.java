package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public abstract class ContinuousEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
{
    @SerializationAsString
    public ContinuousEvaluator fallback;


    public ContinuousEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }


    public void setFallback( ContinuousEvaluator fallback )
    {
        this.fallback = fallback;
    }


    @Override
    public abstract ContinuousDomainValue evaluate( DomainValues context );


    protected double[] evaluateAll( DomainValues context, EnsembleDomain spannedDomain )
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
        ContinuousDomainValue value = null;

        if( domain == valueDomain )
        {
            // Desired domain matches native domain.
            value = evaluate( context );
        }
        else if( ( valueDomain.componentCount == 1 ) && ( domain.componentCount > 1 ) )
        {
            // Native domain is scalar, desired domain is not.
            // MUSTDO Check native vs. desired bounds.

            value = domain.makeValue( evaluateAll( context, domain.componentDomain ) );
        }
        else if( ( valueDomain.componentCount > 1 ) && ( domain.componentCount == 1 ) )
        {
            // MUSTDO Check native vs. desired bounds.
            ContinuousDomainValue values = evaluate( context );

            EnsembleDomainValue componentIndex = context.get( valueDomain.componentDomain );

            value = domain.makeValue( values.values[componentIndex.values[0] - 1] );
        }

        if( ( value == null ) && ( fallback != null ) )
        {
            value = fallback.evaluate( context, domain );
        }

        return value;
    }
}
