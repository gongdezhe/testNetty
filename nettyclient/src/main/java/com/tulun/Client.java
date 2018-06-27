package com.tulun;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.codec.Delimiters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * Created by gongdezhe on 2018/6/27.
 */
public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();

                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast(new ClientHandler());
                            System.out.println("客户端启动了");
                        }
                    });
            Channel channel = bootstrap.connect().sync().channel();
            System.out.println("连接上服务端了。。。");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                String readLine = in.readLine();
                if ("exit".equals(readLine)) {
                    System.out.println("我下线了");
                    return;
                }
                channel.writeAndFlush(readLine + "\r\n");
            }


        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
