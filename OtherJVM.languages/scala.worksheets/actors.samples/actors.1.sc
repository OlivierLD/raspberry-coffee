// From http://danielwestheide.com/blog/2013/02/27/the-neophytes-guide-to-scala-part-14-the-actor-approach-to-concurrency.html

import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.ask


/// STEP 1 ///
val system = ActorSystem("Barista")

//object Barista extends App {
//  val system = ActorSystem("Barista")
//  system.shutdown()
//}

sealed trait CoffeeRequest
case object CappuccinoRequest extends CoffeeRequest
case object EspressoRequest extends CoffeeRequest

import akka.actor.Actor

class Barista extends Actor {
  def receive = {
    case CappuccinoRequest => println("I have to prepare a cappuccino!")
    case EspressoRequest => println("Let's prepare an espresso.")
  }
}

import akka.actor.{ActorRef, Props}
val barista: ActorRef = system.actorOf(Props[Barista], "Barista")

barista ! CappuccinoRequest
barista ! EspressoRequest
println("I ordered a cappuccino and an espresso")

/// STEP 2 ///

case class Bill(cents: Int)
case object ClosingTime
class Barista2 extends Actor {
  def receive = {
    case CappuccinoRequest =>
      sender ! Bill(250)
      println("I have to prepare a cappuccino!")
    case EspressoRequest =>
      sender ! Bill(200)
      println("Let's prepare an espresso.")
    case ClosingTime =>
      println("Shutting down")
      context.system.shutdown()
  }
}

case object CaffeineWithdrawalWarning

class Customer(caffeineSource: ActorRef) extends Actor {
  def receive = {
    case CaffeineWithdrawalWarning => caffeineSource ! EspressoRequest
    case Bill(cents) => println(s"I have to pay $cents cents, or else!")
  }
}

val barista2 = system.actorOf(Props[Barista2], "Barista2")
val customer = system.actorOf(Props(classOf[Customer], barista2), "Customer")
customer ! CaffeineWithdrawalWarning
barista2 ! ClosingTime

/// STEP 3 ///

// val system = ActorSystem("Barista")

class Barista3 extends Actor {
  var cappuccinoCount = 0
  var espressoCount = 0
  def receive = {
    case CappuccinoRequest =>
      sender ! Bill(250)
      cappuccinoCount += 1
      println(s"I have to prepare cappuccino #$cappuccinoCount")
    case EspressoRequest =>
      sender ! Bill(200)
      espressoCount += 1
      println(s"Let's prepare espresso #$espressoCount.")
    case ClosingTime => context.system.shutdown()
  }
}

val barista3 = system.actorOf(Props[Barista3], "Barista3")

implicit val timeout = Timeout(2.second)
implicit val ec      = system.dispatcher
val f: Future[Any] = barista3 ? CappuccinoRequest
f.onSuccess {
  case Bill(cents) => println(s"Will pay $cents cents for a cappuccino")
}

/// END OF STUFF ///