package com.xulei.g4nproxy_client.kidHttpProxy.util;

/**
 * @author lei.X
 * @date 2019/4/23 4:20 PM
 */

import java.net.InetSocketAddress;

import io.netty.channel.ChannelFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用于存储每个通道各自信息的缓存类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChannelCache {
    //目标服务器的地址
    private InetSocketAddress address;
    //当前请求与目标主机建立的连接通道
    private ChannelFuture channelFuture;
}
