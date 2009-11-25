package purgatory.fieldml.value;

import purgatory.fieldml.domain.ContinuousDomain;
import purgatory.fieldml.domain.DiscreteFieldDomain;
import purgatory.fieldml.domain.DiscreteIndexDomain;
import purgatory.fieldml.domain.Domain;

public class Value
{
    // As this class is involved in computationally-heavy work, getters and setters aren't used.
    // Only one of these arrays will be non-null at any time. This makes it eerily close to a
    // C-style tagged union.
    public final int[] indexValues;

    public final double[] realValues;
    
    public final int[] fieldIdValues;

    public final Domain domain;


    public Value( Domain domain )
    {
        this.domain = domain;

        //This is somewhat ugly, but it seems to be the least-ugly way to do it.
        if( domain instanceof DiscreteIndexDomain )
        {
            indexValues = new int[domain.getComponentCount()];
            realValues = null;
            fieldIdValues = null;
        }
        else if( domain instanceof ContinuousDomain )
        {
            indexValues = null;
            realValues = new double[domain.getComponentCount()];
            fieldIdValues = null;
        }
        else if( domain instanceof DiscreteFieldDomain )
        {
            indexValues = null;
            realValues = null;
            fieldIdValues = new int[domain.getComponentCount()];
        }
        else
        {
            assert true : "Value should be able to contain values for domain-type " + domain.getClass();
            //ERROR
            indexValues = null;
            realValues = null;
            fieldIdValues = null;
        }
    }
    
    
    public void assign( Value value )
    {
        int count = domain.getComponentCount();
        
        if( indexValues != null )
        {
            for( int i = 0; i < count; i++ )
            {
                indexValues[i] = value.indexValues[i];
            }
        }
        if( realValues != null )
        {
            for( int i = 0; i < count; i++ )
            {
                realValues[i] = value.realValues[i];
            }
        }
        if( fieldIdValues != null )
        {
            for( int i = 0; i < count; i++ )
            {
                fieldIdValues[i] = value.fieldIdValues[i];
            }
        }
    }
}
