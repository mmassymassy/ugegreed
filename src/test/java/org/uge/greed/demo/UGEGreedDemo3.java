package org.uge.greed.demo;

import org.uge.greed.greed.UGEGreed;

public class UGEGreedDemo3 {
    public static void main(String[] args) throws Exception {
        UGEGreed client2 = new UGEGreed("127.0.0.1", 5555,"127.0.0.1", 4444);

        client2.launch();
    }
}
