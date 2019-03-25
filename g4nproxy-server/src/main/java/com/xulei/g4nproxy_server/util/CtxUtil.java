package com.xulei.g4nproxy_server.util;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageDecoder;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageEncoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import static com.xulei.g4nproxy_protocol.protocol.Constants.INITIAL_BYTES_TO_STRIP;
import static com.xulei.g4nproxy_protocol.protocol.Constants.LENGTH_ADJUSTMENT;
import static com.xulei.g4nproxy_protocol.protocol.Constants.LENGTH_FIELD_LENGTH;
import static com.xulei.g4nproxy_protocol.protocol.Constants.LENGTH_FIELD_OFFSET;
import static com.xulei.g4nproxy_protocol.protocol.Constants.MAX_FRAME_LENGTH;

/**
 * @author lei.X
 * @date 2019/3/25 3:12 PM
 */
public class CtxUtil {



    public static void AddProxyMessageHandler(Channel channel){

        channel.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
        channel.pipeline().addLast(new ProxyMessageEncoder());
    }


    public static void removeProxyMessageHandler(Channel channel){
        channel.pipeline().remove(Constants.PROXY_MESSAGE_ENCODE);
        channel.pipeline().remove(Constants.PROXY_MESSAGE_DECODE);
    }
}
