package org.uge.greed;

import org.uge.greed.messaging.messages.UGEGreedBaseMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Helpers {
    public static int stringToIntAddress(String address) {
        String[] octets = address.split("\\.");
        return (Integer.parseInt(octets[0]) << 24) | (Integer.parseInt(octets[1]) << 16) | (Integer.parseInt(octets[2]) << 8) | Integer.parseInt(octets[3]);
    }

    public static String intToStringAddress(int ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByAddress(
                    new byte[]{
                            (byte) ((ipAddress >> 24) & 0xff),
                            (byte) ((ipAddress >> 16) & 0xff),
                            (byte) ((ipAddress >> 8) & 0xff),
                            (byte) (ipAddress & 0xff)
                    });

            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static boolean validateAddress(String address) {

        String[] parts = address.split("\\.");

        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
                if (num == 0 && part.charAt(0) != '0') {
                    return false;
                }
                if (num != 0 && part.charAt(0) == '0') {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean validatePortFormat(int port){
        return (port > 0 && port < 65536);
    }

    public static boolean validatePortForListen(int port){
        if(!validatePortFormat(port)){
            return false;
        }

        return !isPortInUse(port);
    }

    private static boolean isPortInUse(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
