package fieldml.function;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousAggregateField;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public class BicubicHermiteQuad
    extends ContinuousFunction
{
    @SerializationAsString
    public final ContinuousAggregateField dofs;

    @SerializationAsString
    public final ContinuousParameters dofScaling;

    @SerializationAsString
    public final EnsembleParameters dofIndexes;

    private final EnsembleDomain iteratedDomain;


    public BicubicHermiteQuad( String name, ContinuousAggregateField dofs, ContinuousParameters dofScaling,
        EnsembleParameters dofIndexes, EnsembleDomain localNodeDomain )
    {
        super( name );
        // TODO Assert that dofIndexes value domain is dofs only parameter domain
        // TODO Assert that dofIndexes's parameter domain has the right cardinality for the given interpolation.
        this.dofs = dofs;
        this.dofScaling = dofScaling;
        this.dofIndexes = dofIndexes;
        this.iteratedDomain = localNodeDomain;
    }


    private double evaluate( double[] params, double[] xi )
    {
        final double x = xi[0];
        final double y = xi[1];

        double p01x = ( 1 - 3 * x * x + 2 * x * x * x );
        double p11x = x * ( x - 1 ) * ( x - 1 );
        double p02x = x * x * ( 3 - 2 * x );
        double p12x = x * x * ( x - 1 );

        double p01y = ( 1 - 3 * y * y + 2 * y * y * y );
        double p11y = y * ( y - 1 ) * ( y - 1 );
        double p02y = y * y * ( 3 - 2 * y );
        double p12y = y * y * ( y - 1 );

        double value = 0 + //
            p01x * p01y * params[0] + p02x * p01y * params[4] + //
            p01x * p02y * params[8] + p02x * p02y * params[12] + //
            p11x * p01y * params[1] + p12x * p01y * params[5] + //
            p11x * p02y * params[9] + p12x * p02y * params[13] + //
            p01x * p11y * params[2] + p02x * p11y * params[6] + //
            p01x * p12y * params[10] + p02x * p12y * params[14] + //
            p11x * p11y * params[3] + p12x * p11y * params[7] + //
            p11x * p12y * params[11] + p12x * p12y * params[15];

        return value;
    }


    @Override
    public double evaluate( MeshDomainValue value )
    {
        int parameterCount;
        double[] params = new double[16];
        DomainValues context = new DomainValues();
        context.set( value.domain.elementDomain, value.indexValue );

        parameterCount = 0;
        for( int localNodeIndex = 1; localNodeIndex <= iteratedDomain.getValueCount(); localNodeIndex++ )
        {
            context.set( iteratedDomain, localNodeIndex );

            final ContinuousDomainValue scaling = dofScaling.evaluate( context );

            final EnsembleDomainValue indexOfGlobalNode = dofIndexes.evaluate( context );
            context.set( indexOfGlobalNode );

            ContinuousDomainValue dofValues = dofs.evaluate( context );

            params[parameterCount++] = dofValues.values[0] * scaling.values[0];
            params[parameterCount++] = dofValues.values[1] * scaling.values[1];
            params[parameterCount++] = dofValues.values[2] * scaling.values[2];
            params[parameterCount++] = dofValues.values[3] * scaling.values[3];
        }

        return evaluate( params, value.chartValues );
    }
}
