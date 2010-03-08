package fieldml.evaluator.hardcoded;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.AbstractMeshEvaluator;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class RegularLinearSubdivision
    extends AbstractMeshEvaluator
{
    @SerializationAsString
    public final MeshDomain sourceDomain;

    private final int divisions;


    public RegularLinearSubdivision( String name, MeshDomain destinationDomain, MeshDomain sourceDomain )
    {
        super( name, destinationDomain );

        this.sourceDomain = sourceDomain;

        assert sourceDomain.getXiDomain().componentCount == 1;
        assert destinationDomain.getXiDomain().componentCount == 1;

        divisions = destinationDomain.getElementDomain().getValueCount();
    }


    @Override
    public MeshDomainValue evaluate( DomainValues context )
    {
        MeshDomainValue sourceValue = context.get( sourceDomain );

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
}
