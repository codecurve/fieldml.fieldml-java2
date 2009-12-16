package fieldml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

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
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.PiecewiseField;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.field.PiecewiseTemplate;
import fieldml.function.LinearLagrange;
import fieldml.function.QuadraticBSpline;
import fieldml.function.QuadraticLagrange;
import fieldml.io.DOTReflectiveHandler;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldmlx.util.MinimalColladaExporter;

public class TimeVaryingExample
{
    private static void serialize( Region region )
    {
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____3____4____5\n" );

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

        try
        {
            PrintStream printStream = new PrintStream( new File( "trunk/doc/QuadraticBSpline.dot" ) );

            DOTReflectiveHandler dotHandler = new DOTReflectiveHandler( printStream );
            region.walkObjects( dotHandler );
            printStream.println( "}" );// HACK!
            printStream.close();
        }
        catch( IOException e )
        {
            System.err.println( e );
        }
    }

    private static double[] rawDofs =
    { 0.954915, 1.0450850, -0.427051, -1.190983, -0.427051, 1.0450850, 0.954915 };


    private static void test( Region region )
    {
        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "test_mesh.element.parameters" );
        ContinuousEvaluator meshZ = region.getContinuousEvaluator( "test_mesh.coordinates.z" );

        ContinuousDomainValue output;

        double params[] = new double[3];
        double xi[] = new double[1];
        double expectedValue;

        xi[0] = 0.25;
        params[0] = rawDofs[0];
        params[1] = rawDofs[1];
        params[2] = rawDofs[2];
        output = meshZ.evaluate( meshDomain, 1, xi );
        expectedValue = QuadraticBSpline.evaluate( params, xi );

        assert output.values[0] == expectedValue;

        xi[0] = 0.48;
        params[0] = rawDofs[3];
        params[1] = rawDofs[4];
        params[2] = rawDofs[5];
        output = meshZ.evaluate( meshDomain, 4, xi );
        expectedValue = QuadraticBSpline.evaluate( params, xi );

