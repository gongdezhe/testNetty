package com.tulun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.util.Arrays;

/**
 * Created by gongdezhe on 2018/6/27.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        ctx.writeAndFlush(Unpooled.copiedBuffer("",CharsetUtil.UTF_8));
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        if (!"".equals(s)) {
            String[] split = s.trim().split("%%d");
            if ("".equals(split[0])) {
                if (split[1].trim().equals("me")) {
                    System.out.println("[我] "+ split[2]);
                } else if (split[1].trim().equals("ip")){
                    System.out.println("["+ split[2].replace("/", "")+" ]" + split[3]);
                } else {
                    System.out.println(split[1]);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
