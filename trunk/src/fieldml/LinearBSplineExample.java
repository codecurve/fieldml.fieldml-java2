package fieldml;

import java.io.FileWriter;
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
import fieldml.function.LinearQuadraticBSpline;
import fieldml.function.LinearLagrange;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldmlx.util.MinimalColladaExporter;

public class LinearBSplineExample
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

        region.serializeToXml( root );

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
        ContinuousEvaluator meshX = region.getContinuousEvaluator( "test_mesh.coordinates.x" );

        ContinuousDomainValue output;

        output = meshX.evaluate( meshDomain, 1, 0.5 );
        assert output.values[0] == 10;

        output = meshX.evaluate( meshDomain, 2, 0.5 );
        assert output.values[0] == 25;

        output = meshX.evaluate( meshDomain, 3, 0.5 );
        assert output.values[0] == 25;

        output = meshX.evaluate( meshDomain, 4, 0.5 );
        assert output.values[0] == 25;
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

        ContinuousParameters elementDofMapP1 = new ContinuousParameters( "test_mesh.element_dof_map.p1", weighting, testMeshElementDomain,
            globalDofsDomain );
        elementDofMapP1.setDefaultValue( 0.0 );
        elementDofMapP1.setValue( 1.0, 1, 1 );
        elementDofMapP1.setValue( 1.0, 2, 2 );
        elementDofMapP1.setValue( 1.0, 3, 3 );
        elementDofMapP1.setValue( 1.0, 4, 4 );
        elementDofMapP1.setValue( 1.0, 5, 5 );

        testRegion.addEvaluator( elementDofMapP1 );

        ContinuousParameters elementDofMapP2 = new ContinuousParameters( "test_mesh.element_dof_map.p2", weighting, testMeshElementDomain,
            globalDofsDomain );
        elementDofMapP2.setDefaultValue( 0.0 );
        elementDofMapP2.setValue( 1.0, 1, 2 );
        elementDofMapP2.setValue( 1.0, 2, 3 );
        elementDofMapP2.setValue( 1.0, 3, 4 );
        elementDofMapP2.setValue( 1.0, 4, 5 );
        elementDofMapP2.setValue( 1.0, 5, 6 );

        testRegion.addEvaluator( elementDofMapP2 );

        ContinuousParameters elementDofMapP3 = new ContinuousParameters( "test_mesh.element_dof_map.p3", weighting, testMeshElementDomain,
            globalDofsDomain );
        elementDofMapP3.setDefaultValue( 0.0 );
        elementDofMapP3.setValue( 1.0, 1, 3 );
        elementDofMapP3.setValue( 1.0, 2, 4 );
        elementDofMapP3.setValue( 1.0, 3, 5 );
        elementDofMapP3.setValue( 1.0, 4, 6 );
        elementDofMapP3.setValue( 1.0, 5, 7 );

        testRegion.addEvaluator( elementDofMapP3 );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters dofs = new ContinuousParameters( "test_mesh.dofs.z", weighting, globalDofsDomain );
        dofs.setValue( 0.954915, 1 );
        dofs.setValue( 1.0450850, 2 );
        dofs.setValue( -0.427051, 3 );
        dofs.setValue( -1.190983, 4 );
        dofs.setValue( -0.427051, 5 );
        dofs.setValue( 1.0450850, 6 );
        dofs.setValue( 0.954915, 7 );

        testRegion.addEvaluator( dofs );

        ContinuousCompositeEvaluator elementDofP1 = new ContinuousCompositeEvaluator( "test_mesh.element.p1", weighting,
            testMeshElementDomain );
        elementDofP1.importMappedField( dofs, elementDofMapP1, globalDofsDomain );
        testRegion.addEvaluator( elementDofP1 );

        ContinuousCompositeEvaluator elementDofP2 = new ContinuousCompositeEvaluator( "test_mesh.element.p2", weighting,
            testMeshElementDomain );
        elementDofP2.importMappedField( dofs, elementDofMapP2, globalDofsDomain );
        testRegion.addEvaluator( elementDofP2 );

        ContinuousCompositeEvaluator elementDofP3 = new ContinuousCompositeEvaluator( "test_mesh.element.p3", weighting,
            testMeshElementDomain );
        elementDofP3.importMappedField( dofs, elementDofMapP3, globalDofsDomain );
        testRegion.addEvaluator( elementDofP3 );

        ContinuousDomain bsplineParamsDomain = library.getContinuousDomain( "library.linear_bspline.parameters" );

        ContinuousAggregateEvaluator elementParameters = new ContinuousAggregateEvaluator( "test_mesh.element.params", bsplineParamsDomain );
        elementParameters.setSourceField( 1, elementDofP1 );
        elementParameters.setSourceField( 2, elementDofP2 );
        elementParameters.setSourceField( 3, elementDofP3 );

        testRegion.addEvaluator( elementParameters );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshDomain );
        meshCoordinatesZ.addEvaluator( new LinearQuadraticBSpline( "bspline_line", elementParameters ) );
        meshCoordinatesZ.setEvaluator( 1, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 2, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 3, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 4, "bspline_line" );
        meshCoordinatesZ.setEvaluator( 5, "bspline_line" );

        testRegion.addEvaluator( meshCoordinatesZ );

        // test( testRegion );

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
