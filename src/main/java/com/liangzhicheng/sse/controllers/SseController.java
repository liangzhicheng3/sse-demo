package com.liangzhicheng.sse.controllers;

import cn.hutool.core.util.IdUtil;
import com.liangzhicheng.sse.client.SseClient;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * 打开页面传递标识，用uuid进行标识，uuid可以是用户标识，也可以是业务标识
 * 创建SSE连接（/createSse）
 * 通过请求（/sendMsg），触发后端业务向页面发送消息
 * 关闭SSE连接（/closeSse）
 *
 * 当客户端断开连接时，SseEmitter会抛出IOException，所以务必捕获并处理这种异常，通常情况会调用sseEmitter.complete()或sseEmitter.completeWithError()来关闭SseEmitter
 * SSE连接是持久性，长时间保持连接可能需要处理超时和重连问题
 * 考虑到资源消耗，对于大量的并发客户端，可能需要采用连接池或者其他优化策略
 */
@RestController
public class SseController {

    @Resource
    private SseClient sseClient;

    @GetMapping(value = "/")
    public String index(ModelMap modelMap){
        modelMap.put("uuid", IdUtil.fastUUID());
        return "index";
    }

    @CrossOrigin
    @GetMapping(value = "/createSse")
    public SseEmitter createSse(String uuid){
        return sseClient.createSse(uuid);
    }

    @CrossOrigin
    @GetMapping(value = "/closeSse")
    public void closeSse(String uuid){
        sseClient.closeSse(uuid);
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping(value = "/sendMessage")
    public String sendMessage(String uuid){
        for(int i = 0; i < 10; i++){
            sseClient.sendMessage(uuid, "row" + i, IdUtil.fastUUID());
        }
        return "ok";
    }

}