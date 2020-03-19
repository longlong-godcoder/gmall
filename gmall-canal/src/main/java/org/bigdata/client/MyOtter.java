package org.bigdata.client;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import constants.Constant;
import org.bigdata.utils.MyKafkaProducer;

import java.net.InetSocketAddress;
import java.util.List;

public class MyOtter {

    public static void main(String[] args) {
        //destination是otter实例，也就是canal client
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("node-01", 11111), "example", "", "");

        while (true){
            //连接canal
            canalConnector.connect();
            //指定订阅的数据库
            canalConnector.subscribe("gmall.*");
            //设置一次拉取的batchSize, 数据条数
            Message message = canalConnector.get(1024);

            if (message.getEntries().size() == 0){
                int reTry = 5000;
                System.out.println("=====没有拉取到数据======" + reTry + "ms后重试======");
                try {
                    Thread.sleep(reTry);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                //解析message, 遍历每一个entry, entry对应一个sql，可能有多条信息
                for (CanalEntry.Entry entry : message.getEntries()) {
                    //仅获取rowdate类型的数据
                    if (CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())){
                        //获取表明
                        String tableName = entry.getHeader().getTableName();
                        //获取行值，需要序列化
                        ByteString storeValue = entry.getStoreValue();
                        try {
                            //反序列化，获取行值
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                            //获取行值列表 与 事件类型
                            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                            CanalEntry.EventType eventType = rowChange.getEventType();

                            handler(tableName, rowDatasList, eventType);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    private static void handler(String tableName, List<CanalEntry.RowData> rowDatasList, CanalEntry.EventType eventType) {
        //处理order_info表的数据
        if ("order_info".equals(tableName)){
            //只处理insert新增数据，不处理update等数据
            if (CanalEntry.EventType.INSERT.equals(eventType)){
                //遍历rowDatasList, rowData对应一条信息
                for (CanalEntry.RowData rowData : rowDatasList) {
                    JSONObject jsonObject = new JSONObject();
                    for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                        jsonObject.put(column.getName(), column.getValue());
                    }
                    System.out.println(jsonObject.toJSONString());
                    //写入kafka的gmall_info topic
                    MyKafkaProducer.send(Constant.GMALL_ORDER_INFO, jsonObject.toString());
                }


            }
        }
    }
}
