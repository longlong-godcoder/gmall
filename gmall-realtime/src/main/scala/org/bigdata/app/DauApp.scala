package org.bigdata.app

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.JSON
import constants.Constant
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.bigdata.bean.StartupLog
import org.bigdata.handler.DauHandler
import org.bigdata.utils.MyKafkaUtil

object DauApp {
  def main(args: Array[String]): Unit = {

    //1.创建SparkConf
    val sparkConf: SparkConf = new SparkConf().setAppName("DauApp").setMaster("local[4]")

    //2.创建StreamingContext
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    val sdf = new SimpleDateFormat("yyyy-MM-dd HH")

    //3.读取Kafka数据
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(ssc, Set(Constant.GMALL_STARTUP))

    //4.将其转换为样例类对象
    val startUpLogDStream: DStream[StartupLog] = kafkaDStream.map(record => {
      val log: StartupLog = JSON.parseObject(record.value(), classOf[StartupLog])
      //获取事件戳
      val ts: Long = log.ts
      //将时间戳转换为日期字符串
      val dateHour: String = sdf.format(new Date(ts))
      //按照空格切分
      val dateHourArr: Array[String] = dateHour.split(" ")
      //给日期及小时字段赋值
      log.logDate = dateHourArr(0)
      log.logHour = dateHourArr(1)
      //返回
      log
    })
    startUpLogDStream.cache()

    //5.结合Redis做跨批次去重
    val filterByRedisLogDStream: DStream[StartupLog] = DauHandler.filterByRedis2(startUpLogDStream, ssc.sparkContext)
    filterByRedisLogDStream.cache()

    //6.使用分组做同批次去重
    val filterByMidGroupLogDStream: DStream[StartupLog] = DauHandler.filterByMidGroup(filterByRedisLogDStream)
    filterByMidGroupLogDStream.cache()

    //7.将mid写入Redis
    DauHandler.saveMidToRedis(filterByMidGroupLogDStream)

    //8.将数据整体写入Phoenix

    //测试打印
    startUpLogDStream.count().print()
    filterByRedisLogDStream.count().print()
    filterByMidGroupLogDStream.count().print()

    //启动任务
    ssc.start()
    ssc.awaitTermination()

  }
}
