package fieldml.evaluator;

public interface MarkedUp
{
    public void set( String attribute, String value );


    public String get( String attribute );


    public boolean has( String attribute, String value );
}
