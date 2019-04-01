package com.xulei.g4nproxy_server.server;

import com.google.common.collect.Maps;
import com.xulei.g4nproxy_protocol.protocol.Constants;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 服务器端的可用端口
 *
 * @author lei.X
 * @date 2019/3/18 5:00 PM
 */
public class AvailablePortManager {

    private int startUsePort = Constants.g4nproxyServerPort + 1;
    private static final int maxMappingPort = Constants.g4nproxyServerPort + 10000;
    private LinkedBlockingQueue<Integer> availablePortResourceQueue = new LinkedBlockingQueue<>();

    private Map<String, Integer> agentPortResourceMap = Maps.newConcurrentMap();

    private static AvailablePortManager instance = new AvailablePortManager();

    public static AvailablePortManager getInstance() {
        return instance;
    }

    private AvailablePortManager() {
        expandPort();
    }

    // 添加新的可用端口
    private void expandPort() {
        if (startUsePort > maxMappingPort) {
            throw new IllegalStateException("port space Exhausted");
        }
        for (int i = 0; i < 100; i++) {
            availablePortResourceQueue.add(startUsePort);
            startUsePort++;

        }
    }

    private void expandPortIfEmpty() {
        if (availablePortResourceQueue.size() == 0) {
            synchronized (this) {
                if (availablePortResourceQueue.size() == 0) {
                    expandPort();
                }
            }
        }
    }

    /**
     * 获取一个历史可用port
     * or
     * 生成一个新的port
     *
     * @param clientKey
     * @return
     */
    public Integer poll(String clientKey){
        Integer historyBind = agentPortResourceMap.get(clientKey);
        if (historyBind != null){
            return historyBind;
        }
        expandPortIfEmpty();

        Integer poll = availablePortResourceQueue.poll();
        if (poll != null){
            agentPortResourceMap.put(clientKey,poll);
            return poll;
        }

        return poll(clientKey);
    }

    public Map<String, Integer> deviceMapping() {
        return Maps.newHashMap(agentPortResourceMap);
    }


}
