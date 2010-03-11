package fieldml.value;

import java.util.HashMap;
import java.util.Map;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;

public class DomainValues
{
    private final Map<Domain, DomainValue<? extends Domain>> values;

    private final Map<String, ContinuousEvaluator> continuousVariables;


    public DomainValues()
    {
        values = new HashMap<Domain, DomainValue<? extends Domain>>();
        continuousVariables = new HashMap<String, ContinuousEvaluator>();
    }


    public DomainValues( DomainValues input )
    {
        this();

        for( Domain d : input.values.keySet() )
        {
            values.put( d, input.values.get( d ) );
        }
        for( String name : input.continuousVariables.keySet() )
        {
            continuousVariables.put( name, input.continuousVariables.get( name ) );
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


    public void set( ContinuousDomain domain, double... values )
    {
        set( domain, domain.makeValue( values ) );
    }


    public void set( EnsembleDomain domain, int value )
    {
        set( domain, domain.makeValue( value ) );
    }


    public void set( MeshDomain domain, int indexValue, double... chartValues )
    {
        set( domain, domain.makeValue( indexValue, chartValues ) );
    }


    public ContinuousDomainValue get( ContinuousDomain domain )
    {
        return (ContinuousDomainValue)values.get( domain );
    }


    public EnsembleDomainValue get( EnsembleDomain domain )
    {
        return (EnsembleDomainValue)values.get( domain );
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


    public void copy( Domain source, Domain destination )
    {
        DomainValue<?> value = values.get( source );
        values.put( destination, value );
    }
}
