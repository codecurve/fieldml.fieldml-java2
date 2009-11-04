package fieldml.field;

import java.util.HashMap;
import java.util.Map;

import fieldml.annotations.SerializeToString;
import fieldml.domain.Domain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.Evaluator;
import fieldml.evaluator.NodeDofEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.MeshDomainValue;

public class FEMField<D extends DomainValue>
    extends Field<D>
{
    @SerializeToString
    public final MeshDomain meshDomain;

    private final Map<Integer, Evaluator> evaluators;


    public FEMField( String name, Domain valueDomain, MeshDomain meshDomain )
    {
        super( name, valueDomain );

        this.meshDomain = meshDomain;

        evaluators = new HashMap<Integer, Evaluator>();
    }


    public void setEvaluator( int indexValue, NodeDofEvaluator evaluator )
    {
        evaluators.put( indexValue, evaluator );
    }


    @Override
    @SuppressWarnings("unchecked")
    public D evaluate( DomainValue... input )
    {
        if( input[0].domain != meshDomain )
        {
            return null;
        }

        MeshDomainValue v = (MeshDomainValue)input[0];

        Evaluator e = evaluators.get( v.indexValue );
        if( e == null )
        {
            return null;
        }

        return (D)valueDomain.getValue( 0, e.evaluate( v ) );
    }
}
