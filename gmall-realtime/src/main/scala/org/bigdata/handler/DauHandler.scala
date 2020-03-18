package org.bigdata.handler

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.bigdata.bean.StartupLog
import org.bigdata.utils.RedisUtil
import redis.clients.jedis.Jedis

/**
 * daily active user
 */
object DauHandler {

  private val sdf = new SimpleDateFormat("yyyy-MM-dd")

  /**
   * 过滤后数据写入redis
   */
  def saveMidToRedis(filterByMidGroupLogDStream: DStream[StartupLog]): Unit = {
    filterByMidGroupLogDStream.foreachRDD(rdd => {
      rdd.foreachPartition(iter => {
        val client: Jedis = RedisUtil.getJedisClient
        iter.foreach(log => client.sadd(s"dau:${log.logDate}", log.mid))
        client.close()
      })
    })
  }

  /**
   * 分组去重
   */
  def filterByMidGroup(filterByRedisLogDStream: DStream[StartupLog]): DStream[StartupLog] = {
    filterByRedisLogDStream.map(log => (log.mid, log)).groupByKey().flatMapValues(iter => {
      iter.toList.sortWith(_.ts < _.ts).take(1)
    }).map(_._2)
  }

  /**
   *  一个分区一个连接
   */
  def filterByRedis(startupLogDStream: DStream[StartupLog], sc: SparkContext): DStream[StartupLog] = {
    startupLogDStream.transform(rdd => {
      val filteredRDD: RDD[StartupLog] = rdd.mapPartitions(iter => {
        val client: Jedis = RedisUtil.getJedisClient
        val logs: Iterator[StartupLog] = iter.filter(log => !client.sismember(s"dau:${log.logDate}", log.mid))
        client.close()
        logs
      })
      filteredRDD
    })
  }

  /**
   * 读取redis中的所有数据，广播变量到每个executor中
   */
  def filterByRedis2(startupLogDStream: DStream[StartupLog], sc: SparkContext): DStream[StartupLog] = {

    startupLogDStream.transform(rdd => {
      val ts: Long = System.currentTimeMillis()
      val date: String = sdf.format(new Date(ts))
      val client: Jedis = RedisUtil.getJedisClient

      val midSet: util.Set[String] = client.smembers(s"dau:$date")
      val midSetBroadcast = sc.broadcast(midSet)
      client.close()
      rdd.filter(log => !midSetBroadcast.value.contains(log.mid))
    })
  }


//  def filterByRedis3(startupLogDStream: DStream[StartupLog], sc: SparkContext): DStream[StartupLog] = {
//    val dateToLogs: DStream[(String, Iterable[StartupLog])] = startupLogDStream.map(log => (log.logDate, log)).groupByKey()
//    dateToLogs.transform(rdd => {
//      rdd.mapValues((date, logs: Iterable[StartupLog]) => {
//        val client: Jedis = RedisUtil.getJedisClient
//        val midSet: util.Set[String] = client.smembers(s"dau:$date")
//        client.close()
//        logs.filter(log => !midSet.contains(log.mid)).toList
//      })
//    })
//  }
}
