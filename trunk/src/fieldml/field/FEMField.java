package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValue;
import fieldml.value.MeshDomainValue;

public class FEMField
    extends Field<ContinuousDomain, ContinuousDomainValue>
{
    @SerializationAsString
    public final MeshDomain meshDomain;

    public final List<MapEntry> evaluators;

    public class MapEntry
    {
        public final int key;

        @SerializationAsString
        public final ContinuousEvaluator evaluator;


        private MapEntry( int key, ContinuousEvaluator evaluator )
        {
            this.key = key;
            this.evaluator = evaluator;
        }
    }


    public FEMField( String name, ContinuousDomain valueDomain, MeshDomain meshDomain )
    {
        super( name, valueDomain );

        this.meshDomain = meshDomain;

        evaluators = new ArrayList<MapEntry>();
    }


    public void setEvaluator( int indexValue, ContinuousEvaluator evaluator )
    {
        evaluators.add( new MapEntry( indexValue, evaluator ) );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValue... input )
    {
        if( input[0].domain != meshDomain )
        {
            return null;
        }

        MeshDomainValue v = (MeshDomainValue)input[0];

        for( MapEntry e : evaluators )
        {
            if( e.key == v.indexValue )
            {
                return ContinuousDomainValue.makeValue( valueDomain, e.evaluator.evaluate( v ) );
            }
        }

        return null;
    }
}