        assert output.values[0] == expectedValue;
    }


    public static void main( String[] args )
    {
        Region bsplineRegion = QuadraticBSplineExample.buildRegion();

        Region library = Region.getLibrary();
        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );

        Region tvRegion = new Region( "tv_test" );

        EnsembleDomain timeElementDomain = new EnsembleDomain( "tv_test.time.elements" );
        timeElementDomain.addValues( 1, 2, 3 );
        tvRegion.addDomain( timeElementDomain );

        MeshDomain timeMeshDomain = new MeshDomain( "tv_test.time.mesh", 1, timeElementDomain );
        timeMeshDomain.setDefaultShape( "line_0_1" );
        tvRegion.addDomain( timeMeshDomain );

        EnsembleDomain timeDofsDomain = new EnsembleDomain( "tv_test.time.dofs.domain" );
        timeDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        tvRegion.addDomain( timeDofsDomain );

        ContinuousParameters timeDofs = new ContinuousParameters( "tv_test.time.dofs.values", rc1CoordinatesDomain, timeDofsDomain );
        timeDofs.setValue( 0, 1 );
        timeDofs.setValue( 1, 2 );
        timeDofs.setValue( 2.5, 3 );
        timeDofs.setValue( 4, 4 );
        timeDofs.setValue( 5.0, 5 );
        timeDofs.setValue( 6.625, 6 );
        timeDofs.setValue( 10.0, 7 );// Deliberately non-linear. C1 continuous for my own amusement.
        tvRegion.addEvaluator( timeDofs );

        EnsembleDomain line2LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.line.2" );

        EnsembleParameters elementDofIndexes = new EnsembleParameters( "tv_test.time.element_dof_indexes", timeDofsDomain,
            timeElementDomain, line2LocalNodeDomain );
        elementDofIndexes.setValue( 1, 1, 1 );
        elementDofIndexes.setValue( 2, 1, 2 );
        elementDofIndexes.setValue( 3, 1, 3 );

        elementDofIndexes.setValue( 3, 2, 1 );
        elementDofIndexes.setValue( 4, 2, 2 );
        elementDofIndexes.setValue( 5, 2, 3 );

        elementDofIndexes.setValue( 5, 3, 1 );
        elementDofIndexes.setValue( 6, 3, 2 );
        elementDofIndexes.setValue( 7, 3, 3 );

        tvRegion.addEvaluator( elementDofIndexes );

        PiecewiseTemplate meshTimeTemplate = new PiecewiseTemplate( "tv_test.time.template", timeMeshDomain );
        meshTimeTemplate.addFunction( new QuadraticLagrange( "quadratic_lagrange", rc1CoordinatesDomain, elementDofIndexes,
            line2LocalNodeDomain ) );
        meshTimeTemplate.setFunction( 1, "quadratic_lagrange" );
        meshTimeTemplate.setFunction( 2, "quadratic_lagrange" );
        meshTimeTemplate.setFunction( 3, "quadratic_lagrange" );
        tvRegion.addPiecewiseTemplate( meshTimeTemplate );

        PiecewiseField meshTime = new PiecewiseField( "tv_test.time", rc1CoordinatesDomain, meshTimeTemplate );
        meshTime.addDofs( timeDofs );
        tvRegion.addEvaluator( meshTime );

        ContinuousDomain weighting = library.getContinuousDomain( "library.weighting.1d" );
        EnsembleDomain bsplineDofsDomain = bsplineRegion.getEnsembleDomain( "test_mesh.dofs" );

        ContinuousParameters dofs = new ContinuousParameters( "tv_test.dofs.z", weighting, bsplineDofsDomain, timeDofsDomain );
        dofs.setValue( 0.954915, 1, 1 );
        dofs.setValue( 1.0450850, 2, 1 );
        dofs.setValue( -0.427051, 3, 1 );
        dofs.setValue( -1.190983, 4, 1 );
        dofs.setValue( -0.427051, 5, 1 );
        dofs.setValue( 1.0450850, 6, 1 );
        dofs.setValue( 0.954915, 7, 1 );

        dofs.setValue( 0.0, 1, 2 );
        dofs.setValue( 0.0, 2, 2 );
        dofs.setValue( 0.0, 3, 2 );
        dofs.setValue( 0.0, 4, 2 );
        dofs.setValue( 0.0, 5, 2 );
        dofs.setValue( 0.0, 6, 2 );
        dofs.setValue( 0.0, 7, 2 );

        dofs.setValue( -0.954915, 1, 3 );
        dofs.setValue( -1.0450850, 2, 3 );
        dofs.setValue( 0.427051, 3, 3 );
        dofs.setValue( 1.190983, 4, 3 );
        dofs.setValue( 0.427051, 5, 3 );
        dofs.setValue( -1.0450850, 6, 3 );
        dofs.setValue( -0.954915, 7, 3 );

        dofs.setValue( 0.0, 1, 4 );
        dofs.setValue( 0.0, 2, 4 );
        dofs.setValue( 0.0, 3, 4 );
        dofs.setValue( 0.0, 4, 4 );
        dofs.setValue( 0.0, 5, 4 );
        dofs.setValue( 0.0, 6, 4 );
        dofs.setValue( 0.0, 7, 4 );

        dofs.setValue( 0.954915, 1, 5 );
        dofs.setValue( 1.0450850, 2, 5 );
        dofs.setValue( -0.427051, 3, 5 );
        dofs.setValue( -1.190983, 4, 5 );
        dofs.setValue( -0.427051, 5, 5 );
        dofs.setValue( 1.0450850, 6, 5 );
        dofs.setValue( 0.954915, 7, 5 );

        dofs.setValue( 0.0, 1, 6 );
        dofs.setValue( 0.0, 2, 6 );
        dofs.setValue( 0.0, 3, 6 );
        dofs.setValue( 0.0, 4, 6 );
        dofs.setValue( 0.0, 5, 6 );
        dofs.setValue( 0.0, 6, 6 );
        dofs.setValue( 0.0, 7, 6 );

        dofs.setValue( -0.954915, 1, 7 );
        dofs.setValue( -1.0450850, 2, 7 );
        dofs.setValue( 0.427051, 3, 7 );
        dofs.setValue( 1.190983, 4, 7 );
        dofs.setValue( 0.427051, 5, 7 );
        dofs.setValue( -1.0450850, 6, 7 );
        dofs.setValue( -0.954915, 7, 7 );

        ContinuousEvaluator bsplineDofWeights = bsplineRegion.getContinuousEvaluator( "test_mesh.element_dof_weights" );

        ContinuousMap extrudedDofMap = new ContinuousMap( "tv_test.element_dof_map", bsplineDofWeights, bsplineDofsDomain );
        tvRegion.addMap( extrudedDofMap );

        ContinuousDomain bsplineParamsDomain = library.getContinuousDomain( "library.quadratic_bspline.parameters" );

        ContinuousCompositeEvaluator extrudedParametersMerged = new ContinuousCompositeEvaluator( "tv_test.element.parameters_merged",
            bsplineParamsDomain );
        extrudedParametersMerged.importMap( bsplineParamsDomain, dofs, extrudedDofMap );
        tvRegion.addEvaluator( extrudedParametersMerged );

        PiecewiseTemplate bsplineTemplate = bsplineRegion.getPiecewiseTemplate( "test_mesh.coordinates" );

        PiecewiseField slicedZ = new PiecewiseField( "tv_test.coordinates.sliced_z", rc1CoordinatesDomain, bsplineTemplate );
        slicedZ.addDofs( extrudedParametersMerged );
        tvRegion.addEvaluator( slicedZ );

        PiecewiseField zValue = new PiecewiseField( "tv_test.coordinates.z", rc1CoordinatesDomain, meshTimeTemplate );
        zValue.addDofs( slicedZ );
        tvRegion.addEvaluator( zValue );

        // test( tvRegion );

        // serialize( tvRegion );

        try
        {
            // These are only for visualization. Do not serialize.
            EnsembleDomain globalNodesDomain = bsplineRegion.getEnsembleDomain( "test_mesh.nodes" );
            EnsembleDomain lineLocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.line.1" );
            EnsembleEvaluator lineNodeList = bsplineRegion.getEnsembleEvaluator( "test_mesh.line_nodes" );
            MeshDomain meshDomain = bsplineRegion.getMeshDomain( "test_mesh.domain" );

            ContinuousParameters nodalX = new ContinuousParameters( "test_mesh.node.x", rc1CoordinatesDomain, globalNodesDomain );
            nodalX.setValue( 0.0, 1 );
            nodalX.setValue( 1.0, 2 );
            nodalX.setValue( 2.0, 3 );
            nodalX.setValue( 3.0, 4 );
            nodalX.setValue( 4.0, 5 );
            nodalX.setValue( 5.0, 6 );

            PiecewiseTemplate linearMeshCoordinates = new PiecewiseTemplate( "test_mesh.linear_coordinates", meshDomain );
            linearMeshCoordinates.addFunction( new LinearLagrange( "linear", rc1CoordinatesDomain, lineNodeList, lineLocalNodeDomain ) );
            linearMeshCoordinates.setFunction( 1, "linear" );
            linearMeshCoordinates.setFunction( 2, "linear" );
            linearMeshCoordinates.setFunction( 3, "linear" );
            linearMeshCoordinates.setFunction( 4, "linear" );
            linearMeshCoordinates.setFunction( 5, "linear" );
            tvRegion.addPiecewiseTemplate( linearMeshCoordinates );

            PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", rc1CoordinatesDomain, linearMeshCoordinates );
            meshCoordinatesX.addDofs( nodalX );

            ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.co-ordinates.rc.3d" );

            ContinuousAggregateEvaluator testCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh3DDomain );
            testCoordinates.setSourceField( 1, meshCoordinatesX );
            testCoordinates.setSourceField( 2, meshTime );
            testCoordinates.setSourceField( 3, zValue );

            tvRegion.addEvaluator( testCoordinates );

            MeshDomain bsplineDomain = bsplineRegion.getMeshDomain( "test_mesh.domain" );
            tvRegion.addDomain( bsplineDomain );

            String collada = MinimalColladaExporter.exportFromFieldML( tvRegion, 16, "test_mesh.coordinates", "test_mesh.domain",
                "tv_test.time.mesh" );
            FileWriter f = new FileWriter( "trunk/data/collada tv b-spline.xml" );
            f.write( collada );
            f.close();
        }
        catch( IOException e )
        {
        }
    }
}
