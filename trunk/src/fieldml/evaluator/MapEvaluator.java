package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.evaluator.EnsembleClientVariableEvaluator;

public class MapEvaluator
    extends AbstractContinuousEvaluator
{
    @SerializationAsString
    public final EnsembleEvaluator valueIndexes;

    @SerializationAsString
    public final ContinuousEvaluator valueWeights;

    @SerializationAsString
    public final ContinuousEvaluator valueSource;

    public final String indexedVariable;


    public MapEvaluator( String name, ContinuousDomain valueDomain, EnsembleEvaluator valueIndexes, ContinuousEvaluator valueWeights,
        ContinuousEvaluator valueSource, String indexedVariable )
    {
        super( name, valueDomain );

        this.valueIndexes = valueIndexes;
        this.valueWeights = valueWeights;
        this.valueSource = valueSource;
        this.indexedVariable = indexedVariable;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        double[] weights = valueWeights.evaluate( context ).values;
        int[] indexes = valueIndexes.evaluate( context ).values;
        double[] values = new double[indexes.length];

        EnsembleClientVariableEvaluator index = new EnsembleClientVariableEvaluator( "x", valueIndexes.getValueDomain().baseDomain );

        EnsembleEvaluator oldVariable = context.getEnsembleVariable( indexedVariable );
        context.setVariable( indexedVariable, index );
        for( int i = 0; i < indexes.length; i++ )
        {
            index.setValue( indexes[i] );
            values[i] = valueSource.evaluate( context ).values[0];
        }
        context.setVariable( indexedVariable, oldVariable );

        double finalValue = DotProductEvaluator.dotProduct( values, weights );

        return valueDomain.makeValue( finalValue );
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        ArrayList<Evaluator<?>> variables = new ArrayList<Evaluator<?>>();

        variables.addAll( valueWeights.getVariables() );
        variables.addAll( valueIndexes.getVariables() );
        variables.addAll( valueSource.getVariables() );

        return variables;
    }
}
