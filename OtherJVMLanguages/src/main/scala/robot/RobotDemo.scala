package robot

import i2c.samples.motorHAT.Robot

object RobotDemo {
  def main(args:Array[String]):Unit = {
    val robot = new Robot

    println("Forward")
    robot.forward(150, 1.0f)
    println("Left")
    robot.left(200, 0.5f)
    println("Forward")
    robot.forward(150, 1.0f)
    println("Left")
    robot.left(200, 0.5f)
    println("Forward")
    robot.forward(150, 1.0f)
    println("Left")
    robot.left(200, 0.5f)
    println("Forward")
    robot.forward(150, 1.0f)
    println("Right")
    robot.right(200, 0.5f)

    // Spin in place slowly for a few seconds.
    println("Right...")
    robot.right(100)
    Robot.delay(2.0f)
    robot.stop

    // Now move backwards and spin right a few times.
    println("Backward")
    robot.backward(150, 1.0f)
    println("Right")
    robot.right(200, 0.5f)
    println("Backward")
    robot.backward(150, 1.0f)
    println("Right")
    robot.right(200, 0.5f)
    println("Backward")
    robot.backward(150, 1.0f)
    println("Right")
    robot.right(200, 0.5f)
    println("Backward")
    robot.backward(150, 1.0f)

    println("That's it!")
  }
}
