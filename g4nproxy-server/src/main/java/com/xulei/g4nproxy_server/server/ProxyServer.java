package com.xulei.g4nproxy_server.server;

import com.google.common.collect.Maps;
import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageDecoder;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageEncoder;
import com.xulei.g4nproxy_server.handler.NatServerChannelHandler;
import com.xulei.g4nproxy_server.handler.UserMappingChannelHandler;
import com.xulei.g4nproxy_server.util.LogUtil;

import java.util.Collection;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;

import static com.xulei.g4nproxy_protocol.protocol.Constants.*;
/**
 * @author lei.X
 * @date 2019/3/18 10:43 AM
 */
@Slf4j
public class ProxyServer {


    private static final String tag = "proxyServer";

    /**
     * max packet is 2M.
     */


    private ServerBootstrap natServerBootStrap;
    private ServerBootstrap userMapServerBootStrap;
    private Map<Integer, ChannelFuture> portMappingChannelMap = Maps.newConcurrentMap();

    private static ProxyServer instance = new ProxyServer();


    private ProxyServer() {
    }

    public static ProxyServer getInstance() {
        return instance;
    }

    public Collection<Integer> mappingPort(){
        return portMappingChannelMap.keySet();
    }



    /**
     * 根据port，建立 userMapServerBootStrap
     * @param port
     *
     *
     * @return
     */
    public boolean openMappingPort(final int port,Channel channel){

        //将系统端口与channel进行连接
        LogUtil.i(tag,"将4g代理服务器对应的channel添加进cmdChannels");
        ProxyChannelManager.setCmdChannels(port,channel);

        ChannelFuture channelFuture = userMapServerBootStrap.bind(port);

        //根据端口来获取到natServerChannel
        Channel natDataChannel = ProxyChannelManager.getCmdChannel(Constants.g4nproxyServerPort);

        Channel userMappingChannel = channelFuture.channel();

        try {
            channelFuture.get();
            LogUtil.i(tag,"开启服务器的数据端口: "+String.valueOf(port));


        } catch (Exception e) {
            log.error("wait for port binding error", e);
            return false;
        }
        if (!channelFuture.isSuccess()) {
            log.warn("bind mapping port:{} failed", port);
            return false;
        }
        portMappingChannelMap.put(port, channelFuture);
        return true;

    }

    public void closeMappingPort(int port) {
        ChannelFuture channelFuture = portMappingChannelMap.get(port);
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        portMappingChannelMap.remove(port);
    }

    public void start(){
        natServerBootStrap = new ServerBootstrap();
        NioEventLoopGroup serverBossGroup = new NioEventLoopGroup();
        NioEventLoopGroup serverWorkerGroup = new NioEventLoopGroup();
        natServerBootStrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(Constants.PROXY_MESSAGE_DECODE,new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
                ch.pipeline().addLast(Constants.PROXY_MESSAGE_ENCODE,new ProxyMessageEncoder());
//                ch.pipeline().addLast(new ServerIdleCheckHandler());
                ch.pipeline().addLast(new NatServerChannelHandler());
            }
        });

        log.info("start netty proxy server, port"+ Constants.g4nproxyServerPort);

        //开启server端的服务
        natServerBootStrap.bind(Constants.g4nproxyServerPort);

        userMapServerBootStrap = new ServerBootstrap();
        serverBossGroup = new NioEventLoopGroup();
        serverWorkerGroup = new NioEventLoopGroup();
        userMapServerBootStrap.group(serverBossGroup,serverWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
//                        ch.pipeline().addLast(new ProxyMessageEncoder());
//                        ch.pipeline().addLast(NAME_HTTPSERVER_CODEC,new HttpServerCodec());
//                                /**
//                                 * /**usually we receive http message infragment,if we want full http message,
//                                 * we should bundle HttpObjectAggregator and we can get FullHttpRequest。
//                                 * 我们通常接收到的是一个http片段，如果要想完整接受一次请求的所有数据，我们需要绑定HttpObjectAggregator，然后我们
//                                 * 就可以收到一个FullHttpRequest-是一个完整的请求信息。
//                                 **/
//                        ch.pipeline().addLast(NAME_HTTP_AGGREGATOR_HANDLER,new HttpObjectAggregator(1024*1024)); //定义缓冲区数据量大小
                        ch.pipeline().addLast(new UserMappingChannelHandler());
                    }
                });
    }

}
