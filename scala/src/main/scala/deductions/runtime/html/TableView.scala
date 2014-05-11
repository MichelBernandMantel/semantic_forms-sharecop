package models.deductions.runtime.html

import org.w3.banana._
import org.w3.banana.jena.JenaModule
import deductions.runtime.abstract_syntax.FormSyntaxFactory
import org.w3.banana.diesel._
import deductions.runtime.html.Form2HTML
import org.w3.banana.jena.Jena

trait TableView
  extends RDFModule
  with RDFOpsModule
  with TurtleReaderModule
  with RDFXMLWriterModule
  with JenaModule
  with Form2HTML[Jena#URI] {

  import Ops._
  
  val nullURI : Rdf#URI = Ops.URI( "" )

  val foaf = FOAFPrefix[Rdf]
  val foafURI = foaf.prefixIri
//  val exampleGraph = (
//    URI("betehess")
//    -- foaf.name ->- "Alexandre".lang("fr")
//    -- foaf.title ->- "Mr"
//    -- foaf.knows ->- (
//      URI("http://bblfish.net/#hjs")
//      -- foaf.name ->- "Henry Story"
//      -- foaf.currentProject ->- URI("http://webid.info/"))).graph
      
  def htmlFormString(uri:String) : String = {
    // TODO load ontologies from local SPARQL; probably use a pointed graph
    val graph = TurtleReader.read(uri, uri).get
    graf2form(graph, uri)
  }


  def graf2form(graph1: Rdf#Graph, uri:String): String = {
    val vocabGraph = TurtleReader.read(foafURI, foafURI).get

    val graph = graph1.union(vocabGraph)

    val factory = new FormSyntaxFactory[Rdf](graph)
    println((graph.toIterable).mkString("\n"))
    val form = factory.createForm(URI(uri),
      Seq(foaf.name))
    println("form:\n" + form)

    val htmlForm = generateHTML(form)
    println(htmlForm)

    val htmlFormString = htmlForm.toString
    htmlFormString
  }
}