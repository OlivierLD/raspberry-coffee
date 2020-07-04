package xml

import java.io.{File, FileReader, FileWriter, IOException, InputStream, PrintWriter}
import java.net.URL
import java.util.{Hashtable}

import oracle.xml.parser.schema.{XMLSchema, XSDBuilder}
import oracle.xml.parser.v2.{DOMParser, XMLParser, XMLConstants, NSResolver, XMLDocument, XMLElement, XSLProcessor, XSLStylesheet}
import org.w3c.dom.{Element, Node, NodeList, Text}

object XMLParserSample03 {
  private var startFrom: Int = 0
  private val SCHEMA_LOCATION: String = "xml" + File.separator + "wireframe.xsd"
  private val NAMESPACE: String = "http://donpedro.lediouris.net/wireframe"

  private[xml] var nsHash: Hashtable[String, String] = null

  private[xml] class CustomResolver private[xml]() extends NSResolver {
    nsHash = new Hashtable[String, String]

    override def resolveNamespacePrefix(prefix: String): String = {
      nsHash.get(prefix)
    }

    def addNamespacePrefix(prefix: String, ns: String): Unit = {
      nsHash.put(prefix, ns)
    }
  }


  def generate(fName: String): Int = {
    generate(fName, null, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, 0)
  }

  def generate(fName: String, df: FileWriter): Int = {
    generate(fName, df, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, 0)
  }

