package heart_and_reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 *Description: Client
 *Created: 2024-09-23
 *@author Andrew.Ng
 */
public class Client {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9090;

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 心跳规则
                            socketChannel.pipeline().addLast(new IdleStateHandler(5, 5, 5, TimeUnit.SECONDS));
                            // 心跳触发器
                            socketChannel.pipeline().addLast(new ClientEventTrigger());
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();

            future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello".getBytes()));
            // 防止TCP粘包
            Thread.sleep(1000);
            future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello".getBytes()));
            Thread.sleep(1000);
            future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello".getBytes()));
            future.channel().closeFuture().sync();
        } catch (Exception ignored) {
        } finally {
            group.shutdownGracefully();
        }
    }

    static class ClientHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                ByteBuf buf = (ByteBuf) msg;
                byte[] req = new byte[buf.readableBytes()];
                buf.readBytes(req);
                String body = new String(req, StandardCharsets.UTF_8);

                // 心跳包
                if ("HeartBeat-response".equals(body)) {
                    System.out.println("Received HeartBeat-response from server");
                    return;
                }
                System.out.println("Body is: " + body);
            } catch (Exception ignored) {
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    // 触发器
    static class ClientEventTrigger extends ChannelHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            IdleState state = ((IdleStateEvent) evt).state();

            if (state.equals(IdleState.ALL_IDLE)) {
                System.out.println("client send heart beat");
                ctx.writeAndFlush(Unpooled.copiedBuffer("HeartBeat-req".getBytes()));
            }
        }
    }
}
