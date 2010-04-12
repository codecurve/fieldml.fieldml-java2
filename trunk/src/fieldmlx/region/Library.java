package fieldmlx.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldmlx.evaluator.ContinuousCompositeEvaluator;
import fieldmlx.evaluator.DotProductEvaluator;
import fieldmlx.evaluator.FunctionEvaluator;
import fieldmlx.function.BicubicHermite;
import fieldmlx.function.BilinearLagrange;
import fieldmlx.function.BilinearSimplex;
import fieldmlx.function.BiquadraticLagrange;
import fieldmlx.function.CubicHermite;
import fieldmlx.function.LinearLagrange;
import fieldmlx.function.QuadraticBSpline;
import fieldmlx.function.QuadraticLagrange;

public class Library
{
    private static final String LIBRARY_NAME = "library";

    private static void buildLibraryFunctions( Region library )
    {
        EnsembleDomain anonymous = library.getEnsembleDomain("library.anonymous");
        ContinuousDomain anonymousList = library.getContinuousDomain("library.weighting.list");
        ContinuousDomain xi1d = library.getContinuousDomain("library.xi.rc.1d");
        ContinuousDomain xi2d = library.getContinuousDomain("library.xi.rc.2d");

        ContinuousDomain real1 = library.getContinuousDomain( "library.real.1d" );
        ContinuousDomain parameterList = new ContinuousDomain( library, "library.parameter.list", anonymous );
        ContinuousDomain scaleList = new ContinuousDomain( library, "library.scale.list", anonymous );

        DotProductEvaluator dotProduct = new DotProductEvaluator( "library.dot_product", real1, parameterList, anonymousList );
        library.addEvaluator( dotProduct );

        DotProductEvaluator scaledDotProduct = new DotProductEvaluator( "library.scaled_dot_product", real1, parameterList, anonymousList,
            scaleList );
        library.addEvaluator( scaledDotProduct );

        library.addEvaluator( new FunctionEvaluator( "library.function.quadratic_lagrange", anonymousList, xi2d, new QuadraticLagrange() ) );
        library.addEvaluator( new FunctionEvaluator( "library.function.cubic_hermite", anonymousList, xi1d, new CubicHermite() ) );

        EnsembleDomain quad2x2 = library.getEnsembleDomain( "library.local_nodes.quad.2x2" );
        ContinuousDomain l_l_lagrangeParameters = new ContinuousDomain( library, "library.parameters.bilinear_lagrange", quad2x2 );
        library.addEvaluator( new FunctionEvaluator( "library.function.bilinear_lagrange", anonymousList, xi2d, new BilinearLagrange() ) );
        ContinuousCompositeEvaluator bilinearLagrange = new ContinuousCompositeEvaluator( "library.fem.bilinear_lagrange", real1 );
        bilinearLagrange.alias( l_l_lagrangeParameters, parameterList );
        bilinearLagrange.importField( library.getContinuousEvaluator( "library.function.bilinear_lagrange" ) );
        bilinearLagrange.importField( dotProduct );
        library.addEvaluator( bilinearLagrange );

        EnsembleDomain line2 = library.getEnsembleDomain( "library.local_nodes.line.2" );
        ContinuousDomain l_lagrangeParameters = new ContinuousDomain( library, "library.parameters.linear_lagrange", line2 );
        library.addEvaluator( new FunctionEvaluator( "library.function.linear_lagrange", anonymousList, xi1d, new LinearLagrange() ) );
        ContinuousCompositeEvaluator linearLagrange = new ContinuousCompositeEvaluator( "library.fem.linear_lagrange", real1 );
        linearLagrange.alias( l_lagrangeParameters, parameterList );
        linearLagrange.importField( library.getContinuousEvaluator( "library.function.linear_lagrange" ) );
        linearLagrange.importField( dotProduct );
        library.addEvaluator( linearLagrange );

        EnsembleDomain quad3x3 = library.getEnsembleDomain( "library.local_nodes.quad.3x3" );
        ContinuousDomain q_q_lagrangeParameters = new ContinuousDomain( library, "library.parameters.biquadratic_lagrange", quad3x3 );
        library.addEvaluator( new FunctionEvaluator( "library.function.biquadratic_lagrange", anonymousList, xi2d, new BiquadraticLagrange() ) );
        ContinuousCompositeEvaluator biquadraticLagrange = new ContinuousCompositeEvaluator( "library.fem.biquadratic_lagrange", real1 );
        biquadraticLagrange.alias( q_q_lagrangeParameters, parameterList );
        biquadraticLagrange.importField( library.getContinuousEvaluator( "library.function.biquadratic_lagrange" ) );
        biquadraticLagrange.importField( dotProduct );
        library.addEvaluator( biquadraticLagrange );

        EnsembleDomain line3 = library.getEnsembleDomain( "library.local_nodes.line.3" );
        ContinuousDomain q_lagrangeParameters = new ContinuousDomain( library, "library.parameters.quadratic_lagrange", line3 );
        library.addEvaluator( new FunctionEvaluator( "library.function.quadratic_lagrange", anonymousList, xi1d, new QuadraticLagrange() ) );
        ContinuousCompositeEvaluator quadraticLagrange = new ContinuousCompositeEvaluator( "library.fem.quadratic_lagrange", real1 );
        quadraticLagrange.alias( q_lagrangeParameters, parameterList );
        quadraticLagrange.importField( library.getContinuousEvaluator( "library.function.quadratic_lagrange" ) );
        quadraticLagrange.importField( dotProduct );
        library.addEvaluator( quadraticLagrange );

        ContinuousCompositeEvaluator cubicHermite = new ContinuousCompositeEvaluator( "library.fem.cubic_hermite", real1 );
        cubicHermite.importField( library.getContinuousEvaluator( "library.function.cubic_hermite" ) );
        cubicHermite.importField( dotProduct );
        library.addEvaluator( cubicHermite );

        ContinuousDomain c_c_HermiteParameters = library.getContinuousDomain( "library.bicubic_hermite.parameters" );
        library.addEvaluator( new FunctionEvaluator( "library.function.bicubic_hermite", anonymousList, xi2d, new BicubicHermite() ) );

        ContinuousCompositeEvaluator scaledBicubicHermite = new ContinuousCompositeEvaluator( "library.fem.scaled_bicubic_hermite", real1 );
        scaledBicubicHermite.alias( c_c_HermiteParameters, parameterList );
        scaledBicubicHermite.importField( library.getContinuousEvaluator( "library.function.bicubic_hermite" ) );
        scaledBicubicHermite.importField( scaledDotProduct );
        library.addEvaluator( scaledBicubicHermite );

        ContinuousCompositeEvaluator bicubicHermite = new ContinuousCompositeEvaluator( "library.fem.bicubic_hermite", real1 );
        bicubicHermite.alias( c_c_HermiteParameters, parameterList );
        bicubicHermite.importField( library.getContinuousEvaluator( "library.function.bicubic_hermite" ) );
        bicubicHermite.importField( dotProduct );
        library.addEvaluator( bicubicHermite );

        EnsembleDomain tri2x2 = library.getEnsembleDomain( "library.local_nodes.triangle.2x2" );
        ContinuousDomain l_l_simplexParameters = new ContinuousDomain( library, "library.parameters.bilinear_simplex", tri2x2 );
        library.addEvaluator( new FunctionEvaluator( "library.function.bilinear_simplex", anonymousList, xi2d, new BilinearSimplex() ) );
        ContinuousCompositeEvaluator bilinearSimplex = new ContinuousCompositeEvaluator( "library.fem.bilinear_simplex", real1 );
        bilinearSimplex.alias( l_l_simplexParameters, parameterList );
        bilinearSimplex.importField( library.getContinuousEvaluator( "library.function.bilinear_simplex" ) );
        bilinearSimplex.importField( dotProduct );
        library.addEvaluator( bilinearSimplex );

        ContinuousDomain q_bsplineParameters = library.getContinuousDomain( "library.parameters.quadratic_bspline" );
        library.addEvaluator( new FunctionEvaluator( "library.function.quadratic_bspline", anonymousList, xi1d, new QuadraticBSpline() ) );
        ContinuousCompositeEvaluator quadraticBspline = new ContinuousCompositeEvaluator( "library.fem.quadratic_bspline", real1 );
        quadraticBspline.alias( q_bsplineParameters, parameterList );
        quadraticBspline.importField( library.getContinuousEvaluator( "library.function.quadratic_bspline" ) );
        quadraticBspline.importField( dotProduct );
        library.addEvaluator( quadraticBspline );
    }


