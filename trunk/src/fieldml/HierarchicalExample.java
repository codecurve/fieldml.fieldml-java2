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
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.MapEvaluator;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.evaluator.hardcoded.RegularLinearSubdivision;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.function.QuadraticBSpline;
import fieldml.io.DOTReflectiveHandler;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.util.MinimalColladaExporter;

public class HierarchicalExample
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
    { 0.954915, 1.0450850, -0.427051, -1.190983, -0.427051, 1.0450850, 0.954915, 0.663119, -0.045085, -0.572949, -1.045085, -0.954915 };
    
    public void testEvaluation()
    {
        Region region = buildRegion();

        MeshDomain meshDomain = region.getMeshDomain( "hierarchical_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "hierarchical_mesh.element.parameters" );
        ContinuousEvaluator meshZ = region.getContinuousEvaluator( "hierarchical_mesh.coordinates.z" );
        DomainValues context = new DomainValues();
        ContinuousDomainValue output;

        double[] bsplineValues = new double[3];
        double[] params = new double[3];
        double[] xi = new double[1];
        double expectedValue;

        xi[0] = 0.1;
        context.set( meshDomain, 1, xi );
        output = meshZ.evaluate( context );
        bsplineValues = QuadraticBSpline.evaluateDirect( 0.5 );

        params[0] = rawDofs[0];
        params[1] = rawDofs[1];
        params[2] = rawDofs[2];
        expectedValue = ( bsplineValues[0] * params[0] ) + ( bsplineValues[1] * params[1] ) + ( bsplineValues[2] * params[2] );

        assertEquals( expectedValue, output.values[0] );

        xi[0] = 0.48;
        context.set( meshDomain, 2, xi );
        output = meshZ.evaluate( context );
        bsplineValues = QuadraticBSpline.evaluateDirect( 0.4 );

        params[0] = rawDofs[7];
        params[1] = rawDofs[8];
        params[2] = rawDofs[9];
        expectedValue = ( bsplineValues[0] * params[0] ) + ( bsplineValues[1] * params[1] ) + ( bsplineValues[2] * params[2] );

        assertEquals( expectedValue, output.values[0], 0.00001 );
    }

    public static String REGION_NAME = "Hierarchical_Test";


    public static Region buildRegion()
    {
        Region library = Region.getLibrary();

        Region subRegion = QuadraticBSplineExample.buildRegion();
        
        Region testRegion = new Region( REGION_NAME );
        
        EnsembleDomain hierarchicalMeshElementDomain = new EnsembleDomain( "hierarchical_mesh.elements", 1, 2 );
        testRegion.addDomain( hierarchicalMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "hierarchical_mesh.domain", 1, hierarchicalMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.line.0_1" );
        meshDomain.setShape( 2, "library.shape.line.0_1" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "hierarchical_mesh.dofs", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 );
        testRegion.addDomain( globalDofsDomain );

        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );

        ContinuousParameters zDofs = new ContinuousParameters( "hierarchical_mesh.dofs.z", rc1CoordinatesDomain, globalDofsDomain );
        zDofs.setValue( 1, 0.954915 );
        zDofs.setValue( 2, 1.045085 );
        zDofs.setValue( 3, -0.427051 );
        zDofs.setValue( 4, -1.190983 );
        zDofs.setValue( 5, -0.427051 );
        zDofs.setValue( 6, 1.045085 );
        zDofs.setValue( 7, 0.954915 );
        zDofs.setValue( 8, 0.663119 );
        zDofs.setValue( 9, -0.045085 );
        zDofs.setValue( 10,-0.572949 );
        zDofs.setValue( 11,-1.045085 );
        zDofs.setValue( 12,-0.954915 );

        testRegion.addEvaluator( zDofs );

        EnsembleDomain submeshGlobalDofsDomain = subRegion.getEnsembleDomain( "test_mesh.dofs" );

        EnsembleDomain dofIndexesDomain = new EnsembleDomain( "hierarchical_mesh.dof_indexes", globalDofsDomain );
        testRegion.addDomain( dofIndexesDomain );

        EnsembleParameters elementDofIndexes = new EnsembleParameters( "hierarchical_mesh.element_dof_indexes", dofIndexesDomain,
            hierarchicalMeshElementDomain, submeshGlobalDofsDomain );
        elementDofIndexes.setValue( new int[]{1, 1}, 1 );
        elementDofIndexes.setValue( new int[]{1, 2}, 2 );
        elementDofIndexes.setValue( new int[]{1, 3}, 3 );
        elementDofIndexes.setValue( new int[]{1, 4}, 4 );
        elementDofIndexes.setValue( new int[]{1, 5}, 5 );
        elementDofIndexes.setValue( new int[]{1, 6}, 6 );
        elementDofIndexes.setValue( new int[]{1, 7}, 7 );
        elementDofIndexes.setValue( new int[]{2, 1}, 6 );
        elementDofIndexes.setValue( new int[]{2, 2}, 7 );
        elementDofIndexes.setValue( new int[]{2, 3}, 8 );
        elementDofIndexes.setValue( new int[]{2, 4}, 9 );
        elementDofIndexes.setValue( new int[]{2, 5}, 10 );
        elementDofIndexes.setValue( new int[]{2, 6}, 11 );
        elementDofIndexes.setValue( new int[]{2, 7}, 12 );

        ContinuousDomain weighting = library.getContinuousDomain( "library.weighting.list" );

        ContinuousParameters elementDofWeights = new ContinuousParameters( "hierarchical_mesh.element_dof_weights", weighting );
        elementDofWeights.setDefaultValue( 1, 1, 1, 1, 1, 1, 1 );
        testRegion.addEvaluator( elementDofWeights );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "hierarchical_mesh.dofs", rc1CoordinatesDomain );

        MapEvaluator elementLocalDofs = new MapEvaluator( "hierarchical_mesh.element.local_dofs", rc1CoordinatesDomain, elementDofIndexes,
            elementDofWeights, dofs );
        testRegion.addEvaluator( elementLocalDofs );
        
        MeshDomain submeshDomain = subRegion.getMeshDomain( "test_mesh.domain" );
        PiecewiseTemplate submeshTemplate = subRegion.getPiecewiseTemplate( "test_mesh.coordinates" );
        
        PiecewiseField delegatedEvaluator = new PiecewiseField( "hierarchical_mesh.delegated", rc1CoordinatesDomain, submeshTemplate );
        delegatedEvaluator.setVariable( "test_mesh.dofs", elementLocalDofs );
        
        RegularLinearSubdivision submeshAtlas = new RegularLinearSubdivision( "hierarchical_mesh.submesh_atlas", submeshDomain, meshDomain );
        
        ContinuousCompositeEvaluator submeshEvaluator = new ContinuousCompositeEvaluator( "hierarchical_mesh.submesh_evaluator", rc1CoordinatesDomain );
        submeshEvaluator.importField( submeshAtlas );
        submeshEvaluator.importField( delegatedEvaluator );

        PiecewiseTemplate meshCoordinates = new PiecewiseTemplate( "hierarchical_mesh.coordinates", meshDomain );
        meshCoordinates.setEvaluator( 1, submeshEvaluator );
        meshCoordinates.setEvaluator( 2, submeshEvaluator );
        testRegion.addPiecewiseTemplate( meshCoordinates );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "hierarchical_mesh.coordinates.z", rc1CoordinatesDomain, meshCoordinates );
        meshCoordinatesZ.setVariable( "hierarchical_mesh.dofs", zDofs );

        testRegion.addEvaluator( meshCoordinatesZ );

        return testRegion;
    }


    public void test()
    {
        Region testRegion = buildRegion();

        try
        {
            String collada = MinimalColladaExporter.export1DFromFieldML( testRegion, "hierarchical_mesh.domain", "hierarchical_mesh.coordinates.z", 64 );
            FileWriter f = new FileWriter( "trunk/data/collada hierarchical b-spline.xml" );
            f.write( collada );
            f.close();
        }
        catch( IOException e )
        {
        }
    }
}
