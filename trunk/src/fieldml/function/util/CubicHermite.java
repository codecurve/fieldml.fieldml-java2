package fieldml.function.util;

public class CubicHermite {

  public static double psi01(final double xi) {
    return ( 1 - 3 * xi * xi + 2 * xi * xi * xi );
  }

  public static double psi02(final double xi) {
    return xi * xi * ( 3 - 2 * xi );
  }

  public static double psi11(final double xi) {
    return xi * ( xi - 1 ) * ( xi - 1 );
  }

  public static double psi12(final double xi) {
    return xi * xi * ( xi - 1 );
  }

}
