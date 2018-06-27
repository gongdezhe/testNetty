package com.tulun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by gongdezhe on 2018/6/27.
 */
public class Server {
    public void bind(int port) throws Exception {
        /**
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty 提供了许多不同的 EventLoopGroup 的实现用来处理不同的传输。
         * 在这个例子中我们实现了一个服务端的应用，因此会有2个 NioEventLoopGroup 会被使用。
         * 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
         * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的 Channel上都需要依赖于 EventLoopGroup 的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            /*
            由于我们使用在 NIO 传输
            指定 NioEventLoopGroup接受和处理新连接，
            指定 NioServerSocketChannel为信道类型
            我们设置本地地址是 InetSocketAddress 与所选择的端口
            服务器将绑定到此地址来监听新的连接请求。
             */
            /**
             * ServerBootstrap 是一个启动 NIO 服务的辅助启动类。
             * 你可以在这个服务中直接使用 Channel，但是这会是一个复杂的处理过程，在很多情况下你并不需要这样做。
             */
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    /**
                     * 这里我们指定使用 NioServerSocketChannel 类来举例说明一个新的 Channel 如何接收进来的连接。
                     */
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    /**
                     * 这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel。
                     * 匿名内部类 继承自ChannelInitializer 是一个特殊的处理类，他的目的是帮助使用者配置一个新的 Channel。
                     * 也许你想通过增加一些处理类来配置一个新的 Channel 或者其对应的ChannelPipeline 来实现你的网络程序。
                     * 当你的程序变的复杂时，可能你会增加更多的处理类到 pipline 上，然后提取这些匿名类到最顶层的类上。
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //添加ServerHandler到Channel的ChannelPipeline
                        //通过ServerHandler给每一个新来的Channel初始化
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();

                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("handler", new ServerHandler());
                        }
                    })
                    /**
                     * 你可以设置这里指定的 Channel 实现的配置参数。
                     * 我们正在写一个TCP/IP 的服务端，因此我们被允许设置 socket 的参数选项比如tcpNoDelay 和 keepAlive。
                     * 请参考 ChannelOption 和详细的 ChannelConfig 实现的接口文档以此可以对ChannelOption 的有一个大概的认识。
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)
                    /**
                     * option() 是提供给NioServerSocketChannel 用来接收进来的连接。
                     * childOption() 是提供给由父管道 ServerChannel 接收到的连接，在这个例子中也是 NioServerSocketChannel。
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("Server 启动了");

            //绑定监听端口，调用sync同步阻塞方法等待绑定操作完成，完成后返回ChannelFuture类似于JDK中Future
            /**
             * 剩下的就是绑定端口然后启动服务。
             * 这里我们在机器上绑定了机器所有网卡上的 8080 端口。当然现在你可以多次调用 bind() 方法(基于不同绑定地址)。
             */
            ChannelFuture sync = bootstrap.bind(port).sync();
            //使用sync方法进行阻塞，等待服务端链路关闭之后Main函数才退出
            sync.channel().closeFuture().sync();

        } finally {
            //释放线程池资源
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            System.out.println("Server 关闭了");
        }


    }
}
