package fieldml.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.ContinuousListDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
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


    public Library()
    {
        super( LIBRARY_NAME );

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
        EnsembleDomain triangle1x1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.triangle.1x1" );
        triangle1x1LocalNodeDomain.addValues( 1, 2, 3 );
        addDomain( triangle1x1LocalNodeDomain );

        EnsembleDomain quad1x1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.1x1" );
        quad1x1LocalNodeDomain.addValues( 1, 2, 3, 4 );
        addDomain( quad1x1LocalNodeDomain );

        addDomain( new EnsembleListDomain( "library.local_nodes.quad.1x1_list", quad1x1LocalNodeDomain ) );

        EnsembleDomain line1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.line.1" );
        line1LocalNodeDomain.addValues( 1, 2 );
        addDomain( line1LocalNodeDomain );

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.line.2" );
        line2LocalNodeDomain.addValues( 1, 2, 3 );
        addDomain( line2LocalNodeDomain );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.2x2" );
        quad2x2LocalNodeDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8, 9 );
        addDomain( quad2x2LocalNodeDomain );

        EnsembleDomain quadEdgeDirectionDomain = new EnsembleDomain( "library.edge_direction.quad" );
        quadEdgeDirectionDomain.addValues( 1, 2 );
        addDomain( quadEdgeDirectionDomain );

        ContinuousDomain weighting = new ContinuousDomain( "library.weighting.1d", 1 );
        addDomain( weighting );

        addDomain( new ContinuousDomain( "library.weighting.2d", 2 ) );

        addDomain( new ContinuousDomain( "library.weighting.3d", 3 ) );

        addDomain( new ContinuousDomain( "library.co-ordinates.rc.1d", 1 ) );

        addDomain( new ContinuousDomain( "library.co-ordinates.rc.2d", 2 ) );

        addDomain( new ContinuousDomain( "library.co-ordinates.rc.3d", 3 ) );

        addDomain( new ContinuousDomain( "library.bicubic_hermite.scaling", 16 ) );

        addDomain( new ContinuousListDomain( "library.linear_lagrange.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.bilinear_lagrange.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.quadratic_lagrange.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.biquadratic_lagrange.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.cubic_lagrange.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.bilinear_simplex.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.bicubic_hermite.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.quadratic_bspline.parameters", weighting ) );

        addDomain( new ContinuousListDomain( "library.weighting.list", weighting ) );

        addDomain( new ContinuousDomain( "library.bicubic_hermite.nodal.parameters", 4 ) );
    }
}
