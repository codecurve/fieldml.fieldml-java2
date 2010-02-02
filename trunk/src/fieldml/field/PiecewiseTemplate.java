package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class PiecewiseTemplate
{
    public static class TemplateMap
    {
        public final int element;
        
        @SerializationAsString
        public final ContinuousEvaluator evaluator;


        public TemplateMap( int element, ContinuousEvaluator evaluator )
        {
            this.element = element;
            this.evaluator = evaluator;
        }
    }

    public final String name;

    @SerializationAsString
    public final MeshDomain meshDomain;

    public final List<TemplateMap> elementMaps;


    public PiecewiseTemplate( String name, MeshDomain meshDomain )
    {
        this.name = name;
        this.meshDomain = meshDomain;

        elementMaps = new ArrayList<TemplateMap>();
        for( int i = 0; i <= meshDomain.elementDomain.getValueCount(); i++ )
        {
            elementMaps.add( null );
        }
    }


    public void setEvaluator( int index, ContinuousEvaluator evaluator )
    {
        elementMaps.set( index, new TemplateMap( index, evaluator ) );
    }


    public double[] evaluate( DomainValues context )
    {
        MeshDomainValue v = context.get( meshDomain );

        TemplateMap templateMap = elementMaps.get( v.indexValue );

        if( templateMap != null )
        {
            return templateMap.evaluator.evaluate( context ).values;
        }

        assert false;

        return null;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
