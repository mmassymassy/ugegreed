package org.uge.greed.demo;

import org.uge.greed.greed.UGEGreed;

class UGEGreedDemo4 {
    public static void main(String[] args) throws Exception {
        UGEGreed client4 = new UGEGreed("127.0.0.1", 6666,"127.0.0.1", 4444);

        client4.launch();
    }
}
