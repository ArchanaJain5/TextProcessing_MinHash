package project7;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Minhash {
    private static final int numHashes = 52;

    public double jaccard(String fileA, String fileB) {
        try {
            // Convert documents to sets of shingles
            Set<String> file1_word = getWordsFile(fileA);
            Set<String> file2_word = getWordsFile(fileB);

            // Generate MinHash signatures
            int[][] minHashValues = generateMinHashSignatures(file1_word,file2_word);

            // Compare MinHash signatures
            double estJaccardSim = calculateEstimatedJaccardSimilarity(minHashValues);

            // Return the estimated Jaccard similarity
            return estJaccardSim;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Set<String> getWordsFile(String file) throws IOException {
        Set<String> words = new HashSet<>();

        BufferedReader dataReader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = dataReader.readLine()) != null) {
            words.add(line);
        }
        dataReader.close();

        return words;
    }

    private int[][] generateMinHashSignatures(Set<String> file1_word, Set<String> file2_word) {
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

            for (String word : file1_word) {
                int wordHash = word.hashCode();
                int hashCode = (a * wordHash + b) % nextPrime;
                hashCode1 = Math.min(hashCode1, hashCode);
            }

            for (String word : file2_word) {
                int wordHash = word.hashCode();
                int hashCode = (a * wordHash + b) % nextPrime;
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
