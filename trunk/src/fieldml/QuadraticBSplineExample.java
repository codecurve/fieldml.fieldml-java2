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
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.PiecewiseField;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.function.LinearLagrange;
import fieldml.function.QuadraticBSpline;
import fieldml.io.DOTReflectiveHandler;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldmlx.util.MinimalColladaExporter;

public class QuadraticBSplineExample
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


    private static void test( Region region )
    {
        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "test_mesh.element.parameters" );
        ContinuousEvaluator meshZ = region.getContinuousEvaluator( "test_mesh.coordinates.z" );

        ContinuousDomainValue output;

        output = meshZ.evaluate( meshDomain, 1, 0.25 );
    }


    public static void main( String[] args )
    {
        Region library = Region.getLibrary();

        EnsembleDomain lineLocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.line.1" );

        Region testRegion = new Region( "test" );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3, 4, 5 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 1, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.line.00_10" );
        meshDomain.setShape( 2, "library.shape.line.00_10" );
        meshDomain.setShape( 3, "library.shape.line.00_10" );
        meshDomain.setShape( 4, "library.shape.line.00_10" );
        meshDomain.setShape( 5, "library.shape.line.00_10" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "test_mesh.dofs" );
        globalDofsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        testRegion.addDomain( globalDofsDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleParameters lineNodeList = new EnsembleParameters( "test_mesh.line_nodes", globalNodesDomain, testMeshElementDomain,
            lineLocalNodeDomain );

        lineNodeList.setValue( 1, 1, 1 );
        lineNodeList.setValue( 2, 1, 2 );

        lineNodeList.setValue( 2, 2, 1 );
        lineNodeList.setValue( 3, 2, 2 );

        lineNodeList.setValue( 3, 3, 1 );
        lineNodeList.setValue( 4, 3, 2 );

        lineNodeList.setValue( 4, 4, 1 );
        lineNodeList.setValue( 5, 4, 2 );

        lineNodeList.setValue( 5, 5, 1 );
        lineNodeList.setValue( 6, 5, 2 );

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

        ContinuousDomain bsplineParamsDomain = library.getContinuousDomain( "library.quadratic_bspline.parameters" );

        ContinuousDomain weighting3 = library.getContinuousDomain( "library.weighting.3d" );

        ContinuousParameters elementDofMap = new ContinuousParameters( "test_mesh.element_dof_map", weighting3, testMeshElementDomain,
            globalDofsDomain );
        elementDofMap.setDefaultValue( weighting.makeValue( 0.0, 0.0, 0.0 ) );
        elementDofMap.setValue( weighting.makeValue( 1.0, 0.0, 0.0 ), 1, 1 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 1.0, 0.0 ), 1, 2 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 0.0, 1.0 ), 1, 3 );
        elementDofMap.setValue( weighting.makeValue( 1.0, 0.0, 0.0 ), 2, 2 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 1.0, 0.0 ), 2, 3 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 0.0, 1.0 ), 2, 4 );
        elementDofMap.setValue( weighting.makeValue( 1.0, 0.0, 0.0 ), 3, 3 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 1.0, 0.0 ), 3, 4 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 0.0, 1.0 ), 3, 5 );
        elementDofMap.setValue( weighting.makeValue( 1.0, 0.0, 0.0 ), 4, 4 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 1.0, 0.0 ), 4, 5 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 0.0, 1.0 ), 4, 6 );
        elementDofMap.setValue( weighting.makeValue( 1.0, 0.0, 0.0 ), 5, 5 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 1.0, 0.0 ), 5, 6 );
        elementDofMap.setValue( weighting.makeValue( 0.0, 0.0, 1.0 ), 5, 7 );
        testRegion.addEvaluator( elementDofMap );

        ContinuousCompositeEvaluator elementParametersMerged = new ContinuousCompositeEvaluator( "test_mesh.element.parameters_merged",
            bsplineParamsDomain, testMeshElementDomain );
        elementParametersMerged.importMappedField( bsplineParamsDomain, dofs, elementDofMap, globalDofsDomain );
        testRegion.addEvaluator( elementParametersMerged );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshDomain );
        meshCoordinatesZ.addEvaluator( new QuadraticBSpline( "bspline_line", elementParametersMerged ) );
        meshCoordinatesZ.setEvaluator( 1, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 2, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 3, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 4, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 5, "bspline_line" );

        testRegion.addEvaluator( meshCoordinatesZ );

        test( testRegion );

        serialize( testRegion );

        try
        {
            // These are only for visualization. Do not serialize.
            ContinuousParameters nodalX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
            nodalX.setValue( 0.0, 1 );
            nodalX.setValue( 1.0, 2 );
            nodalX.setValue( 2.0, 3 );
            nodalX.setValue( 3.0, 4 );
            nodalX.setValue( 4.0, 5 );
            nodalX.setValue( 5.0, 6 );

            PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshDomain );
            meshCoordinatesX.addEvaluator( new LinearLagrange( "linear", nodalX, lineNodeList, lineLocalNodeDomain ) );
            meshCoordinatesX.setEvaluator( 1, "linear" );
            meshCoordinatesX.setEvaluator( 2, "linear" );
            meshCoordinatesX.setEvaluator( 3, "linear" );
            meshCoordinatesX.setEvaluator( 4, "linear" );
            meshCoordinatesX.setEvaluator( 5, "linear" );

            ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

            ContinuousAggregateEvaluator testCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh2DDomain );
            testCoordinates.setSourceField( 1, meshCoordinatesX );
            testCoordinates.setSourceField( 2, meshCoordinatesZ );

            testRegion.addEvaluator( testCoordinates );

            String collada = MinimalColladaExporter.export2DFromFieldML( testRegion, "test_mesh.domain", 5, 16 );
            FileWriter f = new FileWriter( "trunk/data/collada b-spline.xml" );
            f.write( collada );
            f.close();
        }
        catch( IOException e )
        {
        }
    }
}
