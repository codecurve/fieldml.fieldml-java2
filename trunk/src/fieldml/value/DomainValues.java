package fieldml.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;

public class DomainValues
{
    private final Map<Domain, DomainValue<? extends Domain>> values;

    private final Map<ContinuousDomain, ContinuousValueSource> continuousSources;

    private final Map<EnsembleDomain, EnsembleValueSource> ensembleSources;

    private final Map<String, ContinuousEvaluator> continuousVariables;


    public DomainValues()
    {
        values = new HashMap<Domain, DomainValue<? extends Domain>>();
        continuousVariables = new HashMap<String, ContinuousEvaluator>();
        continuousSources = new HashMap<ContinuousDomain, ContinuousValueSource>();
        ensembleSources = new HashMap<EnsembleDomain, EnsembleValueSource>();
    }


    public DomainValues( DomainValues input )
    {
        this();

        for( Entry<Domain, DomainValue<? extends Domain>> d : input.values.entrySet() )
        {
            values.put( d.getKey(), d.getValue() );
        }
        for( Entry<String, ContinuousEvaluator> d : input.continuousVariables.entrySet() )
        {
            continuousVariables.put( d.getKey(), d.getValue() );
        }
        for( Entry<ContinuousDomain, ContinuousValueSource> d : input.continuousSources.entrySet() )
        {
            continuousSources.put( d.getKey(), d.getValue() );
        }
        for( Entry<EnsembleDomain, EnsembleValueSource> d : input.ensembleSources.entrySet() )
        {
            ensembleSources.put( d.getKey(), d.getValue() );
        }
    }


    public void set( Domain domain, DomainValue<? extends Domain> value )
    {
        values.put( domain, value );
        if( domain instanceof MeshDomain )// TODO This is ugly
        {
            MeshDomain d = (MeshDomain)domain;
            MeshDomainValue v = (MeshDomainValue)value;
            set( d.getElementDomain(), v.indexValue );
            set( d.getXiDomain(), v.chartValues );
        }
    }
    
    
    public void alias( ContinuousValueSource source, ContinuousDomain domain )
    {
        continuousSources.put( domain, source );
    }
    
    public void alias( EnsembleValueSource source, EnsembleDomain domain )
    {
        ensembleSources.put( domain, source );
    }


    public void set( ContinuousDomain domain, double... values )
    {
        set( domain, domain.makeValue( values ) );
    }


    public void set( EnsembleDomain domain, int ... values )
    {
        set( domain, domain.makeValue( values ) );
    }


    public void set( MeshDomain domain, int indexValue, double... chartValues )
    {
        set( domain, domain.makeValue( indexValue, chartValues ) );
    }


    public ContinuousDomainValue get( ContinuousDomain domain )
    {
        ContinuousValueSource source = continuousSources.get( domain );
        if( source == null )
        {
            return (ContinuousDomainValue)values.get( domain );
        }
        
        return source.getValue( this );
    }


    public EnsembleDomainValue get( EnsembleDomain domain )
    {
        EnsembleValueSource source = ensembleSources.get( domain );
        if( source == null )
        {
            return (EnsembleDomainValue)values.get( domain );
        }
        
        return source.getValue( this );
    }


    public MeshDomainValue get( MeshDomain domain )
    {
        return (MeshDomainValue)values.get( domain );
    }


    public DomainValue<? extends Domain> get( Domain domain )
    {
        return values.get( domain );
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        continuousVariables.put( name, evaluator );
    }


    public ContinuousEvaluator getContinuousVariable( String name )
    {
        return continuousVariables.get( name );
    }
}
