package fieldml.field;

import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;

public class PiecewiseField
    extends Field
{
    public EnsembleDomain piecewiseDomain;
    
    public Domain otherParameterDomains;
}
