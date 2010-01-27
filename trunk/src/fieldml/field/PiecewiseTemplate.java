package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.map.ContinuousMap;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class PiecewiseTemplate
{
    public static class TemplateMap
    {
        public final int element;
        
        @SerializationAsString
        public final ContinuousMap map;

        public final int dofSet;


        public TemplateMap( int element, ContinuousMap map, int dofSet )
        {
            this.element = element;
            this.map = map;
            this.dofSet = dofSet;
        }
    }

    public final String name;

    @SerializationAsString
    public final MeshDomain meshDomain;

    public final List<TemplateMap> elementMaps;

    public final int totalDofSets;


    public PiecewiseTemplate( String name, MeshDomain meshDomain, int totalDofSets )
    {
        this.name = name;
        this.meshDomain = meshDomain;
        this.totalDofSets = totalDofSets;

        elementMaps = new ArrayList<TemplateMap>();
        for( int i = 0; i <= meshDomain.elementDomain.getValueCount(); i++ )
        {
            elementMaps.add( null );
        }
    }


    public void setMap( int index, ContinuousMap map, int dofSet )
    {
        elementMaps.set( index, new TemplateMap( index, map, dofSet ) );
    }


    public double evaluate( DomainValues context, ContinuousEvaluator[] dofs )
    {
        MeshDomainValue v = context.get( meshDomain );

        TemplateMap templateMap = elementMaps.get( v.indexValue );

        if( templateMap != null )
        {
            return templateMap.map.evaluate( context, dofs[templateMap.dofSet - 1] );
        }

        assert false;

        return 0;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
