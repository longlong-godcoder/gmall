package org.bigdata.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}


object RedisUtil {



  private val logger = LoggerFactory.getLogger(classOf[Nothing])

  var jedisPool: JedisPool = _

  {
    logger.info("==============初始化redis连接池=============")
    val config = PropertiesUtil.load("config.properties")
    val host = config.getProperty("redis.host")
    val port = config.getProperty("redis.port")

    val jedisPoolConfig = new JedisPoolConfig
    jedisPoolConfig.setMaxTotal(100) //最大连接数
    jedisPoolConfig.setMaxIdle(20) //最大空闲
    jedisPoolConfig.setMinIdle(20) //最小空闲
    jedisPoolConfig.setBlockWhenExhausted(true) //忙碌时是否等待
    jedisPoolConfig.setMaxWaitMillis(2000) //忙碌时等待时长 毫秒
    jedisPoolConfig.setTestOnBorrow(true) //每次获得连接的进行测试

    jedisPool = new JedisPool(jedisPoolConfig, host, port.toInt)
  }

  def getJedisClient: Jedis = {
    logger.info(s"jedisPool.getNumActive = ${jedisPool.getNumActive}")
    logger.info("====获得一个连接============")
    jedisPool.getResource
  }

}
