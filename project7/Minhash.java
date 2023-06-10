package project7;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Minhash {
  private static final int numHashes = 10;
  private static int numDocs = 1000;

  public double jaccard(String fA, String fB) {
    /**
     * fA: Name of first file
     * fB: Name of second file
     */

    // Your code goes here
    HashMap<String, String> plagiaries = new HashMap<>();
    ArrayList<String> docNames = new ArrayList<>();

    try {
      // Parse the ground truth tables
      BufferedReader truthReader = new BufferedReader(new FileReader(fA));
      String line;
      while ((line = truthReader.readLine()) != null) {
          String[] docs = line.split(" ");
          plagiaries.put(docs[0], docs[1]);
          plagiaries.put(docs[1], docs[0]);
      }
      truthReader.close();

      // Convert documents to sets of shingles
      System.out.println("Shingling articles...");
      int curShingleID = 0;
      Map<String, Set<Integer>> docsAsShingleSets = new HashMap<>();
      BufferedReader dataReader = new BufferedReader(new FileReader(fB));
      int totalShingles = 0;
      for (int i = 0; i < numDocs; i++) {
          String[] words = dataReader.readLine().split(" ");
          String docID = words[0];
          docNames.add(docID);
          Set<Integer> shinglesInDoc = new HashSet<>();
          for (int index = 0; index < words.length - 2; index++) {
              String shingle = words[index] + " " + words[index + 1] + " " + words[index + 2];
              int crc = shingle.hashCode();
              shinglesInDoc.add(crc);
          }
          docsAsShingleSets.put(docID, shinglesInDoc);
          totalShingles += (words.length - 2);
      }
      dataReader.close();
      System.out.println("\nShingling " + numDocs + " docs took " + (totalShingles / numDocs) + " sec.");

      // Define triangle matrices
      int numElems = (numDocs * (numDocs - 1)) / 2;
      double[] JSim = new double[numElems];
      double[] estJSim = new double[numElems];
      double jaccardSim = 0.0;
      // Calculate Jaccard similarities
      if (numDocs <= 2500) {
          System.out.println("\nCalculating Jaccard Similarities...");
          long startTime = System.currentTimeMillis();
          for (int i = 0; i < numDocs; i++) {
              if (i % 100 == 0) {
                  System.out.println("  (" + i + " / " + numDocs + ")");
              }
              Set<Integer> set1 = docsAsShingleSets.get(docNames.get(i));
              for (int j = i + 1; j < numDocs; j++) {
                  Set<Integer> set2 = docsAsShingleSets.get(docNames.get(j));
                  double intersectionSize = 0;
                  Set<Integer> intersection = new HashSet<>(set1);
                  intersection.retainAll(set2);
                  intersectionSize = intersection.size();
                  jaccardSim = intersectionSize / (double) (set1.size() + set2.size() - intersectionSize);

                  JSim[getTriangleIndex(i, j)] = jaccardSim;
              }
          }
          long endTime = System.currentTimeMillis();
          System.out.println("\nCalculating all Jaccard Similarities took " + (endTime - startTime) + " ms");
      }
      // Generate MinHash signatures
      System.out.println("\nGenerating random hash functions...");
      long startTime = System.currentTimeMillis();
      Random rand = new Random();
      int[][] minHashValues = new int[numHashes][numDocs];
      int nextPrime = 429496731;
      for (int i = 0; i < numHashes; i++) {
          int a = rand.nextInt(nextPrime - 1) + 1;
          int b = rand.nextInt(nextPrime);
          for (int j = 0; j < numDocs; j++) {
              int docID = Integer.parseInt(docNames.get(j));
              int hashCode = (a * docID + b) % nextPrime;
              int minHashCode = Integer.MAX_VALUE;
              Set<Integer> shingles = docsAsShingleSets.get(docNames.get(j));
              for (int shingle : shingles) {
                  minHashCode = Math.min(minHashCode, (hashCode ^ shingle));
              }
              minHashValues[i][j] = minHashCode;
          }
      }
      long endTime = System.currentTimeMillis();
      System.out.println("\nGenerating MinHash signatures took " + (endTime - startTime) + " ms");

      // Compare MinHash signatures
      System.out.println("\nComparing MinHash signatures...");
      startTime = System.currentTimeMillis();
      for (int i = 0; i < numDocs; i++) {
          if (i % 100 == 0) {
              System.out.println("  (" + i + " / " + numDocs + ")");
          }
          for (int j = i + 1; j < numDocs; j++) {
              double estJaccardSim = 0.0;
              for (int k = 0; k < numHashes; k++) {
                  if (minHashValues[k][i] == minHashValues[k][j]) {
                      estJaccardSim += 1.0;
                  }
              }
              estJaccardSim = estJaccardSim / numHashes;
              estJSim[getTriangleIndex(i, j)] = estJaccardSim;
          }
      }
      endTime = System.currentTimeMillis();
      System.out.println("\nComparing MinHash signatures took " + (endTime - startTime) + " ms");

      // Compare estimated Jaccard similarities with ground truth
      System.out.println("\n   Est. Jaccard   |   Actual Jaccard");
      System.out.println("-----------------+----------------");
      for (Map.Entry<String, String> entry : plagiaries.entrySet()) {
          int doc1 = Integer.parseInt(entry.getKey());
          int doc2 = Integer.parseInt(entry.getValue());
          double actualJaccard = JSim[getTriangleIndex(doc1, doc2)];
          double estJaccard = estJSim[getTriangleIndex(doc1, doc2)];
          System.out.printf("%6d   %6d   |   %.5f   %.5f%n", doc1, doc2, estJaccard, actualJaccard);
      }
      
      // Return the calculated Jaccard similarity of the two files
      return jaccardSim;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0.0;
  }

  private static int getTriangleIndex(int i, int j) {
    if (i == j) {
        System.err.println("Illegal index: " + i + " is equal to " + j);
        System.exit(1);
    }
    if (i > j) {
        int temp = i;
        i = j;
        j = temp;
    }
    return ((numDocs - i) * (i - 1) / 2) + (j - i) - 1;
  }
}
