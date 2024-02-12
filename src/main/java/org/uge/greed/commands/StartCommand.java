package org.uge.greed.commands;

import java.net.URL;

public class StartCommand {
    private URL urlJar ;
    private String fullyQualifiedName ;
    private long startRange , endRange ;
    private String filename = null;


    public StartCommand(URL urlJar, String fullyQualifiedName, long startRange, long endRange, String filename) {
        this.urlJar = urlJar;
        this.fullyQualifiedName = fullyQualifiedName;
        this.startRange = startRange;
        this.endRange = endRange;
        this.filename = filename;
    }

    public URL getUrlJar() {
        return urlJar;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public long getStartRange() {
        return startRange;
    }

    public long getEndRange() {
        return endRange;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "StartCommand{" +
                "urlJar=" + urlJar +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", startRange=" + startRange +
                ", endRange=" + endRange +
                ", filename='" + filename + '\'' +
                '}';
    }
}
