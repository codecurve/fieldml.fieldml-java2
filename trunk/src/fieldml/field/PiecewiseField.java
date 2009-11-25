package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.MeshDomain;
import fieldml.function.ContinuousFunction;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class PiecewiseField
    extends Field<ContinuousDomain, ContinuousDomainValue>
{
    @SerializationAsString
    public final MeshDomain meshDomain;

    public final List<ContinuousFunction> evaluatorList;

    public final SimpleMap<Integer, String> elementEvaluators;


    public PiecewiseField( String name, ContinuousDomain valueDomain, MeshDomain meshDomain )
    {
        super( name, valueDomain );

        this.meshDomain = meshDomain;

        elementEvaluators = new SimpleMap<Integer, String>();
        evaluatorList = new ArrayList<ContinuousFunction>();
    }


    public void setEvaluator( int indexValue, String evaluatorName )
    {
        elementEvaluators.put( indexValue, evaluatorName );
    }

    
    private ContinuousFunction getEvaluator( String name )
    {
        for( ContinuousFunction e : evaluatorList )
        {
            if(e.name.equals( name ) )
            {
                return e;
            }
        }
        
        return null;
    }

    @Override
    public ContinuousDomainValue evaluate( DomainValues input )
    {
        MeshDomainValue v = input.get( meshDomain );

        final String evaluatorName = elementEvaluators.get(v.indexValue);
        ContinuousFunction e = getEvaluator( evaluatorName);
        
        if( e != null )
        {
            return ContinuousDomainValue.makeValue( valueDomain, e.evaluate( v ) );
        }

        return null;
    }


    public void addEvaluator( ContinuousFunction evaluator )
    {
        evaluatorList.add( evaluator );
    }
}