  def generate(fName: String, destFile: FileWriter, addX: Double, addY: Double, addZ: Double, affX: Double, affY: Double, affZ: Double, startOffset: Int): Int = {
    var sourceDir: File = null
    var source: File = null
    startFrom = startOffset
    val parser: DOMParser = new DOMParser
    try {
      val validatorStream: URL = // new XMLParserSampleThree().getClass().getResource(SCHEMA_LOCATION);
        new File(SCHEMA_LOCATION).toURI.toURL
      if (validatorStream == null) {
        throw new RuntimeException(String.format("Problem finding %s", SCHEMA_LOCATION))
      }
      source = new File(fName)
      val docToValidate: URL = source.toURI.toURL
      sourceDir = source.getParentFile
      parser.showWarnings(true)
      parser.setErrorStream(System.out)
      parser.setValidationMode(XMLConstants.SCHEMA_VALIDATION)
      parser.setPreserveWhitespace(true)
      val xsdBuilder: XSDBuilder = new XSDBuilder
      val is: InputStream = validatorStream.openStream
      val xmlSchema: XMLSchema = xsdBuilder.build(is, null).asInstanceOf[XMLSchema]
      parser.setXMLSchema(xmlSchema)
      val doc: URL = docToValidate
      parser.parse(doc)
      /* XMLDocument valid = */ parser.getDocument
      if ("true" == System.getProperty("verbose", "false")) {
        println(s"In ObjectMaker - ${source.getName} is valid")
      }
    } catch {
      case ex: Exception =>
        println(s"${source.getName} is invalid...")
        ex.printStackTrace()
      //          System.exit(1);
    }
    try {
      parser.parse(new FileReader(fName))
      val doc: XMLDocument = parser.getDocument
      var fw: FileWriter = null
      if (destFile == null) {
        val outputName: String = doc.getDocumentElement.getAttribute("name")
        val outputLocation: String = fName.substring(0, fName.lastIndexOf(File.separator) + 1)
        if ("true" == System.getProperty("verbose", "false")) {
          println(s"Name:${outputName}")
          println(s"Output: [${outputLocation + outputName}.obj]")
        }
        val file: File = new File(outputLocation + outputName + ".obj")
        fw = new FileWriter(file)
        fw.write("# File " + outputName + "\n")
      }
      else {
        fw = destFile
      }
      val resolver: XMLParserSampleThree.CustomResolver = new XMLParserSampleThree.CustomResolver
      resolver.addNamespacePrefix("wf", NAMESPACE)
      var nl: NodeList = doc.selectNodes("/wf:data/wf:keel/wf:plot", resolver)
      fw.write("# Keel\n")
      var nbElements: Int = 0
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          val node: Node = nl.item(i)
          val x: Double = (node.asInstanceOf[Element]).getAttribute("x").toDouble
          val childs: NodeList = node.getChildNodes
          var z: Double = 0.0D
          if (childs != null) {
            for (j <- 0 until childs.getLength) {
              val kid: Node = childs.item(j)
              if (kid.getNodeType == Node.ELEMENT_NODE) {
                z = kid.getFirstChild.getNodeValue.toDouble
              }
            }
          }
          fw.write("v " + (affX * x + addX) + " " + addY + " " + (affZ * z + addZ) + "\n")
          nbElements += 1
        }
        for (i <- 0 until nbElements - 1) {
          fw.write("f " + {
            startFrom += 1; startFrom
          } + " " + (startFrom + 1) + "\n")
        }
        if (nbElements > 0) {
          startFrom += 1
        }
      }
      fw.write("# Deck\n")
      nl = doc.selectNodes("/wf:data/wf:deck/wf:part", resolver)
      nbElements = 0
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          val pl: NodeList = (nl.item(i).asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "plot")
          if (pl != null) {
            for (j <- 0 until pl.getLength) {
              val node: Node = pl.item(j)
              val x: Double = (node.asInstanceOf[Element]).getAttribute("x").toDouble
              val childs: NodeList = node.getChildNodes
              var z: Double = 0.0D
              if (childs != null) {
                for (k <- 0 until childs.getLength) {
                  val kid: Node = childs.item(k)
                  if (kid.getNodeType == Node.ELEMENT_NODE) {
                    z = kid.getFirstChild.getNodeValue.toDouble
                    nbElements += 1
                  }
                }
              }
              fw.write("v " + (x * affX + addX) + " " + addY + " " + (z * affZ + addZ) + "\n")
            }
            for (j <- 0 until nbElements - 1) {
              fw.write("f " + {
                startFrom += 1; startFrom
              } + " " + (startFrom + 1) + "\n")
            }
            if (nbElements > 0) {
              startFrom += 1
            }
          }
        }
      }
      nl = doc.selectNodes("/wf:data/wf:sheer/wf:plot", resolver)
      nbElements = 0
      if (nl != null) {
        fw.write("# Sheer One\n")
        for (i <- 0 until nl.getLength) {
          val node: Node = nl.item(i)
          if (node.getNodeType == Node.ELEMENT_NODE && (node.getNodeName eq "plot")) {
            val x: Double = (node.asInstanceOf[Element]).getAttribute("x").toDouble
            val y: Double = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild.getNodeValue.toDouble
            val z: Double = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild.getNodeValue.toDouble
            fw.write("v " + (x * affX + addX) + " " + (y * affY + addY) + " " + (z * affZ + addZ) + "\n")
            nbElements += 1
          }
        }
        for (i <- 0 until nbElements - 1) {
          fw.write("f " + {
            startFrom += 1; startFrom
          } + " " + (startFrom + 1) + "\n")
        }
        if (nbElements > 0) {
          startFrom += 1
        }
        fw.write("# Sheer Two\n")
        nbElements = 0
        for (i <- 0 until nl.getLength) {
          val node: Node = nl.item(i)
          if (node.getNodeType == Node.ELEMENT_NODE && (node.getNodeName eq "plot")) {
            val x: Double = (node.asInstanceOf[Element]).getAttribute("x").toDouble
            val y: Double = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild.getNodeValue.toDouble
            val z: Double = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild.getNodeValue.toDouble
            fw.write("v " + (x * affX + addX) + " " + (addY - y * affY) + " " + (z * affZ + addZ) + "\n")
            nbElements += 1
          }
        }
        for (i <- 0 until nbElements - 1) {
          fw.write("f " + {
            startFrom += 1; startFrom
          } + " " + (startFrom + 1) + "\n")
        }
        if (nbElements > 0) {
          startFrom += 1
        }
      }
      nl = doc.selectNodes("/wf:data/wf:forms/wf:form", resolver)
      nbElements = 0
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          val node: Node = nl.item(i)
          val x: Double = (node.asInstanceOf[Element]).getAttribute("x").toDouble
          fw.write("# Form " + (addX + x) + "\n")
          val plots: NodeList = node.getChildNodes
          nbElements = 0
          for (j <- 0 until plots.getLength) {
            if (plots.item(j).getNodeType == Node.ELEMENT_NODE) {
              val yz: NodeList = plots.item(j).getChildNodes
              var y: Double = Double.MinValue
              var z: Double = Double.MinValue
              for (k <- 0 until yz.getLength) {
                if (yz.item(k).getNodeType == Node.ELEMENT_NODE) {
                  var yzKids: NodeList = null
                  if (yz.item(k).getNodeName == "y") {
                    yzKids = yz.item(k).getChildNodes
                    var l: Int = 0
                    var keepGoing: Boolean = true
                    do {
                      {
                        if (l >= yzKids.getLength) {
                          keepGoing = false
                        } else {
                          if (yzKids.item(l).getNodeType == Node.TEXT_NODE) {
                            y = yzKids.item(l).getNodeValue.toDouble
                          }
                          l += 1
                        }
                      }
                    } while (keepGoing)
                  }
                  if (!(yz.item(k).getNodeName == "z")) {
                    yzKids = yz.item(k).getChildNodes
                    for (l <- 0 until yzKids.getLength) {
                      if (yzKids.item(l).getNodeType == Node.TEXT_NODE) {
                        z = yzKids.item(l).getNodeValue.toDouble
                      }
                    }
                  }
                }
              }
              if (y != Double.MinValue && z != Double.MinValue) {
                fw.write("v " + (x * affX + addX) + " " + (y * affY + addY) + " " + (z * affZ + addZ) + "\n")
                nbElements += 1
              }
            }
          }
          for (j <- 0 until nbElements - 1) {
            fw.write("f " + {
              startFrom += 1; startFrom
            } + " " + (startFrom + 1) + "\n")
          }
          if (nbElements > 0) {
            startFrom += 1
          }
          nbElements = 0
          for (j <- 0 until plots.getLength) {
            val yz: NodeList = plots.item(j).getChildNodes
            var y: Double = Double.MinValue
            var z: Double = Double.MinValue
            for (k <- 0 until yz.getLength) {
              if (yz.item(k).getNodeType == Node.ELEMENT_NODE) {
                var yzKids: NodeList = null
                if (yz.item(k).getNodeName == "y") {
                  yzKids = yz.item(k).getChildNodes
                  var l: Int = 0
                  var keepGoing: Boolean = true
                  do {
                    {
                      if (l >= yzKids.getLength) {
                        keepGoing = false
                      } else {
                        if (yzKids.item(l).getNodeType == Node.TEXT_NODE) {
                          y = yzKids.item(l).getNodeValue.toDouble
                        }
                        l += 1
                      }
                    }
                  } while (keepGoing)
                }
                if (yz.item(k).getNodeName == "z") {
                  yzKids = yz.item(k).getChildNodes
                  for (l <- 0 until yzKids.getLength) {
                    if (yzKids.item(l).getNodeType == Node.TEXT_NODE) {
                      z = yzKids.item(l).getNodeValue.toDouble
                    }
                  }
                }
              }
            }
            if (y != Double.MinValue && z != Double.MinValue) {
              fw.write("v " + (addX + x * affX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n")
              nbElements += 1
            }
          }
          for (j <- 0 until nbElements - 1) {
            fw.write("f " + {
              startFrom += 1; startFrom
            } + " " + (startFrom + 1) + "\n")
          }
          if (nbElements > 0) {
            startFrom += 1
          }
        }
      }
      nl = doc.selectNodes("/wf:data/wf:waterlines/wf:wl", resolver)
      nbElements = 0
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          val node: Node = nl.item(i)
          if (node.getNodeType == Node.ELEMENT_NODE && node.getNodeName == "wl") {
            val z: Double = (node.asInstanceOf[Element]).getAttribute("z").toDouble
            fw.write("# WaterLine " + (addZ + z) + "\n")
            val plots: NodeList = node.getChildNodes
            nbElements = 0
            for (j <- 0 until plots.getLength) {
              val xy: NodeList = plots.item(j).getChildNodes
              var x: Double = Double.MinValue
              var y: Double = Double.MinValue
              for (k <- 0 until xy.getLength) {
                if (xy.item(k).getNodeType == Node.ELEMENT_NODE) {
                  var xyKids: NodeList = null
                  if (xy.item(k).getNodeName == "x") {
                    xyKids = xy.item(k).getChildNodes
                    var l: Int = 0
                    var keepGoing: Boolean = true
                    do {
                      {
                        if (l >= xyKids.getLength) {
                          keepGoing = false
                        } else {
                          if (xyKids.item(l).getNodeType == Node.TEXT_NODE) {
                            x = xyKids.item(l).getNodeValue.toDouble
                          }
                          l += 1
                        }
                      }
                    } while (keepGoing)
                  }
                  if (xy.item(k).getNodeName == "y") {
                    xyKids = xy.item(k).getChildNodes
                    for (l <- 0 until xyKids.getLength) {
                      if (xyKids.item(l).getNodeType == Node.TEXT_NODE) {
                        y = xyKids.item(l).getNodeValue.toDouble
                      }
                    }
                  }
                }
              }
              if (x != Double.MinValue && y != Double.MinValue) {
                fw.write("v " + (x * affX + addX) + " " + (y * affY + addY) + " " + (z * affZ + addZ) + "\n")
                nbElements += 1
              }
            }
            for (j <- 0 until nbElements - 1) {
              fw.write("f " + {
                startFrom += 1; startFrom
              } + " " + (startFrom + 1) + "\n")
            }
            if (nbElements > 0) {
              startFrom += 1
            }
            nbElements = 0
            for (j <- 0 until plots.getLength) {
              val xy: NodeList = plots.item(j).getChildNodes
              var x: Double = Double.MinValue
              var y: Double = Double.MinValue
              for (k <- 0 until xy.getLength) {
                if (xy.item(k).getNodeType == Node.ELEMENT_NODE) {
                  var xyKids: NodeList = null
                  if (xy.item(k).getNodeName == "x") {
                    xyKids = xy.item(k).getChildNodes
                    var l: Int = 0
                    var keepGoing: Boolean = true
                    do {
                      {
                        if (l >= xyKids.getLength) {
                          keepGoing = false
                        } else {
                          if (xyKids.item(l).getNodeType == Node.TEXT_NODE) {
                            x = xyKids.item(l).getNodeValue.toDouble
                          }
                          l += 1
                        }
                      }
                    } while (keepGoing)
                  }
                  if (xy.item(k).getNodeName == "y") {
                    xyKids = xy.item(k).getChildNodes
                    for (l <- 0 until xyKids.getLength) {
                      if (xyKids.item(l).getNodeType == Node.TEXT_NODE) {
                        y = xyKids.item(l).getNodeValue.toDouble
                      }
                    }
                  }
                }
              }
              if (x != Double.MinValue && y != Double.MinValue) {
                fw.write("v " + (x * affX + addX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n")
                nbElements += 1
              }
            }
            for (j <- 0 until nbElements - 1) {
              fw.write("f " + {
                startFrom += 1; startFrom
              } + " " + (startFrom + 1) + "\n")
            }
            if (nbElements > 0) {
              startFrom += 1
            }
          }
        }
      }
      nl = doc.selectNodes("/wf:data/wf:buttocks/wf:buttock", resolver)
      nbElements = 0
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          val node: Node = nl.item(i)
          val y: Double = (node.asInstanceOf[Element]).getAttribute("y").toDouble
          fw.write("# Buttock " + (addY + y) + "\n")
          val parts: NodeList = node.getChildNodes
          for (j <- 0 until parts.getLength) {
            if (parts.item(j).getNodeType == Node.ELEMENT_NODE) {
              nbElements = 0
              val plots: NodeList = parts.item(j).getChildNodes
              for (k <- 0 until plots.getLength) {
                if (plots.item(k).getNodeType == Node.ELEMENT_NODE) {
                  val xz: NodeList = plots.item(k).getChildNodes
                  var x: Double = 0.0D
                  var z: Double = 0.0D
                  for (l <- 0 until xz.getLength) {
                    if (xz.item(l).getNodeType == Node.ELEMENT_NODE) {
                      var lastOne: NodeList = null
                      var m: Int = 0
                      if (xz.item(l).getNodeName == "x") {
                        lastOne = xz.item(l).getChildNodes
                        m = 0
                        var keepGoing: Boolean = true
                        do {
                          {
                            if (m >= lastOne.getLength) {
                              keepGoing = false
                            } else {
                              if (lastOne.item(m).getNodeType == Node.TEXT_NODE) {
                                x = lastOne.item(m).getNodeValue.toDouble
                                keepGoing = false
                              }
                              m += 1
                            }
                          }
                        } while (keepGoing)
                      }
                      if (xz.item(l).getNodeName == "z") {
                        lastOne = xz.item(l).getChildNodes
                        m = 0
                        var keepGoing: Boolean = true
                        do {
                          {
                            if (m >= lastOne.getLength) {
                              keepGoing = false
                            } else {
                              if (lastOne.item(m).getNodeType == Node.TEXT_NODE) {
                                z = lastOne.item(m).getNodeValue.toDouble
                                keepGoing = false
                              }
                            }
                            m += 1
                          }
                        } while (keepGoing)
                      }
                    }
                  }
                  fw.write("v " + (addX + x * affX) + " " + (addY + y * affY) + " " + (addZ + z * affZ) + "\n")
                  nbElements += 1
                }
              }
              for (k <- 0 until nbElements - 1) {
                fw.write("f " + {
                  startFrom += 1; startFrom
                } + " " + (startFrom + 1) + "\n")
              }
              if (nbElements > 0) {
                startFrom += 1
              }
            }
          }
          nbElements = 0
          for (j <- 0 until parts.getLength) {
            if (parts.item(j).getNodeType == Node.ELEMENT_NODE) {
              nbElements = 0
              val plots: NodeList = parts.item(j).getChildNodes
              for (k <- 0 until plots.getLength) {
                if (plots.item(k).getNodeType == Node.ELEMENT_NODE) {
                  val xz: NodeList = plots.item(k).getChildNodes
                  var x: Double = 0.0D
                  var z: Double = 0.0D
                  for (l <- 0 until xz.getLength) {
                    if (xz.item(l).getNodeType == Node.ELEMENT_NODE) {
                      var lastOne: NodeList = null
                      var m: Int = 0
                      if (xz.item(l).getNodeName == "x") {
                        lastOne = xz.item(l).getChildNodes
                        m = 0
                        var keepGoing: Boolean = true
                        do {
                          {
                            if (m >= lastOne.getLength) {
                              keepGoing = false
                            } else {
                              if (lastOne.item(m).getNodeType == Node.TEXT_NODE) {
                                x = lastOne.item(m).getNodeValue.toDouble
                                keepGoing = false
                              }
                              m += 1
                            }
                          }
                        } while (keepGoing)
                      }
                      if (xz.item(l).getNodeName == "z") {
                        lastOne = xz.item(l).getChildNodes
                        m = 0
                        var keepGoing: Boolean = true
                        do {
                          {
                            if (m >= lastOne.getLength) {
                              keepGoing = false
                            }
                            if (lastOne.item(m).getNodeType == Node.TEXT_NODE) {
                              z = lastOne.item(m).getNodeValue.toDouble
                              keepGoing = false
                            }
                            m += 1
                          }
                        } while (keepGoing)
                      }
                    }
                  }
                  fw.write("v " + (addX + x * affX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n")
                  nbElements += 1
                }
              }
              for (k <- 0 until nbElements - 1) {
                fw.write("f " + {
                  startFrom += 1; startFrom
                } + " " + (startFrom + 1) + "\n")
              }
              if (nbElements > 0) {
                startFrom += 1
              }
            }
          }
        }
      }
      nl = doc.selectNodes("/wf:data/wf:modules/wf:module", resolver)
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          nbElements = 0
          val node: Node = nl.item(i)
          val name: String = (node.asInstanceOf[Element]).getAttribute("name")
          val sym: Boolean = (node.asInstanceOf[Element]).getAttribute("symetric") == "yes"
          fw.write("# Module " + name + "\n")
          val plots: NodeList = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "plot")
          if (plots != null) {
            for (j <- 0 until plots.getLength) {
              val plot: Element = plots.item(j).asInstanceOf[Element]
              val x: Double = plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "x").item(0).getFirstChild.getNodeValue.toDouble
              val y: Double = plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild.getNodeValue.toDouble
              val z: Double = plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild.getNodeValue.toDouble
              fw.write("v " + (addX + x * affX) + " " + (addY + y * affY) + " " + (addZ + z * affZ) + "\n")
              nbElements += 1
            }
            for (j <- 0 until nbElements - 1) {
              fw.write("f " + {
                startFrom += 1; startFrom
              } + " " + (startFrom + 1) + "\n")
            }
            if (nbElements > 0) {
              startFrom += 1
            }
            nbElements = 0
            if (sym) {
              for (j <- 0 until plots.getLength) {
                val plot: Element = plots.item(j).asInstanceOf[Element]
                val x: Double = plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "x").item(0).getFirstChild.getNodeValue.toDouble
                val y: Double = plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild.getNodeValue.toDouble
                val z: Double = plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild.getNodeValue.toDouble
                fw.write("v " + (addX + x * affX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n")
                nbElements += 1
              }
              for (j <- 0 until nbElements - 1) {
                fw.write("f " + {
                  startFrom += 1; startFrom
                } + " " + (startFrom + 1) + "\n")
              }
              if (nbElements > 0) {
                startFrom += 1
              }
            }
          }
        }
      }
      nl = doc.selectNodes("/wf:data/wf:imports/wf:import", resolver)
      if (nl != null) {
        for (i <- 0 until nl.getLength) {
          nbElements = 0
          val node: Node = nl.item(i)
          val name: String = sourceDir.toString + File.separator + (node.asInstanceOf[XMLElement]).getAttribute("source")
          fw.write("# Imported " + name + "\n")
          val origin: NodeList = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "origin")
          var affineX: Double = 1.0D
          var affineY: Double = 1.0D
          var affineZ: Double = 1.0D
          val affineTramsform: NodeList = (node.asInstanceOf[Element]).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "affine-transform")
          if (affineTramsform != null) {
            for (j <- 0 until affineTramsform.getLength) {
              val affineNode: Node = affineTramsform.item(j)
              if (affineNode.getNodeType == Node.ELEMENT_NODE) {
                val coords: NodeList = affineNode.getChildNodes
                for (k <- 0 until coords.getLength) {
                  val n: Node = coords.item(k)
                  if (n.getNodeType == Node.ELEMENT_NODE && n.getNodeName == "x") {
                    affineX = n.getFirstChild.getNodeValue.toDouble
                  }
                  if (n.getNodeType == Node.ELEMENT_NODE && n.getNodeName == "y") {
                    affineY = n.getFirstChild.getNodeValue.toDouble
                  }
                  if (n.getNodeType == Node.ELEMENT_NODE && n.getNodeName == "z") {
                    affineZ = n.getFirstChild.getNodeValue.toDouble
                  }
                }
              }
            }
          }
          if (origin != null) {
            for (j <- 0 until origin.getLength) {
              val originNode: Node = origin.item(j)
              if (originNode.getNodeType == Node.ELEMENT_NODE) {
                var x: Double = Double.MinValue
                var y: Double = Double.MinValue
                var z: Double = Double.MinValue
                val coords: NodeList = originNode.getChildNodes
                for (k <- 0 until coords.getLength) {
                  val n: Node = coords.item(k)
                  if (n.getNodeType == Node.ELEMENT_NODE && n.getNodeName == "x") {
                    x = affineX * n.getFirstChild.getNodeValue.toDouble
                  }
                  if (n.getNodeType == Node.ELEMENT_NODE && n.getNodeName == "y") {
                    y = affineY * n.getFirstChild.getNodeValue.toDouble
                  }
                  if (n.getNodeType == Node.ELEMENT_NODE && n.getNodeName == "z") {
                    z = affineZ * n.getFirstChild.getNodeValue.toDouble
                  }
                }
                val managed: Int = generate(name, fw, x, y, z, affineX, affineY, affineZ, startFrom)
                startFrom = managed
              }
            }
          }
        }
      }
      fw.flush()
      fw.close()
      println("Done!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    startFrom
  }

  def getOffset: Int = {
    startFrom
  }

  def main(args: Array[String]): Unit = {
    var fileName: String = "xml" + File.separator + "polars.xml"
    if (args.length > 0) {
      fileName = args(0)
    }
    println(s"Transforming ${fileName} in Scala")
    val version: String = XMLParser.getReleaseVersion
    println(s"Using ${version}")
    generate(fileName)
  }
}
