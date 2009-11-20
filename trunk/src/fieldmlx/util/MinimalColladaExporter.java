package fieldmlx.util;

import fieldml.domain.MeshDomain;
import fieldml.field.Field;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Very very simplistic FieldML-java to Collada converter.
 */
public class MinimalColladaExporter {

  public static String exportFromFieldML(final Region region, final String meshName, final int elementCount, int discretisation) throws FileNotFoundException, IOException {
    MeshDomain meshDomain = region.getMeshDomain(meshName);
    Field<?, ContinuousDomainValue> mesh = (Field<?, ContinuousDomainValue>) region.getField("test_mesh.coordinates");

    ContinuousDomainValue v;

    StringBuilder xyzArray = new StringBuilder();
    StringBuilder polygonBlock = new StringBuilder();
    int nodeNumber = 0;
    for (int elementNumber = 1; elementNumber <= elementCount; elementNumber++) {
      for (int i = 0; i < discretisation; i++) {
        for (int j = 0; j < discretisation; j++) {
          polygonBlock.append("<p>");

          final double xi1lower = (i + 0) / (double) discretisation;
          final double xi2lower = (j + 0) / (double) discretisation;
          final double xi1upper = (i + 1) / (double) discretisation;
          final double xi2upper = (j + 1) / (double) discretisation;

          v = mesh.evaluate(meshDomain, elementNumber, xi1lower, xi2lower);
          xyzArray.append(" " + v.values[0] + " " + v.values[1] + " " + v.values[2]);
          polygonBlock.append(" " + nodeNumber++);

          v = mesh.evaluate(meshDomain, elementNumber, xi1upper, xi2lower);
          xyzArray.append(" " + v.values[0] + " " + v.values[1] + " " + v.values[2]);
          polygonBlock.append(" " + nodeNumber++);

          v = mesh.evaluate(meshDomain, elementNumber, xi1upper, xi2upper);
          xyzArray.append(" " + v.values[0] + " " + v.values[1] + " " + v.values[2]);
          polygonBlock.append(" " + nodeNumber++);

          v = mesh.evaluate(meshDomain, elementNumber, xi1lower, xi2upper);
          xyzArray.append(" " + v.values[0] + " " + v.values[1] + " " + v.values[2]);
          polygonBlock.append(" " + nodeNumber++);
          polygonBlock.append("</p>\n");

        }
      }
    }

    final int polygonCount = discretisation * discretisation * elementCount;
    final int vertexCount = polygonCount * 4;
    final int xyzArrayCount = vertexCount * 3;

    final String colladaString = fillInColladaTemplate(xyzArray, polygonBlock, polygonCount, vertexCount, xyzArrayCount);

    return colladaString;
  }

  private static String fillInColladaTemplate(StringBuilder xyzArray, StringBuilder polygonBlock, final int polygonCount, final int vertexCount, final int xyzArrayCount) throws FileNotFoundException, IOException {
    StringBuilder fullCollada = new StringBuilder();

    FileReader f = new FileReader("trunk/data/ColladaSkeleton.xml");
    BufferedReader b = new BufferedReader(f);
    String nextLine = b.readLine();
    while (nextLine != null) {
      fullCollada.append(nextLine);
      fullCollada.append("\n");
      nextLine = b.readLine();
    }

    {
      final String xyzArrayCountToken = "xyzArrayCount";
      searchAndReplaceOnce(fullCollada, xyzArrayCountToken, "" + xyzArrayCount);
    }

    {
      final String xyzArrayToken = "xyzArray";
      searchAndReplaceOnce(fullCollada, xyzArrayToken, xyzArray.toString());
    }

    {
      final String vertexCountToken = "vertexCount";
      searchAndReplaceOnce(fullCollada, vertexCountToken, "" + vertexCount);
    }

    {
      final String polygonCountToken = "polygonCount";
      searchAndReplaceOnce(fullCollada, polygonCountToken, "" + polygonCount);
    }

    {
      final String polygonBlockToken = "polygonBlock";
      searchAndReplaceOnce(fullCollada, polygonBlockToken, polygonBlock.toString());
    }

    final String colladaString = fullCollada.toString();
    return colladaString;
  }

  private static void searchAndReplaceOnce(StringBuilder subjectText, final String token, String string) {
    final int tokenStart = subjectText.indexOf(token);
    subjectText.replace(tokenStart, tokenStart + token.length(), string);
  }
}
