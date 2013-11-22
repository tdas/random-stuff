import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import StreamingContext._
import org.apache.spark.SparkContext
import SparkContext._

object GlitchTest extends org.apache.spark.Logging {

  def main(args: Array[String]) {
    if (args.length < 3) {
      println(this.getClass.getSimpleName + "  <master>  <# input streams>  <# records / sec / stream>")
      System.exit(0)
    }
    val master = args(0)
    val numInputStreams = args(1).toInt
    val inputRate = args(2).toLong
    val numReducers = 100
    logInfo("Sync setting = " + System.getProperty("spark.shuffle.sync"))

    System.setProperty("spark.executor.memory", "10g")
    val ssc = new StreamingContext(master, this.getClass.getSimpleName, Seconds(1),
      "/root/spark", Seq("./target/scala-2.9.3/random-stuff_2.9.3-1.0.jar"))
    val inputStreams = (1 to numInputStreams).map(x => {
      ssc.networkStream(new IntMockReceiver(inputRate, StorageLevel.MEMORY_ONLY_SER_2))
    })
    val unifiedStream = ssc.union(inputStreams)
    // val transformedStream = unifiedStream.map(x => (x.toString, 1)).reduceByKey((x: Int, y: Int) => x + y, numReducers)
    val transformedStream = unifiedStream.map(x => (x % 10000, new Array[Byte](1000))).groupByKey(numReducers)
    transformedStream.count.print()//foreach(rdd => { val count = rdd.first; println("\n\n\nCount = " + count + "\n\n\n") })
    ssc.start()
    Thread.sleep(1800 * 1000)
    ssc.stop()
  }
}
