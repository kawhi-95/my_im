package org.jk;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class IMserver {
    public static void start() throws InterruptedException {
        // 相当于两个线程池
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        // 使用bootstrap来监听端口
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 放入两个线程池
        bootstrap.group(boss, worker)
                // 指定channel
                .channel(NioServerSocketChannel.class)
                // 初始化handler
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        pipeline
                                // 添加http编码解码器
                                .addLast(new HttpServerCodec())
                                // 对大数据量的支持
                                .addLast(new ChunkedWriteHandler())
                                // 对http消息进行聚合
                                .addLast(new HttpObjectAggregator(1024 * 64))
                                // 对websocket做支持
                                .addLast(new WebSocketServerProtocolHandler("/"))
                                // 自定义消息处理等
                                .addLast(new WebSocketHandler());
                    }
                });

        // 绑定端口
        ChannelFuture future = bootstrap.bind(8080).sync();
    }
}
