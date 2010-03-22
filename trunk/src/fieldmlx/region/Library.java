package fieldmlx.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.DotProductEvaluator;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.ImportedContinuousEvaluator;
import fieldml.function.BicubicHermite;
import fieldml.function.BilinearLagrange;
import fieldml.function.BilinearSimplex;
import fieldml.function.BiquadraticLagrange;
import fieldml.function.CubicHermite;
import fieldml.function.LinearLagrange;
import fieldml.function.QuadraticBSpline;
import fieldml.function.QuadraticLagrange;
import fieldml.function.TensorBasis;
import fieldml.region.Region;
import fieldml.region.WorldRegion;
import fieldmlx.evaluator.ContinuousCompositeEvaluator;

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


    public Library( WorldRegion parent )
    {
        super( LIBRARY_NAME );

        buildLibraryDomains();

        buildLibraryFunctions();
    }


    private void buildLibraryFunctions()
    {
        ContinuousDomain real1 = getContinuousDomain( "library.real.1d" );
        ContinuousDomain parameterList = new ContinuousDomain( this, "library.parameter.list", anonymous );
        ContinuousDomain scaleList = new ContinuousDomain( this, "library.scale.list", anonymous );

        DotProductEvaluator dotProduct = new DotProductEvaluator( "library.dot_product", real1, parameterList, anonymousList );
        addEvaluator( dotProduct );
        
        DotProductEvaluator scaledDotProduct = new DotProductEvaluator( "library.scaled_dot_product", real1, parameterList, anonymousList, scaleList );
        addEvaluator( scaledDotProduct );

        addEvaluator( new FunctionEvaluator( "library.function.quadratic_lagrange", anonymousList, xi2d, new QuadraticLagrange() ) );
        addEvaluator( new FunctionEvaluator( "library.function.cubic_hermite", anonymousList, xi1d, new CubicHermite() ) );

        EnsembleDomain quad1x1 = getEnsembleDomain( "library.local_nodes.quad.1x1" );
        ContinuousDomain l_l_lagrangeParameters = new ContinuousDomain( this, "library.parameters.bilinear_lagrange", quad1x1 );
        addEvaluator( new FunctionEvaluator( "library.function.bilinear_lagrange", anonymousList, xi2d, new BilinearLagrange() ) );
        ContinuousCompositeEvaluator bilinearLagrange = new ContinuousCompositeEvaluator( "library.fem.bilinear_lagrange", real1 );
        bilinearLagrange.alias( l_l_lagrangeParameters, parameterList );
        bilinearLagrange.importField( getContinuousEvaluator( "library.function.bilinear_lagrange" ) );
        bilinearLagrange.importField( dotProduct );
        addEvaluator( bilinearLagrange );

        EnsembleDomain line1 = getEnsembleDomain( "library.local_nodes.line.1" );
        ContinuousDomain l_lagrangeParameters = new ContinuousDomain( this, "library.parameters.linear_lagrange", line1 );
        addEvaluator( new FunctionEvaluator( "library.function.linear_lagrange", anonymousList, xi1d, new LinearLagrange() ) );
        ContinuousCompositeEvaluator linearLagrange = new ContinuousCompositeEvaluator( "library.fem.linear_lagrange", real1 );
        linearLagrange.alias( l_lagrangeParameters, parameterList );
        linearLagrange.importField( getContinuousEvaluator( "library.function.linear_lagrange" ) );
        linearLagrange.importField( dotProduct );
        addEvaluator( linearLagrange );

        EnsembleDomain quad2x2 = getEnsembleDomain( "library.local_nodes.quad.2x2" );
        ContinuousDomain q_q_lagrangeParameters = new ContinuousDomain( this, "library.parameters.biquadratic_lagrange", quad2x2 );
        addEvaluator( new FunctionEvaluator( "library.function.biquadratic_lagrange", anonymousList, xi2d, new BiquadraticLagrange() ) );
        ContinuousCompositeEvaluator biquadraticLagrange = new ContinuousCompositeEvaluator( "library.fem.biquadratic_lagrange", real1 );
        biquadraticLagrange.alias( q_q_lagrangeParameters, parameterList );
        biquadraticLagrange.importField( getContinuousEvaluator( "library.function.biquadratic_lagrange" ) );
        biquadraticLagrange.importField( dotProduct );
        addEvaluator( biquadraticLagrange );

        EnsembleDomain line2 = getEnsembleDomain( "library.local_nodes.line.2" );
        ContinuousDomain q_lagrangeParameters = new ContinuousDomain( this, "library.parameters.quadratic_lagrange", line2 );
        addEvaluator( new FunctionEvaluator( "library.function.quadratic_lagrange", anonymousList, xi1d, new QuadraticLagrange() ) );
        ContinuousCompositeEvaluator quadraticLagrange = new ContinuousCompositeEvaluator( "library.fem.quadratic_lagrange", real1 );
        quadraticLagrange.alias( q_lagrangeParameters, parameterList );
        quadraticLagrange.importField( getContinuousEvaluator( "library.function.quadratic_lagrange" ) );
        quadraticLagrange.importField( dotProduct );
        addEvaluator( quadraticLagrange );

        ContinuousCompositeEvaluator cubicHermite = new ContinuousCompositeEvaluator( "library.fem.cubic_hermite", real1 );
        cubicHermite.importField( getContinuousEvaluator( "library.function.cubic_hermite" ) );
        cubicHermite.importField( dotProduct );
        addEvaluator( cubicHermite );

        ContinuousDomain c_c_HermiteParameters = getContinuousDomain( "library.bicubic_hermite.parameters" );
        addEvaluator( new FunctionEvaluator( "library.function.bicubic_hermite", anonymousList, xi2d, new BicubicHermite() ) );
        
        ContinuousCompositeEvaluator scaledBicubicHermite = new ContinuousCompositeEvaluator( "library.fem.scaled_bicubic_hermite", real1 );
        scaledBicubicHermite.alias( c_c_HermiteParameters, parameterList );
        scaledBicubicHermite.importField( getContinuousEvaluator( "library.function.bicubic_hermite" ) );
        scaledBicubicHermite.importField( scaledDotProduct );
        addEvaluator( scaledBicubicHermite );

        ContinuousCompositeEvaluator bicubicHermite = new ContinuousCompositeEvaluator( "library.fem.bicubic_hermite", real1 );
        bicubicHermite.alias( c_c_HermiteParameters, parameterList );
        bicubicHermite.importField( getContinuousEvaluator( "library.function.bicubic_hermite" ) );
        bicubicHermite.importField( dotProduct );
        addEvaluator( bicubicHermite );

        EnsembleDomain tri1x1 = getEnsembleDomain( "library.local_nodes.triangle.1x1" );
        ContinuousDomain l_l_simplexParameters = new ContinuousDomain( this, "library.parameters.bilinear_simplex", tri1x1 );
        addEvaluator( new FunctionEvaluator( "library.function.bilinear_simplex", anonymousList, xi2d, new BilinearSimplex() ) );
        ContinuousCompositeEvaluator bilinearSimplex = new ContinuousCompositeEvaluator( "library.fem.bilinear_simplex", real1 );
        bilinearSimplex.alias( l_l_simplexParameters, parameterList );
        bilinearSimplex.importField( getContinuousEvaluator( "library.function.bilinear_simplex" ) );
        bilinearSimplex.importField( dotProduct );
        addEvaluator( bilinearSimplex );

        ContinuousDomain q_bsplineParameters = getContinuousDomain( "library.parameters.quadratic_bspline" );
        addEvaluator( new FunctionEvaluator( "library.function.quadratic_bspline", anonymousList, xi1d, new QuadraticBSpline() ) );
        ContinuousCompositeEvaluator quadraticBspline = new ContinuousCompositeEvaluator( "library.fem.quadratic_bspline", real1 );
        quadraticBspline.alias( q_bsplineParameters, parameterList );
        quadraticBspline.importField( getContinuousEvaluator( "library.function.quadratic_bspline" ) );
        quadraticBspline.importField( dotProduct );
        addEvaluator( quadraticBspline );
    }


    private void buildLibraryDomains()
    {
        anonymous = new EnsembleDomain( this, "library.anonymous", null );

        EnsembleDomain topologyDomain = new EnsembleDomain( this, "library.topology.general", null );

        new EnsembleDomain( this, "library.topology.0d", topologyDomain );

        new EnsembleDomain( this, "library.topology.1d", topologyDomain );

        new EnsembleDomain( this, "library.topology.2d", topologyDomain );

        new EnsembleDomain( this, "library.topology.3d", topologyDomain );

        EnsembleDomain pointLayout = new EnsembleDomain( this, "library.local_nodes.layout", null );

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

        new ContinuousDomain( this, "library.parameters.quadratic_bspline", quadraticBSplineParameterDomain );

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
    public ImportedContinuousEvaluator importContinuousEvaluator( String localName, String remoteName )
    {
        ImportedContinuousEvaluator evaluator = super.importContinuousEvaluator( localName, remoteName );
        if( evaluator != null )
        {
            return evaluator;
        }
        if( !remoteName.startsWith( INTERPOLATION_PREFIX ) )
        {
            return null;
        }

        String basisDescription = remoteName.substring( INTERPOLATION_PREFIX.length() );
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

        ContinuousEvaluator functionEvaluator = new FunctionEvaluator( remoteName, anonymousList, xiSource, tensorBasis );
        addEvaluator( functionEvaluator );
        
        return new ImportedContinuousEvaluator( localName, functionEvaluator );
    }
}
