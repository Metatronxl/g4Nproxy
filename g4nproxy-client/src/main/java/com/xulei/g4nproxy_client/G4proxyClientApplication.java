package com.xulei.g4nproxy_client;

import com.xulei.g4nproxy_protocol.protocol.Constants;


/**
 * @author lei.X
 * @date 2019/3/18 10:43 AM
 */

class G4proxyClientApplication {

    public static void main(String[] args) {
        ProxyClient.start(Constants.g4nproxyServerHost,Constants.g4nproxyServerPort,"testClientId");

    }


}
