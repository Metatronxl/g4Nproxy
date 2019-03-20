# g4Nproxy
使用手机4G流量提供高质量的代理服务

# 基本方案

- 提供一个统一的代理服务器，部署在公网（netty http/https proxy）
- 提供一个手机程序，作为agent部署在装有4G卡的手机里面
- 手机agent通过长连接的方式，和server建立channel。并保证 idle channel>=1 (也即，至少拥有一个空闲的channel，如果有很多的代理请求，那么可能有其他请求使用了channel，当channel资源不够的时候，agent异步的主动链接服务器建立channel通道)所有在服务器的channel，安装clinet id分组。构建资源池。在进行代理转发的时候，提供资源管理模型
- 当用户的代理请求调用过来的时候，在channel资源池中，根据需要选择一个channel，转发请求（考虑通过固定header绑定到固定的手机下面，可以使用一致性hash算法）
- 考虑多台代理服务器的实现
