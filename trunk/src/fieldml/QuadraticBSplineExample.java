package fieldml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.MapEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.function.QuadraticBSpline;
import fieldml.io.DOTReflectiveHandler;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.util.MinimalColladaExporter;

public class QuadraticBSplineExample
    extends FieldmlTestCase
{
    public void testSerialize()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "x____x____x____x____x____x\n" );
        
        serialize( region, s.toString() ); 

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
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "test_mesh.element.parameters" );
        ContinuousEvaluator meshZ = region.getContinuousEvaluator( "test_mesh.coordinates.z" );
        DomainValues context = new DomainValues();
        ContinuousDomainValue output;

        double[] bsplineValues = new double[3];
        double[] params = new double[3];
        double[] xi = new double[1];
        double expectedValue;

        xi[0] = 0.25;
        params[0] = rawDofs[0];
        params[1] = rawDofs[1];
        params[2] = rawDofs[2];
        context.set( meshDomain, 1, xi );
        output = meshZ.evaluate( context );
        bsplineValues = QuadraticBSpline.evaluateDirect( xi[0] );

        expectedValue = ( bsplineValues[0] * params[0] ) + ( bsplineValues[1] * params[1] ) + ( bsplineValues[2] * params[2] );

        assertEquals( expectedValue, output.values[0] );

        xi[0] = 0.48;
        params[0] = rawDofs[3];
        params[1] = rawDofs[4];
        params[2] = rawDofs[5];
        context.set( meshDomain, 4, xi );
        output = meshZ.evaluate( context );
        bsplineValues = QuadraticBSpline.evaluateDirect( xi[0] );

        expectedValue = ( bsplineValues[0] * params[0] ) + ( bsplineValues[1] * params[1] ) + ( bsplineValues[2] * params[2] );

        assertEquals( expectedValue, output.values[0] );
    }

    public static String REGION_NAME = "QuadraticBSpline_Test";


    public static Region buildRegion( Region parent )
    {
        Region library = parent.getLibrary();
        Region testRegion = new SubRegion( REGION_NAME, parent );

        EnsembleDomain xiComponentDomain = library.getEnsembleDomain( "library.co-ordinates.rc.1d" );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", xiComponentDomain, 5 );
        meshDomain.setShape( 1, "library.shape.line.0_1" );
        meshDomain.setShape( 2, "library.shape.line.0_1" );
        meshDomain.setShape( 3, "library.shape.line.0_1" );
        meshDomain.setShape( 4, "library.shape.line.0_1" );
        meshDomain.setShape( 5, "library.shape.line.0_1" );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( testRegion, "test_mesh.dofs", 7 );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", 6 );

        EnsembleDomain line1Domain = library.getEnsembleDomain( "library.local_nodes.line.1" );

        EnsembleDomain lineNodesDomain = new EnsembleDomain( testRegion, "test_mesh.line_nodes.domain", line1Domain, globalNodesDomain );

        EnsembleParameters lineNodeList = new EnsembleParameters( "test_mesh.line_nodes", lineNodesDomain, meshDomain.getElementDomain() );

        lineNodeList.setValue( 1, 1, 2 );
        lineNodeList.setValue( 2, 2, 3 );
        lineNodeList.setValue( 3, 3, 4 );
        lineNodeList.setValue( 4, 4, 5 );
        lineNodeList.setValue( 5, 5, 6 );

        testRegion.addEvaluator( lineNodeList );

        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );

        ContinuousParameters zDofs = new ContinuousParameters( "test_mesh.dofs.z", rc1CoordinatesDomain, globalDofsDomain );
        zDofs.setValue( 1, 0.954915 );
        zDofs.setValue( 2, 1.0450850 );
        zDofs.setValue( 3, -0.427051 );
        zDofs.setValue( 4, -1.190983 );
        zDofs.setValue( 5, -0.427051 );
        zDofs.setValue( 6, 1.0450850 );
        zDofs.setValue( 7, 0.954915 );

        testRegion.addEvaluator( zDofs );

        EnsembleDomain bsplineDofsDomain = library.getEnsembleDomain( "library.interpolation.bspline.quadratic" );

        EnsembleDomain dofIndexesDomain = new EnsembleDomain( testRegion, "test_mesh.dof_indexes", bsplineDofsDomain, globalDofsDomain );

        EnsembleParameters elementDofIndexes = new EnsembleParameters( "test_mesh.element_dof_indexes", dofIndexesDomain,
            meshDomain.getElementDomain() );
        elementDofIndexes.setValue( 1, 1, 2, 3 );
        elementDofIndexes.setValue( 2, 2, 3, 4 );
        elementDofIndexes.setValue( 3, 3, 4, 5 );
        elementDofIndexes.setValue( 4, 4, 5, 6 );
        elementDofIndexes.setValue( 5, 5, 6, 7 );
        testRegion.addEvaluator( elementDofIndexes );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.list" );

        FunctionEvaluator quadraticBSpline = new FunctionEvaluator( "test_mesh.quadratic_bspline", weightingDomain, meshDomain.getXiDomain(), library
            .getContinuousFunction( "library.function.quadratic_bspline" ) );
        testRegion.addEvaluator( quadraticBSpline );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.dofs", rc1CoordinatesDomain );

        MapEvaluator elementBSpline = new MapEvaluator( "test_mesh.element.quadratic_bspline_map", rc1CoordinatesDomain, elementDofIndexes,
            quadraticBSpline, dofs );
        testRegion.addEvaluator( elementBSpline );

        ContinuousPiecewiseEvaluator meshCoordinates = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates", rc1CoordinatesDomain, meshDomain.getElementDomain() );
        meshCoordinates.setEvaluator( 1, elementBSpline );
        meshCoordinates.setEvaluator( 2, elementBSpline );
        meshCoordinates.setEvaluator( 3, elementBSpline );
        meshCoordinates.setEvaluator( 4, elementBSpline );
        meshCoordinates.setEvaluator( 5, elementBSpline );
        testRegion.addEvaluator( meshCoordinates );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", rc1CoordinatesDomain, meshCoordinates );
        meshCoordinatesZ.setVariable( "test_mesh.dofs", zDofs );

        testRegion.addEvaluator( meshCoordinatesZ );

        return testRegion;
    }


    public void test()
    {
        WorldRegion world = new WorldRegion();
        Region testRegion = buildRegion( world );

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
