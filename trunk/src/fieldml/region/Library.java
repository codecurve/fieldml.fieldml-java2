package fieldml.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousCompositeEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.DotProductEvaluator;
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

    private ContinuousDomain xi1d;

    private ContinuousDomain xi2d;

    private ContinuousDomain xi3d;


    Library( WorldRegion parent )
    {
        super( LIBRARY_NAME );

        buildLibraryDomains();

        buildLibraryFunctions();
    }


    private void buildLibraryFunctions()
    {
        ContinuousDomain real1 = getContinuousDomain( "library.real.1d" );
        ContinuousDomain parameterList = new ContinuousDomain( this, "library.parameter.list", anonymous );
        DotProductEvaluator interpolation = new DotProductEvaluator( "library.fem.dot_product", real1, parameterList, anonymousList );

        addEvaluator( new FunctionEvaluator( "library.function.linear_lagrange", anonymousList, xi1d, new LinearLagrange() ) );
        addEvaluator( new FunctionEvaluator( "library.function.bilinear_lagrange", anonymousList, xi2d, new BilinearLagrange() ) );
        addEvaluator( new FunctionEvaluator( "library.function.quadratic_lagrange", anonymousList, xi1d, new QuadraticLagrange() ) );
        addEvaluator( new FunctionEvaluator( "library.function.biquadratic_lagrange", anonymousList, xi2d, new BiquadraticLagrange() ) );
        addEvaluator( new FunctionEvaluator( "library.function.cubic_hermite", anonymousList, xi1d, new CubicHermite() ) );
        addEvaluator( new FunctionEvaluator( "library.function.bicubic_hermite", anonymousList, xi2d, new BicubicHermite() ) );
        addEvaluator( new FunctionEvaluator( "library.function.bilinear_simplex", anonymousList, xi2d, new BilinearSimplex() ) );
        addEvaluator( new FunctionEvaluator( "library.function.quadratic_bspline", anonymousList, xi1d, new QuadraticBSpline() ) );

        ContinuousCompositeEvaluator linearLagrange = new ContinuousCompositeEvaluator( "library.fem.linear_lagrange", real1 );
        linearLagrange.importField( getContinuousEvaluator( "library.function.linear_lagrange" ) );
        linearLagrange.importField( interpolation );
        addEvaluator( linearLagrange );

        ContinuousCompositeEvaluator bilinearLagrange = new ContinuousCompositeEvaluator( "library.fem.bilinear_lagrange", real1 );
        bilinearLagrange.importField( getContinuousEvaluator( "library.function.bilinear_lagrange" ) );
        bilinearLagrange.importField( interpolation );
        addEvaluator( bilinearLagrange );

        ContinuousCompositeEvaluator quadraticLagrange = new ContinuousCompositeEvaluator( "library.fem.quadratic_lagrange", real1 );
        quadraticLagrange.importField( getContinuousEvaluator( "library.function.quadratic_lagrange" ) );
        quadraticLagrange.importField( interpolation );
        addEvaluator( quadraticLagrange );

        ContinuousCompositeEvaluator biquadraticLagrange = new ContinuousCompositeEvaluator( "library.fem.biquadratic_lagrange", real1 );
        biquadraticLagrange.importField( getContinuousEvaluator( "library.function.biquadratic_lagrange" ) );
        biquadraticLagrange.importField( interpolation );
        addEvaluator( biquadraticLagrange );

        ContinuousCompositeEvaluator cubicHermite = new ContinuousCompositeEvaluator( "library.fem.cubic_hermite", real1 );
        cubicHermite.importField( getContinuousEvaluator( "library.function.cubic_hermite" ) );
        cubicHermite.importField( interpolation );
        addEvaluator( cubicHermite );

        ContinuousCompositeEvaluator bicubicHermite = new ContinuousCompositeEvaluator( "library.fem.bicubic_hermite", real1 );
        bicubicHermite.importField( getContinuousEvaluator( "library.function.bicubic_hermite" ) );
        bicubicHermite.importField( interpolation );
        addEvaluator( bicubicHermite );

        ContinuousCompositeEvaluator bilinearSimplex = new ContinuousCompositeEvaluator( "library.fem.bilinear_simplex", real1 );
        bilinearSimplex.importField( getContinuousEvaluator( "library.function.bilinear_simplex" ) );
        bilinearSimplex.importField( interpolation );
        addEvaluator( bilinearSimplex );

        ContinuousCompositeEvaluator quadraticBSpline = new ContinuousCompositeEvaluator( "library.fem.quadratic_bspline", real1 );
        quadraticBSpline.importField( getContinuousEvaluator( "library.function.quadratic_bspline" ) );
        quadraticBSpline.importField( interpolation );
        addEvaluator( quadraticBSpline );
    }


    private void buildLibraryDomains()
    {
        anonymous = new EnsembleDomain( this, "library.anonymous", null );

        EnsembleDomain topologyDomain = new EnsembleDomain( this, "library.topology.general", null );

        new EnsembleDomain( this, "library.topology.0d", topologyDomain );

        new EnsembleDomain( this, "library.topology.1d", topologyDomain );

        new EnsembleDomain( this, "library.topology.2d", topologyDomain );

        new EnsembleDomain( this, "library.topology.3d", topologyDomain );

        EnsembleDomain pointLayout = new EnsembleDomain( this, "library.local_nodes.general", null );

        EnsembleDomain line1LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.line.1", pointLayout, 2 );

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.line.2", pointLayout, 3 );

        EnsembleDomain triangle1x1LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.triangle.1x1", pointLayout, 3 );

        new EnsembleDomain( this, "library.local_nodes.triangle.2x2", pointLayout, 6 );

        EnsembleDomain quad1x1LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.quad.1x1", pointLayout, 4 );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.quad.2x2", pointLayout, 9 );

        EnsembleDomain quad3x3LocalNodeDomain = new EnsembleDomain( this, "library.local_nodes.quad.3x3", pointLayout, 16 );

        EnsembleDomain coordinateDomain = new EnsembleDomain( this, "library.coordinates.general", null );

        EnsembleDomain rcCoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.general", coordinateDomain );

        EnsembleDomain rc1CoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.1d", rcCoordinateDomain, 1 );

        EnsembleDomain rc2CoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.2d", rcCoordinateDomain, 2 );

        EnsembleDomain rc3CoordinateDomain = new EnsembleDomain( this, "library.coordinates.rc.3d", rcCoordinateDomain, 3 );

        EnsembleDomain derivativeDomain = new EnsembleDomain( this, "library.derivative.general", null );

        EnsembleDomain cubicHermiteDerivativesDomain = new EnsembleDomain( this, "library.interpolation.hermite.derivatives",
            derivativeDomain, 4 );

        EnsembleDomain interpolationParameterDomain = new EnsembleDomain( this, "library.interpolation.general", null );

        EnsembleDomain bicubicHermiteParameterDomain = new EnsembleDomain( this, "library.interpolation.hermite.bicubic",
            interpolationParameterDomain, 16 );

        EnsembleDomain quadraticBSplineParameterDomain = new EnsembleDomain( this, "library.interpolation.bspline.quadratic",
            interpolationParameterDomain, 3 );

        addDomain( new ContinuousDomain( this, "library.weighting" ) );

        new ContinuousDomain( this, "library.real.1d" );

        new ContinuousDomain( this, "library.coordinates.rc.1d", rc1CoordinateDomain );

        new ContinuousDomain( this, "library.coordinates.rc.2d", rc2CoordinateDomain );

        new ContinuousDomain( this, "library.coordinates.rc.3d", rc3CoordinateDomain );

        new ContinuousDomain( this, "library.linear_lagrange.parameters", line1LocalNodeDomain );

        new ContinuousDomain( this, "library.bilinear_lagrange.parameters", quad1x1LocalNodeDomain );

        new ContinuousDomain( this, "library.quadratic_lagrange.parameters", line2LocalNodeDomain );

        new ContinuousDomain( this, "library.biquadratic_lagrange.parameters", quad2x2LocalNodeDomain );

        new ContinuousDomain( this, "library.cubic_lagrange.parameters", quad3x3LocalNodeDomain );

        new ContinuousDomain( this, "library.bilinear_simplex.parameters", triangle1x1LocalNodeDomain );

        new ContinuousDomain( this, "library.bicubic_hermite.parameters", bicubicHermiteParameterDomain );

        new ContinuousDomain( this, "library.bicubic_hermite.scaling", bicubicHermiteParameterDomain );

        new ContinuousDomain( this, "library.quadratic_bspline.parameters", quadraticBSplineParameterDomain );

        new ContinuousDomain( this, "library.bicubic_hermite.nodal.parameters", cubicHermiteDerivativesDomain );

        new ContinuousDomain( this, "library.parameter.list", anonymous );

        anonymousList = new ContinuousDomain( this, "library.weighting.list", anonymous );

        xi1d = new ContinuousDomain( this, "library.xi.rc.1d", rc1CoordinateDomain );

        xi2d = new ContinuousDomain( this, "library.xi.rc.2d", rc2CoordinateDomain );

        xi3d = new ContinuousDomain( this, "library.xi.rc.3d", rc3CoordinateDomain );
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
        ContinuousDomain xiSource;
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
            // Too many dimensions for now.
            return null;
        }

        evaluator = new FunctionEvaluator( name, anonymousList, xiSource, tensorBasis );
        addEvaluator( evaluator );

        return evaluator;
    }
}
