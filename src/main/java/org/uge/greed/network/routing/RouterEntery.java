package org.uge.greed.network.routing;

public class RouterEntery {
    String appId ;
    Boolean isParent ;
    Boolean isClient ;
    String nextAppId ;

    public RouterEntery(String appId, Boolean isParent, Boolean isClient, String nextAppId){
        this.appId = appId ;
        this.isParent = isParent ;
        this.isClient = isClient ;
        this.nextAppId = nextAppId ;
    }

    @Override
    public String toString() {
        int colWidth = 15; // Set the column width to 15 characters
        String paddedAppId = String.format("%-" + colWidth + "s", appId);
        String paddedIsParent = String.format("%-" + colWidth + "s", isParent.toString());
        String paddedIsClient = String.format("%-" + colWidth + "s", isClient.toString());
        String paddedNextAppId = String.format("%-" + colWidth + "s", nextAppId);
        return String.format("| %s | %s | %s | %s |", paddedAppId, paddedIsParent, paddedIsClient, paddedNextAppId);
    }
}
