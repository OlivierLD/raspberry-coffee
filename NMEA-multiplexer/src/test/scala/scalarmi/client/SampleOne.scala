package scalarmi.client

import context.NMEADataCache
import nmea.forwarders.rmi.ServerInterface
import nmea.forwarders.rmi.clientoperations.BoatPosition
import nmea.forwarders.rmi.clientoperations.CalculatedCurrent
import nmea.forwarders.rmi.clientoperations.InstantCurrent
import nmea.forwarders.rmi.clientoperations.LastString
import nmea.forwarders.rmi.clientoperations.NMEACache
import nmea.forwarders.rmi.clientoperations.TrueWind
import nmea.parser.GeoPos
import java.rmi.registry.LocateRegistry
import java.text.NumberFormat

object SampleOne extends App {

  val ONE_SEC = 1_000L

//def main(args: Array[String]): Unit = {
    println("Hello Scala!")

    val bindingName = "RMI-NMEA"
    val name = "olediouris-mbp" // "raspberrypi3.att.net" // "10.10.226.181";
    val port = "1099"
    println(s"Looking up $bindingName on $name:$port ...")

    try {
      var before = System.currentTimeMillis
      val registry = LocateRegistry.getRegistry(name, Integer.valueOf(port))
      // Server name, port
      val remote = registry.lookup(bindingName)
      println(s"Remote is a ${remote.getClass.getName}")
      val comp = registry.lookup(bindingName).asInstanceOf[ServerInterface]
      // RMI Name
      var after = System.currentTimeMillis
      println(s"Lookup took ${NumberFormat.getInstance.format(after - before)} ms.")
      val task = new LastString
      before = System.currentTimeMillis
      val last = comp.executeTask(task)
      after = System.currentTimeMillis
      println(s"LastString execution took ${NumberFormat.getInstance.format(after - before)} ms.")
      println(last)
      try {
        Thread sleep(ONE_SEC)
      } catch {
        case ie: InterruptedException => {
        }
      }
      try {
        val cacheTask = new NMEACache
        before = System.currentTimeMillis
        val cache = comp.executeTask(cacheTask)
        after = System.currentTimeMillis
        println(s"NMEACache execution took ${NumberFormat.getInstance.format(after - before)} ms.")
        val position = cache.get(NMEADataCache.POSITION)
        println(s"Position is a ${position.getClass.getName}")
        if (position.isInstanceOf[GeoPos]) println(s"Position is ${position.asInstanceOf[GeoPos].toString} (Grid Square ${position.asInstanceOf[GeoPos].gridSquare})")
        try {
          Thread sleep(ONE_SEC)
        } catch {
          case ie: InterruptedException => {
          }
        }
      } catch {
        case e: Exception => {
          println(s"Oops, getting full cache: ${e.toString}")
        }
      }
      val boatPositionTask = new BoatPosition
      before = System.currentTimeMillis
      val boatGeoPos = comp.executeTask(boatPositionTask)
      after = System.currentTimeMillis
      println(s"BoatPosition execution took ${NumberFormat.getInstance.format(after - before)} ms.")
      println(s"Position is ${boatGeoPos.toString}")
      try {
        Thread sleep(ONE_SEC)
      } catch {
        case ie: InterruptedException => {
        }
      }
      val trueWind = new TrueWind
      val calculatedCurrent = new CalculatedCurrent
      val instantCurrent = new InstantCurrent
      // Instant: CSP & CDR
      (1 to 30).foreach((idx: Int) => {
        before = System.currentTimeMillis
        val tw = comp.executeTask(trueWind)
        after = System.currentTimeMillis
        println(s"TrueWind execution took ${NumberFormat.getInstance.format(after - before)} ms")
        println(s"TW is ${tw.getSpeed} knots, from ${tw.getAngle}")
        val calc = comp.executeTask(calculatedCurrent)
        val inst = comp.executeTask(instantCurrent)
        println(s"Instant Current    ${inst.speed} knots, dir ${inst.angle}")
        println(s"Calculated Current ${calc.speed} knots, dir ${calc.angle}")
        try {
          Thread sleep(ONE_SEC)
        } catch {
          case ie: InterruptedException => {
          }
        }
      })
    } catch {
      case e: Exception => {
        Console.err.println("Compute exception:")
        e.printStackTrace
      }
    }
    println("Done!")
//}
}

