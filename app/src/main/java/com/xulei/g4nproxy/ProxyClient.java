package com.xulei.g4nproxy;

import android.util.Log;

import com.xulei.g4nproxy.handler.AppClientChannelHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author lei.X
 * @date 2019/3/18 9:25 AM
 */
public class ProxyClient {

    private static final String tag = "proxyCient_tag";

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;

    private static final int LENGTH_FIELD_OFFSET = 0;

    private static final int LENGTH_FIELD_LENGTH = 4;

    private static final int INITIAL_BYTES_TO_STRIP = 0;

    private static final int LENGTH_ADJUSTMENT = 0;


    private static long sleepTimeMill = 1000;

    private String serverHost;
    private int serverPort;
    private String clientID;

    //手机的代理客户端
    private Bootstrap appBootstrap;

    private NioEventLoopGroup workerGroup;



    public ProxyClient(String serverHost,int serverPort,String clientId){
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientID = clientId;

    }

    /**
     * 启动内网穿透客户端
     *
     * @param serverHost 公网服务主机
     * @param serverPort 公网服务端口
     * @param clientID   客户端标记，同一个客户端标记在服务器端会映射为同一个端口
     */
    public static ProxyClient start(String serverHost, int serverPort, final String clientID) {
        ProxyClient proxyClient = new ProxyClient(serverHost, serverPort, clientID);
        proxyClient.startInernal();
        return proxyClient;
    }


    private void startInernal(){
        workerGroup = new NioEventLoopGroup();
        appBootstrap = new Bootstrap();
        appBootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new AppClientChannelHandler());
                    }
                });

        connectServer();

    }


    private void connectServer(){
        appBootstrap.connect(serverHost,serverPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()){
                    Log.i(tag,"connect to server successs");
                }else {
                    Log.i(tag,"connnct to sercer failed");
                    //等待后重新尝试连接
                    reconnectWait();
                    connectServer();
                }
            }
        });
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }


    private static void reconnectWait() {
        try {
            if (sleepTimeMill > 60000) {
                sleepTimeMill = 1000;
            }

            synchronized (ProxyClient.class) {
                sleepTimeMill = sleepTimeMill * 2;
                Thread.sleep(sleepTimeMill);
            }
        } catch (InterruptedException e) {
        }
    }

}
