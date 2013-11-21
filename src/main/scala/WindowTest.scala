import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ConstantInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import StreamingContext._

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

    val ssc = new StreamingContext("local[4]", this.getClass.getSimpleName, Seconds(1))
    val inputStreams = (1 to numInputStreams).map(x => {
      ssc.networkStream(new IntMockReceiver(inputRate, StorageLevel.MEMORY_ONLY_SER_2))
    })
    val unifiedStream = ssc.union(inputStreams)
    val transformedStream = unifiedStream.map(x => (x.toString, x.toString))
      .reduceByKeyAndWindow((x: String, y: String) => x + y, Seconds(30), Seconds(1), numReducers)
    transformedStream.count.print
    ssc.start()
    Thread.sleep(180 * 1000)
    ssc.stop()
  }
}