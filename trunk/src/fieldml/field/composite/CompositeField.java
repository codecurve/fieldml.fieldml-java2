package fieldml.field.composite;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;
import fieldml.field.Field;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public abstract class CompositeField<D extends Domain, V extends DomainValue<D>>
    extends Field<D, V>
{
    @SerializationAsString
    public final Domain[] parameterDomains;

    public final List<FieldOperation> operations;


    public CompositeField( String name, D valueDomain, Domain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;

        operations = new ArrayList<FieldOperation>();
    }


    protected void apply( DomainValues values )
    {
        for( FieldOperation o : operations )
        {
            o.perform( values );
        }
    }


    public void importField( Field<?, ?> field )
    {
        operations.add( new FieldImport( field ) );
    }


    public void importThrough( ContinuousParameters parameters, EnsembleParameters iteratedParameters, EnsembleDomain iteratedDomain,
        ContinuousDomain valueDomain )
    {
        operations.add( new FieldImportThrough( parameters, iteratedParameters, iteratedDomain, valueDomain ) );
    }
}
