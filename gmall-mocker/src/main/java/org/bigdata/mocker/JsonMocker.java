package org.bigdata.mocker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bigdata.uploader.LogUploader;
import org.bigdata.utils.RanOpt;
import org.bigdata.utils.RandomDate;
import org.bigdata.utils.RandomNum;
import org.bigdata.utils.RandomOptionGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class JsonMocker {

    private int startupNum = 100000;
    int eventNum = 200000;

    private RandomDate logDateUtil = null;


    private RanOpt[] osOpts = {new RanOpt("ios", 3), new RanOpt("andriod", 7)};
    private RandomOptionGroup<String> osOptionGroup = new RandomOptionGroup(osOpts);
    private Date startTime = null;
    private Date endTime = null;

    private RanOpt[] areaOpts = {new RanOpt("beijing", 10),
            new RanOpt("shanghai", 10), new RanOpt("guangdong", 20), new RanOpt("hebei", 5),
            new RanOpt("heilongjiang", 5), new RanOpt("shandong", 5), new RanOpt("tianjin", 5),
            new RanOpt("shan3xi", 5), new RanOpt("shan1xi", 5), new RanOpt("sichuan", 5)
    };
    private RandomOptionGroup<String> areaOptionGroup = new RandomOptionGroup(areaOpts);

    private String appId = "gmall2019";

    private RanOpt[] vsOpts = {new RanOpt("1.2.0", 50), new RanOpt("1.1.2", 15),
            new RanOpt("1.1.3", 30),
            new RanOpt("1.1.1", 5)
    };

    private RandomOptionGroup<String> vsOptionGroup = new RandomOptionGroup(vsOpts);

    private RanOpt[] eventOpts = {new RanOpt("addFavor", 10), new RanOpt("addComment", 30), new RanOpt("addCart", 20), new RanOpt("clickItem", 40), new RanOpt("coupon", 45)};

    private RandomOptionGroup<String> eventOptionGroup = new RandomOptionGroup(eventOpts);

    private RanOpt[] channelOpts = {new RanOpt("xiaomi", 10), new RanOpt("huawei", 20),
            new RanOpt("wandoujia", 30), new RanOpt("360", 20), new RanOpt("tencent", 20)
            , new RanOpt("baidu", 10), new RanOpt("website", 10)
    };

    private RandomOptionGroup<String> channelOptionGroup = new RandomOptionGroup(channelOpts);

    private RanOpt[] quitOpts = {new RanOpt(true, 20), new RanOpt(false, 80)};

    private RandomOptionGroup<Boolean> isQuitGroup = new RandomOptionGroup(quitOpts);

    private JsonMocker() {

    }

    public JsonMocker(String startTimeString, String endTimeString, int startupNum, int eventNum) {
        try {
            startTime = new SimpleDateFormat("yyyy-MM-dd").parse(startTimeString);
            endTime = new SimpleDateFormat("yyyy-MM-dd").parse(endTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logDateUtil = new RandomDate(startTime, endTime, startupNum + eventNum);
    }

    private String initEventLog(String startLogJson) {
            /*`type` string   COMMENT '日志类型',
             `mid` string COMMENT '设备唯一 表示',
            `uid` string COMMENT '用户标识',
            `os` string COMMENT '操作系统',
            `appid` string COMMENT '应用id',
            `area` string COMMENT '地区' ,
            `evid` string COMMENT '事件id',
            `pgid` string COMMENT '当前页',
            `npgid` string COMMENT '跳转页',
            `itemid` string COMMENT '商品编号',
            `ts` bigint COMMENT '时间',*/

        JSONObject startLog = JSON.parseObject(startLogJson);
        String mid = startLog.getString("mid");
        String uid = startLog.getString("uid");
        String os = startLog.getString("os");
        String appid = this.appId;
        String area = startLog.getString("area");
        String evid = eventOptionGroup.getRandomOpt().getValue();
        int pgid = new Random().nextInt(50) + 1;
        int npgid = new Random().nextInt(50) + 1;
        int itemid = new Random().nextInt(50);
        //  long ts= logDateUtil.getRandomDate().getTime();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "event");
        jsonObject.put("mid", mid);
        jsonObject.put("uid", uid);
        jsonObject.put("os", os);
        jsonObject.put("appid", appid);
        jsonObject.put("area", area);
        jsonObject.put("evid", evid);
        jsonObject.put("pgid", pgid);
        jsonObject.put("npgid", npgid);
        jsonObject.put("itemid", itemid);
        return jsonObject.toJSONString();
    }

    private String initStartsupLog() {
            /*`type` string   COMMENT '日志类型',
             `mid` string COMMENT '设备唯一标识',
             `uid` string COMMENT '用户标识',
             `os` string COMMENT '操作系统', ,
             `appId` string COMMENT '应用id', ,
             `vs` string COMMENT '版本号',
             `ts` bigint COMMENT '启动时间', ,
             `area` string COMMENT '城市' */

        String mid = "mid_" + RandomNum.getRandInt(1, 100);
        String uid = "" + RandomNum.getRandInt(1, 100);
        String os = osOptionGroup.getRandomOpt().getValue();
        String appid = this.appId;
        String area = areaOptionGroup.getRandomOpt().getValue();
        String vs = vsOptionGroup.getRandomOpt().getValue();
        //long ts= logDateUtil.getRandomDate().getTime();
        String ch = os.equals("ios") ? "appstore" : channelOptionGroup.getRandomOpt().getValue();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "startup");
        jsonObject.put("mid", mid);
        jsonObject.put("uid", uid);
        jsonObject.put("os", os);
        jsonObject.put("appid", appid);
        jsonObject.put("area", area);
        jsonObject.put("ch", ch);
        jsonObject.put("vs", vs);
        return jsonObject.toJSONString();
    }

    private static void genLog() {
        JsonMocker jsonMocker = new JsonMocker();
        jsonMocker.startupNum = 1000000;
        for (int i = 0; i < jsonMocker.startupNum; i++) {
            String startupLog = jsonMocker.initStartsupLog();
            jsonMocker.sendLog(startupLog);
            while (!jsonMocker.isQuitGroup.getRandomOpt().getValue()) {
                String eventLog = jsonMocker.initEventLog(startupLog);
                jsonMocker.sendLog(eventLog);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendLog(String log) {
        LogUploader.sendLogStream(log);
    }

    public static void main(String[] args) {
        genLog();
    }
}
