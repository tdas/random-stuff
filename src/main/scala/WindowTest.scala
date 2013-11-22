import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ConstantInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import StreamingContext._
import org.apache.spark.rdd.RDD
object WindowTest {
  def main(args: Array[String]) {
    if (args.length < 3) {
      println(this.getClass.getSimpleName + "  <master>  <# input streams>  <# records / sec / stream>")
      System.exit(0)
    }
    val master = args(0)
    val numInputStreams = args(1).toInt
    val inputRate = args(2).toLong
    val numReducers = 10

    System.setProperty("spark.executor.memory", "10g")
    val ssc = new StreamingContext(master, this.getClass.getSimpleName, Seconds(1),
      "/root/spark", Seq("./target/scala-2.9.3/random-stuff_2.9.3-1.0.jar"))
    val inputStreams = (1 to numInputStreams).map(x => {
      ssc.networkStream(new IntMockReceiver(inputRate, StorageLevel.MEMORY_ONLY_SER_2))
    })
    val unifiedStream = ssc.union(inputStreams)
    val transformedStream = unifiedStream.map(x => (x.toString, x.toString))
      .reduceByKeyAndWindow((x: String, y: String) => x + y, Seconds(120), Seconds(1), numReducers)
    transformedStream.count.foreach((rdd: RDD[Long], time: Time) => println("\n\nTime: " + time + ", count: " + rdd.first) )
    ssc.start()
    Thread.sleep(1800 * 1000)
    ssc.stop()
  }
}
