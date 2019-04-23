package com.xulei.g4nproxy_client;

import com.xulei.g4nproxy_protocol.protocol.Constants;


/**
 * @author lei.X
 * @date 2019/3/18 10:43 AM
 */

class G4NproxyClientApplication {

    /**
     * 暂时设置成一个固定值，后期移植到手机上可以自动生成
     */
    private static final String clientID  = "testClientKey";

    public static void main(String[] args) throws InterruptedException {
        ProxyClient.start(Constants.g4nproxyServerHost,Constants.g4nproxyServerPort,clientID);

    }


}
