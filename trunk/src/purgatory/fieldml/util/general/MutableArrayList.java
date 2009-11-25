package purgatory.fieldml.util.general;

import java.util.ArrayList;

/**
 * A glue-class that allows us to expose an immutable interface to an ArrayList
 */
public class MutableArrayList<T extends Object>
    extends ArrayList<T>
    implements ImmutableList<T>
{

}
