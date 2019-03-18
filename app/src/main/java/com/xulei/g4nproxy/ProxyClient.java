package com.xulei.g4nproxy;

/**
 * @author lei.X
 * @date 2019/3/18 9:25 AM
 */
public class ProxyClient {

    private static long sleepTimeMill = 1000;

    private String serverHost;
    private int serverPort;
    private String clientID;

    BootS


    public ProxyClient(String serverHost,int serverPort,String clientId){
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientID = clientId;

    }
}
