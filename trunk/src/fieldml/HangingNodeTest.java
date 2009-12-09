package fieldml;

import java.io.IOException;

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
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.PiecewiseField;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.function.BilinearQuad;
import fieldml.function.DirectBilinearLagrange;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class HangingNodeTest
{
    private static void serialize( Region region )
    {
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


    private static void test( Region region )
    {
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
    private static void virtualNodeExample()
    {
        Region library = Region.getLibrary();

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        Region testRegion = new Region( "test" );

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

        ContinuousParameters p2nArithmeticMeanMap = new ContinuousParameters( "test_mesh.p2nAMap", weighting, localDofsDomain,
            globalDofsDomain );
        p2nArithmeticMeanMap.setDefaultValue( 0 );
        p2nArithmeticMeanMap.setValue( 1.0, 1, 1 );
        p2nArithmeticMeanMap.setValue( 1.0, 2, 2 );
        p2nArithmeticMeanMap.setValue( 1.0, 3, 3 );
        p2nArithmeticMeanMap.setValue( 0.5, 4, 2 );
        p2nArithmeticMeanMap.setValue( 0.5, 4, 6 );
        p2nArithmeticMeanMap.setValue( 1.0, 5, 4 );
        p2nArithmeticMeanMap.setValue( 1.0, 6, 5 );
        p2nArithmeticMeanMap.setValue( 1.0, 7, 6 );
        p2nArithmeticMeanMap.setValue( 1.0, 8, 7 );

        testRegion.addEvaluator( p2nArithmeticMeanMap );

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
        meshX.importMappedField( mesh1DDomain, meshPointsX, p2nArithmeticMeanMap, globalDofsDomain );
        testRegion.addEvaluator( meshX );

        ContinuousCompositeEvaluator meshY = new ContinuousCompositeEvaluator( "test_mesh.point.y", mesh1DDomain );
        meshY.importMappedField( mesh1DDomain, meshPointsY, p2nArithmeticMeanMap, globalDofsDomain );
        testRegion.addEvaluator( meshY );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshDomain );
        meshCoordinatesX.addEvaluator( new BilinearQuad( "bilinear_quad", meshX, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesX.setEvaluator( 1, "bilinear_quad" );
        meshCoordinatesX.setEvaluator( 2, "bilinear_quad" );
        meshCoordinatesX.setEvaluator( 3, "bilinear_quad" );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshDomain );
        meshCoordinatesY.addEvaluator( new BilinearQuad( "bilinear_quad", meshY, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.setEvaluator( 1, "bilinear_quad" );
        meshCoordinatesY.setEvaluator( 2, "bilinear_quad" );
        meshCoordinatesY.setEvaluator( 3, "bilinear_quad" );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        test( testRegion );

        serialize( testRegion );
    }


    /**
     * This example creates a local node set from the available dofs, adding 'virtual' nodes as needed, then using a standard
     * element x localnode -> globalnode lookup for generating the dof vectors needed for interpolation.
     */
    private static void directMapExample()
    {
        Region library = Region.getLibrary();

        Region testRegion = new Region( "test" );

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

        ContinuousParameters bilinearMapP1 = new ContinuousParameters( "test_mesh.dof_map.bilinear.p1", weighting, globalDofsDomain,
            testMeshElementDomain );
        bilinearMapP1.setDefaultValue( 0 );
        bilinearMapP1.setValue( 1.0, 5, 1 );
        bilinearMapP1.setValue( 0.5, 2, 2 );
        bilinearMapP1.setValue( 0.5, 6, 2 );
        bilinearMapP1.setValue( 1.0, 6, 3 );

        testRegion.addEvaluator( bilinearMapP1 );

        ContinuousParameters bilinearMapP2 = new ContinuousParameters( "test_mesh.dof_map.bilinear.p2", weighting, globalDofsDomain,
            testMeshElementDomain );
        bilinearMapP2.setDefaultValue( 0 );
        bilinearMapP2.setValue( 1.0, 6, 1 );
        bilinearMapP2.setValue( 1.0, 4, 2 );
        bilinearMapP2.setValue( 1.0, 7, 3 );

        testRegion.addEvaluator( bilinearMapP2 );

        ContinuousParameters bilinearMapP3 = new ContinuousParameters( "test_mesh.dof_map.bilinear.p3", weighting, globalDofsDomain,
            testMeshElementDomain );
        bilinearMapP3.setDefaultValue( 0 );
        bilinearMapP3.setValue( 1.0, 1, 1 );
        bilinearMapP3.setValue( 1.0, 2, 2 );
        bilinearMapP3.setValue( 0.5, 2, 3 );
        bilinearMapP3.setValue( 0.5, 6, 3 );

        testRegion.addEvaluator( bilinearMapP3 );

        ContinuousParameters bilinearMapP4 = new ContinuousParameters( "test_mesh.dof_map.bilinear.p4", weighting, globalDofsDomain,
            testMeshElementDomain );
        bilinearMapP4.setDefaultValue( 0 );
        bilinearMapP4.setValue( 1.0, 2, 1 );
        bilinearMapP4.setValue( 1.0, 3, 2 );
        bilinearMapP4.setValue( 1.0, 4, 3 );

        testRegion.addEvaluator( bilinearMapP4 );

        ContinuousDomain bilinearLagrangeParametersDomain = library.getContinuousDomain( "library.bilinear_lagrange.parameters" );

        ContinuousAggregateEvaluator bilinearLagrangeParameters = new ContinuousAggregateEvaluator(
            "test_mesh.bilinear_lagrange.parameter_map", bilinearLagrangeParametersDomain );

        bilinearLagrangeParameters.setSourceField( 1, bilinearMapP1 );
        bilinearLagrangeParameters.setSourceField( 2, bilinearMapP2 );
        bilinearLagrangeParameters.setSourceField( 3, bilinearMapP3 );
        bilinearLagrangeParameters.setSourceField( 4, bilinearMapP4 );

        testRegion.addEvaluator( bilinearLagrangeParameters );

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

        ContinuousCompositeEvaluator meshX = new ContinuousCompositeEvaluator( "test_mesh.bilinear_lagrange.parameters.x", bilinearLagrangeParametersDomain );
        meshX.importMappedField( bilinearLagrangeParametersDomain, meshPointsX, bilinearLagrangeParameters, globalDofsDomain );
        testRegion.addEvaluator( meshX );

        ContinuousCompositeEvaluator meshY = new ContinuousCompositeEvaluator( "test_mesh.bilinear_lagrange.parameters.y", bilinearLagrangeParametersDomain );
        meshY.importMappedField( bilinearLagrangeParametersDomain, meshPointsY, bilinearLagrangeParameters, globalDofsDomain );
        testRegion.addEvaluator( meshY );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshDomain );
        meshCoordinatesX.addEvaluator( new DirectBilinearLagrange( "bilinear_lagrange", meshX ) );
        meshCoordinatesX.setEvaluator( 1, "bilinear_lagrange" );
        meshCoordinatesX.setEvaluator( 2, "bilinear_lagrange" );
        meshCoordinatesX.setEvaluator( 3, "bilinear_lagrange" );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshDomain );
        meshCoordinatesY.addEvaluator( new DirectBilinearLagrange( "bilinear_lagrange", meshY ) );
        meshCoordinatesY.setEvaluator( 1, "bilinear_lagrange" );
        meshCoordinatesY.setEvaluator( 2, "bilinear_lagrange" );
        meshCoordinatesY.setEvaluator( 3, "bilinear_lagrange" );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        test( testRegion );

        serialize( testRegion );
    }


    public static void main( String[] args )
    {
        // virtualNodeExample();

        directMapExample();
    }
}
