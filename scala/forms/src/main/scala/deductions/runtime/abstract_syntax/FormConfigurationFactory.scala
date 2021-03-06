package deductions.runtime.abstract_syntax

import org.apache.log4j.Logger
import org.w3.banana.Prefix
import org.w3.banana.RDF
import deductions.runtime.services.Configuration
import deductions.runtime.utils.RDFHelpers
import deductions.runtime.sparql_cache.RDFCacheAlgo

/**
 * Factory for populating Form from graph
 */
trait FormConfigurationFactory[Rdf <: RDF, DATASET]
    extends Configuration
    with RDFCacheAlgo[Rdf, DATASET]
    with RDFHelpers[Rdf] {
  
  import ops._

  val formPrefix: Prefix[Rdf] = Prefix("form", formVocabPrefix)

  /**
   * lookup for form:showProperties (ordered list of fields) in Form Configuration within RDF graph about this class
   *  usable for unfilled and filled Forms
   */
  def lookPropertieslistFormInConfiguration(classs: Rdf#URI)
  (implicit graph: Rdf#Graph)
  : (Seq[Rdf#URI], Rdf#Node) = {
    val formSpecOption = lookFormSpecInConfiguration(classs)
    formSpecOption match {
      case None => (Seq(), URI(""))
      case Some(formConfiguration) =>
        val propertiesList = propertiesListFromFormConfiguration(formConfiguration)
        (propertiesList, formConfiguration)
    }
  }
  
  /** TODO consider returning Seq[Rdf#Node] */
  def propertiesListFromFormConfiguration(formConfiguration: Rdf#Node)
  (implicit graph: Rdf#Graph)
  : Seq[Rdf#URI] = {
    val props = getObjects(graph, formConfiguration, formPrefix("showProperties"))
    for (p <- props) { println("showProperties " + p) }
    val p = props.headOption
    val propertiesList = nodeSeqToURISeq(rdfListToSeq(p))
    propertiesList
  }

  /** lookup Form Spec from OWL class in Configuration */
  private def lookFormSpecInConfiguration(classs: Rdf#URI)
  (implicit graph: Rdf#Graph)
  : Option[Rdf#Node] = {
    val forms = getSubjects(graph, formPrefix("classDomain"), classs)
    val debugString = new StringBuilder; Logger.getRootLogger().debug("forms " + forms.addString(debugString, "; "))
    val formSpecOption = forms.flatMap {
      form => ops.foldNode(form)(uri => Some(uri), bn => Some(bn), lit => None)
    }.headOption
    Logger.getRootLogger().warn(s"WARNING: several form specs for $classs")
    Logger.getRootLogger().debug("formNodeOption " + formSpecOption)
    formSpecOption
  }

  /**
   * return e g :  <topic_interest>
   *  in :
   *  <pre>
   *  &lt;topic_interest&gt; :fieldAppliesToForm &lt;personForm> ;
   *   :fieldAppliesToProperty foaf:topic_interest ;
   *   :widgetClass form:DBPediaLookup .
   *  <pre>
   *  that is, query:
   *  ?S form:fieldAppliesToProperty prop .
   */
  def lookFieldSpecInConfiguration(
    prop: Rdf#Node)
    (implicit graph: Rdf#Graph)
    = {
    find(graph, ANY, formPrefix("fieldAppliesToProperty"), prop).toSeq
  }

  /** look for Properties List & form Configuration From URI in Database after trying to donwload */
  def lookPropertiesListFromDatabaseOrDownload(formuri: String)
      (implicit graph: Rdf#Graph) = {
    val formConfiguration = URI(formuri)
    retrieveURINoTransaction( formConfiguration, dataset)
    val propertiesList = propertiesListFromFormConfiguration(formConfiguration)
    (propertiesList, formConfiguration)
  }

}