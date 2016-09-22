package hydra.utils

// import _root_.scala.collection.mutable
import scala.collection.mutable

object DrillTestV2 {
  val verbose = "true".equals(System.getProperty("verbose", "false"))
  object data {
    val cUS = List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    val cCA = List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A")

    val site1 = List("A", "b", "C", "D", "1", "2", "3", "W", "X", "Z", "w")
    val site2 = List("A", "Q", "D", "z", "0", "M", "N", "O", "P", "_", "H", "i", "w")
    val site3 = List("A", "D", "R", "r", "s", "T", "u", "V", "W", "X", "y", "z", "w")

    val cat10 = List("N", "O", "P", "q", "r", "s", "T", "U", "v", "w")
    val cat20 = List("A", "n", "_", "0", "c", "D", "E", "F", "G", "J", "*", "$")

    val stuffX = List("9", "8", "7", "6", "5", "4", "3", "2", "1", "Q", "W", "E", "R", "T", "Y", "*", "A")
    val stuffY = List("Q", "W", "E", "R", "T", "Y")
  }

  var counters = mutable.Map.empty[String, List[String]]

  counters += ("COUNTRY:US" -> data.cUS)
  counters += ("COUNTRY:CA" -> data.cCA)
  counters += ("SITE:1"     -> data.site1)
  counters += ("SITE:2"     -> data.site2)
  counters += ("SITE:3"     -> data.site3)
  counters += ("CAT:10"     -> data.cat10)
  counters += ("CAT:20"     -> data.cat20)
  counters += ("STUFF:X"    -> data.stuffX)
  counters += ("STUFF:Y"    -> data.stuffY)

  val groupBy = "COUNTRY, SITE, CAT"
  //val groupBy = "COUNTRY, SITE"
  //val groupBy = "SITE, COUNTRY, CAT"
  //val groupBy = "COUNTRY, SITE, CAT, STUFF"
  //val groupBy = "COUNTRY, STUFF"

  var leafCounters   = mutable.Map.empty[String, List[Any]]
  var brokenDownData = mutable.Map.empty[String, Any]

  private def lpad(str:String, pad:String, len:Int):String = {
    var s = str
    while (s.length < len)
      s = pad + s
    s
  }

  private def listToString(list:List[String]): String = {
    var str = ""
    list.foreach(s => {
      str += ((if (str.length > 0) "," else "") + s)
    })
    str
  }

  private def stringToList(str:String): List[String] = {
    val array = str.split(",")
    var lst = List.empty[String]
    array.foreach(s => {
      lst :+= s.trim
    })
    lst
  }

  private def inter(listOne: List[String], listTwo: List[String]): List[String] = {
    var resultList = List.empty[String]
    listOne.foreach( s => {
      if (listTwo.contains(s))
        resultList :+= s
    })
    resultList
  }

  private def union(listOne: List[String], listTwo: List[String]): List[String] = {
    var resultList = List.empty[String]
    listOne.foreach( s => {
      if (!resultList.contains(s))
        resultList :+= s
    })
    listTwo.foreach( s => {
      if (!resultList.contains(s))
        resultList :+= s
    })
    resultList
  }

  // Closure on leafCounters
  def buildLeaves(breakDownStep: Array[String], level: Int, counters: mutable.Map[String, List[String]], parent: List[String] = List.empty[String]) : Unit = {
    counters.foreach( k => {
      if (k._1.startsWith(breakDownStep(level).trim)) {
        if (level == (breakDownStep.length - 1)) {
          //  println(s"On a leaf, Parent: $parent, ${breakDownStep(level)}")
          // Leaf. Intersection, from leaf to root
          var leafList:List[String] = null // List.empty[String]
          parent.foreach(p => {
            val parentList = counters(p)
            //          leafList = if (leafList.isEmpty) parentList else inter(leafList, parentList)
            leafList = if (leafList == null) parentList else inter(leafList, parentList)
            //    println(s" == On a leaf, Parent $p : parentList: $parentList, LeafList: $leafList")
          })
          //        leafList = if (leafList.isEmpty) k._2 else inter(leafList, k._2)
          leafList = if (leafList == null) k._2 else inter(leafList, k._2)
          //        println(s"-> ${lpad("", " ", (level * 2))} - Leaf Result for /$parent/${k._1} = $leafList, ${leafList.size} distinct value(s)")
          val coordinates = parent :+ k._1
          //  println(s"Coordinates:$coordinates => $leafList")
          leafCounters += (listToString(coordinates) -> leafList.asInstanceOf[List[Any]])
        } else {
          // Keep drilling down
          buildLeaves(breakDownStep, level + 1, counters, parent :+ k._1)
        }
      }
    })
  }

