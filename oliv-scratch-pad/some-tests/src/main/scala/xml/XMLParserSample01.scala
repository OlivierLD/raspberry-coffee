package xml

import java.io.{File, IOException, PrintWriter}
import java.net.URL

import oracle.xml.parser.v2.{DOMParser, XMLDocument, XMLElement, XSLProcessor, XSLStylesheet}
import org.w3c.dom.Text

object XMLParserSample01 {
  private val title = "De l'ile d'Ouessant a la Pointe de Penmarc'h"
  private val provider = "SHOM"
  private val chartNo = 5316
  private val year = 1976

  @throws[Exception]
  def openInBrowser(page: String): Unit = {
    val os = System.getProperty("os.name")
    if (os.indexOf("Windows") > -1) {
      var cmd = ""
      if (page.indexOf(" ") != -1) cmd = "cmd /k start \"" + page + "\""
      else cmd = "cmd /k start " + page + ""
      System.out.println("Command:" + cmd)
      Runtime.getRuntime.exec(cmd) // Can contain blanks...
    }
    else if (os.indexOf("Linux") > -1) { // Assuming htmlview
      Runtime.getRuntime.exec("htmlview " + page)
    }
    else if (os.indexOf("Mac") > -1) Runtime.getRuntime.exec("open " + page)
    else throw new RuntimeException("OS [" + os + "] not supported yet")
  }

  def main(args: Array[String]): Unit = {
    val doc = new XMLDocument
    val root = doc.createElement("selection-root").asInstanceOf[XMLElement]
    doc.appendChild(root)
    val chart = doc.createElement("chart").asInstanceOf[XMLElement]
    chart.setAttribute("chart-no", chartNo.toString)
    chart.setAttribute("provider", provider)
    chart.setAttribute("year", year.toString)
    val txt = doc.createTextNode("text#")
    chart.appendChild(txt)
    txt.setNodeValue(title)
    root.appendChild(chart)
    try doc.print(System.out)
    catch {
      case ioe: IOException =>
        ioe.printStackTrace()
    }
    try { // in HTML
      val xslURL = new File("xml" + File.separator + "charthtml.xsl").toURI.toURL
      //  System.out.println("Transforming using " + xslURL.toString());
      val parser = new DOMParser
      parser.parse(xslURL)
      val xsldoc = parser.getDocument
      // instantiate a stylesheet
      val processor = new XSLProcessor
      processor.setBaseURL(xslURL)
      val xslss = processor.newXSLStylesheet(xsldoc)
      // display any warnings that may occur
      processor.showWarnings(true)
      processor.setErrorStream(System.err)
      // Process XSL
      val pw = new PrintWriter(new File("xml" + File.separator + "selection.html"))
      //  processor.setParam("xmlnx:url", "prm1", "value1");
      processor.processXSL(xslss, doc, pw)
      pw.close()
      openInBrowser("xml" + File.separator + "selection.html")
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }
}
