# 短链接策略
## 客户端主动关闭
```java
// 发送消息后，客户端主动断开于服务端的链接
io.netty.channel.ChannelFuture.addListener
io.netty.channel.ChannelFutureListener.CLOSE
```
## 服务端主动关闭
### 处理监听器的结尾关闭
```java
// 处理完客户端的消息后，服务端断开和客户端的链接，等待新的客户端链接
io.netty.channel.ChannelHandlerContext.close()
```
### 处理结束的监听器中关闭
```java
@Override
public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    ByteBuf time = ctx.alloc().buffer(4);
    time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
    ChannelFuture f = ctx.writeAndFlush(time);
    f.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            assert f == future;
            ctx.close();
        }
    });
}
```
