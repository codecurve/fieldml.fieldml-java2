package fieldml.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.function.BicubicHermite;
import fieldml.function.BilinearLagrange;
import fieldml.function.BilinearSimplex;
import fieldml.function.BiquadraticLagrange;
import fieldml.function.CubicHermite;
import fieldml.function.LinearLagrange;
import fieldml.function.QuadraticBSpline;
import fieldml.function.QuadraticLagrange;
import fieldml.function.TensorBasis;

public class Library
    extends Region
{
    private static final String LIBRARY_NAME = "library";

    private static final String INTERPOLATION_PREFIX = LIBRARY_NAME + ".interpolation.";

    public EnsembleDomain anonymous;

    public ContinuousDomain anonymousList;

    private ContinuousVariableEvaluator xi1d;

    private ContinuousVariableEvaluator xi2d;

    private ContinuousVariableEvaluator xi3d;


    Library( WorldRegion parent )
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
        anonymous = new EnsembleDomain( "library.anonymous" );
        addDomain( anonymous );

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

        addDomain( new ContinuousDomain( "library.bicubic_hermite.nodal.parameters", cubicHermiteDerivativesDomain ) );

        anonymousList = new ContinuousDomain( "library.weighting.list", anonymous );
        addDomain( anonymousList );

        ContinuousDomain xiRC1Domain = new ContinuousDomain( "library.xi.rc.1d", rc1CoordinateDomain );
        xi1d = new ContinuousVariableEvaluator( "library.xi.rc.1d", xiRC1Domain );

        ContinuousDomain xiRC2Domain = new ContinuousDomain( "library.xi.rc.2d", rc2CoordinateDomain );
        xi2d = new ContinuousVariableEvaluator( "library.xi.rc.2d", xiRC2Domain );

        ContinuousDomain xiRC3Domain = new ContinuousDomain( "library.xi.rc.3d", rc3CoordinateDomain );
        xi3d = new ContinuousVariableEvaluator( "library.xi.rc.3d", xiRC3Domain );
    }


    @Override
    public Region getLibrary()
    {
        return this;
    }


    @Override
    public ContinuousEvaluator getContinuousEvaluator( String name )
    {
        ContinuousEvaluator evaluator = super.getContinuousEvaluator( name );
        if( evaluator != null )
        {
            return evaluator;
        }
        if( !name.startsWith( INTERPOLATION_PREFIX ) )
        {
            return null;
        }

        String basisDescription = name.substring( INTERPOLATION_PREFIX.length() );
        TensorBasis tensorBasis = new TensorBasis( basisDescription );
        ContinuousEvaluator xiSource;
        if( tensorBasis.getDimensions() == 1 )
        {
            xiSource = xi1d;
        }
        else if( tensorBasis.getDimensions() == 2 )
        {
            xiSource = xi2d;
        }
        else if( tensorBasis.getDimensions() == 3 )
        {
            xiSource = xi3d;
        }
        else
        {
            //Too many dimensions for now.
            return null;
        }

        evaluator = new FunctionEvaluator( name, anonymousList, xiSource.getValueDomain(), tensorBasis );
        addEvaluator( evaluator );
        
        return evaluator;
    }
}
