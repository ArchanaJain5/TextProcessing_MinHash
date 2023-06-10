package project7;

import java.util.ArrayList;

public class Main {
  public static void main(String[] args) {
    Minhash mh = new Minhash();
    double res = mh.jaccard(args[0], args[1]);

    System.out.println(res);
    
  }
}

