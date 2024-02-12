package org.uge.greed.network.tcp.server;

import org.uge.greed.Helpers;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class ServerConfig implements Serializable {
    public String ipAddress = null;
    public int port = 0 ;
    public int ipAddressInt ;

    public String getAppId(){
        return ipAddress + ":" + port;
    }

    public ServerConfig(String appId){
        String[] args = appId.split(":");
        if(args.length == 2){
            this.ipAddress = args[0];
            this.port = Integer.parseInt(args[1]) ;
            this.ipAddressInt = Helpers.stringToIntAddress(ipAddress);
        }
    }
    public ServerConfig(String ipAddress, int port){
        this.ipAddress = ipAddress;
        this.port = port ;
        this.ipAddressInt = Helpers.stringToIntAddress(ipAddress);
    }

    public ServerConfig(int ipAddress, int port){
        this.ipAddressInt = ipAddress;
        this.port = port;

        this.ipAddress = Helpers.intToStringAddress(ipAddress);
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfig that = (ServerConfig) o;

        if (port != that.port) return false;
        return Objects.equals(ipAddress, that.ipAddress);
    }

    @Override
    public int hashCode() {
        int result = ipAddress != null ? ipAddress.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + ipAddressInt;
        return result;
    }
}
