package marshalling;

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
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

/**
 *Description: Client
 *Created: 2024-09-23
 *@author Andrew.Ng
 */
public class Client {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8080;

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 编码
                            socketChannel.pipeline().addLast(MarshallingCodecFactory.buildMarshallingEncoder());
                            // 解码
                            socketChannel.pipeline().addLast(MarshallingCodecFactory.buildMarshallingDecoder());
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            Book book = new Book();
            book.setId(1);
            book.setName("资治通鉴");
            book.setDate(new Date());
            future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello".getBytes()));
            Thread.sleep(1000);
            future.channel().writeAndFlush(book);
            // 等待客户端链路关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {}
        finally {
            group.shutdownGracefully();
        }
    }

    static class ClientHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                Book response = (Book) msg;
                System.out.println("response is: " + response);
            } catch (Exception e){}
            finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
