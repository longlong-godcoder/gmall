package org.bigdata.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import org.bigdata.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PublisherController {

    @Autowired
    private PublisherService publisherService;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping("realtime-total")
    public String getRealTimeTotal(@RequestParam("date") String date){
        System.out.println("=====接收 realtime-total 请求 ==========");
        //get result count from Phoenix
        Integer total = publisherService.getRealTimeTotal(date);
        //可能有多个结果集，统一封装成JSONArray的String返回
        ArrayList<Map> resultList = new ArrayList<>();
        Map<String, Object> dauMap = new HashMap<>();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");
        dauMap.put("value", total);
        resultList.add(dauMap);
        return JSON.toJSONString(resultList);
    }

    @RequestMapping("realtime-hours")
    public String getDauTotalHourMap(@RequestParam("id") String id, @RequestParam("date") String date){
        HashMap<String, Map> resultMap = new HashMap<>();
        Map todayHourCountsMap = publisherService.getDauTotalHourMap(date);

        Calendar calendar = Calendar.getInstance();
        String yesterdayDate = null;

        try {
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            yesterdayDate = sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map yesterdayHourCountsMap = publisherService.getDauTotalHourMap(yesterdayDate);

        resultMap.put("yesterday", yesterdayHourCountsMap);
        resultMap.put("today", todayHourCountsMap);

        return JSON.toJSONString(resultMap);
    }
}
