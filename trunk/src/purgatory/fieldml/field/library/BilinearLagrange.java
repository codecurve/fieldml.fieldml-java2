package purgatory.fieldml.field.library;

import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.field.Field;
import purgatory.fieldml.field.FieldValues;
import purgatory.fieldml.field.ParameterEvaluator;
import purgatory.fieldml.field.RealField;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;

/**
 * Source code: <code>
  <field id="library::bilinear_lagrange" value_domain="library::bilinear_interpolation_parameters">
    <parameter id="xi" domain="library::unit_square" />
    
    <evaluate_component id="node1">
      <multiply>
        <subtract>
          <constant_value value="1" />
          <parameter_value id="xi" component="xi1" />
        </subtract>
        <subtract>
          <constant_value value="1" />
          <parameter_value id="xi" component="xi2" />
        </subtract>
      </multiply>
    </evaluate_component>

    <evaluate_component id="node2">
      <multiply>
        <parameter_value id="xi" component="xi1" />
        <subtract>
          <constant_value value="1" />
          <parameter_value id="xi" component="xi2" />
        </subtract>
      </multiply>
    </evaluate_component>

    <evaluate_component id="node3">
      <multiply>
        <subtract>
          <constant_value value="1" />
          <parameter_value id="xi" component="xi1" />
        </subtract>
        <parameter_value id="xi" component="xi2" />
      </multiply>
    </evaluate_component>

    <evaluate_component id="node4">
      <multiply>
        <parameter_value id="xi" component="xi1" />
        <parameter_value id="xi" component="xi2" />
      </multiply>
    </evaluate_component>
  </field>
</code>
 */
public class BilinearLagrange
    extends Field
    implements RealField
{
    public BilinearLagrange( FieldmlObjectManager<Field> manager, FieldmlObjectManager<Domain> domainManager )
        throws FieldmlException
    {
        super( manager, "library::bilinear_lagrange", domainManager.get( "library::bilinear_interpolation_parameters" ) );

        addEvaluator( new ParameterEvaluator( "xi", domainManager.get( "library::unit_square" ), 0 ) );
    }


    @Override
    public void evaluate( FieldValues values, int[] valueIndexes, Value value )
        throws FieldmlException
    {
        if( valueIndexes.length < 1 )
        {
            throw new BadFieldmlParameterException();
        }
        Value parameter0 = values.values.get( valueIndexes[0] );
        
        if( ( parameter0.realValues == null ) || ( parameter0.realValues.length < 2 ) ||
            ( value.realValues == null ) || ( value.realValues.length < 4 ) )
        {
            throw new BadFieldmlParameterException();
        }
        
        double xi1 = parameter0.realValues[0];
        double xi2 = parameter0.realValues[1];

        value.realValues[0] = ( 1 - xi1 ) * ( 1 - xi2 );
        value.realValues[1] = xi1 * ( 1 - xi2 );
        value.realValues[2] = ( 1 - xi1 ) * xi2;
        value.realValues[3] = xi1 * xi2;
    }
}
