```java
io.netty.bootstrap.AbstractBootstrap.option
io.netty.channel.ChannelOption.TCP_NODELAY
```

```java
// 特定时间内没有响应则断开链接
// 客户端添加（第一个）
// 顺序错误抛出异常
// WARNING: An exceptionCaught() event was fired, and it reached at the tail of the pipeline. It usually means the last handler in the pipeline did not handle the exception.
// io.netty.handler.timeout.ReadTimeoutException

socketChannel.pipeline().addLast(new ReadTimeoutHandler(5));
```

