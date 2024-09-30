# 心跳和重连
```java
// 参数一：读超时时间
// 参数二：写超时时间
// 参数三：读写超时时间
// 时间单位
io.netty.handler.timeout.IdleStateHandler.IdleStateHandler(long, long, long, java.util.concurrent.TimeUnit)

// 心跳配置
socketChannel.pipeline().addLast(new IdleStateHandler(5, 5, 5, TimeUnit.SECONDS));
// 心跳触发器
socketChannel.pipeline().addLast(new TimeClientEventTrigger());
```