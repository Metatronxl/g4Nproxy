# g4Nproxy
使用手机4G流量提供高质量的代理服务

## 项目简介
这是一个代理服务共享系统，可以将多个android手机组建成一个http代理服务集群。可以实现通过变化的手机的地理位置，来达到拥有动态IP的功能
## 注意
目前只支持HTTP/HTTPS层的协议代理

## 部署指北
### 服务器部署
1. 修改端口地址为：com.xulei.g4nproxy_protocol.g4nproxyServerPort
```Java
    // 请求服务器port端口
    int g4nproxyServerPort = 30000;
```
2.springboot 打包 执行命令 `./gradlew g4nproxy-server:bootJar`,之后将会得到文件 g4nproxy-server/build/libs/g4nproxy-server-0.0.1-SNAPSHOT.jar
3.上传至服务器 ，执行命令`nohup java jar path/to/g4proxy-server-0.0.1-SNAPSHOT.jar &`

### 客户端部署
1.修改服务器IP和端口的地址为：com.xulei.g4nproxy_protocol.g4nproxyServerPort
```Java
    // 请求服务器port端口
    int g4nproxyServerPort = 30000;
    // 请求服务器IP地址
    String g4nproxyServerHost = "127.0.0.1";
```

2. 生成APK文件 `./gradlew app:assembleRelease` 将会得到apk文件 `app/build/outputs/apk/release/app-release.apk`
3. 使用 `adb install`命令安装apk文件至手机

### 使用说明
在部署完毕后，http://「your ip 」:30000/portList 可以查看已经连接上服务器的手机，一个手机对应一个端口，可以通过访问这些端口来让手机作为代理服务器


## Finished Job

- 能够实现 ` curl -x 127.0.0.1:30001 www.baidu.com` 这样的短连接代理请求
- 长连接的代理（ex：下载一个几十M的文件）
- 并发连接采用序列号来区分（废弃了旧版的每次请求均建立一次连接的做法）

## UnTest

- 大规模的并发测试



