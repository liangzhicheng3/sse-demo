package com.liangzhicheng.sse.client;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SseClient {

    private static final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 创建SSE连接
     */
    public SseEmitter createSse(String uuid){
        //默认30s超时，设置为0则不超时
        SseEmitter sseEmitter = new SseEmitter(0L);
        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("【{}】连接结束 ...", uuid);
            sseEmitterMap.remove(uuid);
        });
        //超时后回调
        sseEmitter.onTimeout(() -> {
            log.warn("【{}】连接超时 ...", uuid);
        });
        //异常后回调
        sseEmitter.onError(throwable -> {
            log.error("【{}】连接异常，异常为【{}】", uuid, throwable.getMessage());
            try{
                sseEmitter.send(
                        SseEmitter.event()
                                .id(uuid)
                                .name("发生异常")
                                .data("发生异常请重试")
                                .reconnectTime(3000));
                sseEmitterMap.putIfAbsent(uuid, sseEmitter);
            }catch(IOException e){
                e.printStackTrace();
            }
        });
        try{
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        }catch(IOException e){
            e.printStackTrace();
        }
        log.info("【{}】创建SSE连接成功 ...", uuid);
        sseEmitterMap.putIfAbsent(uuid, sseEmitter);
        return sseEmitter;
    }

    /**
     * 断开SSE连接
     */
    public void closeSse(String uuid){
        if(sseEmitterMap.containsKey(uuid)){
            sseEmitterMap.get(uuid).complete();
            sseEmitterMap.remove(uuid);
        }else{
            log.info("用户【{}】，连接已关闭", uuid);
        }
    }

    /**
     * 给指定用户发送消息
     */
    public void sendMessage(String uuid,
                               String messageId,
                               String message){
        executorService.execute(() -> {
            if(StrUtil.isBlank(message)){
                log.warn("【{}】消息参数为空", uuid);
            }
            SseEmitter sseEmitter = sseEmitterMap.get(uuid);
            if(sseEmitter == null){
                log.warn("消息发送失败，【{}】，没有创建连接，请重试 ...", uuid);
            }
            try{
                sseEmitter.send(SseEmitter.event()
                        .id(messageId)
                        .data(message)
                        .reconnectTime(1 * 60 * 1000L));
                log.info("用户【{}】，消息id【{}】，发送成功【{}】", uuid, messageId, message);
            }catch(IOException e){
                sseEmitterMap.remove(uuid);
                log.error("用户【{}】，消息id【{}】，发送失败【{}】", uuid, messageId, e.getMessage());
            }
        });
    }

}