package purgatory.fieldml.field.component.evaluated;

import purgatory.fieldml.field.FieldValues;

public interface Evaluator
{
    // TODO Normalization of field parameters for evaluation
    //
    // The parent field should maintain a separate list of values, one for each actual type.
    // As new domains/parameter types are specified, a 'null' entry should be placed on
    // each list except for the correct one. Evaluators will not actually reference those
    // null entries, as their parameter indexes will only ever point to correctly allocated
    // values.
    //
    // The alternative is for the field to sort its domains into a 'canonical ordering', i.e.
    // a list of real-valued domains, then index-valued, etc. In doing so, it must correct
    // the 'parameter numbers' expressed in the input, before passing them along to evaluators.
    //
    // This allows us to pass a small number of homogenous lists of values when evaluating,
    // rather than a single inhomogenous list which needs to be subjected to casting
    // (and the associated overhead) before it can be used.
    //
    // At the moment, evaluated components can only act on real-valued parameters, so
    // we don't bother passing other types of parameters.
    //
    // For extra optimization, this could easily be a simple array, as it is a fixed-length
    // for any given configuration.

    // Specifying an arbitrarily nested composition of binary operators on
    // domain, constant and/or imported arguments seems non-trivial. Perhaps
    // passing an array of argument specifiers, and an array of operator
    // specifiers, and applying an RPN-style evaluation algorithm might work.
    public double evaluate( FieldValues values );
}
