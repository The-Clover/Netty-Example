package connect._short;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Date;

/**
 *Description: Server
 *Created: 2024-09-23
 *@author Andrew.Ng
 */
public class Server {

    public static final int PORT = 8080;

    public static void main(String[] args) {
        bing(PORT);
    }

    private static void bing(int port) {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("netty server started on port " + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
        } finally {
            // 优雅关闭，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class ServerHandler extends ChannelHandlerAdapter {

        /**
         *
         */
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            ByteBuf time = ctx.alloc().buffer(4);
            // unix Time -> normal time：1900年1月1日起
            long l = System.currentTimeMillis() / 1000L + 2208988800L;
            time.writeLong(l);
            ChannelFuture f = ctx.writeAndFlush(time);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println("server time: " + l);
                    assert f == future;
                    ctx.close();
                }
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 服务端读取客户端发送的数据
            ByteBuf buf = (ByteBuf) msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");
            System.out.println("The TimeServer receive: " + body);

            // 服务端响应客户端
            //ByteBuf response = Unpooled.copiedBuffer(new Date().toString().getBytes());
            //ctx.writeAndFlush(response);
            // 服务端断开链接
            //ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
