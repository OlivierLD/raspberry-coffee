import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

// Good reading at http://doc.akka.io/docs/akka/snapshot/scala/actors.html

class HelloActor extends Actor {
  def receive = {
    case "hello"      => println("hello back at you!")                                   // Object
    case x: String    => println(s"Unexpected String: $x")
    case Whatever(id) => println(s"Whatever message : $id")
    case TellMeSomething(s, i) =>
      var ret = ""
      (1 to i).foreach( x => {
        ret += (s + " ")
      })
      sender() ! ret
    case x: Any       => println("Got an un-managed " + x.getClass + " : " + x.toString) // Class
    case _            => println("huh? what?")                                           // Anything else... (like what?)
  }
}

case class Whatever(id: String)
case class Bullshit(moo: String)
case class TellMeSomething(what: String, repeat: Int)

object HelloActor extends App {
  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val helloActor = system.actorOf(Props[HelloActor](), name = "helloactor")
  // Sen various messages, see how they are received
  helloActor ! "hello"
  helloActor ! "buenos dias"
  helloActor ! Whatever("Watafok")
  helloActor ! Bullshit("Moo!")

  // Now, expect a response.
  implicit val timeout = Timeout(5 seconds)
  val future = helloActor ? TellMeSomething("Miom", 4)
  val result = Await.result(future, Duration.Inf).asInstanceOf[String] // Wait forever
  println(">> Result is: " + result)

  // Done!
  system.shutdown()
}
