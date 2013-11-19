import it.unimi.dsi.fastutil.io.FastBufferedOutputStream
import java.io.FileOutputStream

object DiskWriteTest {
  def main(args: Array[String]) {
    val path = if (args.length > 0) args(0) else "."
    val bytes = new Array[Byte](1024 * 1024)
    val fos = new FileOutputStream(path + "/test")
    val bos = new FastBufferedOutputStream(fos)
    var sumOfTimeTaken = 0.0
    var numTimeTaken = 0
    while(true) {
      val startTime = System.currentTimeMillis()
      bos.write(bytes)
      bos.flush()
      fos.flush()
      fos.getFD().sync()
      val timeTaken = System.currentTimeMillis() - startTime
      sumOfTimeTaken += timeTaken
      numTimeTaken += 1
      println("Time taken = " + timeTaken + " ms " + ( if (timeTaken > 2 * sumOfTimeTaken / numTimeTaken) " *** " else "" ) )
    }
  }
}
