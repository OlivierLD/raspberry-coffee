package yaml.parsing

import java.io.FileInputStream
import java.util

import org.yaml.snakeyaml.Yaml

object SampleScalaMUX {

  val YAML_FILE = "multiplexer.yaml"

  private def dumpChannel(channel: util.Map[String, Any]) = println(s"Channel ${channel}")

  private def dumpForwarder(forwarder: util.Map[String, Any]) = println(s"Forwarder ${forwarder}")

  private def dumpComputer(computer: util.Map[String, Any]) = println(s"Computer ${computer}")

  @throws[Exception]
  private def go() = {
    val yaml = new Yaml
    val inputStream = new FileInputStream(YAML_FILE)
    val map:util.HashMap[String, Object] = yaml.load(inputStream)
    map.keySet.forEach((k: String) => {
      println(s"${k} -> ${map.get(k).getClass.getName}")
      k match {
        case "name" =>
          println(s"Name: ${map.get(k)}")
        case "context" =>
          val context = map.get(k).asInstanceOf[util.Map[String, Any]]
          println(context)
        case "channels" =>
          val channels = map.get(k).asInstanceOf[util.List[util.Map[String, Any]]]
          channels.stream.forEach(this.dumpChannel)
        case "forwarders" =>
          val forwarders = map.get(k).asInstanceOf[util.List[util.Map[String, Any]]]
          forwarders.stream.forEach(this.dumpForwarder)
        case "computers" =>
          val computers = map.get(k).asInstanceOf[util.List[util.Map[String, Any]]]
          computers.stream.forEach(this.dumpComputer)
        case _ =>
        // Bam!
      }
    })
  }

  def main(args: Array[String]): Unit = {

    try go()
    catch {
      case ex: Exception =>
        ex.printStackTrace()
    }

  }
}


