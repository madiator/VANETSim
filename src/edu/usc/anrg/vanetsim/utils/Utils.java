/**
 *
 */
package edu.usc.anrg.vanetsim.utils;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
public class Utils {
  /**
   * Get the harmonic number of m
   * @param m
   * @return
   */
  public static double Harmonic(int m) {
    double sum = 0;
    for(int i = 1; i <= m; i++) {
      sum += (double) 1/i;
    }
    return sum;
  }
  /**
   * Return a random number from {0, 1, .. , m-1}
   * @param m set size
   * @return a random number
   */
  public static int randomNumber(int m) {
    return (int)(Math.random()*m);
  }

  public static void getZipf(double[] probabilities, double parameter) {
    // probability(i) = i^(-parameter)/sum(j^(-parameter));
    double sum = 0;
    for(int i = 0; i < probabilities.length; i++) {
      probabilities[i] = Math.pow(i + 1, parameter);
      sum += probabilities[i];
    }
    for(int i = 0; i < probabilities.length; i++) {
      probabilities[i] = probabilities[i]/sum;
    }
  }

  public static double average(int[] array) {
    double sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
    }
    return (sum/array.length);
  }

  public static double average(double[] array) {
    double sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
    }
    return (sum/array.length);
  }

  public static double average(boolean[] array) {
    double sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i]?1:0;
    }
    return (sum/array.length);
  }

  public static int min(int a, int b) {
    return (a > b ? b : a);
  }

  public static int max(int a, int b) {
    return (a > b ? a : b);
  }

 }