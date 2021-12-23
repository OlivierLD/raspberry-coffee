package serial

import com.pi4j.io.serial.{Serial, SerialDataEvent, SerialDataEventListener, SerialFactory}

import scala.language.postfixOps

class SerialPI4J {
  private val serial: Serial = SerialFactory.createInstance

  def delay(t: Float): Unit = {
    try { Thread.sleep((t * 1000).asInstanceOf[Long]) }
    catch {
      case ie: InterruptedException => ie.printStackTrace
    }
  }
  def init(operation: (String) => Unit): Unit = {  // Send a block here
    val sdl = new SerialDataEventListener {
      override def dataReceived(event: SerialDataEvent): Unit = {
        operation(event.getAsciiString)
      }
    }
    serial.addListener(sdl)
  }

  def openSerial(port: String, br: Int): Unit = {
    serial.open(port, br)
  }

  def isSerialOpen: Boolean = {
    serial.isOpen
  }

  def closeSerial: Unit = {
    if (serial.isOpen)
      serial.close
  }

  def writeSerial(mess: String): Unit = {
    mess.toArray.foreach(b => {
      serial.write(b)
      delay(0.001f)    // To break the 16 character barrier...
    })
    serial.flush
  }
}

object utils {
  def isAsciiPrintable (ch: Char): Boolean = {
    (ch >= 32 && ch < 127)
  }

  def lpad(s: String, len: Int, `with`: String): String = { // :) Use a reserved word as prm name ...
    var str = s
    while (str.length < len) str = `with` + str
    str
  }

  def rpad(s: String, len: Int, `with`: String): String = {
    var str = s
    while (str.length < len) str += `with`
    str
  }

  private val LINE_LEN: Int = 16

  def dualDump(str: String): Array[String] = {
    val ba: Array[Byte] = str.getBytes
    val dim: Int = ba.length / LINE_LEN
    val result = Array.ofDim[String](dim + 1)

    (0 to dim).foreach(l => {
      var lineLeft  = ""
      var lineRight = ""
      val start = l * LINE_LEN

      (start to Math.min(start + LINE_LEN, ba.length) - 1).foreach(c => {
        lineLeft  += (lpad(Integer.toHexString(ba(c).asInstanceOf[Int]).toUpperCase, 2, "0") + " ")
        lineRight += (if (isAsciiPrintable(str.charAt(c))) str.charAt(c) else ".")
      })
      lineLeft = rpad(lineLeft, 3 * LINE_LEN, " ") + " "
      result(l) = lineLeft + "    " + lineRight
    })
    result
  }
}

object SerialPI4J {

  var serial:SerialPI4J = null
  var me:Thread = null

  def manageEvent(payload: String): Unit = {
    // TASK Do something interesting here...
    try {
      val sa = utils.dualDump(payload)
      sa.foreach(s => println(s))
    } catch {
      case ex: Exception =>
        println(payload)

    }
  }

  /**
   * Also shows how to use a shutdown hook.
   */
  def main(args: Array[String]): Unit = {
    println("Starting Serial, hit Ctrl + C to quit.")
    serial = new SerialPI4J
    println("Initializing")
    serial.init(payload => { // argument of 'init' is a block, the body of a function taking a string (payload here) as a parameter.
      manageEvent(payload)
    })
    println("Opening")
    try {
      serial.openSerial("/dev/ttyS0", 9600) // TODO Those prms as System variables.
    } catch {
      case ex: Exception =>
        ex.printStackTrace
    }
    println("Adding shutdown hook")
    sys addShutdownHook {
      println("Shutdown hook caught.")
      me synchronized {
        me notify
      }
      println("Closing")
      serial.closeSerial
      println("Bye.")
    }
    // Wait here
    me = Thread currentThread

    me synchronized {
      me wait
    }
    System.exit(0)
  }
}
