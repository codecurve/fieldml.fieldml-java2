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
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.hardcoded.BilinearLagrange;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.JdomReflectiveHandler;
import fieldml.map.IndirectMap;
import fieldml.map.NestedMap;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class HangingNodeTest
    extends TestCase
{
    public static String REGION_NAME = "HangingNode_Test";
    
    public void testSerialization()
    {
//      Region region = buildDirectMapRegion();
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
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName()  + ".xml");
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

        ContinuousDomainValue output;

        output = meshXY.evaluate( meshDomain, 1, 0.5, 0.5 );
        assert output.values[0] == 10;
        assert output.values[1] == 10;

        output = meshXY.evaluate( meshDomain, 2, 0.5, 0.5 );
        assert output.values[0] == 25;
        assert output.values[1] == 15;

        output = meshXY.evaluate( meshDomain, 3, 0.5, 0.5 );
        assert output.values[0] == 25;
        assert output.values[1] == 5;

        region = buildDirectMapRegion();
        
        meshDomain = region.getMeshDomain( "test_mesh.domain" );
        meshXY = region.getContinuousEvaluator( "test_mesh.coordinates.xy" );

        output = meshXY.evaluate( meshDomain, 1, 0.5, 0.5 );
        assert output.values[0] == 10;
        assert output.values[1] == 10;

        output = meshXY.evaluate( meshDomain, 2, 0.5, 0.5 );
        assert output.values[0] == 25;
        assert output.values[1] == 15;

        output = meshXY.evaluate( meshDomain, 3, 0.5, 0.5 );
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

        ContinuousListParameters p2nArithmeticMeanWeights = new ContinuousListParameters( "test_mesh.p2nA.weights", weighting, localDofsDomain );
        p2nArithmeticMeanWeights.setValue( 1, 1.0 );
        p2nArithmeticMeanWeights.setValue( 2, 1.0 );
        p2nArithmeticMeanWeights.setValue( 3, 1.0 );
        p2nArithmeticMeanWeights.setValue( 4, 0.5, 0.5 );
        p2nArithmeticMeanWeights.setValue( 5, 1.0 );
        p2nArithmeticMeanWeights.setValue( 6, 1.0 );
        p2nArithmeticMeanWeights.setValue( 7, 1.0 );
        p2nArithmeticMeanWeights.setValue( 8, 1.0 );
        testRegion.addEvaluator( p2nArithmeticMeanWeights );

        EnsembleListParameters p2nIndexes = new EnsembleListParameters( "test_mesh.p2nA.indexes", globalDofListDomain, localDofsDomain );
        p2nIndexes.setValue( 1, 1 );
        p2nIndexes.setValue( 2, 2 );
        p2nIndexes.setValue( 3, 3 );
        p2nIndexes.setValue( 4, 2, 6 );
        p2nIndexes.setValue( 5, 4 );
        p2nIndexes.setValue( 6, 5 );
        p2nIndexes.setValue( 7, 6 );
        p2nIndexes.setValue( 8, 7 );
        testRegion.addEvaluator( p2nIndexes );

        IndirectMap p2nArithmeticMeanMap = new IndirectMap( "test_mesh.p2nA.map", p2nIndexes, p2nArithmeticMeanWeights );
        testRegion.addMap( p2nArithmeticMeanMap );
        
        EnsembleListParameters quadNodeList = new EnsembleListParameters( "test_mesh.quad_nodes", localDofListDomain, testMeshElementDomain );
        quadNodeList.setValue( 1, 6, 7, 1, 2 );
        quadNodeList.setValue( 2, 4, 5, 2, 3 );
        quadNodeList.setValue( 3, 7, 8, 4, 5 );

        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalDofsDomain );
        meshPointsX.setValue( 00.0, 1 );
        meshPointsX.setValue( 20.0, 2 );
        meshPointsX.setValue( 30.0, 3 );
        meshPointsX.setValue( 30.0, 4 );
        meshPointsX.setValue( 00.0, 5 );
        meshPointsX.setValue( 20.0, 6 );
        meshPointsX.setValue( 30.0, 7 );

        testRegion.addEvaluator( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalDofsDomain );
        meshPointsY.setValue( 20.0, 1 );
        meshPointsY.setValue( 20.0, 2 );
        meshPointsY.setValue( 20.0, 3 );
        meshPointsY.setValue( 10.0, 4 );
        meshPointsY.setValue( 00.0, 5 );
        meshPointsY.setValue( 00.0, 6 );
        meshPointsY.setValue( 00.0, 7 );

        testRegion.addEvaluator( meshPointsY );
        
        ContinuousListEvaluator bilinearLagrange = new BilinearLagrange( "test_mesh.mesh.bilinear_lagrange", meshDomain );
        testRegion.addEvaluator( bilinearLagrange );

        NestedMap elementBilinearLagrange = new NestedMap( "test_mesh.element.bilinear_lagrange_map", quadNodeList, bilinearLagrange, p2nArithmeticMeanMap );
        testRegion.addMap( elementBilinearLagrange );
        
        PiecewiseTemplate meshCoordinatesTemplate = new PiecewiseTemplate( "test_mesh.coordinates.template", meshDomain, 1 );
        meshCoordinatesTemplate.setMap( 1, elementBilinearLagrange, 1 );
        meshCoordinatesTemplate.setMap( 2, elementBilinearLagrange, 1 );
        meshCoordinatesTemplate.setMap( 3, elementBilinearLagrange, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.setDofs( 1, meshPointsX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.setDofs( 1, meshPointsY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );
        
        return testRegion;
    }


    /**
     * This example creates a local node set from the available dofs, adding 'virtual' nodes as needed, then using a standard
     * element x localnode -> globalnode lookup for generating the dof vectors needed for interpolation.
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
        
        ContinuousListParameters element1Weights = new ContinuousListParameters( "test_mesh.e1.dof_weights", weighting, quad1x1NodeDomain );
        element1Weights.setValue( 1, 1.0 );
        element1Weights.setValue( 2, 1.0 );
        element1Weights.setValue( 3, 1.0 );
        element1Weights.setValue( 4, 1.0 );
        
        EnsembleListParameters element1Indexes = new EnsembleListParameters( "test_mesh.e1.dof_indexes", globalDofIndexesDomain, quad1x1NodeDomain );
        element1Indexes.setValue( 1, 5 );
        element1Indexes.setValue( 2, 6 );
        element1Indexes.setValue( 3, 1 );
        element1Indexes.setValue( 4, 2 );
        
        IndirectMap element1Map = new IndirectMap( "test_mesh.e1.dof_map", element1Indexes, element1Weights );
        
        ContinuousListParameters element2Weights = new ContinuousListParameters( "test_mesh.e2.dof_weights", weighting, quad1x1NodeDomain );
        element2Weights.setValue( 1, 0.5, 0.5 );
        element2Weights.setValue( 2, 1.0 );
        element2Weights.setValue( 3, 1.0 );
        element2Weights.setValue( 4, 1.0 );
        
        EnsembleListParameters element2Indexes = new EnsembleListParameters( "test_mesh.e2.dof_indexes", globalDofIndexesDomain, quad1x1NodeDomain );
        element2Indexes.setValue( 1, 2, 6 );
        element2Indexes.setValue( 2, 4 );
        element2Indexes.setValue( 3, 2 );
        element2Indexes.setValue( 4, 3 );
        
        IndirectMap element2Map = new IndirectMap( "test_mesh.e2.dof_map", element2Indexes, element2Weights );

        ContinuousListParameters element3Weights = new ContinuousListParameters( "test_mesh.e3.dof_weights", weighting, quad1x1NodeDomain );
        element3Weights.setValue( 1, 1.0 );
        element3Weights.setValue( 2, 1.0 );
        element3Weights.setValue( 3, 0.5, 0.5 );
        element3Weights.setValue( 4, 1.0 );
        
        EnsembleListParameters element3Indexes = new EnsembleListParameters( "test_mesh.e3.dof_indexes", globalDofIndexesDomain, quad1x1NodeDomain );
        element3Indexes.setValue( 1, 5 );
        element3Indexes.setValue( 2, 7 );
        element3Indexes.setValue( 3, 2, 6 );
        element3Indexes.setValue( 4, 4 );

        IndirectMap element3Map = new IndirectMap( "test_mesh.e3.dof_map", element3Indexes, element3Weights );
        
        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalDofsDomain );
        meshPointsX.setValue( 00.0, 1 );
        meshPointsX.setValue( 20.0, 2 );
        meshPointsX.setValue( 30.0, 3 );
        meshPointsX.setValue( 30.0, 4 );
        meshPointsX.setValue( 00.0, 5 );
        meshPointsX.setValue( 20.0, 6 );
        meshPointsX.setValue( 30.0, 7 );

        testRegion.addEvaluator( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalDofsDomain );
        meshPointsY.setValue( 20.0, 1 );
        meshPointsY.setValue( 20.0, 2 );
        meshPointsY.setValue( 20.0, 3 );
        meshPointsY.setValue( 10.0, 4 );
        meshPointsY.setValue( 00.0, 5 );
        meshPointsY.setValue( 00.0, 6 );
        meshPointsY.setValue( 00.0, 7 );

        testRegion.addEvaluator( meshPointsY );

        EnsembleListParameters elementDofIndexes = new EnsembleListParameters( "test_mesh.element.dof_indexes", quad1x1NodeListDomain, testMeshElementDomain );
        elementDofIndexes.setValue( 1, 1, 2, 3, 4 );
        elementDofIndexes.setValue( 2, 1, 2, 3, 4 );
        elementDofIndexes.setValue( 3, 1, 2, 3, 4 );
        
        ContinuousListEvaluator bilinearQuad = new BilinearLagrange( "test_mesh.mesh.bilinear_lagrange", meshDomain );
        testRegion.addEvaluator( bilinearQuad );
        
        NestedMap element1Bilinear = new NestedMap( "test_mesh.element1.bilinear_lagrange", elementDofIndexes, bilinearQuad, element1Map );
        NestedMap element2Bilinear = new NestedMap( "test_mesh.element2.bilinear_lagrange", elementDofIndexes, bilinearQuad, element2Map );
        NestedMap element3Bilinear = new NestedMap( "test_mesh.element3.bilinear_lagrange", elementDofIndexes, bilinearQuad, element3Map );

        PiecewiseTemplate meshCoordinatesTemplate = new PiecewiseTemplate( "test_mesh.coordinates.template", meshDomain, 1 );
        meshCoordinatesTemplate.setMap( 1, element1Bilinear, 1 );
        meshCoordinatesTemplate.setMap( 2, element2Bilinear, 1 );
        meshCoordinatesTemplate.setMap( 3, element3Bilinear, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.setDofs( 1, meshPointsX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.setDofs( 1, meshPointsY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );
        
        return testRegion;
    }
}
