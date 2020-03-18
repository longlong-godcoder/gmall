package org.bigdata.gmallpublisher.service.impl;

import org.bigdata.gmallpublisher.mapper.DauMapper;
import org.bigdata.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    private DauMapper dauMapper;

    @Override
    public Integer getRealTimeTotal(String date) {
        return dauMapper.selectDauTotal(date);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Map getDauTotalHourMap(String date) {

        List<Map> list = dauMapper.selectDauTotalHourMap(date);
        HashMap<String, Long> resultMap = new HashMap<>();
        for (Map map : list){
           resultMap.put((String)map.get("LH"), (Long) map.get("CT"));
        }
        return resultMap;
    }
}
