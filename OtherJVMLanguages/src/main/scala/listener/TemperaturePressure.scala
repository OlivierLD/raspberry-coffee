package listener

import i2c.sensor.{BMP180, HTU21DF}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
/*
 * Requires a BMP180, on I2C address 0x77
 */
class StartProducing {}

class StopProducing(message:String) {
  def getMessage:String = message
}

class AddSubscribers(sub:Array[ActorRef]) {
  def getSubscribers:Array[ActorRef] = sub
}

class DataMessage(mess:String) {
  def getPayload:String = mess
}

class ExitMsg {}

class PubSubActor(actorName: String) extends Actor {

  var subscribers:Array[ActorRef] = Array.empty[ActorRef]
  var keepLooping = true

  def getName:String = actorName

  override def receive: Receive = {
    case add: AddSubscribers =>
      subscribers ++= add.getSubscribers
    case mess: DataMessage =>
      println(s"  $actorName received: ${ mess.getPayload }")
      // TASK Do what you have to here with the payload
    case exit: ExitMsg  =>
      println("Bye now (from actor " + actorName + ")")
      this.context.stop(self)
    case start: StartProducing =>
      val bmp180  = new BMP180
//    val htu21df = new HTU21DF
      println("Starting production")
      var i = 1
      while (keepLooping) {
        subscribers foreach (act => {
          val temperature = bmp180.readTemperature
          val pressure = bmp180.readPressure
          val mess = "{ \"rnk\":" + i + ", \"temperature\":" + temperature + ", \"pressure\":" + pressure + " }"
      //  println(">>> " + actorName + " sending data to " + act)
          act ! new DataMessage(mess)
        })
        Thread sleep(100 + Math.round(Math.random * 2000)) // Random wait, 100 ms minimum, 2099 ms maximum.
        // QUESTION Isn't there a better way in Scala?
        i += 1
      }
      println("Production stopped")
    case _ =>
      println(s"Duh? What was that? (from ${ actorName })")
  }
}

object Main {
  def main(args: Array[String]):Unit = {
    val context = ActorSystem("PubSubSystem")

    // One producer, One consumer
    val sensorReaderActor = context.actorOf(Props(new PubSubActor("SensorReader")), name="sensor-reader")
    val consumerActor     = context.actorOf(Props(new PubSubActor("DataConsumer")), name="data-consumer")

    val subscribers = Array(consumerActor)
    sensorReaderActor ! new AddSubscribers(subscribers)

    sys.addShutdownHook({
          println("\nShutting down")
          subscribers.foreach(act => {
            println(s"Sending stop request to $act")
            context.stop(act)  //  shutdown()
            act ! new ExitMsg
          })
          // context.shutdown()
          println("If needed, free resources here.") // Free resources here
        }) // ,
    // "Shutdown Hook")

    sensorReaderActor ! new StartProducing
  }
}
