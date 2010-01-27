package fieldml.value;

import fieldml.domain.ContinuousListDomain;

public class ContinuousListDomainValue
    extends DomainValue<ContinuousListDomain>
{
    public final double[] values;


    public ContinuousListDomainValue( ContinuousListDomain domain, double ... values )
    {
        super( domain );

        this.values = values;
    }


    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append("( ");
        for( double d : values )
        {
            string.append( d );
            string.append( " " );
        }
        string.append( ")" );
        
        return string.toString();
    }
}
