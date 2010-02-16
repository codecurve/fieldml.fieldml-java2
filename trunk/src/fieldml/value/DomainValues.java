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

        for( DomainValue<? extends Domain> v : input.values.values() )
        {
            values.put( v.domain, v );
        }
        for( String name : input.continuousVariables.keySet() )
        {
            continuousVariables.put( name, input.continuousVariables.get( name ) );
        }
    }


    public void set( DomainValue<? extends Domain> value )
    {
        values.put( value.domain, value );
        if( value instanceof MeshDomainValue )// TODO This is ugly
        {
            MeshDomainValue v = (MeshDomainValue)value;
            set( v.domain.elementDomain, v.indexValue );
        }
    }


    public void set( ContinuousDomain domain, double... values )
    {
        set( domain.makeValue( values ) );
    }


    public void set( EnsembleDomain domain, int value )
    {
        set( domain.makeValue( value ) );
    }


    public void set( MeshDomain domain, int indexValue, double... chartValues )
    {
        set( domain.makeValue( indexValue, chartValues ) );
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
}
