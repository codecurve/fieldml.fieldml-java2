package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValue;
import fieldml.value.MeshDomainValue;

public class PiecewiseField
    extends Field<ContinuousDomain, ContinuousDomainValue>
{
    @SerializationAsString
    public final MeshDomain meshDomain;

    public final List<ContinuousEvaluator> evaluatorList;

    public final SimpleMap<Integer, String> elementEvaluators;


    public PiecewiseField( String name, ContinuousDomain valueDomain, MeshDomain meshDomain )
    {
        super( name, valueDomain );

        this.meshDomain = meshDomain;

        elementEvaluators = new SimpleMap<Integer, String>();
        evaluatorList = new ArrayList<ContinuousEvaluator>();
    }


    public void setEvaluator( int indexValue, String evaluatorName )
    {
        elementEvaluators.put( indexValue, evaluatorName );
    }

    
    private ContinuousEvaluator getEvaluator( String name )
    {
        for( ContinuousEvaluator e : evaluatorList )
        {
            if(e.name.equals( name ) )
            {
                return e;
            }
        }
        
        return null;
    }

    @Override
    public ContinuousDomainValue evaluate( DomainValue... input )
    {
        if( input[0].domain != meshDomain )
        {
            return null;
        }

        MeshDomainValue v = (MeshDomainValue)input[0];

        ContinuousEvaluator e = getEvaluator( elementEvaluators.get( v.indexValue ) );
        if( e != null )
        {
            return ContinuousDomainValue.makeValue( valueDomain, e.evaluate( v ) );
        }

        return null;
    }


    public void addEvaluator( ContinuousEvaluator evaluator )
    {
        evaluatorList.add( evaluator );
    }
}