    private static void buildLibraryDomains( Region library )
    {
        EnsembleDomain anonymous = new EnsembleDomain( library, "library.anonymous" );

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain( library, "library.local_nodes.line.2", 2 );

        EnsembleDomain line3LocalNodeDomain = new EnsembleDomain( library, "library.local_nodes.line.3", 3 );

        EnsembleDomain triangle2x2LocalNodeDomain = new EnsembleDomain( library, "library.local_nodes.triangle.2x2", 3 );

        new EnsembleDomain( library, "library.local_nodes.triangle.3x3", 6 );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( library, "library.local_nodes.quad.2x2", 4 );

        EnsembleDomain quad3x3LocalNodeDomain = new EnsembleDomain( library, "library.local_nodes.quad.3x3", 9 );

        EnsembleDomain quad4x4LocalNodeDomain = new EnsembleDomain( library, "library.local_nodes.quad.4x4", 16 );

        EnsembleDomain rc1CoordinateDomain = new EnsembleDomain( library, "library.ensemble.rc.1d", 1 );

        EnsembleDomain rc2CoordinateDomain = new EnsembleDomain( library, "library.ensemble.rc.2d", 2 );

        EnsembleDomain rc3CoordinateDomain = new EnsembleDomain( library, "library.ensemble.rc.3d", 3 );

        EnsembleDomain cubicHermiteDerivativesDomain = new EnsembleDomain( library, "library.interpolation.hermite.derivatives", 4 );

        EnsembleDomain bicubicHermiteParameterDomain = new EnsembleDomain( library, "library.interpolation.hermite.bicubic", 16 );

        EnsembleDomain quadraticBSplineParameterDomain = new EnsembleDomain( library, "library.interpolation.bspline.quadratic", 3 );

        new ContinuousDomain( library, "library.weighting" );

        new ContinuousDomain( library, "library.real.1d" );

        new ContinuousDomain( library, "library.coordinates.rc.1d", rc1CoordinateDomain );

        new ContinuousDomain( library, "library.coordinates.rc.2d", rc2CoordinateDomain );

        new ContinuousDomain( library, "library.coordinates.rc.3d", rc3CoordinateDomain );

        new ContinuousDomain( library, "library.linear_lagrange.parameters", line2LocalNodeDomain );

        new ContinuousDomain( library, "library.bilinear_lagrange.parameters", quad2x2LocalNodeDomain );

        new ContinuousDomain( library, "library.quadratic_lagrange.parameters", line3LocalNodeDomain );

        new ContinuousDomain( library, "library.biquadratic_lagrange.parameters", quad3x3LocalNodeDomain );

        new ContinuousDomain( library, "library.cubic_lagrange.parameters", quad4x4LocalNodeDomain );

        new ContinuousDomain( library, "library.bilinear_simplex.parameters", triangle2x2LocalNodeDomain );

        new ContinuousDomain( library, "library.bicubic_hermite.parameters", bicubicHermiteParameterDomain );

        new ContinuousDomain( library, "library.bicubic_hermite.scaling", bicubicHermiteParameterDomain );

        new ContinuousDomain( library, "library.parameters.quadratic_bspline", quadraticBSplineParameterDomain );

        new ContinuousDomain( library, "library.bicubic_hermite.nodal.parameters", cubicHermiteDerivativesDomain );

        new ContinuousDomain( library, "library.parameter.list", anonymous );

        new ContinuousDomain( library, "library.weighting.list", anonymous );

        new ContinuousDomain( library, "library.xi.rc.1d", rc1CoordinateDomain );

        new ContinuousDomain( library, "library.xi.rc.2d", rc2CoordinateDomain );

        new ContinuousDomain( library, "library.xi.rc.3d", rc3CoordinateDomain );
    }

    private static Region librarySingleton;


    /**
     * Factory method, implements singleton pattern.
     */
    public static Region getLibrarySingleton( Region worldRegion )
    {
        // TODO: Not thread safe.
        if( librarySingleton == null )
        {
            librarySingleton = new SubRegion( LIBRARY_NAME, worldRegion );
            buildLibraryDomains( librarySingleton );
            buildLibraryFunctions( librarySingleton );
        }
        return librarySingleton;
    }


    public static Region getLibrarySingleton()
    {
        if( librarySingleton == null )
        {
            throw new RuntimeException( "World region singleton first needs to be instantiated." );
        }
        return librarySingleton;

    }


    private Library()
    {
        // Do nothing, point of this method is to hide the constructor,
        // attempting to prevent instantiation of Library objects, since the
        // purpose
        // of this class is just to house the factory method.
    }

}
