import akka.actor._
import akka.pattern.ask
import akka.util.{Timeout}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

// Do check https://doc.akka.io/docs/akka/current/typed/actors.html

case object AskNameMessage

class TestActor extends Actor {
  def receive: Receive = {
    case AskNameMessage => // respond to the 'ask' request
      try { Thread.sleep(2000L); } catch { case ex: Exception => ex.printStackTrace }
      println("Now releasing.")
      sender() ! "Oliv"
    case _ => println("that was unexpected")
  }
}

object Actors_101 extends App {

  // create the system and actor
  val system = ActorSystem("AskTestSystem")
  val olivActor = system.actorOf(Props[TestActor](), name = "olivActor")

  // (1) this is one way to "ask" another actor for information
  implicit val timeout: Timeout = Timeout(5 seconds)
  val future = olivActor ? AskNameMessage

//val result = Await.result(future, timeout.duration).asInstanceOf[String]
  val result = Await.result(future, Duration.Inf).asInstanceOf[String]
  println(">> Result is: " + result)

  // (2) a slightly different way to ask another actor for information
  val future2: Future[String] = ask(olivActor, AskNameMessage).mapTo[String]
  val result2 = Await.result(future2, 3 seconds)
  println(">> Second attempt: " + result2)

  system.stop(olivActor)   // .shutdown()
}
