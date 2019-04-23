package com.xulei.g4nproxy_client;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import com.xulei.g4nproxy_protocol.protocol.Constants;

/**
 * @author lei.X
 * @date 2019/4/23 7:09 PM
 */
@Slf4j
public class HttpProxyConnectionManager {
    private Map<Long, Channel> littelProxyChannelMap = Maps.newConcurrentMap();

    public void register(Long seq, Channel littelProxyChannel) {
        littelProxyChannel.attr(Constants.SERIAL_NUM).set(seq);
        littelProxyChannelMap.put(seq, littelProxyChannel);
    }

    public Channel query(Long seq) {
        return littelProxyChannelMap.get(seq);
    }


    public void releaseConnection(Long seq) {
        Channel channel = littelProxyChannelMap.remove(seq);
        if (channel == null) {
            log.warn("no LITTEL proxy channel bound for request:{}", seq);
            return;
        }
        if (channel.isActive()) {
            channel.close();
        }
    }


    /**
     * 关闭所有到代理服务器的链接，当手机和server的链接断开的时候，需要同步关闭所有upstream
     */
    public void closeAllProxyConnection() {
        Map<Long, Channel> snapshotMap = new HashMap<>(littelProxyChannelMap);
        for (Long seq : snapshotMap.keySet()) {
            Channel channel = littelProxyChannelMap.remove(seq);
            if (channel == null) {
                continue;
            }
            if (channel.isActive()) {
                channel.close();
            }
        }
    }
}

