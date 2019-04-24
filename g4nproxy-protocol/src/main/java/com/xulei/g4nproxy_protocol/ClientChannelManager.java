package com.xulei.g4nproxy_protocol;

import com.xulei.g4nproxy_protocol.protocol.Constants;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;

/**
 * 代理客户端与后端真实服务器连接管理
 *
 * @author lei.X
 * @date 2019/3/18 4:06 PM
 */

public class ClientChannelManager {

    private final static String tag = "ClientChannelManager";
    private final AttributeKey<Boolean> USER_CHANNEL_WRITEABLE = AttributeKey.newInstance("user_channel_writeable");

    private final AttributeKey<Boolean> CLIENT_CHANNEL_WRITEABLE = AttributeKey.newInstance("client_channel_writeable");

    private Map<String, Channel> littleProxyServerChannels = new ConcurrentHashMap<>();


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


    /**
     * 以手机生成的userId作为每个代理channel的USER_ID
     *
     * @param littleProxyServerChannel
     * @param userId
     */
    public void setRealServerChannelUserId(Channel littleProxyServerChannel, String userId) {
        littleProxyServerChannel.attr(Constants.USER_ID).set(userId);
    }

    public String getRealServerChannelUserId(Channel realServerChannel) {
        return realServerChannel.attr(Constants.USER_ID).get();
    }

    public Channel getRealServerChannel(String userId) {
        return littleProxyServerChannels.get(userId);
    }

    public void addLittleProxyServerChannel(String userId, Channel realServerChannel) {
        littleProxyServerChannels.put(userId, realServerChannel);
    }

    public Channel removeRealServerChannel(String userId) {
        return littleProxyServerChannels.remove(userId);
    }

    public boolean isRealServerReadable(Channel realServerChannel) {
        return realServerChannel.attr(CLIENT_CHANNEL_WRITEABLE).get() && realServerChannel.attr(USER_CHANNEL_WRITEABLE).get();
    }

    public void clearRealServerChannels() {
        ALOG.w(Constants.tag, "channel closed, clear real server channels");

        Iterator<Map.Entry<String, Channel>> ite = littleProxyServerChannels.entrySet().iterator();
        while (ite.hasNext()) {
            Channel realServerChannel = ite.next().getValue();
            if (realServerChannel.isActive()) {
                realServerChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }

        littleProxyServerChannels.clear();
    }

}
