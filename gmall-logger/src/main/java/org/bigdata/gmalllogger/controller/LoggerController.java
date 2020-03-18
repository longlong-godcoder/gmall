package org.bigdata.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import constants.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoggerController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping("test")
    public String test(){
        System.out.println("===testing===");
        return "success";
    }

    @RequestMapping("log")
    public String addLogToKafka(@RequestParam("log") String logString){
        JSONObject jsonObject = JSON.parseObject(logString);
        //加入时间戳属性
        jsonObject.put("ts", System.currentTimeMillis());

        log.info(jsonObject.toString());

        if ("startup".equals(jsonObject.getString("type"))) {
            kafkaTemplate.send(Constant.GMALL_STARTUP, jsonObject.toString());
        } else {
            kafkaTemplate.send(Constant.GMALL_EVENT, jsonObject.toString());
        }

        return "success";

    }
}
