package fieldml.evaluator.hardcoded;

import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractMeshEvaluator;
import fieldml.evaluator.Evaluator;
import fieldml.evaluator.MeshEvaluator;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class RegularLinearSubdivision
    extends AbstractMeshEvaluator
{
    @SerializationAsString
    public final MeshEvaluator source;

    private final int divisions;


    public RegularLinearSubdivision( String name, MeshDomain destinationDomain, MeshEvaluator source )
    {
        super( name, destinationDomain );

        this.source = source;

        assert source.getValueDomain().getXiDomain().componentCount == 1;
        assert destinationDomain.getXiDomain().componentCount == 1;

        divisions = destinationDomain.getElementDomain().getValueCount();
    }


    @Override
    public MeshDomainValue evaluate( DomainValues context )
    {
        MeshDomainValue sourceValue = source.evaluate( context );

        double upscale = sourceValue.chartValues[0] * divisions;
        int element = (int)( upscale );
        double xi = upscale - element;

        if( upscale < divisions )
        {
            element++;
        }
        else
        {
            xi = 1.0;
        }

        return valueDomain.makeValue( element, xi );
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        return source.getVariables();
    }
}
