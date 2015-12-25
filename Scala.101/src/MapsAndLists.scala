import scala.collection.mutable._

object MapsAndLists {

  val verbose = false

  var counters = Map.empty[String, MutableList[String]]

  val c1 = MutableList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
  val c2 = MutableList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A")

  val site1 = MutableList("A", "b", "C", "D", "1", "2", "3", "W", "X", "Z", "w")
  val site2 = MutableList("A", "Q", "D", "z", "0", "M", "N", "O", "P", "_", "H", "i", "w")
  val site3 = MutableList("A", "D", "R", "r", "s", "T", "u", "V", "W", "X", "y", "z", "w")

  val cat1 = MutableList("N", "O", "P", "q", "r", "s", "T", "U", "v", "w")
  val cat2 = MutableList("A", "n", "_", "0", "c", "D", "E", "F", "G", "J")

  counters += ("COUNTRY:US" -> c1)
  counters += ("COUNTRY:CA" -> c2)
  counters += ("SITE:1"     -> site1)
  counters += ("SITE:2"     -> site2)
  counters += ("SITE:3"     -> site3)
  counters += ("CAT:10"     -> cat1)
  counters += ("CAT:20"     -> cat2)

  val groupBy = "COUNTRY, SITE, CAT"

  var leafCounters   = Map.empty[String, MutableList[Any]]
  var brokenDownData = Map.empty[String, Any]

  def lpad(str:String, pad:String, len:Int):String = {
    var s = str
    while (s.length < len)
      s = pad + s
    s
  }

  def listToString(list:MutableList[String]): String = {
    var str = ""
    list.foreach(s => {
      str += ((if (str.length > 0) "," else "") + s)
    })
    str
  }

  def stringToList(str:String): MutableList[String] = {
    val array = str.split(",")
    var lst = MutableList.empty[String]
    array.foreach(s => {
      lst :+= s.trim
    })
    lst
  }

  private def inter(listOne: MutableList[String], listTwo: MutableList[String]): MutableList[String] = {
    var resultList = MutableList.empty[String]
    listOne.foreach( s => {
      if (listTwo.contains(s))
        resultList :+= s
    })
    resultList
  }

  private def union(listOne: MutableList[String], listTwo: MutableList[String]): MutableList[String] = {
    var resultList = MutableList.empty[String]
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

  def buildLeaves(breakDownStep: Array[String], level: Int, counters: Map[String, MutableList[String]], parent: MutableList[String] = MutableList.empty[String]) : Unit = {
    counters.foreach( k => {
      if (k._1.startsWith(breakDownStep(level).trim)) {
        if (level == (breakDownStep.length - 1)) {
          // Leaf. Intersection, from leaf to root
          var leafList = MutableList.empty[String]
          parent.foreach(p => {
            val parentList = counters(p)
            leafList = if (leafList.isEmpty) parentList else inter(leafList, parentList)
          })
          leafList = if (leafList.isEmpty) k._2 else inter(leafList, k._2)
          //        println(s"-> ${lpad("", " ", (level * 2))} - Leaf Result for /${parent}/${k._1} = ${leafList}, ${leafList.size} distinct value(s)")
          val coordinates = parent :+ k._1
          //  println(s"Coordinates:${coordinates} => ${leafList}")
          leafCounters += (listToString(coordinates) -> leafList.asInstanceOf[MutableList[Any]])
        } else {
          // Keep drilling down
          buildLeaves(breakDownStep, level + 1, counters, parent :+ k._1)
        }
      }
    })
  }

  def buildTree: Unit = {
    leafCounters.foreach( line => {
      val coordinates = stringToList(line._1)
      val leafList    = line._2

      var parentMap = brokenDownData
      var parentName = "" // root

      var currMap:Any = null
      coordinates.foreach( k => {
        if (parentName.equals("")) { // Root
          if (verbose)
            println(s" ======== AT THE ROOT for ${k}")
          try {
            currMap = parentMap(k)
    //      println(s"    >>>> Found root for ${k}")
          } catch {
            case ex: NoSuchElementException => {
              currMap = Map.empty[String, Any]
              brokenDownData += (k -> currMap)
            }
          }
          parentName = k
          parentMap  = brokenDownData
        } else {
          if (!k.equals(coordinates(coordinates.length - 1))) { // Intermediate
            if (verbose)
              println(s" ============= Inter Node for ${k} ====")
            currMap = parentMap(parentName)
            var found = false
            var map:Map[String, Any] = null
            currMap.asInstanceOf[Map[String, Any]].foreach( occ => {
//            println(s"Looking for ${k} in ${occ}")
              try {
                val key = occ._1 // .asInstanceOf[Map[String, Any]]
                if (k.equals(occ._1)) {
                  found = true
                  map = occ._2.asInstanceOf[Map[String, Any]]
                  if (verbose)
                    println(s"  ==== >>> Break. found ${k} under ${parentName}")
                }
              } catch {
                case ex: NoSuchElementException =>
        //        println(s"${k} not found")
              }
            })
            if (!found) {
              var nextMap = Map.empty[String, Any]
              currMap.asInstanceOf[Map[String, Any]] += (k -> nextMap)
              parentMap += (parentName -> currMap.asInstanceOf[Map[String, Any]])
              if (verbose)
                println(s"  ----> Added a ${k} node under ${parentName} tree ${brokenDownData} ")
              parentMap = currMap.asInstanceOf[Map[String, Any]]
              currMap = nextMap
            } else {
              currMap = map
            }
            parentName = k
          } else { // This is a Leaf
            if (verbose)
              println(s" ===========> Leaf Node for ${k}, ${leafList} ====")
            currMap.asInstanceOf[Map[String, Any]] += (k -> leafList.asInstanceOf[MutableList[Any]])
            if (verbose)
              println(s"  ---->> Added a leaf ${k} under ${parentName} in the tree, ${leafList}") // ${breakDownCounters} [parent map ${parentMap}]")
          }
        }
      })
    })
  }

  def drillDownCount(map:Map[String, Any], level:Int = 0): Unit = {
    map.foreach( tuple => {
      if (tuple._2.isInstanceOf[Map[String, Any]]) {
        val uList = unionSubNodes(tuple._2.asInstanceOf[Map[String, Any]])
        println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t${uList}, ${uList.size} element(s)")
        drillDownCount(tuple._2.asInstanceOf[Map[String, Any]], level + 1)
      } else { // Leaf
        if (tuple._2.isInstanceOf[MutableList[Any]]) {
          val idList = tuple._2.asInstanceOf[MutableList[Any]]
          println(s"${lpad("+-", " ", 2 * (level + 1))} Level ${level + 1} => \t${tuple._1}: \t${idList}, ${idList.size} distinct element(s)")
        }
      }
    })
  }

  def unionSubNodes(node:Map[String, Any]):MutableList[String] = {
    var unionedList = MutableList.empty[String]
    node.foreach( sub => {
      if (sub._2.isInstanceOf[MutableList[Any]]) { // there we are
        unionedList = union(unionedList, sub._2.asInstanceOf[MutableList[String]])
      } else {
        val list = unionSubNodes(sub._2.asInstanceOf[Map[String, Any]])
        unionedList = union(unionedList, list)
      }
    })
    unionedList
  }

  def main(args:Array[String]): Unit = {

    buildLeaves(groupBy.split(","), 0, counters) // Closure on leafCounters
    leafCounters.foreach(t => {
      println(s"${t._1} -> ${t._2}")
    })

    buildTree                                    // Closure on brokenDownData
    println("======================================")
    println(brokenDownData)
    println("======================================")

    drillDownCount(brokenDownData)
  }
}
