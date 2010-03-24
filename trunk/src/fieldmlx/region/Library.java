package fieldmlx.region;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
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
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldmlx.evaluator.ContinuousCompositeEvaluator;

public class Library {
    private static final String LIBRARY_NAME = "library";

    private static void buildLibraryFunctions(Region library) {
        EnsembleDomain anonymous = library.getEnsembleDomain("library.anonymous");
        ContinuousDomain anonymousList = library.getContinuousDomain("library.weighting.list");
        ContinuousDomain xi1d = library.getContinuousDomain("library.xi.rc.1d");
        ContinuousDomain xi2d = library.getContinuousDomain("library.xi.rc.2d");

        ContinuousDomain real1 = library.getContinuousDomain("library.real.1d");
        ContinuousDomain parameterList = new ContinuousDomain(library, "library.parameter.list", anonymous);
        ContinuousDomain scaleList = new ContinuousDomain(library, "library.scale.list", anonymous);

        DotProductEvaluator dotProduct = new DotProductEvaluator("library.dot_product", real1, parameterList, anonymousList);
        library.addEvaluator(dotProduct);

        DotProductEvaluator scaledDotProduct = new DotProductEvaluator("library.scaled_dot_product", real1, parameterList, anonymousList, scaleList);
        library.addEvaluator(scaledDotProduct);

        library.addEvaluator(new FunctionEvaluator("library.function.quadratic_lagrange", anonymousList, xi2d, new QuadraticLagrange()));
        library.addEvaluator(new FunctionEvaluator("library.function.cubic_hermite", anonymousList, xi1d, new CubicHermite()));

        EnsembleDomain quad1x1 = library.getEnsembleDomain("library.local_nodes.quad.1x1");
        ContinuousDomain l_l_lagrangeParameters = new ContinuousDomain(library, "library.parameters.bilinear_lagrange", quad1x1);
        library.addEvaluator(new FunctionEvaluator("library.function.bilinear_lagrange", anonymousList, xi2d, new BilinearLagrange()));
        ContinuousCompositeEvaluator bilinearLagrange = new ContinuousCompositeEvaluator("library.fem.bilinear_lagrange", real1);
        bilinearLagrange.alias(l_l_lagrangeParameters, parameterList);
        bilinearLagrange.importField(library.getContinuousEvaluator("library.function.bilinear_lagrange"));
        bilinearLagrange.importField(dotProduct);
        library.addEvaluator(bilinearLagrange);

        EnsembleDomain line1 = library.getEnsembleDomain("library.local_nodes.line.1");
        ContinuousDomain l_lagrangeParameters = new ContinuousDomain(library, "library.parameters.linear_lagrange", line1);
        library.addEvaluator(new FunctionEvaluator("library.function.linear_lagrange", anonymousList, xi1d, new LinearLagrange()));
        ContinuousCompositeEvaluator linearLagrange = new ContinuousCompositeEvaluator("library.fem.linear_lagrange", real1);
        linearLagrange.alias(l_lagrangeParameters, parameterList);
        linearLagrange.importField(library.getContinuousEvaluator("library.function.linear_lagrange"));
        linearLagrange.importField(dotProduct);
        library.addEvaluator(linearLagrange);

        EnsembleDomain quad2x2 = library.getEnsembleDomain("library.local_nodes.quad.2x2");
        ContinuousDomain q_q_lagrangeParameters = new ContinuousDomain(library, "library.parameters.biquadratic_lagrange", quad2x2);
        library.addEvaluator(new FunctionEvaluator("library.function.biquadratic_lagrange", anonymousList, xi2d, new BiquadraticLagrange()));
        ContinuousCompositeEvaluator biquadraticLagrange = new ContinuousCompositeEvaluator("library.fem.biquadratic_lagrange", real1);
        biquadraticLagrange.alias(q_q_lagrangeParameters, parameterList);
        biquadraticLagrange.importField(library.getContinuousEvaluator("library.function.biquadratic_lagrange"));
        biquadraticLagrange.importField(dotProduct);
        library.addEvaluator(biquadraticLagrange);

        EnsembleDomain line2 = library.getEnsembleDomain("library.local_nodes.line.2");
        ContinuousDomain q_lagrangeParameters = new ContinuousDomain(library, "library.parameters.quadratic_lagrange", line2);
        library.addEvaluator(new FunctionEvaluator("library.function.quadratic_lagrange", anonymousList, xi1d, new QuadraticLagrange()));
        ContinuousCompositeEvaluator quadraticLagrange = new ContinuousCompositeEvaluator("library.fem.quadratic_lagrange", real1);
        quadraticLagrange.alias(q_lagrangeParameters, parameterList);
        quadraticLagrange.importField(library.getContinuousEvaluator("library.function.quadratic_lagrange"));
        quadraticLagrange.importField(dotProduct);
        library.addEvaluator(quadraticLagrange);

        ContinuousCompositeEvaluator cubicHermite = new ContinuousCompositeEvaluator("library.fem.cubic_hermite", real1);
        cubicHermite.importField(library.getContinuousEvaluator("library.function.cubic_hermite"));
        cubicHermite.importField(dotProduct);
        library.addEvaluator(cubicHermite);

        ContinuousDomain c_c_HermiteParameters = library.getContinuousDomain("library.bicubic_hermite.parameters");
        library.addEvaluator(new FunctionEvaluator("library.function.bicubic_hermite", anonymousList, xi2d, new BicubicHermite()));

        ContinuousCompositeEvaluator scaledBicubicHermite = new ContinuousCompositeEvaluator("library.fem.scaled_bicubic_hermite", real1);
        scaledBicubicHermite.alias(c_c_HermiteParameters, parameterList);
        scaledBicubicHermite.importField(library.getContinuousEvaluator("library.function.bicubic_hermite"));
        scaledBicubicHermite.importField(scaledDotProduct);
        library.addEvaluator(scaledBicubicHermite);

        ContinuousCompositeEvaluator bicubicHermite = new ContinuousCompositeEvaluator("library.fem.bicubic_hermite", real1);
        bicubicHermite.alias(c_c_HermiteParameters, parameterList);
        bicubicHermite.importField(library.getContinuousEvaluator("library.function.bicubic_hermite"));
        bicubicHermite.importField(dotProduct);
        library.addEvaluator(bicubicHermite);

        EnsembleDomain tri1x1 = library.getEnsembleDomain("library.local_nodes.triangle.1x1");
        ContinuousDomain l_l_simplexParameters = new ContinuousDomain(library, "library.parameters.bilinear_simplex", tri1x1);
        library.addEvaluator(new FunctionEvaluator("library.function.bilinear_simplex", anonymousList, xi2d, new BilinearSimplex()));
        ContinuousCompositeEvaluator bilinearSimplex = new ContinuousCompositeEvaluator("library.fem.bilinear_simplex", real1);
        bilinearSimplex.alias(l_l_simplexParameters, parameterList);
        bilinearSimplex.importField(library.getContinuousEvaluator("library.function.bilinear_simplex"));
        bilinearSimplex.importField(dotProduct);
        library.addEvaluator(bilinearSimplex);

        ContinuousDomain q_bsplineParameters = library.getContinuousDomain("library.parameters.quadratic_bspline");
        library.addEvaluator(new FunctionEvaluator("library.function.quadratic_bspline", anonymousList, xi1d, new QuadraticBSpline()));
        ContinuousCompositeEvaluator quadraticBspline = new ContinuousCompositeEvaluator("library.fem.quadratic_bspline", real1);
        quadraticBspline.alias(q_bsplineParameters, parameterList);
        quadraticBspline.importField(library.getContinuousEvaluator("library.function.quadratic_bspline"));
        quadraticBspline.importField(dotProduct);
        library.addEvaluator(quadraticBspline);
    }

