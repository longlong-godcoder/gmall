package org.bigdata.gmallpublisher.service;

import java.util.Map;

public interface PublisherService {

    public Integer getRealTimeTotal(String date);

    public Map getDauTotalHourMap(String date);
}
