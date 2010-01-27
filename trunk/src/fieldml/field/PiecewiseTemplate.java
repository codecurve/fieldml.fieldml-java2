package fieldml.field;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.map.ContinuousMap;
import fieldml.util.SimpleMap;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class PiecewiseTemplate
{
    public static class TemplateMap
    {
        @SerializationAsString
        public final ContinuousMap map;

        public final int dofSet;


        public TemplateMap( ContinuousMap map, int dofSet )
        {
            this.map = map;
            this.dofSet = dofSet - 1;
        }
    }

    public final String name;

    @SerializationAsString
    public final MeshDomain meshDomain;

    public final SimpleMap<Integer, TemplateMap> maps;

    public final int totalDofSets;


    public PiecewiseTemplate( String name, MeshDomain meshDomain, int totalDofSets )
    {
        this.name = name;
        this.meshDomain = meshDomain;
        this.totalDofSets = totalDofSets;

        maps = new SimpleMap<Integer, TemplateMap>();
    }


    public void setMap( int index, ContinuousMap map, int dofSet )
    {
        maps.put( index, new TemplateMap( map, dofSet ) );
    }


    public double evaluate( DomainValues context, ContinuousEvaluator[] dofs )
    {
        MeshDomainValue v = context.get( meshDomain );

        TemplateMap templateMap = maps.get( v.indexValue );

        if( templateMap != null )
        {
            return templateMap.map.evaluate( context, dofs[templateMap.dofSet] );
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
