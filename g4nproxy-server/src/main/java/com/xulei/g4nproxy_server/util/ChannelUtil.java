package com.xulei.g4nproxy_server.util;

/**
 * @author lei.X
 * @date 2019/4/23 6:43 PM
 */

import com.xulei.g4nproxy_protocol.protocol.Constants;

import io.netty.channel.Channel;

/**
 * channel 相关的工具类
 */
public class ChannelUtil {

    /**
     * 赋给channel一个序列号
     * @param userMappingChannel
     * @return
     */
    public static Long onNewConnection(Channel userMappingChannel) {
        long seq = IdGenerator.getUniqueId();
        userMappingChannel.attr(Constants.SERIAL_NUM).set(seq);

        return seq;
    }
}
