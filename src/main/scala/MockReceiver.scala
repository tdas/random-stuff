import java.util.{Timer, TimerTask}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.NetworkReceiver
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scala.util.Random

class MockReceiver[T: ClassManifest](
    generatingFunction: () => T,
    recordsPerSecond: Long,
    storageLevel: StorageLevel
  ) extends NetworkReceiver[T] {

  lazy val blockGenerator = new BlockGenerator(storageLevel)
  lazy val timer = new Timer(true)
  val epochDurationInMillis = 50
  val numRecordsPerEpoch = (recordsPerSecond.toDouble / 1000.0 * epochDurationInMillis).toLong
  val syncIntervalInEpochs = (1000.0 / epochDurationInMillis).toInt
  val numRecordsPerSyncInterval = syncIntervalInEpochs * numRecordsPerEpoch
  val averagingInterval = 60 * 1000

  var epochCount: Long = 0
  var numRecordsGeneratedInEpoch: Long = 0
  var numRecordsGenerated: Long = 0
  var startTime: Long = 0

  protected def onStart() {
    startTime = System.currentTimeMillis()
    blockGenerator.start()
    timer.scheduleAtFixedRate(new TimerTask { def run() { generateData() } } , 0 , epochDurationInMillis)
  }

  protected def onStop() {
    timer.cancel()
    blockGenerator.stop()
  }

  def generateData() {
    epochCount += 1

    val numRecordsToGenerate = if (isSyncInterval) {
      numRecordsPerSyncInterval - numRecordsGeneratedInEpoch
    } else {
      numRecordsPerEpoch
    }
    logDebug("Sending " + numRecordsToGenerate + " records at " + System.currentTimeMillis)
    var i = 0
    while(i < numRecordsToGenerate) {
      blockGenerator += generatingFunction()
      //Thread.sleep(1)
      i += 1
    }
    numRecordsGeneratedInEpoch += numRecordsToGenerate
    numRecordsGenerated += numRecordsToGenerate

    if (!approxEqual(recordsPerSecond, averageRecordsPerSecond, 0.1)) {
      logWarning("Achieved rate is " + (averageRecordsPerSecond / 10e3).formatted("%.3f") + "K rec/sec, " +
        "expected " + (recordsPerSecond / 10e3).formatted("%.3f") + "K rec/sec")
    } else {
      logDebug("Generated " + numRecordsGenerated + " at " + (averageRecordsPerSecond / 10e3).formatted("%.3f") + "K rec/sec")
    }

    if (isSyncInterval) numRecordsGeneratedInEpoch = 0
    if (isAverageInterval) resetAverage()
  }

  def isSyncInterval = epochCount % syncIntervalInEpochs == 0

  def isAverageInterval = System.currentTimeMillis - startTime > averagingInterval

  def averageRecordsPerSecond = numRecordsGenerated.toDouble / (System.currentTimeMillis - startTime) * 1000

  def resetAverage() {
    numRecordsGenerated = 0
    startTime = System.currentTimeMillis
  }

  def approxEqual(expected: Double, achieved: Double, relativeError: Double) = {
    achieved <= expected * (1 + relativeError) && achieved >= expected * (1 - relativeError)
  }
}

class IntMockReceiver(recordsPerSecond: Long, storageLevel: StorageLevel)
  extends MockReceiver[Int](() => Random.nextInt(), recordsPerSecond, storageLevel)

object MockReceiver {
  def main(args: Array[String]) {
    val receiver = new IntMockReceiver(200000, StorageLevel.MEMORY_ONLY)
    val ssc = new StreamingContext("local[4]", "test", Seconds(1), jars = Seq("./target/scala-2.9.3/random-stuff_2.9.3-1.0.jar"))
    val networkStream = ssc.networkStream(receiver)
    networkStream.count.print()
    ssc.start()
    Thread.sleep(60 * 1000)
    ssc.stop()
  }
}