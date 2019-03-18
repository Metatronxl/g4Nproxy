package com.xulei.g4nproxy_protocol;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * 代理客户端与后端真实服务器连接管理
 * @author lei.X
 * @date 2019/3/18 4:06 PM
 */
public class ClientChannelManager {
    private final AttributeKey<Boolean> USER_CHANNEL_WRITEABLE = AttributeKey.newInstance("user_channel_writeable");

    private final AttributeKey<Boolean> CLIENT_CHANNEL_WRITEABLE = AttributeKey.newInstance("client_channel_writeable");

    private final int MAX_POOL_SIZE = 100;

    private volatile Channel cmdChannel;

    public void setCmdChannel(Channel cmdChannel) {
        this.cmdChannel = cmdChannel;
    }

    public Channel getCmdChannel() {
        return cmdChannel;
    }


    private String serverHost;

    public ClientChannelManager(String serverHost) {
        this.serverHost = serverHost;
    }

}
