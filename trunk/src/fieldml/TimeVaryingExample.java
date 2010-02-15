package fieldml;

import java.io.File;
import java.io.FileWriter;
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
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleListEvaluator;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.MapEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.function.QuadraticBSpline;
import fieldml.function.QuadraticLagrange;
import fieldml.io.DOTReflectiveHandler;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.util.MinimalColladaExporter;

public class TimeVaryingExample
    extends TestCase
{
    public void testSerialize()
    {
        Region region = buildRegion();

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
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName() + ".xml" );
            outputter.output( doc, output );
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


    public void testEvaluation()
    {
        Region testRegion = buildRegion();
        Region bsplineRegion = testRegion.getSubregion( QuadraticBSplineExample.REGION_NAME );

        MeshDomain timeMeshDomain = testRegion.getMeshDomain( "tv_test.time.mesh" );
        MeshDomain splineMeshDomain = bsplineRegion.getMeshDomain( "test_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "test_mesh.element.parameters" );
        ContinuousEvaluator meshZ = testRegion.getContinuousEvaluator( "tv_test.coordinates.z" );

        DomainValues context = new DomainValues();

        ContinuousDomainValue output;

        double[] interpolatorValues = new double[3];
        double params[] = new double[3];
        double xi[] = new double[1];
        double timeXi[] = new double[1];
        double quadraticParams[] = new double[3];
        double value;

        xi[0] = 0.25;
        timeXi[0] = 0.33;

        params[0] = rawDofs[0];
        params[1] = rawDofs[1];
        params[2] = rawDofs[2];
        interpolatorValues = QuadraticBSpline.evaluateDirect( xi[0] );
        quadraticParams[0] = ( interpolatorValues[0] * params[0] ) + ( interpolatorValues[1] * params[1] )
            + ( interpolatorValues[2] * params[2] );

        params[0] = 0;
        params[1] = 0;
        params[2] = 0;
        interpolatorValues = QuadraticBSpline.evaluateDirect( xi[0] );
        quadraticParams[1] = ( interpolatorValues[0] * params[0] ) + ( interpolatorValues[1] * params[1] )
            + ( interpolatorValues[2] * params[2] );

        params[0] = -rawDofs[0];
        params[1] = -rawDofs[1];
        params[2] = -rawDofs[2];
        interpolatorValues = QuadraticBSpline.evaluateDirect( xi[0] );
        quadraticParams[2] = ( interpolatorValues[0] * params[0] ) + ( interpolatorValues[1] * params[1] )
            + ( interpolatorValues[2] * params[2] );

        context.set( splineMeshDomain, 1, xi );
        context.set( timeMeshDomain, 1, timeXi );
        output = meshZ.evaluate( context );

        interpolatorValues = QuadraticLagrange.evaluateDirect( timeXi[0] );
        value = ( interpolatorValues[0] * quadraticParams[0] ) + ( interpolatorValues[1] * quadraticParams[1] )
            + ( interpolatorValues[2] * quadraticParams[2] );

        assertEquals( value, output.values[0] );
    }

    public static String REGION_NAME = "TimeVaryingExample_Test";


    public static Region buildRegion()
    {
        Region library = Region.getLibrary();

        Region tvRegion = new Region( REGION_NAME );

        Region bsplineRegion = QuadraticBSplineExample.buildRegion();

        tvRegion.addSubregion( bsplineRegion );

        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousListDomain weightingDomain = library.getContinuousListDomain( "library.weighting.list" );
        EnsembleDomain timeElementDomain = new EnsembleDomain( "tv_test.time.elements" );
        timeElementDomain.addValues( 1, 2, 3 );
        tvRegion.addDomain( timeElementDomain );

        MeshDomain timeMeshDomain = new MeshDomain( "tv_test.time.mesh", 1, timeElementDomain );
        timeMeshDomain.setDefaultShape( "line_0_1" );
        tvRegion.addDomain( timeMeshDomain );

        EnsembleDomain timeDofsDomain = new EnsembleDomain( "tv_test.time.dofs.domain" );
        timeDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        tvRegion.addDomain( timeDofsDomain );

        EnsembleListDomain timeDofsListDomain = new EnsembleListDomain( "tv_test.time.dofs.list.domain", timeDofsDomain );
        tvRegion.addDomain( timeDofsListDomain );

        ContinuousParameters timeDofs = new ContinuousParameters( "tv_test.time.dofs.values", rc1CoordinatesDomain, timeDofsDomain );
        timeDofs.setValue( 1, 0 );
        timeDofs.setValue( 2, 1 );
        timeDofs.setValue( 3, 2.5 );
        timeDofs.setValue( 4, 4 );
        timeDofs.setValue( 5, 5.0 );
        timeDofs.setValue( 6, 6.625 );
        timeDofs.setValue( 7, 10.0 );// Deliberately non-linear. C1 continuous for my own amusement.
        tvRegion.addEvaluator( timeDofs );

        EnsembleListParameters elementDofIndexes = new EnsembleListParameters( "tv_test.time.element_dof_indexes", timeDofsListDomain,
            timeElementDomain );
        elementDofIndexes.setValue( 1, 1, 2, 3 );
        elementDofIndexes.setValue( 2, 3, 4, 5 );
        elementDofIndexes.setValue( 3, 5, 6, 7 );

        tvRegion.addEvaluator( elementDofIndexes );

        FunctionEvaluator quadraticLagrange = new FunctionEvaluator( "test_mesh.quadratic_lagrange", weightingDomain, timeMeshDomain, library
            .getContinuousFunction( "library.function.quadratic_lagrange" ) );
        tvRegion.addEvaluator( quadraticLagrange );

        ContinuousVariableEvaluator tvMeshDofs = new ContinuousVariableEvaluator( "tv_test.mesh.dofs", rc1CoordinatesDomain );

        MapEvaluator elementQLagrange = new MapEvaluator( "test_mesh.element.quadratic_lagrange_map", rc1CoordinatesDomain,
            elementDofIndexes, quadraticLagrange, tvMeshDofs );
        tvRegion.addEvaluator( elementQLagrange );

        PiecewiseTemplate meshTimeTemplate = new PiecewiseTemplate( "tv_test.time.template", timeMeshDomain );
        meshTimeTemplate.setEvaluator( 1, elementQLagrange );
        meshTimeTemplate.setEvaluator( 2, elementQLagrange );
        meshTimeTemplate.setEvaluator( 3, elementQLagrange );
        tvRegion.addPiecewiseTemplate( meshTimeTemplate );

        PiecewiseField meshTime = new PiecewiseField( "tv_test.time", rc1CoordinatesDomain, meshTimeTemplate );
        meshTime.setVariable( "tv_test.mesh.dofs", timeDofs );
        tvRegion.addEvaluator( meshTime );

        EnsembleDomain bsplineDofsDomain = bsplineRegion.getEnsembleDomain( "test_mesh.dofs" );

        ContinuousParameters dofs = new ContinuousParameters( "tv_test.dofs.z", rc1CoordinatesDomain, bsplineDofsDomain, timeDofsDomain );
        dofs.setValue( new int[]{ 1, 1 }, 0.954915 );
        dofs.setValue( new int[]{ 2, 1 }, 1.0450850 );
        dofs.setValue( new int[]{ 3, 1 }, -0.427051 );
        dofs.setValue( new int[]{ 4, 1 }, -1.190983 );
        dofs.setValue( new int[]{ 5, 1 }, -0.427051 );
        dofs.setValue( new int[]{ 6, 1 }, 1.0450850 );
        dofs.setValue( new int[]{ 7, 1 }, 0.954915 );

        dofs.setValue( new int[]{ 1, 2 }, 0.0 );
        dofs.setValue( new int[]{ 2, 2 }, 0.0 );
        dofs.setValue( new int[]{ 3, 2 }, 0.0 );
        dofs.setValue( new int[]{ 4, 2 }, 0.0 );
        dofs.setValue( new int[]{ 5, 2 }, 0.0 );
        dofs.setValue( new int[]{ 6, 2 }, 0.0 );
        dofs.setValue( new int[]{ 7, 2 }, 0.0 );

        dofs.setValue( new int[]{ 1, 3 }, -0.954915 );
        dofs.setValue( new int[]{ 2, 3 }, -1.0450850 );
        dofs.setValue( new int[]{ 3, 3 }, 0.427051 );
        dofs.setValue( new int[]{ 4, 3 }, 1.190983 );
        dofs.setValue( new int[]{ 5, 3 }, 0.427051 );
        dofs.setValue( new int[]{ 6, 3 }, -1.0450850 );
        dofs.setValue( new int[]{ 7, 3 }, -0.954915 );

        dofs.setValue( new int[]{ 1, 4 }, 0.0 );
        dofs.setValue( new int[]{ 2, 4 }, 0.0 );
        dofs.setValue( new int[]{ 3, 4 }, 0.0 );
        dofs.setValue( new int[]{ 4, 4 }, 0.0 );
        dofs.setValue( new int[]{ 5, 4 }, 0.0 );
        dofs.setValue( new int[]{ 6, 4 }, 0.0 );
        dofs.setValue( new int[]{ 7, 4 }, 0.0 );

        dofs.setValue( new int[]{ 1, 5 }, 0.954915 );
        dofs.setValue( new int[]{ 2, 5 }, 1.0450850 );
        dofs.setValue( new int[]{ 3, 5 }, -0.427051 );
        dofs.setValue( new int[]{ 4, 5 }, -1.190983 );
        dofs.setValue( new int[]{ 5, 5 }, -0.427051 );
        dofs.setValue( new int[]{ 6, 5 }, 1.0450850 );
        dofs.setValue( new int[]{ 7, 5 }, 0.954915 );

        dofs.setValue( new int[]{ 1, 6 }, 0.0 );
        dofs.setValue( new int[]{ 2, 6 }, 0.0 );
        dofs.setValue( new int[]{ 3, 6 }, 0.0 );
        dofs.setValue( new int[]{ 4, 6 }, 0.0 );
        dofs.setValue( new int[]{ 5, 6 }, 0.0 );
        dofs.setValue( new int[]{ 6, 6 }, 0.0 );
        dofs.setValue( new int[]{ 7, 6 }, 0.0 );

        dofs.setValue( new int[]{ 1, 7 }, -0.954915 );
        dofs.setValue( new int[]{ 2, 7 }, -1.0450850 );
        dofs.setValue( new int[]{ 3, 7 }, 0.427051 );
        dofs.setValue( new int[]{ 4, 7 }, 1.190983 );
        dofs.setValue( new int[]{ 5, 7 }, 0.427051 );
        dofs.setValue( new int[]{ 6, 7 }, -1.0450850 );
        dofs.setValue( new int[]{ 7, 7 }, -0.954915 );
        tvRegion.addEvaluator( dofs );

        PiecewiseTemplate bsplineTemplate = bsplineRegion.getPiecewiseTemplate( "test_mesh.coordinates" );

        PiecewiseField slicedZ = new PiecewiseField( "tv_test.coordinates.sliced_z", rc1CoordinatesDomain, bsplineTemplate );
        slicedZ.setVariable( "test_mesh.dofs", dofs );
        tvRegion.addEvaluator( slicedZ );

        PiecewiseField zValue = new PiecewiseField( "tv_test.coordinates.z", rc1CoordinatesDomain, meshTimeTemplate );
        zValue.setVariable( "tv_test.mesh.dofs", slicedZ );
        tvRegion.addEvaluator( zValue );

        return tvRegion;
    }


    public void test()
        throws IOException
    {
        Region library = Region.getLibrary();
        Region testRegion = buildRegion();
        Region bsplineRegion = testRegion.getSubregion( QuadraticBSplineExample.REGION_NAME );

        ContinuousListDomain weightingDomain = library.getContinuousListDomain( "library.weighting.list" );
        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.co-ordinates.rc.3d" );
        // These are only for visualization. Do not serialize.

        EnsembleDomain globalNodesDomain = bsplineRegion.getEnsembleDomain( "test_mesh.nodes" );
        EnsembleListEvaluator lineNodeList = bsplineRegion.getEnsembleListEvaluator( "test_mesh.line_nodes" );
        MeshDomain bsplineDomain = bsplineRegion.getMeshDomain( "test_mesh.domain" );

        ContinuousParameters nodalX = new ContinuousParameters( "test_mesh.node.x", rc1CoordinatesDomain, globalNodesDomain );
        nodalX.setValue( 1, 0.0 );
        nodalX.setValue( 2, 1.0 );
        nodalX.setValue( 3, 2.0 );
        nodalX.setValue( 4, 3.0 );
        nodalX.setValue( 5, 4.0 );
        nodalX.setValue( 6, 5.0 );

        FunctionEvaluator linearLagrange = new FunctionEvaluator( "test_mesh.linear_lagrange", weightingDomain, bsplineDomain, library
            .getContinuousFunction( "library.function.linear_lagrange" ) );
        testRegion.addEvaluator( linearLagrange );

        ContinuousVariableEvaluator linearDofs = new ContinuousVariableEvaluator( "test_mesh.linear.dofs", rc1CoordinatesDomain );

        MapEvaluator elementLLagrange = new MapEvaluator( "test_mesh.element.linear_lagrange_map", rc1CoordinatesDomain, lineNodeList,
            linearLagrange, linearDofs );
        testRegion.addEvaluator( elementLLagrange );

        PiecewiseTemplate linearMeshCoordinates = new PiecewiseTemplate( "test_mesh.linear_coordinates", bsplineDomain );
        linearMeshCoordinates.setEvaluator( 1, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 2, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 3, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 4, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 5, elementLLagrange );
        testRegion.addPiecewiseTemplate( linearMeshCoordinates );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", rc1CoordinatesDomain, linearMeshCoordinates );
        meshCoordinatesX.setVariable( "test_mesh.linear.dofs", nodalX );

        ContinuousEvaluator meshTime = testRegion.getContinuousEvaluator( "tv_test.time" );
        ContinuousEvaluator zValue = testRegion.getContinuousEvaluator( "tv_test.coordinates.z" );

        ContinuousAggregateEvaluator testCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh3DDomain );
        testCoordinates.setSourceField( 1, meshCoordinatesX );
        testCoordinates.setSourceField( 2, meshTime );
        testCoordinates.setSourceField( 3, zValue );

        testRegion.addEvaluator( testCoordinates );

        testRegion.addDomain( bsplineDomain );

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, 16, "test_mesh.coordinates", "test_mesh.domain",
            "tv_test.time.mesh" );
        FileWriter f = new FileWriter( "trunk/data/collada tv b-spline.xml" );
        f.write( collada );
        f.close();
    }
}
