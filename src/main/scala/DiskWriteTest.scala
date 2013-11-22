import it.unimi.dsi.fastutil.io.FastBufferedOutputStream
import java.io.FileOutputStream

object DiskWriteTest {
  def main1(args: Array[String]) {
    val path = if (args.length > 0) args(0) else "."
    val bytes = new Array[Byte](10 * 1024 * 1024)
    val fos = new FileOutputStream(path + "/test")
    val bos = new FastBufferedOutputStream(fos)
    var sumOfTimeTaken = 0.0
    var numTimeTaken = 0
    var intervalSinceAnomaly = 0
    var numIntervals = 0
    var anomalyIntervalSum = 0

    while(true) {
      Thread.sleep(scala.util.Random.nextInt(100))
      val startTime = System.currentTimeMillis()
      bos.write(bytes)
      bos.flush()
      fos.flush()
      fos.getFD().sync()
      val timeTaken = System.currentTimeMillis() - startTime
      sumOfTimeTaken += timeTaken
      numTimeTaken += 1
      var x = ""
      if (timeTaken > 2 * sumOfTimeTaken / numTimeTaken) {
        anomalyIntervalSum += intervalSinceAnomaly
        numIntervals += 1
        intervalSinceAnomaly = 0
        x = " ***** periodicity = " + (anomalyIntervalSum.toDouble / numIntervals).formatted("%.1f")
      } else {
        intervalSinceAnomaly += 1
      }
      println("Time taken = " + timeTaken + " ms " + x )
    }
  }
}
