package com.xulei.g4nproxy_server.server;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.util.LogUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 代理服务连接管理（代理客户端连接+用户请求连接）
 * @author lei.X
 * @date 2019/3/18 4:44 PM
 */

@Slf4j
public class ProxyChannelManager {


    private static final String tag = "ProxyChannelManager";

    // clientId 和 channel 的绑定
    private static Map<String, Channel> cmdChannels = new ConcurrentHashMap<String, Channel>();
    // channel 和 port 的绑定
    private static Map<Integer, Channel> portCmdChannelMapping = new ConcurrentHashMap<Integer, Channel>();
    // channel 对应的port
    private static final AttributeKey<Integer> CHANNEL_PORT = AttributeKey.newInstance("channel_port");
    // channel 中连接的clientId
    public static final AttributeKey<String> CHANNEL_CLIENT_KEY = AttributeKey.newInstance("channel_client_key");

    //
    private static final AttributeKey<Map<String, Channel>> USER_CHANNELS = AttributeKey.newInstance("user_channels");


    // userMapping channel
    private static final Map<Long,Channel> userMappingChannelMap = new ConcurrentHashMap<>();

    /**
     * 根据serialNumber获取usermappingChannel
     * @param serialNumber
     * @param channel
     */
    public static void setUserMappingChannelMap(long serialNumber,Channel channel){
        userMappingChannelMap.put(serialNumber,channel);
    }
    public static Channel getUsermappingChannel(long serialNumber){
        return userMappingChannelMap.get(serialNumber);
    }





    public static void addCmdChannel(String clientKey, Channel channel){
        Channel oldChannel = ProxyChannelManager.getCmdChannel(clientKey);
        Integer nextPort;

        if (oldChannel !=null){
            //新的channel注册，老的被替换掉
            log.warn("exist channel for key {}, {}", clientKey, channel);
            nextPort = oldChannel.attr(CHANNEL_PORT).get();
            //TODO 需要优化userChannels
//            Map<String, Channel> userChannels = oldChannel.attr(USER_CHANNELS).get();
//            for (String s : userChannels.keySet()) {
//                Channel userChannel = userChannels.get(s);
//                if (userChannel.isActive()) {
//                    userChannel.close();
//                    log.info("disconnect user channel {}", userChannel);
//                }
//            }
            oldChannel.close();
            log.info("close old channel,the channel replaced by new channel");
        }else {
            // 获取一个新的可用端口号
            LogUtil.i(tag,"服务器生成一个新的userMapping端口");
            nextPort = AvailablePortManager.getInstance().poll(clientKey);

        }

            //开始绑定端口
            boolean bindSuccess = false;

            //userMapServerBootStrap 绑定端口并开启
            for (int i=0;i<10;i++){
                if (!ProxyServer.getInstance().openMappingPort(nextPort,channel)) {
                    nextPort = AvailablePortManager.getInstance().poll(clientKey);
                    continue;
                }
                bindSuccess = true;
                break;
            }

            if (!bindSuccess){
                throw new IllegalStateException("no availeable port resource !!!");
            }


        // 客户端（proxy-client）相对较少，这里同步的比较重
        // 保证服务器对外端口与客户端到服务器的连接关系在临界情况时调用removeChannel(Channel channel)时不出问题
        synchronized (portCmdChannelMapping) {
            portCmdChannelMapping.put(nextPort, channel);

        }
        channel.attr(CHANNEL_PORT).set(nextPort);
        channel.attr(CHANNEL_CLIENT_KEY).set(clientKey);
        channel.attr(USER_CHANNELS).set(new ConcurrentHashMap<String, Channel>());
        cmdChannels.put(clientKey, channel);
    }

    public static Channel getCmdChannel(Integer port) {
        return portCmdChannelMapping.get(port);
    }

    //添加port与channel之间的关系
    public static void setCmdChannels(Integer port,Channel channel){
        portCmdChannelMapping.put(port,channel);
    }

    public static Channel getCmdChannel(String clientKey) {
        return cmdChannels.get(clientKey);
    }

    /**
     * 增加用户连接与代理客户端连接关系
     *
     * @param cmdChannel
     * @param userId
     * @param userChannel
     */
    public static void addUserChannelToCmdChannel(Channel cmdChannel, String userId, Channel userChannel) {
        userChannel.attr(Constants.USER_ID).set(userId);
        cmdChannel.attr(USER_CHANNELS).get().put(userId, userChannel);
    }

    /**
     * 删除用户连接与代理客户端连接关系
     *
     * @param cmdChannel
     * @param userId
     * @return
     */
    public static Channel removeUserChannelFromCmdChannel(Channel cmdChannel, String userId) {
        if (cmdChannel.attr(USER_CHANNELS).get() == null) {
            return null;
        }

        synchronized (cmdChannel) {
            return cmdChannel.attr(USER_CHANNELS).get().remove(userId);
        }
    }


    /**
     * 代理客户端连接断开后清除关系
     *
     * @param channel
     */
    public static void removeCmdChannel(Channel channel) {
        log.warn("channel closed, clear user channels, {}", channel);
        if (channel.attr(CHANNEL_PORT).get() == null) {
            // data channel ,just close
            channel.close();
            return;
        }

        String clientKey = channel.attr(CHANNEL_CLIENT_KEY).get();
        Channel channel0 = cmdChannels.remove(clientKey);
        if (channel != channel0) {
            cmdChannels.put(clientKey, channel);
        }

        Integer port = channel.attr(CHANNEL_PORT).get();
        Channel proxyChannel = portCmdChannelMapping.remove(port);
        if (proxyChannel != null && proxyChannel != channel) {
            // 在执行断连之前新的连接已经连上来了
            portCmdChannelMapping.put(port, proxyChannel);
        }

        if (channel.isActive()) {
            log.info("disconnect proxy channel {}", channel);
            channel.close();
        }

        ProxyServer.getInstance().closeMappingPort(port);


        Map<String, Channel> userChannels = getUserChannels(channel);
        Iterator<String> ite = userChannels.keySet().iterator();
        while (ite.hasNext()) {
            Channel userChannel = userChannels.get(ite.next());
            if (userChannel.isActive()) {
                userChannel.close();
                log.info("disconnect user channel {}", userChannel);
            }
        }
    }


    /**
     * 获取代理控制客户端连接绑定的所有用户连接
     *
     * @param cmdChannel
     * @return
     */
    public static Map<String, Channel> getUserChannels(Channel cmdChannel) {
        return cmdChannel.attr(USER_CHANNELS).get();
    }

    /**
     * 获取用户编号
     *
     * @param userChannel
     * @return
     */
    public static String getUserChannelUserId(Channel userChannel) {
        return userChannel.attr(Constants.USER_ID).get();
    }
}
