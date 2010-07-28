package fieldmlx.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import fieldml.domain.MeshDomain;
import fieldml.evaluator.ImportedContinuousEvaluator;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

/**
 * Very very simplistic FieldML-java to Collada converter.
 */
public class MinimalColladaExporter
{

    private static final String PATH_TO_COLLADA_SKELETON = "trunk/resources/ColladaSkeleton.xml";


    public static String exportFromFieldML( final Region region, int discretisation, final String meshName, final String fieldName )
        throws FileNotFoundException, IOException
    {
        MeshDomain meshDomain = region.getMeshDomain( meshName );
        ImportedContinuousEvaluator mesh = region.importContinuousEvaluator( "mesh_geometry", fieldName );
        final int elementCount = meshDomain.getElementDomain().getValueCount();
        DomainValues context = new DomainValues();

        ContinuousDomainValue v;

        StringBuilder xyzArray = new StringBuilder();
        StringBuilder polygonBlock = new StringBuilder();
        for( int elementNumber = 1; elementNumber <= elementCount; elementNumber++ )
        {
            for( int i = 0; i <= discretisation; i++ )
            {
                for( int j = 0; j <= discretisation; j++ )
                {

                    final double xi1 = i / (double)discretisation;
                    final double xi2 = j / (double)discretisation;

                    context.set( meshDomain, elementNumber, xi1, xi2 );
                    v = mesh.getValue( context );
                    xyzArray.append( "\n" );
                    xyzArray.append( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );

                }
            }
            xyzArray.append( "\n" );

            final int nodeOffsetOfElement = ( elementNumber - 1 ) * ( discretisation + 1 ) * ( discretisation + 1 );
            for( int i = 0; i < discretisation; i++ )
            {
                for( int j = 0; j < discretisation; j++ )
                {
                    final int nodeAtLowerXi1LowerXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 0 ) + ( j + 0 );
                    final int nodeAtLowerXi1UpperXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 0 ) + ( j + 1 );
                    final int nodeAtUpperXi1UpperXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 1 ) + ( j + 1 );
                    final int nodeAtUpperXi1LowerXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 1 ) + ( j + 0 );
                    polygonBlock.append( "<p>" );
                    polygonBlock.append( " " + nodeAtLowerXi1LowerXi2 );
                    polygonBlock.append( " " + nodeAtLowerXi1UpperXi2 );
                    polygonBlock.append( " " + nodeAtUpperXi1UpperXi2 );
                    polygonBlock.append( " " + nodeAtUpperXi1LowerXi2 );
                    polygonBlock.append( "</p>\n" );
                }
            }
        }

        final int polygonCount = discretisation * discretisation * elementCount;
        final int vertexCount = ( discretisation + 1 ) * ( discretisation + 1 ) * elementCount;
        final int xyzArrayCount = vertexCount * 3;

        final String colladaString = fillInColladaTemplate( xyzArray, polygonBlock, polygonCount, vertexCount, xyzArrayCount );

        return colladaString;
    }


    public static String exportFromFieldML( final Region region, int discretisation, final String fieldName, final String mesh1Name,
        final String mesh2Name )
        throws FileNotFoundException, IOException
    {
        MeshDomain mesh1Domain = region.getMeshDomain( mesh1Name );
        MeshDomain mesh2Domain = region.getMeshDomain( mesh2Name );
        ImportedContinuousEvaluator mesh = region.importContinuousEvaluator( "mesh_geometry", fieldName );
        final int element1Count = mesh1Domain.getElementDomain().getValueCount();
        final int element2Count = mesh2Domain.getElementDomain().getValueCount();

        ContinuousDomainValue v;

        StringBuilder xyzArray = new StringBuilder();
        StringBuilder polygonBlock = new StringBuilder();
        DomainValues context = new DomainValues();
        int elementNumber = 0;

        for( int element1Number = 1; element1Number <= element1Count; element1Number++ )
        {
            for( int element2Number = 1; element2Number <= element2Count; element2Number++ )
            {
                for( int i = 0; i <= discretisation; i++ )
                {
                    for( int j = 0; j <= discretisation; j++ )
                    {

                        final double xi1 = i / (double)discretisation;
                        final double xi2 = j / (double)discretisation;

                        context.set( mesh1Domain, element1Number, xi1 );
                        context.set( mesh2Domain, element2Number, xi2 );

                        v = mesh.getValue( context );
                        xyzArray.append( "\n" );
                        xyzArray.append( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );

                    }
                }
                xyzArray.append( "\n" );

                elementNumber++;

                final int nodeOffsetOfElement = ( elementNumber - 1 ) * ( discretisation + 1 ) * ( discretisation + 1 );
                for( int i = 0; i < discretisation; i++ )
                {
                    for( int j = 0; j < discretisation; j++ )
                    {
                        final int nodeAtLowerXi1LowerXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 0 ) + ( j + 0 );
                        final int nodeAtLowerXi1UpperXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 0 ) + ( j + 1 );
                        final int nodeAtUpperXi1UpperXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 1 ) + ( j + 1 );
                        final int nodeAtUpperXi1LowerXi2 = nodeOffsetOfElement + ( discretisation + 1 ) * ( i + 1 ) + ( j + 0 );
                        polygonBlock.append( "<p>" );
                        polygonBlock.append( " " + nodeAtLowerXi1LowerXi2 );
                        polygonBlock.append( " " + nodeAtLowerXi1UpperXi2 );
                        polygonBlock.append( " " + nodeAtUpperXi1UpperXi2 );
                        polygonBlock.append( " " + nodeAtUpperXi1LowerXi2 );
                        polygonBlock.append( "</p>\n" );
                    }
                }
            }
        }

        final int polygonCount = discretisation * discretisation * elementNumber;
        final int vertexCount = ( discretisation + 1 ) * ( discretisation + 1 ) * elementNumber;
        final int xyzArrayCount = vertexCount * 3;

        final String colladaString = fillInColladaTemplate( xyzArray, polygonBlock, polygonCount, vertexCount, xyzArrayCount );

        return colladaString;
    }


    public static String export1DFromFieldML( final Region region, final String meshName, final String fieldName, int discretisation )
        throws FileNotFoundException, IOException
    {
        MeshDomain meshDomain = region.getMeshDomain( meshName );
        ImportedContinuousEvaluator mesh = region.importContinuousEvaluator( "mesh_geometry", fieldName );
        int elementCount = meshDomain.getElementDomain().getValueCount();
        double deltaX = 1.0 / discretisation;
        double x = deltaX;
        ContinuousDomainValue v;
        DomainValues context = new DomainValues();

        StringBuilder xyzArray = new StringBuilder();
        StringBuilder polygonBlock = new StringBuilder();
        for( int elementNumber = 1; elementNumber <= elementCount; elementNumber++ )
        {
            x -= deltaX;
            for( int j = 0; j <= discretisation; j++ )
            {
                final double xi1 = j / (double)discretisation;

                context.set( meshDomain, elementNumber, xi1 );
                v = mesh.getValue( context );
                xyzArray.append( "\n" );
                xyzArray.append( " " + x + " " + v.values[0] + " 0.0" );
                xyzArray.append( " " + x + " " + v.values[0] + " 1.0" );

                x += deltaX;
            }
            xyzArray.append( "\n" );

            final int nodeOffsetOfElement = ( elementNumber - 1 ) * ( discretisation + 1 ) * 2;

            for( int j = 0; j < discretisation; j++ )
            {
                final int nodeAtLowerXi1LowerXi2 = nodeOffsetOfElement + ( j * 2 ) + 0;
                final int nodeAtLowerXi1UpperXi2 = nodeOffsetOfElement + ( j * 2 ) + 2;
                final int nodeAtUpperXi1UpperXi2 = nodeOffsetOfElement + ( j * 2 ) + 3;
                final int nodeAtUpperXi1LowerXi2 = nodeOffsetOfElement + ( j * 2 ) + 1;
                polygonBlock.append( "<p>" );
                polygonBlock.append( " " + nodeAtLowerXi1LowerXi2 );
                polygonBlock.append( " " + nodeAtLowerXi1UpperXi2 );
                polygonBlock.append( " " + nodeAtUpperXi1UpperXi2 );
                polygonBlock.append( " " + nodeAtUpperXi1LowerXi2 );
                polygonBlock.append( "</p>\n" );
            }
        }

        final int polygonCount = discretisation * elementCount;
        final int vertexCount = ( discretisation + 1 ) * 2 * elementCount;
        final int xyzArrayCount = vertexCount * 3;

        final String colladaString = fillInColladaTemplate( xyzArray, polygonBlock, polygonCount, vertexCount, xyzArrayCount );

        return colladaString;
    }


    private static String fillInColladaTemplate( StringBuilder xyzArray, StringBuilder polygonBlock, final int polygonCount,
        final int vertexCount, final int xyzArrayCount )
        throws FileNotFoundException, IOException
    {
        StringBuilder fullCollada = new StringBuilder();

        FileReader f = new FileReader( PATH_TO_COLLADA_SKELETON );
        BufferedReader b = new BufferedReader( f );
        String nextLine = b.readLine();
        while( nextLine != null )
        {
            fullCollada.append( nextLine );
            fullCollada.append( "\n" );
            nextLine = b.readLine();
        }

        {
            final String xyzArrayCountToken = "xyzArrayCount";
            searchAndReplaceOnce( fullCollada, xyzArrayCountToken, "" + xyzArrayCount );
        }

        {
            final String xyzArrayToken = "xyzArray";
            searchAndReplaceOnce( fullCollada, xyzArrayToken, xyzArray.toString() );
        }

        {
            final String vertexCountToken = "vertexCount";
            searchAndReplaceOnce( fullCollada, vertexCountToken, "" + vertexCount );
        }

        {
            final String polygonCountToken = "polygonCount";
            searchAndReplaceOnce( fullCollada, polygonCountToken, "" + polygonCount );
        }

        {
            final String polygonBlockToken = "polygonBlock";
            searchAndReplaceOnce( fullCollada, polygonBlockToken, polygonBlock.toString() );
        }

        final String colladaString = fullCollada.toString();
        return colladaString;
    }


    private static void searchAndReplaceOnce( StringBuilder subjectText, final String token, String string )
    {
        final int tokenStart = subjectText.indexOf( token );
        subjectText.replace( tokenStart, tokenStart + token.length(), string );
    }
}
