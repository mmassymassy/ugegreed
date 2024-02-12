package org.uge.greed;

import org.uge.greed.greed.UGEGreed;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        if (args.length == 2) {
            try {
                String address = args[0];
                int port = Integer.parseInt(args[1]);
                if (!Helpers.validateAddress(address)) {
                    System.out.println("SVP entrer une address valid");
                    return;
                }
                if (!Helpers.validatePortFormat(port)) {
                    System.out.println("SVP verfier que le port est dans l'interval 0 a 65536 !!!");
                    return;
                }

                if (!Helpers.validatePortForListen(port)) {
                    System.out.println("Le port " + port + " est occuper, SVP choisir un qutre port");
                    return;
                }

                UGEGreed root = new UGEGreed(address, port);

                root.launch();

            } catch (NumberFormatException ex) {
                System.out.println("SVP verifier que votre port est un entier");
                return;
            } catch (IOException e) {
                System.out.println("Le serveur ne peux pas etre creer");
            }
        }

        if (args.length == 4) {
            try {
                String address = args[0];
                int port = Integer.parseInt(args[1]);

                String parentAddress = args[2];
                int parentPort = Integer.parseInt(args[3]);

                if (!Helpers.validateAddress(address) || !Helpers.validateAddress(parentAddress)) {
                    System.out.println("SVP entrer une address valid");
                    return;
                }
                if (!Helpers.validatePortFormat(port) || !Helpers.validatePortFormat(parentPort)) {
                    System.out.println("SVP verifier que le port est dans l'interval 0 a 65536");
                    return;
                }

                if (!Helpers.validatePortForListen(port)) {
                    System.out.println("Le port " + port + " est occuper, SVP choisir un qutre port");
                    return;
                }

                UGEGreed root = new UGEGreed(address, port, parentAddress, parentPort);

                root.launch();

            } catch (NumberFormatException ex) {
                System.out.println("SVP verifier que votre port est un entier");
            } catch (IOException e) {
                System.out.println("Le serveur ne peux pas etre creer");
            }
        }

        System.out.println("Ce Programme naicessite 2 ou 4 arguments");

    }
}