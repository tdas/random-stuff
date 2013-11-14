import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import StreamingContext._

object GlitchTest {

  def main(args: Array[String]) {
    if (args.length < 3) {
      println(this.getClass.getSimpleName + "  <master>  <# input streams>  <# records / sec / stream>")
      System.exit(0)
    }
    val master = args(0)
    val numInputStreams = args(1).toInt
    val inputRate = args(2).toLong
    val numReducers = 10

    val ssc = new StreamingContext(master, this.getClass.getSimpleName, Seconds(1),
      jars = Seq("./target/scala-2.9.3/random-stuff_2.9.3-1.0.jar"))
    val inputStreams = (1 to numInputStreams).map(x => {
      ssc.networkStream(new IntMockReceiver(inputRate, StorageLevel.MEMORY_ONLY_SER_2))
    })
    val unifiedStream = ssc.union(inputStreams)
    val transformedStream = unifiedStream.map(x => (x.toString, 1)).reduceByKey((x: Int, y: Int) => x + y, numReducers)
    transformedStream.count.print
    ssc.start()
    Thread.sleep(180 * 1000)
    ssc.stop()
  }
}
