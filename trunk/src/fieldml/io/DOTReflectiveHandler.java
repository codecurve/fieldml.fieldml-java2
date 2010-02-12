package fieldml.io;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;

public class DOTReflectiveHandler
    implements ReflectiveHandler
{
    private PrintStream output;

    private Deque<String> nodeStack;

    private Deque<StringBuilder> labelStack;

    private StringBuilder currentLabel;
    

    private String rawString( Object o )
    {
        return o.getClass().getName() + '@' + Integer.toHexString( o.hashCode() );
    }


    public DOTReflectiveHandler( PrintStream output )
    {
        this.output = output;

        nodeStack = new ArrayDeque<String>();
        labelStack = new ArrayDeque<StringBuilder>();

        output.println( "digraph FieldML { ratio = 0.707; graph [fontsize=24];" );
    }


    @Override
    public void onEndInstance( Object o )
    {
        if( nodeStack.size() > 0 )
        {
            output.println( nodeStack.peek() + " [shape=rectangle,label=\"" + currentLabel.toString() + "\"];" );
    
            nodeStack.pop();
            labelStack.pop();
    
            currentLabel = labelStack.peek();
        }
    }


    @Override
    public void onIntField( String name, Integer value )
    {
        currentLabel.append( name + " = " + value + "\\l" );
    }


    @Override
    public boolean onStartInstance( Object o )
    {
        if( o.getClass().getSimpleName().contains( "MapEntry" ) )
        {
            return false;
        }
        
        if( nodeStack.size() > 0 )
        {
            output.println( nodeStack.peek() + " -> \"" + rawString( o ) + "\"; /* start instance */" );
        }

        nodeStack.push( "\"" + rawString( o ) + "\"" );
        labelStack.push( new StringBuilder() );

        currentLabel = labelStack.peek();
        currentLabel.append( o.getClass().getSimpleName() + "\\l" );
        
        return true;
    }


    @Override
    public void onIntListElement( Object o )
    {
        currentLabel.append( " " + o.toString() );
    }


    @Override
    public void onMapEntry( String key, String value )
    {
//        currentLabel.append( "[" + key + " -> " + value + "]" );
    }


    @Override
    public void onDoubleListElement( Object o )
    {
        currentLabel.append( " " + o.toString() );
    }


    @Override
    public void onStartList( Object o, String name )
    {
        currentLabel.append( name + ": ..." );
    }


    @Override
    public void onEndList( Object o )
    {
        currentLabel.append( "\\l" );
    }


    @Override
    public void onStringField( String name, String string )
    {
        currentLabel.append( name + " = " + string + "\\l" );
    }


    @Override
    public void onFieldAsString( String name, Object o )
    {
        if( nodeStack.size() > 0 )
        {
            output.println( nodeStack.peek() + " -> \"" + rawString( o ) + "\"; /* fas */" );
        }

        currentLabel.append( name + " = " + o.toString() + "\\l" );

        // output.println( "\"" + rawString( o ) + "\" [shape=rectangle,label=\"" + o.toString() + "\"];" );
    }


    @Override
    public void onListElementAsString( Object o )
    {
        if( nodeStack.size() > 0 )
        {
            output.println( nodeStack.peek() + " -> \"" + rawString( o ) + "\"; /* fas */" );
        }

        currentLabel.append( " " + o.toString() );
    }


    @Override
    public void onStringListElement( String o )
    {
        currentLabel.append( " " + o.toString() );
    }
}
