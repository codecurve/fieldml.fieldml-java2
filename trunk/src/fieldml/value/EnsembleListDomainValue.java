package fieldml.value;

import fieldml.domain.EnsembleListDomain;

public class EnsembleListDomainValue
    extends DomainValue<EnsembleListDomain>
{
    public final int[] values;


    public EnsembleListDomainValue( EnsembleListDomain domain, int ... values )
    {
        super( domain );

        this.values = values;
    }


    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append("( ");
        for( int i : values )
        {
            string.append( i );
            string.append( " " );
        }
        string.append( ")" );
        
        return string.toString();
    }
}
