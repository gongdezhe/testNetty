package com.tulun;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by gongdezhe on 2018/6/27.
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {
    /**
     *ServerHandler 继承自 SimpleChannelInboundHandler，
     * 这个类实现了 ChannelInboundHandler 接口，
     * ChannelInboundHandler 提供了许多事件处理的接口方法，然后你可以覆盖这些方法。
     * 现在仅仅只需要继承 SimpleChannelInboundHandler 类而不是你自己去实现接口方法。
     */
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    /**
     * 覆盖了 handlerAdded() 事件处理方法。
     * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n");
        }
        channels.add(ctx.channel());
    }

    /**
     * 覆盖了 handlerRemoved() 事件处理方法。
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("[ServerHandler] - " + incoming.remoteAddress() + " 离开\n");
        }
        channels.remove(ctx.channel());
    }

    /**
     * 覆盖了 channelRead0() 事件处理方法。每当从服务端读到客户端写入信息时，
     * 将信息转发给其他客户端的 Channel。其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
     * @param ctx
     * @param s
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception { // (4)
        Channel incoming = ctx.channel();
        String key = "%%d";
        for (Channel channel : channels) {
            if (channel != incoming){
                channel.writeAndFlush(key + "ip" + key + incoming.remoteAddress() + key + s + "\n");
            } else {
                channel.writeAndFlush(key + "me" + key + s + "\n");
            }
        }
    }

    /**
     * 覆盖了 channelActive() 事件处理方法。服务端监听到客户端活动
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
        System.out.println(incoming.remoteAddress().toString().replace("/", "")+"上线了");
    }

    /**
     * 覆盖了 channelInactive() 事件处理方法。服务端监听到客户端不活动
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println(incoming.remoteAddress().toString().replace("/", "")+"掉线了");
    }


    /**
     * exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，
     * 即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
     * 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。
     * 然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (7)
        Channel incoming = ctx.channel();
        System.out.println(incoming.remoteAddress().toString().replace("/", "")+"异常了");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
