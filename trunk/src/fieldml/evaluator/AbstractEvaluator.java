package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public abstract class AbstractEvaluator<D extends Domain, V extends DomainValue<D>>
{
    public final String name;

    @SerializationAsString
    public final D valueDomain;


    public AbstractEvaluator( String name, D valueDomain )
    {
        this.name = name;
        this.valueDomain = valueDomain;
    }


    @Override
    public String toString()
    {
        return name;
    }


    public abstract V evaluate( DomainValues input );
    
    
    public final V evaluate( MeshDomain domain, int index, double ... chartValues )
    {
        DomainValues values = new DomainValues();
        values.set( domain, index, chartValues );
        
        return evaluate( values );
    }
    
    
    public final V evaluate( EnsembleDomain domain, int index )
    {
        DomainValues values = new DomainValues();
        values.set( domain, index );
        
        return evaluate( values );
    }
    
    
    public final V evaluate( ContinuousDomain domain, double ... continuousValues )
    {
        DomainValues values = new DomainValues();
        values.set( domain, continuousValues );
        
        return evaluate( values );
    }
}
