package fieldml;

import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format.TextMode;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.ContinuousListDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.ContinuousListParameters;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.MapEvaluator;
import fieldml.evaluator.hardcoded.BilinearLagrange;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class HangingNodeTest
    extends TestCase
{
    public static String REGION_NAME = "HangingNode_Test";


    public void testSerialization()
    {
        // Region region = buildDirectMapRegion();
        Region region = buildVirtualNodeRegion();

        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1______2____3\n" );
        s.append( "|      |    |\n" );
        s.append( "|      | *2 |\n" );
        s.append( "|      |    |\n" );
        s.append( "|  *1  4____5\n" );
        s.append( "|      |    |\n" );
        s.append( "|      | *3 |\n" );
        s.append( "6______7____8\n" );

        Comment comment1 = new Comment( s.toString() );
        root.addContent( comment1 );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( root );
        region.walkObjects( handler );

        Format format = Format.getPrettyFormat();
        format.setTextMode( TextMode.PRESERVE );
        XMLOutputter outputter = new XMLOutputter( format );
        try
        {
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName() + ".xml" );
            outputter.output( doc, output );
        }
        catch( IOException e )
        {
            System.err.println( e );
        }
    }


    public void testEvaluation()
    {
        Region region = buildVirtualNodeRegion();

        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        ContinuousEvaluator meshXY = region.getContinuousEvaluator( "test_mesh.coordinates.xy" );
        DomainValues context = new DomainValues();

        ContinuousDomainValue output;

        context.set( meshDomain, 1, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 10;
        assert output.values[1] == 10;

        context.set( meshDomain, 2, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 15;

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 5;

        region = buildDirectMapRegion();

        meshDomain = region.getMeshDomain( "test_mesh.domain" );
        meshXY = region.getContinuousEvaluator( "test_mesh.coordinates.xy" );

        context.set( meshDomain, 1, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 10;
        assert output.values[1] == 10;

        context.set( meshDomain, 2, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 15;

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 5;
    }


    /**
     * This example creates a local node set from the available dofs, adding 'virtual' nodes as needed, then using a standard
     * element x localnode -> globalnode lookup for generating the dof vectors needed for interpolation.
     */
    public static Region buildVirtualNodeRegion()
    {
        Region library = Region.getLibrary();

        Region testRegion = new Region( REGION_NAME );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "test_mesh.global_dofs" );
        globalDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        testRegion.addDomain( globalDofsDomain );

        EnsembleListDomain globalDofListDomain = new EnsembleListDomain( "test_mesh_global_dof_list", globalDofsDomain );
        testRegion.addDomain( globalDofListDomain );

        EnsembleDomain localDofsDomain = new EnsembleDomain( "test_mesh.local_dofs" );
        localDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8 );
        testRegion.addDomain( localDofsDomain );

        EnsembleListDomain localDofListDomain = new EnsembleListDomain( "test_mesh_local_dof_list", localDofsDomain );
        testRegion.addDomain( localDofListDomain );

        ContinuousListDomain weighting = library.getContinuousListDomain( "library.weighting.list" );

        ContinuousListParameters globalToLocalWeights = new ContinuousListParameters( "test_mesh.global_to_local.weights", weighting,
            localDofsDomain );
        globalToLocalWeights.setValue( 1, 1.0 );
        globalToLocalWeights.setValue( 2, 1.0 );
        globalToLocalWeights.setValue( 3, 1.0 );
        globalToLocalWeights.setValue( 4, 0.5, 0.5 );
        globalToLocalWeights.setValue( 5, 1.0 );
        globalToLocalWeights.setValue( 6, 1.0 );
        globalToLocalWeights.setValue( 7, 1.0 );
        globalToLocalWeights.setValue( 8, 1.0 );
        testRegion.addEvaluator( globalToLocalWeights );

        EnsembleListParameters globalToLocalIndexes = new EnsembleListParameters( "test_mesh.global_to_local.indexes", globalDofListDomain, localDofsDomain );
        globalToLocalIndexes.setValue( 1, 1 );
        globalToLocalIndexes.setValue( 2, 2 );
        globalToLocalIndexes.setValue( 3, 3 );
        globalToLocalIndexes.setValue( 4, 2, 6 );
        globalToLocalIndexes.setValue( 5, 4 );
        globalToLocalIndexes.setValue( 6, 5 );
        globalToLocalIndexes.setValue( 7, 6 );
        globalToLocalIndexes.setValue( 8, 7 );
        testRegion.addEvaluator( globalToLocalIndexes );

        EnsembleListParameters quadNodeList = new EnsembleListParameters( "test_mesh.quad_nodes", localDofListDomain, testMeshElementDomain );
        quadNodeList.setValue( 1, 6, 7, 1, 2 );
        quadNodeList.setValue( 2, 4, 5, 2, 3 );
        quadNodeList.setValue( 3, 7, 8, 4, 5 );

        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalDofsDomain );
        meshPointsX.setValue( 1, 00.0 );
        meshPointsX.setValue( 2, 20.0 );
        meshPointsX.setValue( 3, 30.0 );
        meshPointsX.setValue( 4, 30.0 );
        meshPointsX.setValue( 5, 00.0 );
        meshPointsX.setValue( 6, 20.0 );
        meshPointsX.setValue( 7, 30.0 );

        testRegion.addEvaluator( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalDofsDomain );
        meshPointsY.setValue( 1, 20.0 );
        meshPointsY.setValue( 2, 20.0 );
        meshPointsY.setValue( 3, 20.0 );
        meshPointsY.setValue( 4, 10.0 );
        meshPointsY.setValue( 5, 00.0 );
        meshPointsY.setValue( 6, 00.0 );
        meshPointsY.setValue( 7, 00.0 );

        testRegion.addEvaluator( meshPointsY );

        ContinuousListEvaluator bilinearLagrange = new BilinearLagrange( "test_mesh.mesh.bilinear_lagrange", meshDomain );
        testRegion.addEvaluator( bilinearLagrange );

        ContinuousVariableEvaluator globalDofs = new ContinuousVariableEvaluator( "test_mesh.dofs", mesh1DDomain );

        MapEvaluator localDofs = new MapEvaluator( "test_mesh.local_dofs", mesh1DDomain, globalToLocalIndexes, globalToLocalWeights, globalDofs );
        testRegion.addEvaluator( localDofs );

        MapEvaluator elementBilinearLagrange = new MapEvaluator( "test_mesh.element.bilinear_lagrange", mesh1DDomain, quadNodeList,
            bilinearLagrange, localDofs );
        testRegion.addEvaluator( elementBilinearLagrange );

        PiecewiseTemplate meshCoordinatesTemplate = new PiecewiseTemplate( "test_mesh.coordinates.template", meshDomain );
        meshCoordinatesTemplate.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesTemplate.setEvaluator( 2, elementBilinearLagrange );
        meshCoordinatesTemplate.setEvaluator( 3, elementBilinearLagrange );
        testRegion.addPiecewiseTemplate( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.setVariable( "test_mesh.dofs", meshPointsX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.setVariable( "test_mesh.dofs", meshPointsY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }


    /**
     * This example maps global dofs directly to per-element parameters.
     */
    public static Region buildDirectMapRegion()
    {
        Region library = Region.getLibrary();

        Region testRegion = new Region( REGION_NAME );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "test_mesh.global_dofs" );
        globalDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        testRegion.addDomain( globalDofsDomain );

        EnsembleListDomain globalDofIndexesDomain = new EnsembleListDomain( "test_mesh.global_dof_list", globalDofsDomain );

        ContinuousListDomain weighting = library.getContinuousListDomain( "library.weighting.list" );

        EnsembleDomain quad1x1NodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );
        EnsembleListDomain quad1x1NodeListDomain = new EnsembleListDomain( "test_mesh.1x1.nodes", quad1x1NodeDomain );

        ContinuousListParameters elementWeights = new ContinuousListParameters( "test_mesh.element.dof_weights", weighting,
            testMeshElementDomain, quad1x1NodeDomain );
        EnsembleListParameters elementIndexes = new EnsembleListParameters( "test_mesh.e1.dof_indexes", globalDofIndexesDomain,
            testMeshElementDomain, quad1x1NodeDomain );

        elementWeights.setValue( new int[]{ 1, 1 }, 1.0 );
        elementWeights.setValue( new int[]{ 1, 2 }, 1.0 );
        elementWeights.setValue( new int[]{ 1, 3 }, 1.0 );
        elementWeights.setValue( new int[]{ 1, 4 }, 1.0 );

        elementIndexes.setValue( new int[]{ 1, 1 }, 5 );
        elementIndexes.setValue( new int[]{ 1, 2 }, 6 );
        elementIndexes.setValue( new int[]{ 1, 3 }, 1 );
        elementIndexes.setValue( new int[]{ 1, 4 }, 2 );

        elementWeights.setValue( new int[]{ 2, 1 }, 0.5, 0.5 );
        elementWeights.setValue( new int[]{ 2, 2 }, 1.0 );
        elementWeights.setValue( new int[]{ 2, 3 }, 1.0 );
        elementWeights.setValue( new int[]{ 2, 4 }, 1.0 );

        elementIndexes.setValue( new int[]{ 2, 1 }, 2, 6 );
        elementIndexes.setValue( new int[]{ 2, 2 }, 4 );
        elementIndexes.setValue( new int[]{ 2, 3 }, 2 );
        elementIndexes.setValue( new int[]{ 2, 4 }, 3 );

        elementWeights.setValue( new int[]{ 3, 1 }, 1.0 );
        elementWeights.setValue( new int[]{ 3, 2 }, 1.0 );
        elementWeights.setValue( new int[]{ 3, 3 }, 0.5, 0.5 );
        elementWeights.setValue( new int[]{ 3, 4 }, 1.0 );

        elementIndexes.setValue( new int[]{ 3, 1 }, 5 );
        elementIndexes.setValue( new int[]{ 3, 2 }, 7 );
        elementIndexes.setValue( new int[]{ 3, 3 }, 2, 6 );
        elementIndexes.setValue( new int[]{ 3, 4 }, 4 );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalDofsDomain );
        meshPointsX.setValue( 1, 00.0 );
        meshPointsX.setValue( 2, 20.0 );
        meshPointsX.setValue( 3, 30.0 );
        meshPointsX.setValue( 4, 30.0 );
        meshPointsX.setValue( 5, 00.0 );
        meshPointsX.setValue( 6, 20.0 );
        meshPointsX.setValue( 7, 30.0 );

        testRegion.addEvaluator( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalDofsDomain );
        meshPointsY.setValue( 1, 20.0 );
        meshPointsY.setValue( 2, 20.0 );
        meshPointsY.setValue( 3, 20.0 );
        meshPointsY.setValue( 4, 10.0 );
        meshPointsY.setValue( 5, 00.0 );
        meshPointsY.setValue( 6, 00.0 );
        meshPointsY.setValue( 7, 00.0 );

        testRegion.addEvaluator( meshPointsY );

        EnsembleListParameters elementDofIndexes = new EnsembleListParameters( "test_mesh.element.dof_indexes", quad1x1NodeListDomain );
        elementDofIndexes.setValue( 1, 2, 3, 4 );

        ContinuousListEvaluator bilinearQuad = new BilinearLagrange( "test_mesh.mesh.bilinear_lagrange", meshDomain );
        testRegion.addEvaluator( bilinearQuad );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.dofs", mesh1DDomain );

        MapEvaluator bilinearElementDofs = new MapEvaluator( "test_mesh.element.bilinear_dofs", mesh1DDomain, elementIndexes,
            elementWeights, dofs );

        MapEvaluator elementBilinear = new MapEvaluator( "test_mesh.element.bilinear_lagrange", mesh1DDomain, elementDofIndexes,
            bilinearQuad, bilinearElementDofs );

        PiecewiseTemplate meshCoordinatesTemplate = new PiecewiseTemplate( "test_mesh.coordinates.template", meshDomain );
        meshCoordinatesTemplate.setEvaluator( 1, elementBilinear );
        meshCoordinatesTemplate.setEvaluator( 2, elementBilinear );
        meshCoordinatesTemplate.setEvaluator( 3, elementBilinear );
        testRegion.addPiecewiseTemplate( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.setVariable( "test_mesh.dofs", meshPointsX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.setVariable( "test_mesh.dofs", meshPointsY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }
}
