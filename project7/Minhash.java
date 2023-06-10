package project7;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Minhash {
    private static final int numHashes = 42;

    public double jaccard(String fileA, String fileB) {
        try {
            // Convert documents to sets of shingles
            Set<String> shingles1 = getShinglesFromFile(fileA);
            Set<String> shingles2 = getShinglesFromFile(fileB);

            // Generate MinHash signatures
            int[][] minHashValues = generateMinHashSignatures(shingles1, shingles2);

            // Compare MinHash signatures
            double estJaccardSim = calculateEstimatedJaccardSimilarity(minHashValues);

            // Return the estimated Jaccard similarity
            return estJaccardSim;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Set<String> getShinglesFromFile(String file) throws IOException {
        Set<String> shingles = new HashSet<>();

        BufferedReader dataReader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = dataReader.readLine()) != null) {
            shingles.add(line);
        }
        dataReader.close();

        return shingles;
    }

    private int[][] generateMinHashSignatures(Set<String> shingles1, Set<String> shingles2) {
        int[][] minHashValues = new int[numHashes][2];
        Random rand = new Random();
        int nextPrime = 429496731;
        Set<Integer> usedHashValues = new HashSet<>();
        for (int i = 0; i < numHashes; i++) {
          int a, b;
          do {
              a = rand.nextInt(nextPrime - 1) + 1;
              b = rand.nextInt(nextPrime);
          } while (usedHashValues.contains(a) || usedHashValues.contains(b));
          
          usedHashValues.add(a);
          usedHashValues.add(b);
            int hashCode1 = Integer.MAX_VALUE;
            int hashCode2 = Integer.MAX_VALUE;

            for (String shingle : shingles1) {
                int shingleHash = shingle.hashCode();
                int hashCode = (a * shingleHash + b) % nextPrime;
                hashCode1 = Math.min(hashCode1, hashCode);
            }

            for (String shingle : shingles2) {
                int shingleHash = shingle.hashCode();
                int hashCode = (a * shingleHash + b) % nextPrime;
                hashCode2 = Math.min(hashCode2, hashCode);
            }

            minHashValues[i][0] = hashCode1;
            minHashValues[i][1] = hashCode2;
        }

        return minHashValues;
    }

    private double calculateEstimatedJaccardSimilarity(int[][] minHashValues) {
        double estJaccardSim = 0.0;
        for (int k = 0; k < numHashes; k++) {
            if (minHashValues[k][0] == minHashValues[k][1]) {
                estJaccardSim += 1.0;
            }
        }
        estJaccardSim = estJaccardSim / numHashes;

        return estJaccardSim;
    }
}
