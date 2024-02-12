package org.uge.greed.network.routing;

import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messages.UGEGreedBaseMessage;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.util.HashMap;

public class UGEGreedRouterService {
    private HashMap<String, RouterEntery> routingTable ;

    private UGEGreed mainService ;
    @Override
    public String toString(){
        return routingTable.toString();
    }

    public UGEGreedRouterService(UGEGreed mainService){
        this.mainService = mainService ;
        this.routingTable = new HashMap<>();
    }

    private void printRoutingTable(){
        System.out.println("---- ROUTING TABLE ----");
        int colWidth = 15; // Set the column width to 15 characters
        String paddedAppId = String.format("%-" + colWidth + "s", "AppId");
        String paddedIsParent = String.format("%-" + colWidth + "s", "isParent");
        String paddedIsClient = String.format("%-" + colWidth + "s", "isClient");
        String paddedNextAppId = String.format("%-" + colWidth + "s", "nextAppId");
        System.out.println(String.format("| %s | %s | %s | %s |", paddedAppId, paddedIsParent, paddedIsClient, paddedNextAppId));

        for(String key : routingTable.keySet()){
            System.out.println(routingTable.get(key));
        }
        System.out.println("---- ROUTING TABLE ----");
    }
    public void addRoute(String appId, Boolean isParent, Boolean isClient, String nextAppId){
        System.out.println("-- ROUTER TABLE ADD --");
        RouterEntery routerEntery = new RouterEntery(appId, isParent, isClient, nextAppId);
        routingTable.put(appId, routerEntery);
        System.out.println("info: ajouter un entrer dans la table de routage pour le noeud avec le ID " + appId);
        printRoutingTable();
        System.out.println("-- ROUTER TABLE ADD DONE --");

    }

    public void removeRoute(String appId){
        System.out.println("info: Supression de l'entrée u routage pour le noeud avec le ID " + appId);
        routingTable.remove(appId);
        printRoutingTable();
    }

    public void updateRoute(String appId, Boolean isParent, Boolean isClient, String nextAppId){
        RouterEntery routerEntery = new RouterEntery(appId, isParent, isClient, nextAppId);
        routingTable.replace(appId, routerEntery);
        printRoutingTable();
    }

    public RouterEntery getRoute(String appId){
        return routingTable.get(appId);
    }

    public Boolean routeExists(String appId){
        return routingTable.containsKey(appId);
    }

    public void routeMessage(String appId, UGEGreedBaseMessage message){
        RouterEntery routerEntery = routingTable.get(appId);
        if(routerEntery == null){
            System.out.println("Err: aucune entrée dans la table de routage pour le noeud avec le ID " + appId);
            return;
        }
        if(routerEntery.isParent){
            mainService.sendMessageToParent(message);
        }else{
            mainService.sendMessageToClient(routerEntery.nextAppId, message);
        }
    }

    public void updateAllForAppId(String nextop, String newNextHop){
        System.out.println("info: mise a jour de la table de routage pour les ligne qui on la NextHop =" + nextop + " avec la nouvelle NextHop = " + newNextHop);
        // loop thought the router enteries
        for (String key : routingTable.keySet()) {
            RouterEntery routerEntery = routingTable.get(key);
            if(routerEntery.nextAppId.equals(nextop)){
                if(newNextHop.equals(this.mainService.getServerConfig().getAppId())){
                    routerEntery.nextAppId = routerEntery.appId;
                }else{
                    routerEntery.nextAppId = newNextHop;
                }
            }
        }
        printRoutingTable();
    }


    public void broadcastMessageToAllBut(String eliminated , UGEGreedBaseMessage message){
        ServerConfig parent = mainService.getParent();

        if(parent == null) {
            mainService.sendToAllClientsBut(eliminated, message);
            return ;
        }

        String parentId = parent.ipAddress + ":" + parent.port;


        if(parentId.equals(eliminated)) {
            mainService.sendToAllClients(message);
        }else{
            mainService.sendMessageToParent(message);
            mainService.sendToAllClientsBut(eliminated, message);
        }
    }

}