    private static void buildLibraryDomains(Region library) {
        EnsembleDomain anonymous = new EnsembleDomain(library, "library.anonymous", null);

        EnsembleDomain topologyDomain = new EnsembleDomain(library, "library.topology.general", null);

        new EnsembleDomain(library, "library.topology.0d", topologyDomain);

        new EnsembleDomain(library, "library.topology.1d", topologyDomain);

        new EnsembleDomain(library, "library.topology.2d", topologyDomain);

        new EnsembleDomain(library, "library.topology.3d", topologyDomain);

        EnsembleDomain pointLayout = new EnsembleDomain(library, "library.local_nodes.layout", null);

        EnsembleDomain line1LocalNodeDomain = new EnsembleDomain(library, "library.local_nodes.line.1", pointLayout, 2);

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain(library, "library.local_nodes.line.2", pointLayout, 3);

        EnsembleDomain triangle1x1LocalNodeDomain = new EnsembleDomain(library, "library.local_nodes.triangle.1x1", pointLayout, 3);

        new EnsembleDomain(library, "library.local_nodes.triangle.2x2", pointLayout, 6);

        EnsembleDomain quad1x1LocalNodeDomain = new EnsembleDomain(library, "library.local_nodes.quad.1x1", pointLayout, 4);

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain(library, "library.local_nodes.quad.2x2", pointLayout, 9);

        EnsembleDomain quad3x3LocalNodeDomain = new EnsembleDomain(library, "library.local_nodes.quad.3x3", pointLayout, 16);

        EnsembleDomain coordinateDomain = new EnsembleDomain(library, "library.coordinates.general", null);

        EnsembleDomain rcCoordinateDomain = new EnsembleDomain(library, "library.coordinates.rc.general", coordinateDomain);

        EnsembleDomain rc1CoordinateDomain = new EnsembleDomain(library, "library.coordinates.rc.1d", rcCoordinateDomain, 1);

        EnsembleDomain rc2CoordinateDomain = new EnsembleDomain(library, "library.coordinates.rc.2d", rcCoordinateDomain, 2);

        EnsembleDomain rc3CoordinateDomain = new EnsembleDomain(library, "library.coordinates.rc.3d", rcCoordinateDomain, 3);

        EnsembleDomain derivativeDomain = new EnsembleDomain(library, "library.derivative.general", null);

        EnsembleDomain cubicHermiteDerivativesDomain = new EnsembleDomain(library, "library.interpolation.hermite.derivatives", derivativeDomain, 4);

        EnsembleDomain interpolationParameterDomain = new EnsembleDomain(library, "library.interpolation.general", null);

        EnsembleDomain bicubicHermiteParameterDomain = new EnsembleDomain(library, "library.interpolation.hermite.bicubic", interpolationParameterDomain, 16);

        EnsembleDomain quadraticBSplineParameterDomain = new EnsembleDomain(library, "library.interpolation.bspline.quadratic", interpolationParameterDomain, 3);

        library.addDomain(new ContinuousDomain(library, "library.weighting"));

        new ContinuousDomain(library, "library.real.1d");

        new ContinuousDomain(library, "library.coordinates.rc.1d", rc1CoordinateDomain);

        new ContinuousDomain(library, "library.coordinates.rc.2d", rc2CoordinateDomain);

        new ContinuousDomain(library, "library.coordinates.rc.3d", rc3CoordinateDomain);

        new ContinuousDomain(library, "library.linear_lagrange.parameters", line1LocalNodeDomain);

        new ContinuousDomain(library, "library.bilinear_lagrange.parameters", quad1x1LocalNodeDomain);

        new ContinuousDomain(library, "library.quadratic_lagrange.parameters", line2LocalNodeDomain);

        new ContinuousDomain(library, "library.biquadratic_lagrange.parameters", quad2x2LocalNodeDomain);

        new ContinuousDomain(library, "library.cubic_lagrange.parameters", quad3x3LocalNodeDomain);

        new ContinuousDomain(library, "library.bilinear_simplex.parameters", triangle1x1LocalNodeDomain);

        new ContinuousDomain(library, "library.bicubic_hermite.parameters", bicubicHermiteParameterDomain);

        new ContinuousDomain(library, "library.bicubic_hermite.scaling", bicubicHermiteParameterDomain);

        new ContinuousDomain(library, "library.parameters.quadratic_bspline", quadraticBSplineParameterDomain);

        new ContinuousDomain(library, "library.bicubic_hermite.nodal.parameters", cubicHermiteDerivativesDomain);

        new ContinuousDomain(library, "library.parameter.list", anonymous);

        new ContinuousDomain(library, "library.weighting.list", anonymous);

        new ContinuousDomain(library, "library.xi.rc.1d", rc1CoordinateDomain);

        new ContinuousDomain(library, "library.xi.rc.2d", rc2CoordinateDomain);

        new ContinuousDomain(library, "library.xi.rc.3d", rc3CoordinateDomain);
    }

    private static Region librarySingleton;

    /**
     * Factory method, implements singleton pattern.
     */
    public static Region getLibrarySingleton( Region worldRegion ) {
        // TODO: Not thread safe.
        if (librarySingleton == null ) {
            librarySingleton = new SubRegion(LIBRARY_NAME, worldRegion );
            buildLibraryDomains(librarySingleton);
            buildLibraryFunctions(librarySingleton);
        }
        return librarySingleton;
    }

    public static Region getLibrarySingleton() {
        if (librarySingleton == null ) {
            throw new RuntimeException("World region singleton first needs to be instantiated.");
        }
        return librarySingleton;

    }
    
    private Library() {
        // Do nothing, point of this method is to hide the constructor,
        // attempting to prevent instantiation of Library objects, since the
        // purpose
        // of this class is just to house the factory method.
    }

}
