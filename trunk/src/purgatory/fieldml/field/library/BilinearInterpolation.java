package purgatory.fieldml.field.library;

import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.field.Field;
import purgatory.fieldml.field.FieldValues;
import purgatory.fieldml.field.ParameterEvaluator;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;

/**
 * Source code:<code>
  <field id="library::bilinear_interpolation" value_domain="library::infinite_line">
    <parameter id="parameters" domain="library::bilinear_interpolation_parameters" />
    <mapped_parameter id="phi" mapping_field="library::bilinear_lagrange" />
    
    <evaluate_component id="value">
      <dot_product>
        <parameter_vector id="parameters" />
        <parameter_vector id="phi" />
      </dot_product>
    </evaluate_component>
  </field>
  </code>
 */
public class BilinearInterpolation
    extends Field
{
    public BilinearInterpolation( FieldmlObjectManager<Field> manager, FieldmlObjectManager<Domain> domainManager )
        throws FieldmlException
    {
        super( manager, "library::bilinear_lagrange", domainManager.get( "library::bilinear_interpolation_parameters" ) );

        addEvaluator( new ParameterEvaluator( "parameters", domainManager.get( "library::bilinear_interpolation_parameters" ), 0 ) );
        addEvaluator( new ParameterEvaluator( "phi", domainManager.get( "library::unit_square" ), 1 ) );
    }


    @Override
    public void evaluate( FieldValues values, int[] valueIndexes, Value value )
        throws FieldmlException
    {
        if( valueIndexes.length < 2 )
        {
            throw new BadFieldmlParameterException();
        }
        Value parameter0 = values.values.get( valueIndexes[0] );
        Value parameter1 = values.values.get( valueIndexes[1] );
        
        if( ( parameter0.realValues == null ) || ( parameter0.realValues.length < 4 ) ||
            ( parameter1.realValues == null ) || ( parameter1.realValues.length < 2 ) ||
            ( value.realValues == null ) || ( value.realValues.length < 1 ) )
        {
            throw new BadFieldmlParameterException();
        }
        
        double[] nodeValues = parameter0.realValues;
        double[] xi = parameter1.realValues;

        double phi1 = ( 1 - xi[0] ) * ( 1 - xi[1] );
        double phi2 = xi[0] * ( 1 - xi[1] );
        double phi3 = ( 1 - xi[0] ) * xi[1];
        double phi4 = xi[0] * xi[1];

        value.realValues[0] = ( phi1 * nodeValues[0] ) + ( phi2 * nodeValues[1] ) + ( phi3 * nodeValues[2] )
            + ( phi4 * nodeValues[3] );
    }
}
