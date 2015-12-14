import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.util.Random

object WaitReleaseSample {
  def main(args: Array[String]):Unit = {

    val me = Thread currentThread

    var completed = false
    // Not too exciting, the result will always be 42. but more importantly, when?
    println("1 - starting calculation ...")
    val f = Future {
      sleep(Random.nextInt(5000))
      completed = true
      me.synchronized {
        me.notify
      }
      println("Work is completed")
      42 // Hard coded returned value
    }

    println("2 - before onComplete")
    f.onComplete {
      case Success(value) => println(s"Got the callback, meaning = $value")
      case Failure(e) => e.printStackTrace
    }

    var i = 0
    while (i < 10 && !completed) {
      println(s"Wait loop # ${i + 1}")
      me.synchronized({
        try {
          me.wait(500)
        } catch {
          case ex: Exception =>
            ex.printStackTrace
        }
      })
      i += 1
    }
    println("Loop bottom")
    if (completed) {
      println("Work is completed in time")
      me.synchronized({
        try {
          me.wait(1000) // Just to finish the printouts
        } catch {
          case ex: Exception =>
            ex.printStackTrace
        }
      })
    } else {
      println("Work was NOT completed in time")
      me.synchronized({
        try {
          me.wait
        } catch {
          case ex: Exception =>
            ex.printStackTrace
        }
      })
    }
  }

  def sleep(duration: Long) { Thread.sleep(duration) }
}