  def buildTree(): Unit = {
    leafCounters.foreach( line => {
      val coordinates = stringToList(line._1)
      val leafList    = line._2

      var parentMap = brokenDownData
      var parentName = "" // root

      var currMap:Any = null
      coordinates.foreach( k => {
        if (parentName.equals("")) { // Root
          if (verbose)
            println(s" ======== AT THE ROOT for $k")
          try {
            currMap = parentMap(k)
            //      println(s"    >>>> Found root for $k")
          } catch {
            case ex: NoSuchElementException =>
              currMap = mutable.Map.empty[String, Any]
              brokenDownData += (k -> currMap)
          }
          parentName = k
          parentMap  = brokenDownData
        } else {
          if (!k.equals(coordinates.last)) { // Intermediate, between the root and a leaf
            if (verbose)
              println(s" ============= Inter Node for $k ====")
            var found = false
            var map: mutable.Map[String, Any] = null
            try {
              currMap = parentMap(parentName)
              currMap.asInstanceOf[mutable.Map[String, Any]].foreach(occ => {
                //            println(s"Looking for $k in $occ")
                try {
                  val key = occ._1
                  if (k.equals(key)) {
                    found = true
                    map = occ._2.asInstanceOf[mutable.Map[String, Any]]
                    if (verbose)
                      println(s"  ==== >>> Break. found $k under $parentName")
                  }
                } catch {
                  case ex: NoSuchElementException =>
                  // println(s"$k not found")
                }
              })
            } catch {
              case nsee: NoSuchElementException =>
                println(s"No $parentName found in $parentMap")
            }
            if (!found) {
              val nextMap = mutable.Map.empty[String, Any]
              currMap.asInstanceOf[mutable.Map[String, Any]] += (k -> nextMap)
              parentMap += (parentName -> currMap.asInstanceOf[mutable.Map[String, Any]])
              if (verbose)
                println(s"  ----> Added a $k node under $parentName tree $brokenDownData ")
              parentMap = currMap.asInstanceOf[mutable.Map[String, Any]]
              currMap = nextMap
            } else {
              currMap = map
            }
            parentName = k
          } else { // This is a Leaf
            if (verbose)
              println(s" ===========> Leaf Node for $k, $leafList ====")
            currMap.asInstanceOf[mutable.Map[String, Any]] += (k -> leafList.asInstanceOf[List[Any]])
            if (verbose)
              println(s"  ---->> Added a leaf $k under $parentName in the tree, $leafList") // ${breakDownCounters} [parent map ${parentMap}]")
          }
        }
      })
    })
  }

  def drillDownPrint(map:mutable.Map[String, Any], level:Int = 0): Unit = {
    map.foreach( tuple => {
      tuple._2 match {
        case node: mutable.Map[String, Any] =>
          println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}")
          drillDownPrint(node, level + 1)
        case leaf:List[Any] => // Leaf
          println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t$leaf, ${leaf.size} distinct element(s)")
        case _ =>
          println("Unexpected type")
      }
    })
  }

  val UNION        = 1
  val INTERSECTION = 2
  val ALG_SUM      = 3

  def drillDownCount(map:mutable.Map[String, Any], level:Int = 0, aggType:Int = UNION): Unit = {
    map.foreach( tuple => {
      tuple._2 match {
        case node: mutable.Map[String, Any] =>
          aggType match {
            case UNION =>
              val aggList = unionSubNodes(node)
              println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t$aggList, ${aggList.size} element(s) (UNION)")
            case INTERSECTION =>
              val aggList = interSubNodes(node)
              println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t$aggList, ${aggList.size} element(s) (INTERSECTION)")
            case ALG_SUM =>
              val count = algrebraicSumSubNodes(node)
              println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t$count element(s) (ALGEBRAIC SUM)")
          }
          drillDownCount(node, level + 1, aggType)
        case leaf:List[Any] => // Leaf
          println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t$leaf, ${leaf.size} distinct element(s)")
        case _ =>
          println("Unexpected type")
      }
    })
  }

  def unionSubNodes(node:mutable.Map[String, Any]):List[String] = {
    var unionedList = List.empty[String]
    node.foreach( sub => {
      if (sub._2.isInstanceOf[List[Any]]) { // there we are
        unionedList = union(unionedList, sub._2.asInstanceOf[List[String]])
      } else {
        val list = unionSubNodes(sub._2.asInstanceOf[mutable.Map[String, Any]])
        unionedList = union(unionedList, list)
      }
    })
    unionedList
  }

  def interSubNodes(node:mutable.Map[String, Any]):List[String] = {
    var intersectedList = List.empty[String]
    node.foreach( sub => {
      if (sub._2.isInstanceOf[List[Any]]) { // there we are
        intersectedList = inter(intersectedList, sub._2.asInstanceOf[List[String]])
      } else {
        val list = interSubNodes(sub._2.asInstanceOf[mutable.Map[String, Any]])
        intersectedList = inter(intersectedList, list)
      }
    })
    intersectedList
  }

  def algrebraicSumSubNodes(node:mutable.Map[String, Any]):Long = {
    var sum:Long = 0
    node.foreach( sub => {
      if (sub._2.isInstanceOf[List[Any]]) { // there we are
        sum += (sub._2.asInstanceOf[List[String]]).size
      } else {
        sum += algrebraicSumSubNodes(sub._2.asInstanceOf[mutable.Map[String, Any]])
      }
    })
    sum
  }

  def main(args:Array[String]): Unit = {
    println(" -- Original counters --")
    counters.foreach( map => {
      println(s"${map._1} => ${map._2}, ${map._2.size} element(s).")
    })
    buildLeaves(groupBy.split(","), 0, counters) // Closure on leafCounters
    println("--- buildLeaves - cartesian product, intersections ---")
    leafCounters.foreach(t => {
      println(s"${t._1} -> ${t._2}, ${t._2.size} value(s)")
    })
    buildTree()                                  // Closure on brokenDownData
    println("======================================")
    println(brokenDownData)
    println("--- formatted ---")
    drillDownPrint(brokenDownData)
    println("======================================")
    println(" -- With counts -- (Union) --")
    drillDownCount(brokenDownData)
    println("======================================")
    println(" -- With counts -- (Intersection) --")
    drillDownCount(brokenDownData, 0, INTERSECTION)
    println("======================================")
    println(" -- With counts -- (Alg Sum) --")
    drillDownCount(brokenDownData, 0, ALG_SUM)
  }
}
