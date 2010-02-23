package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.evaluator.ParameterTable;

public class ContinuousParameters
    extends ContinuousEvaluator
{
    private final ParameterTable<ContinuousDomainValue> table;


    public ContinuousParameters( String name, ContinuousDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain );

        table = new ParameterTable<ContinuousDomainValue>( parameterDomains );
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
        return table.evaluate( context );
    }
}
