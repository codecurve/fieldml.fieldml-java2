package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.evaluator.ParameterTable;

public class ContinuousParameters
    extends AbstractContinuousEvaluator
{
    public final ParameterTable<ContinuousDomainValue> table;

    private final EnsembleEvaluator[] indexEvaluators;


    public ContinuousParameters( String name, ContinuousDomain valueDomain, EnsembleEvaluator... indexEvaluators )
    {
        super( name, valueDomain );

        EnsembleDomain[] parameterDomains = new EnsembleDomain[indexEvaluators.length];
        for( int i = 0; i < indexEvaluators.length; i++ )
        {
            parameterDomains[i] = indexEvaluators[i].getValueDomain();
        }

        table = new ParameterTable<ContinuousDomainValue>( parameterDomains );
        
        this.indexEvaluators = indexEvaluators;
    }


    public void setValue( int key, double... values )
    {
        if( table.parameterCount() == 1 )
        {
            table.setValue( key, valueDomain.makeValue( values ) );
        }
        else
        {
            int[] keys = new int[2];
            keys[0] = key;
            for( int i = 1; i <= values.length; i++ )
            {
                keys[1] = i;
                setValue( keys, values[i - 1] );
            }
        }
    }


    public void setValue( int[] keys, double... values )
    {
        table.setValue( keys, valueDomain.makeValue( values ) );
    }


    public void setDefaultValue( double... values )
    {
        table.setDefaultValue( valueDomain.makeValue( values ) );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        int[] indexes = new int[indexEvaluators.length];
        for( int i = 0; i < indexEvaluators.length; i++ )
        {
            indexes[i] = indexEvaluators[i].evaluate( context ).values[0];
        }

        return table.evaluate( indexes );
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        return new ArrayList<Evaluator<?>>();
    }
}
