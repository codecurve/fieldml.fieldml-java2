package fieldml;

import java.io.IOException;

import junit.framework.TestCase;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format.TextMode;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousMap;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.function.BilinearQuad;
import fieldml.function.DirectBilinearLagrange;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class HangingNodeTest
    extends TestCase
{
    public static String REGION_NAME = "HangingNode_Test";
    
    public void testSerialization()
    {
        Region region = buildDirectMapRegion();

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
            outputter.output( doc, System.out );
        }
        catch( IOException e )
        {
            System.err.println( e );
        }
    }


    public void testEvaluation()
    {
        Region region = buildDirectMapRegion();
        
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
    }


    /**
     * This example creates a local node set from the available dofs, adding 'virtual' nodes as needed, then using a standard
     * element x localnode -> globalnode lookup for generating the dof vectors needed for interpolation.
     */
    public static Region buildVirtualNodeRegion()
    {
        Region library = Region.getLibrary();

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

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

        EnsembleDomain localDofsDomain = new EnsembleDomain( "test_mesh.local_dofs" );
        localDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8 );
        testRegion.addDomain( localDofsDomain );

        ContinuousDomain weighting = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters p2nArithmeticMeanWeights = new ContinuousParameters( "test_mesh.p2nA.weights", weighting, localDofsDomain,
            globalDofsDomain );
        p2nArithmeticMeanWeights.setDefaultValue( 0 );
        p2nArithmeticMeanWeights.setValue( 1.0, 1, 1 );
        p2nArithmeticMeanWeights.setValue( 1.0, 2, 2 );
        p2nArithmeticMeanWeights.setValue( 1.0, 3, 3 );
        p2nArithmeticMeanWeights.setValue( 0.5, 4, 2 );
        p2nArithmeticMeanWeights.setValue( 0.5, 4, 6 );
        p2nArithmeticMeanWeights.setValue( 1.0, 5, 4 );
        p2nArithmeticMeanWeights.setValue( 1.0, 6, 5 );
        p2nArithmeticMeanWeights.setValue( 1.0, 7, 6 );
        p2nArithmeticMeanWeights.setValue( 1.0, 8, 7 );

        testRegion.addEvaluator( p2nArithmeticMeanWeights );

        ContinuousMap p2nArithmeticMeanMap = new ContinuousMap( "test_mesh.p2nA.map", p2nArithmeticMeanWeights, globalDofsDomain );
        testRegion.addMap( p2nArithmeticMeanMap );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", localDofsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );

        quadNodeList.setValue( 6, 1, 1 );
        quadNodeList.setValue( 7, 1, 2 );
        quadNodeList.setValue( 1, 1, 3 );
        quadNodeList.setValue( 2, 1, 4 );

        quadNodeList.setValue( 4, 2, 1 );
        quadNodeList.setValue( 5, 2, 2 );
        quadNodeList.setValue( 2, 2, 3 );
        quadNodeList.setValue( 3, 2, 4 );

        quadNodeList.setValue( 7, 3, 1 );
        quadNodeList.setValue( 8, 3, 2 );
        quadNodeList.setValue( 4, 3, 3 );
        quadNodeList.setValue( 5, 3, 4 );

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

        ContinuousCompositeEvaluator meshX = new ContinuousCompositeEvaluator( "test_mesh.point.x", mesh1DDomain );
        meshX.importMap( mesh1DDomain, meshPointsX, p2nArithmeticMeanMap );
        testRegion.addEvaluator( meshX );

        ContinuousCompositeEvaluator meshY = new ContinuousCompositeEvaluator( "test_mesh.point.y", mesh1DDomain );
        meshY.importMap( mesh1DDomain, meshPointsY, p2nArithmeticMeanMap );
        testRegion.addEvaluator( meshY );

        PiecewiseTemplate meshCoordinatesTemplate = new PiecewiseTemplate( "test_mesh.coordinates.template", meshDomain );
        meshCoordinatesTemplate.addFunction( new BilinearQuad( "bilinear_quad", mesh1DDomain, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesTemplate.setFunction( 1, "bilinear_quad" );
        meshCoordinatesTemplate.setFunction( 2, "bilinear_quad" );
        meshCoordinatesTemplate.setFunction( 3, "bilinear_quad" );
        testRegion.addPiecewiseTemplate( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.addDofs( meshX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.addDofs( meshY );

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

        ContinuousDomain weighting = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters bilinearP1Weights = new ContinuousParameters( "test_mesh.dof_weights.bilinear.p1", weighting,
            globalDofsDomain, testMeshElementDomain );
        bilinearP1Weights.setDefaultValue( 0 );
        bilinearP1Weights.setValue( 1.0, 5, 1 );
        bilinearP1Weights.setValue( 0.5, 2, 2 );
        bilinearP1Weights.setValue( 0.5, 6, 2 );
        bilinearP1Weights.setValue( 1.0, 6, 3 );

        testRegion.addEvaluator( bilinearP1Weights );

        ContinuousParameters bilinearP2Weights = new ContinuousParameters( "test_mesh.dof_weights.bilinear.p2", weighting,
            globalDofsDomain, testMeshElementDomain );
        bilinearP2Weights.setDefaultValue( 0 );
        bilinearP2Weights.setValue( 1.0, 6, 1 );
        bilinearP2Weights.setValue( 1.0, 4, 2 );
        bilinearP2Weights.setValue( 1.0, 7, 3 );

        testRegion.addEvaluator( bilinearP2Weights );

        ContinuousParameters bilinearP3Weights = new ContinuousParameters( "test_mesh.dof_weights.bilinear.p3", weighting,
            globalDofsDomain, testMeshElementDomain );
        bilinearP3Weights.setDefaultValue( 0 );
        bilinearP3Weights.setValue( 1.0, 1, 1 );
        bilinearP3Weights.setValue( 1.0, 2, 2 );
        bilinearP3Weights.setValue( 0.5, 2, 3 );
        bilinearP3Weights.setValue( 0.5, 6, 3 );

        testRegion.addEvaluator( bilinearP3Weights );

        ContinuousParameters bilinearP4Weights = new ContinuousParameters( "test_mesh.dof_weights.bilinear.p4", weighting,
            globalDofsDomain, testMeshElementDomain );
        bilinearP4Weights.setDefaultValue( 0 );
        bilinearP4Weights.setValue( 1.0, 2, 1 );
        bilinearP4Weights.setValue( 1.0, 3, 2 );
        bilinearP4Weights.setValue( 1.0, 4, 3 );

        testRegion.addEvaluator( bilinearP4Weights );

        ContinuousDomain bilinearLagrangeParametersDomain = library.getContinuousDomain( "library.bilinear_lagrange.parameters" );

        ContinuousAggregateEvaluator bilinearLagrangeWeights = new ContinuousAggregateEvaluator(
            "test_mesh.bilinear_lagrange.parameter_weights", bilinearLagrangeParametersDomain );

        bilinearLagrangeWeights.setSourceField( 1, bilinearP1Weights );
        bilinearLagrangeWeights.setSourceField( 2, bilinearP2Weights );
        bilinearLagrangeWeights.setSourceField( 3, bilinearP3Weights );
        bilinearLagrangeWeights.setSourceField( 4, bilinearP4Weights );

        testRegion.addEvaluator( bilinearLagrangeWeights );

        ContinuousMap bilinearLagrangeMap = new ContinuousMap( "test_mesh.bilinear_lagrange.parameter_map", bilinearLagrangeWeights,
            globalDofsDomain );
        testRegion.addMap( bilinearLagrangeMap );

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

        ContinuousCompositeEvaluator meshX = new ContinuousCompositeEvaluator( "test_mesh.bilinear_lagrange.parameters.x",
            bilinearLagrangeParametersDomain );
        meshX.importMap( bilinearLagrangeParametersDomain, meshPointsX, bilinearLagrangeMap );
        testRegion.addEvaluator( meshX );

        ContinuousCompositeEvaluator meshY = new ContinuousCompositeEvaluator( "test_mesh.bilinear_lagrange.parameters.y",
            bilinearLagrangeParametersDomain );
        meshY.importMap( bilinearLagrangeParametersDomain, meshPointsY, bilinearLagrangeMap );
        testRegion.addEvaluator( meshY );

        PiecewiseTemplate meshCoordinatesTemplate = new PiecewiseTemplate( "test_mesh.coordinates.template", meshDomain );
        meshCoordinatesTemplate.addFunction( new DirectBilinearLagrange( "bilinear_lagrange", bilinearLagrangeParametersDomain ) );
        meshCoordinatesTemplate.setFunction( 1, "bilinear_lagrange" );
        meshCoordinatesTemplate.setFunction( 2, "bilinear_lagrange" );
        meshCoordinatesTemplate.setFunction( 3, "bilinear_lagrange" );
        testRegion.addPiecewiseTemplate( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.addDofs( meshX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.addDofs( meshY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );
        
        return testRegion;
    }
}
