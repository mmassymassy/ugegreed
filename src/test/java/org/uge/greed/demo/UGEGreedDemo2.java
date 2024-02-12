package org.uge.greed.demo;

import org.uge.greed.greed.UGEGreed;

public class UGEGreedDemo2 {
    public static void main(String[] args) throws Exception {
        UGEGreed client1 = new UGEGreed("127.0.0.1", 4444,"127.0.0.1", 4567);

        client1.launch();
    }
}
