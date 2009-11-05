package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.Evaluator;
import fieldml.evaluator.NodeDofEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.MeshDomainValue;

public class FEMField<D extends DomainValue>
    extends Field<D>
{
    @SerializationAsString
    public final MeshDomain meshDomain;

    public final List<MapEntry> evaluators;

    public class MapEntry
    {
        public final int key;

        @SerializationAsString
        public final Evaluator evaluator;


        private MapEntry( int key, Evaluator evaluator )
        {
            this.key = key;
            this.evaluator = evaluator;
        }
    }


    public FEMField( String name, Domain valueDomain, MeshDomain meshDomain )
    {
        super( name, valueDomain );

        this.meshDomain = meshDomain;

        evaluators = new ArrayList<MapEntry>();
    }


    public void setEvaluator( int indexValue, NodeDofEvaluator evaluator )
    {
        evaluators.add( new MapEntry( indexValue, evaluator ) );
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public D evaluate( DomainValue... input )
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
                return (D)valueDomain.getValue( 0, e.evaluator.evaluate( v ) );
            }
        }

        return null;
    }
}
