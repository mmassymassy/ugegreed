package org.uge.greed.demo;

import org.uge.greed.greed.UGEGreed;
//START http://www-igm.univ-mlv.fr/~carayol/Factorize.jar fr.uge.factors.Factorizer 1 100 Factorize.txt
//START http://www-igm.univ-mlv.fr/~carayol/Collatz.jar fr.uge.collatz.Collatz 1 200 Collatz.txt
//START https://www-igm.univ-mlv.fr/~carayol/SlowChecker.jar fr.uge.slow.SlowChecker 1 200 SlowChecker.txt
public class UGEGreedDemo1 {
    public static void main(String[] args) throws Exception {
        UGEGreed root = new UGEGreed("127.0.0.1", 4567);
        root.launch();
    }
}
//START http://www-igm.univ-mlv.fr/~carayol/Factorize.jar fr.uge.factors.Factorizer 1 100 Factorize.txt