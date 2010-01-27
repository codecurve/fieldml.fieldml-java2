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
import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.hardcoded.QuadraticBSpline;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.DOTReflectiveHandler;
import fieldml.io.JdomReflectiveHandler;
import fieldml.map.IndirectMap;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldmlx.util.MinimalColladaExporter;

public class QuadraticBSplineExample
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
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName()  + ".xml");
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
        Region region = buildRegion();

        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "test_mesh.element.parameters" );
        ContinuousEvaluator meshZ = region.getContinuousEvaluator( "test_mesh.coordinates.z" );

        ContinuousDomainValue output;

        double[] bsplineValues = new double[3];
        double[] params = new double[3];
        double[] xi = new double[1];
        double expectedValue;

        xi[0] = 0.25;
        params[0] = rawDofs[0];
        params[1] = rawDofs[1];
        params[2] = rawDofs[2];
        output = meshZ.evaluate( meshDomain, 1, xi );
        bsplineValues = QuadraticBSpline.evaluateDirect( xi[0] );

        expectedValue = ( bsplineValues[0] * params[0] ) + ( bsplineValues[1] * params[1] ) + ( bsplineValues[2] * params[2] );

        assertEquals( expectedValue, output.values[0] );

        xi[0] = 0.48;
        params[0] = rawDofs[3];
        params[1] = rawDofs[4];
        params[2] = rawDofs[5];
        output = meshZ.evaluate( meshDomain, 4, xi );
        bsplineValues = QuadraticBSpline.evaluateDirect( xi[0] );

        expectedValue = ( bsplineValues[0] * params[0] ) + ( bsplineValues[1] * params[1] ) + ( bsplineValues[2] * params[2] );

        assertEquals( expectedValue, output.values[0] );
    }

    public static String REGION_NAME = "QuadraticBSpline_Test";


    public static Region buildRegion()
    {
        Region library = Region.getLibrary();

        Region testRegion = new Region( REGION_NAME );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3, 4, 5 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 1, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.line.0_1" );
        meshDomain.setShape( 2, "library.shape.line.0_1" );
        meshDomain.setShape( 3, "library.shape.line.0_1" );
        meshDomain.setShape( 4, "library.shape.line.0_1" );
        meshDomain.setShape( 5, "library.shape.line.0_1" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "test_mesh.dofs" );
        globalDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        testRegion.addDomain( globalDofsDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleListDomain lineNodesDomain = new EnsembleListDomain( "test_mesh.line_nodes.domain", globalNodesDomain );
        testRegion.addDomain( lineNodesDomain );

        EnsembleListParameters lineNodeList = new EnsembleListParameters( "test_mesh.line_nodes", lineNodesDomain, testMeshElementDomain );

        lineNodeList.setValue( 1, 1, 2 );
        lineNodeList.setValue( 2, 2, 3 );
        lineNodeList.setValue( 3, 3, 4 );
        lineNodeList.setValue( 4, 4, 5 );
        lineNodeList.setValue( 5, 5, 6 );

        testRegion.addEvaluator( lineNodeList );

        ContinuousDomain weighting = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters dofs = new ContinuousParameters( "test_mesh.dofs.z", weighting, globalDofsDomain );
        dofs.setValue( 0.954915, 1 );
        dofs.setValue( 1.0450850, 2 );
        dofs.setValue( -0.427051, 3 );
        dofs.setValue( -1.190983, 4 );
        dofs.setValue( -0.427051, 5 );
        dofs.setValue( 1.0450850, 6 );
        dofs.setValue( 0.954915, 7 );

        testRegion.addEvaluator( dofs );

        EnsembleListDomain dofIndexesDomain = new EnsembleListDomain( "test_mesh.dof_indexes", globalDofsDomain );
        testRegion.addDomain( dofIndexesDomain );

        EnsembleListParameters elementDofIndexes = new EnsembleListParameters( "test_mesh.element_dof_indexes", dofIndexesDomain,
            testMeshElementDomain );
        elementDofIndexes.setValue( 1, 1, 2, 3 );
        elementDofIndexes.setValue( 2, 2, 3, 4 );
        elementDofIndexes.setValue( 3, 3, 4, 5 );
        elementDofIndexes.setValue( 4, 4, 5, 6 );
        elementDofIndexes.setValue( 5, 5, 6, 7 );
        testRegion.addEvaluator( elementDofIndexes );

        ContinuousListEvaluator quadraticBSpline = new QuadraticBSpline( "test_mesh.mesh.quadratic_bspline", meshDomain );
        testRegion.addEvaluator( quadraticBSpline );

        IndirectMap elementBSpline = new IndirectMap( "test_mesh.element.quadratic_bspline_map", elementDofIndexes, quadraticBSpline );
        testRegion.addMap( elementBSpline );

        PiecewiseTemplate meshCoordinates = new PiecewiseTemplate( "test_mesh.coordinates", meshDomain, 1 );
        meshCoordinates.setMap( 1, elementBSpline, 1 );
        meshCoordinates.setMap( 2, elementBSpline, 1 );
        meshCoordinates.setMap( 3, elementBSpline, 1 );
        meshCoordinates.setMap( 4, elementBSpline, 1 );
        meshCoordinates.setMap( 5, elementBSpline, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinates );

        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", rc1CoordinatesDomain, meshCoordinates );
        meshCoordinatesZ.setDofs( 1, dofs );

        testRegion.addEvaluator( meshCoordinatesZ );

        return testRegion;
    }


    public void test()
    {
        Region testRegion = buildRegion();

        try
        {
            String collada = MinimalColladaExporter.export1DFromFieldML( testRegion, "test_mesh.domain", "test_mesh.coordinates.z", 16 );
            FileWriter f = new FileWriter( "trunk/data/collada b-spline.xml" );
            f.write( collada );
            f.close();
        }
        catch( IOException e )
        {
        }
    }
}
