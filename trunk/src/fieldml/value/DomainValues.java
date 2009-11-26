package fieldml.value;

import java.util.HashMap;
import java.util.Map;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;

public class DomainValues
{
    public final Map<Domain, DomainValue<? extends Domain>> values;


    public DomainValues()
    {
        values = new HashMap<Domain, DomainValue<? extends Domain>>();
    }


    public DomainValues( DomainValues input )
    {
        this();

        for( DomainValue<? extends Domain> v : input.values.values() )
        {
            set( v );
        }
    }


    public void set( DomainValue<? extends Domain> value )
    {
        values.put( value.domain, value );
    }


    public void set( ContinuousDomain domain, double... values )
    {
        set( ContinuousDomainValue.makeValue( domain, values ) );
    }


    public void set( EnsembleDomain domain, int value )
    {
        set( EnsembleDomainValue.makeValue( domain, value ) );
    }


    public void set( MeshDomain domain, int indexValue, double... chartValues )
    {
        set( MeshDomainValue.makeValue( domain, indexValue, chartValues ) );
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
}
