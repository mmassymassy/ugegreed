package org.uge.greed.commands;

import javax.lang.model.SourceVersion;
import java.net.MalformedURLException;
import java.net.URL;

public class CommandValidator {
    public static StartCommand validateStartCommand(String input){
        String[] args = input.split(" ");

        if(args.length != 6){
            System.out.println("Invalid number of arguments");
            return null;
        }

        URL urlJar = null;
        try{
            urlJar = new URL(args[1]);
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL");
            return null ;
        }
        String fullyQualifiedName = args[2];

        if(!SourceVersion.isName(fullyQualifiedName)){
            System.out.println("Invalid fully qualified name");
            return null;
        }

        long startRange = 0;
        long endRange  = 0;

        try{
            startRange = Long.parseLong(args[3]);
            endRange = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid range");
            return null;
        }

        String filename = args[5];

        return new StartCommand(urlJar, fullyQualifiedName, startRange, endRange, filename);
    }
}
