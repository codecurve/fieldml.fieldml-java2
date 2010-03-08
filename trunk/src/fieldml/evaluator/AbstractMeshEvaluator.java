package fieldml.evaluator;

import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class AbstractMeshEvaluator
    extends AbstractEvaluator<MeshDomain, MeshDomainValue>
    implements MeshEvaluator
{
    /**
     * This is something of a hack class until we remove MeshDomainValue, and allow multiple return values (particularly from
     * MeshEvaluators).
     */
    private class XiEvaluator
        implements ContinuousEvaluator
    {
        @SerializationAsString
        public MeshEvaluator meshEvaluator;


        public XiEvaluator( MeshEvaluator meshEvaluator )
        {
            this.meshEvaluator = meshEvaluator;
        }


        @Override
        public ContinuousDomainValue evaluate( DomainValues context )
        {
            MeshDomainValue value = meshEvaluator.evaluate( context );

            return meshEvaluator.getValueDomain().getXiDomain().makeValue( value.chartValues );
        }


        @Override
        public String getName()
        {
            return meshEvaluator.getName();
        }


        @Override
        public ContinuousDomain getValueDomain()
        {
            return meshEvaluator.getValueDomain().getXiDomain();
        }


        public String toString()
        {
            return getName();
        }


        @Override
        public Collection<? extends Evaluator<?>> getVariables()
        {
            return AbstractMeshEvaluator.this.getVariables();
        }
    }

    private class ElementEvaluator
        implements EnsembleEvaluator
    {
        @SerializationAsString
        public MeshEvaluator meshEvaluator;


        public ElementEvaluator( MeshEvaluator meshEvaluator )
        {
            this.meshEvaluator = meshEvaluator;
        }


        @Override
        public EnsembleDomainValue evaluate( DomainValues context )
        {
            MeshDomainValue value = meshEvaluator.evaluate( context );

            return meshEvaluator.getValueDomain().getElementDomain().makeValue( value.indexValue );
        }


        @Override
        public String getName()
        {
            return meshEvaluator.getName();
        }


        @Override
        public EnsembleDomain getValueDomain()
        {
            return meshEvaluator.getValueDomain().getElementDomain();
        }


        public String toString()
        {
            return getName();
        }


        @Override
        public Collection<? extends Evaluator<?>> getVariables()
        {
            return AbstractMeshEvaluator.this.getVariables();
        }
    }


    public AbstractMeshEvaluator( String name, MeshDomain valueDomain )
    {
        super( name, valueDomain );
    }


    @Override
    public abstract MeshDomainValue evaluate( DomainValues context );


    public ContinuousEvaluator getXiEvaluator()
    {
        return new XiEvaluator( this );
    }


    public EnsembleEvaluator getElementEvaluator()
    {
        return new ElementEvaluator( this );
    }
}
