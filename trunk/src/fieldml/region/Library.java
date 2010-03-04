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
        anonymous = new EnsembleDomain( this, "library.anonymous" );

        EnsembleDomain line1LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.line.1", 2 );

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.line.2", 3 );

        EnsembleDomain triangle1x1LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.triangle.1x1", 3 );

        new EnsembleDomain( this, "library.local_nodes.triangle.2x2", 6 );

        EnsembleDomain quad1x1LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.quad.1x1", 4 );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.quad.2x2", 9 );

        EnsembleDomain quad3x3LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.quad.3x3", 16 );

        EnsembleDomain rc1CoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.1", 1 );

        EnsembleDomain rc2CoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.2", 2 );

        EnsembleDomain rc3CoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.3", 3 );

        new EnsembleDomain( this, "library.edge_direction.quad", 2 );

        EnsembleDomain bicubicHermiteParameterDomain = new EnsembleDomain( this, "library.interpolation.hermite.bicubic", 16 );

        EnsembleDomain cubicHermiteDerivativesDomain = new EnsembleDomain( this, "library.interpolation.hermite.derivatives", 4 );

        EnsembleDomain quadraticBSplineParameterDomain = new EnsembleDomain( this, "library.interpolation.bspline.quadratic", 3 );

        addDomain( new ContinuousDomain( this, "library.weighting" ) );

        new ContinuousDomain( this, "library.co-ordinates.rc.1d", rc1CoordinateDomain );

        new ContinuousDomain( this, "library.co-ordinates.rc.2d", rc2CoordinateDomain );

        new ContinuousDomain( this, "library.co-ordinates.rc.3d", rc3CoordinateDomain );

        new ContinuousDomain( this, "library.linear_lagrange.parameters", line1LocalNodeDomain );

        new ContinuousDomain( this, "library.bilinear_lagrange.parameters", quad1x1LocalNodeDomain );

        new ContinuousDomain( this, "library.quadratic_lagrange.parameters", line2LocalNodeDomain );

        new ContinuousDomain( this, "library.biquadratic_lagrange.parameters", quad2x2LocalNodeDomain );

        new ContinuousDomain( this, "library.cubic_lagrange.parameters", quad3x3LocalNodeDomain );

        new ContinuousDomain( this, "library.bilinear_simplex.parameters", triangle1x1LocalNodeDomain );

        new ContinuousDomain( this, "library.bicubic_hermite.parameters", bicubicHermiteParameterDomain );

        new ContinuousDomain( this, "library.quadratic_bspline.parameters", quadraticBSplineParameterDomain );

        new ContinuousDomain( this, "library.bicubic_hermite.nodal.parameters", cubicHermiteDerivativesDomain );

        anonymousList = new ContinuousDomain( this, "library.weighting.list", anonymous );

        ContinuousDomain xiRC1Domain = new ContinuousDomain( this, "library.xi.rc.1d", rc1CoordinateDomain );
        xi1d = new ContinuousVariableEvaluator( "library.xi.rc.1d", xiRC1Domain );

        ContinuousDomain xiRC2Domain = new ContinuousDomain( this, "library.xi.rc.2d", rc2CoordinateDomain );
        xi2d = new ContinuousVariableEvaluator( "library.xi.rc.2d", xiRC2Domain );

        ContinuousDomain xiRC3Domain = new ContinuousDomain( this, "library.xi.rc.3d", rc3CoordinateDomain );
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
