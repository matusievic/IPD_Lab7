package by.andrej.cd.service

import java.io.File

import scala.io.Source
import scala.sys.process._

class Burner(begin: () => Unit, update: String => Unit, end: Int => Unit) extends Thread {
  private val logFileName = "log.txt"
  var files: List[String] = List()
  var eject: Boolean = false

  override def run() = {
    begin()
    generateISO
    end(burnISO)
  }

  private def generateISO = {
    //prepare log file
    val logFile = new File(logFileName)
    logFile.delete()
    val logger = ProcessLogger(logFile)

    //prepare terminal command
    val cmd = new StringBuilder("genisoimage -o temp.iso ")
    for (f <- files) cmd.append(f).append(' ')
    val p = Process(cmd.mkString)

    //execute command
    p ! logger
    executeCmd(p, logger, 0)
  }

  private def burnISO = {
    //prepare log file
    val logFile = new File(logFileName)
    logFile.delete()
    val logger = ProcessLogger(logFile)
    //prepare terminal command
    val p = Process("wodim" + {if (eject) " -eject" else ""} + " -v -data temp.iso")

    //execute command
    val pi = p.run(logger)
    executeCmd(p, logger, 0)

    //release resources
    logFile.delete()
    new File("temp.iso").delete()
    pi.exitValue
  }

  private def executeCmd(p: ProcessBuilder, l: FileProcessLogger, cur: Int): Unit = {
    if (!p.hasExitValue || cur == 0) {
      Thread.sleep(1000)
      l.flush()
      val f = Source.fromFile(logFileName).getLines().toList
      for (l <- f if f.indexOf(l) > cur) update(l)
      executeCmd(p, l, f.length)
    }
  }
}