package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldmlx.evaluator.ParameterTable;

public class EnsembleParameters
    extends EnsembleEvaluator
{
    public final ParameterTable<EnsembleDomainValue> table;


    public EnsembleParameters( String name, EnsembleDomain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain );

        table = new ParameterTable<EnsembleDomainValue>( parameterDomains );
    }


    public void setValue( int key, int... values )
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


    public void setValues( int... values )
    {
        if( table.parameterCount() == 1 )
        {
            int[] keys = new int[1];
            for( int i = 1; i <= values.length; i++ )
            {
                keys[0] = i;
                setValue( keys, values[i - 1] );
            }
        }
    }


    public void setValue( int[] keys, int... values )
    {
        table.setValue( keys, valueDomain.makeValue( values ) );
    }


    public void setDefaultValue( int... values )
    {
        table.setDefaultValue( valueDomain.makeValue( values ) );
    }


    @Override
    public EnsembleDomainValue getValue( DomainValues context )
    {
        return table.evaluate( context );
    }
}
