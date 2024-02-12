package org.uge.greed.demo;

import org.uge.greed.greed.UGEGreed;

class UGEGreedDemo5 {
    public static void main(String[] args) throws Exception {
        UGEGreed client5 = new UGEGreed("127.0.0.1", 4213,"127.0.0.1", 4444);

        client5.launch();
    }
}
