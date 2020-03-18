package org.bigdata.utils

import java.io.InputStreamReader
import java.util.Properties

object PropertiesUtil {

  def load(propertyName: String): Properties = {
    val prop = new Properties()
    prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertyName), "UTF-8"))
    prop
  }
}
