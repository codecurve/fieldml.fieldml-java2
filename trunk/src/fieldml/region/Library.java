package fieldml.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.function.BicubicHermite;
import fieldml.function.BilinearLagrange;
import fieldml.function.BilinearSimplex;
import fieldml.function.BiquadraticLagrange;
import fieldml.function.CubicHermite;
import fieldml.function.LinearLagrange;
import fieldml.function.QuadraticBSpline;
import fieldml.function.QuadraticLagrange;

public class Library
    extends Region
{
    private static final String LIBRARY_NAME = "library";

    public final EnsembleDomain anonymous;

    Library( WorldRegion parent )
    {
        super( LIBRARY_NAME );

        anonymous = new EnsembleDomain( "library.anonymous" );
        addDomain( anonymous );

        buildLibraryDomains();

        buildLibraryFunctions();
    }


    private void buildLibraryFunctions()
    {
        addFunction( "library.function.linear_lagrange", new LinearLagrange() );
        addFunction( "library.function.bilinear_lagrange", new BilinearLagrange() );
        addFunction( "library.function.quadratic_lagrange", new QuadraticLagrange() );
        addFunction( "library.function.biquadratic_lagrange", new BiquadraticLagrange() );
        addFunction( "library.function.cubic_hermite", new CubicHermite() );
        addFunction( "library.function.bicubic_hermite", new BicubicHermite() );
        addFunction( "library.function.bilinear_simplex", new BilinearSimplex() );
        addFunction( "library.function.quadratic_bspline", new QuadraticBSpline() );
    }


    private void buildLibraryDomains()
    {
        EnsembleDomain line1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.line.1", 2 );
        addDomain( line1LocalNodeDomain );

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.line.2", 3 );
        addDomain( line2LocalNodeDomain );

        EnsembleDomain triangle1x1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.triangle.1x1", 3 );
        addDomain( triangle1x1LocalNodeDomain );

        EnsembleDomain triangle2x2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.triangle.2x2", 6 );
        addDomain( triangle2x2LocalNodeDomain );

        EnsembleDomain quad1x1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.1x1", 4 );
        addDomain( quad1x1LocalNodeDomain );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.2x2", 9 );
        addDomain( quad2x2LocalNodeDomain );

        EnsembleDomain quad3x3LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.3x3", 16 );
        addDomain( quad3x3LocalNodeDomain );

        EnsembleDomain rc1CoordinateDomain = new EnsembleDomain( "library.coordinates.rc.1", 1 );
        addDomain( rc1CoordinateDomain );

        EnsembleDomain rc2CoordinateDomain = new EnsembleDomain( "library.coordinates.rc.2", 2 );
        addDomain( rc2CoordinateDomain );

        EnsembleDomain rc3CoordinateDomain = new EnsembleDomain( "library.coordinates.rc.3", 3 );
        addDomain( rc3CoordinateDomain );

        EnsembleDomain quadEdgeDirectionDomain = new EnsembleDomain( "library.edge_direction.quad", 2 );
        addDomain( quadEdgeDirectionDomain );

        EnsembleDomain bicubicHermiteParameterDomain = new EnsembleDomain( "library.interpolation.hermite.bicubic", 16 );
        addDomain( bicubicHermiteParameterDomain );

        EnsembleDomain cubicHermiteDerivativesDomain = new EnsembleDomain( "library.interpolation.hermite.derivatives", 4 );
        addDomain( cubicHermiteDerivativesDomain );

        EnsembleDomain quadraticBSplineParameterDomain = new EnsembleDomain( "library.interpolation.bspline.quadratic", 3 );
        addDomain( quadraticBSplineParameterDomain );

        addDomain( new ContinuousDomain( "library.weighting" ) );

        addDomain( new ContinuousDomain( "library.co-ordinates.rc.1d", rc1CoordinateDomain ) );

        addDomain( new ContinuousDomain( "library.co-ordinates.rc.2d", rc2CoordinateDomain ) );

        addDomain( new ContinuousDomain( "library.co-ordinates.rc.3d", rc3CoordinateDomain ) );

        addDomain( new ContinuousDomain( "library.linear_lagrange.parameters", line1LocalNodeDomain ) );

        addDomain( new ContinuousDomain( "library.bilinear_lagrange.parameters", quad1x1LocalNodeDomain ) );

        addDomain( new ContinuousDomain( "library.quadratic_lagrange.parameters", line2LocalNodeDomain ) );

        addDomain( new ContinuousDomain( "library.biquadratic_lagrange.parameters", quad2x2LocalNodeDomain ) );

        addDomain( new ContinuousDomain( "library.cubic_lagrange.parameters", quad3x3LocalNodeDomain ) );

        addDomain( new ContinuousDomain( "library.bilinear_simplex.parameters", triangle1x1LocalNodeDomain ) );

        addDomain( new ContinuousDomain( "library.bicubic_hermite.parameters", bicubicHermiteParameterDomain ) );

        addDomain( new ContinuousDomain( "library.quadratic_bspline.parameters", quadraticBSplineParameterDomain ) );

        addDomain( new ContinuousDomain( "library.weighting.list", anonymous ) );

        addDomain( new ContinuousDomain( "library.bicubic_hermite.nodal.parameters", cubicHermiteDerivativesDomain ) );
    }


    @Override
    public Region getLibrary()
    {
        return this;
    }
}
