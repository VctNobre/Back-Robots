package sciencesystem.backrobots.DAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.shared.JenaException;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;

import sciencesystem.backrobots.model.robos;
public class RobotsOntology {
	
	private String URI;
	private final String fileName;
	private OntModel aModel;
	private InfModel iModel;
	
	
	
	private void loadOntology() {

		aModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

		System.out.println("Carregando a Ontologia " + fileName + " ...");
		try {
			final InputStream input = org.apache.jena.util.FileManager.get().open(fileName);

			try {
				aModel.read(input, null);
				System.out.println("Concluído...");
			} catch (final Exception e) {
				e.printStackTrace();
			}
				
				final Iterator<?> iter = aModel.listOntologies();

			
				if (iter.hasNext()) {
					final Ontology ontology = (Ontology) iter.next();
					this.URI = ontology.getURI(); }
			} catch (final JenaException je) {
				System.err.println("ERROR" + je.getMessage());
				je.printStackTrace();
				System.exit(0);
			}}
	
	public RobotsOntology () {
		this.fileName = "Robots.owl";
		loadOntology();
		
	}
	public RobotsOntology(final String fileName) {
		this.fileName = fileName;
		loadOntology();
	}

	//Reasoner
	public boolean executeReasoner() {
		boolean success = false;
		System.out.println("Executando o Reasoner...");
		try {
			final Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
			iModel = ModelFactory.createInfModel(reasoner, aModel);
			validateModel(iModel);
			success = true;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return success;
	}
	
	//Modelo sendo validado
	public boolean validateModel(final InfModel iModel) {
		boolean consistent = false;
		System.out.print("...Validating Inferred Model: ");
		try {
			final ValidityReport validity = iModel.validate();
			if (validity.isValid()) {
				System.out.println("Consistent...");
				consistent = true;
			} else {
				System.out.println("Conflicts...");
				for (final Iterator<?> i = validity.getReports(); i.hasNext();) {
					System.out.println(" - " + i.next());
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return consistent;
	}

	//Estrutura da ontologia 
	public void printAssertedOntModel() {
		System.out.println("Printing Model...");
		aModel.write(System.out, "TTL");}
	
	//Salvar ontologia no local RDF/XML
	public boolean saveOntology(final String filePath, final OntModel model) {
		boolean executeSave = false;
		final StringWriter sw = new StringWriter();
		model.write(sw, "RDF/XML");
		final String owlCode = sw.toString();
		final File file = new File(filePath);
		try {
			final FileWriter fw = new FileWriter(file);
			fw.write(owlCode);
			fw.close();
			executeSave = true;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return executeSave;
	}
	
	//salvar ontologia formato RDF/XML no proprio arquivo
	public void saveOntology() {
		final StringWriter sw = new StringWriter();
		aModel.write(sw, "RDF/XML");
		final String owlCode = sw.toString();
		final File file = new File(fileName);
		try {
			final FileWriter fw = new FileWriter(file);
			fw.write(owlCode);
			fw.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	void insertAttributePerformance(final String individualPerfomance, final String attribute,
			final float performanceValue, final String propertyFirst, final String propertyMeans) {
		System.out.println("Executing SPARQL Insert:");
		final String owl = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
		final String rdf = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
		final String rdfs = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
		final String xsd = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		final String ont = "PREFIX ont: <" + URI + "#>\n";
		String insertSparql = owl + rdf + rdfs + xsd + ont;
		insertSparql += "INSERT DATA {\n" + "\tont:" + individualPerfomance + " rdf:type ont:Desempenho.\n" + "\tont:"
				+ individualPerfomance + " ont:" + propertyFirst + " \"" + performanceValue + "\"^^xsd:float.\n"
				+ "\tont:" + individualPerfomance + " ont:" + propertyMeans + " \"" + performanceValue
				+ "\"^^xsd:float.\n" + "\tont:" + individualPerfomance + " ont:eDesempenho" + " ont:" + attribute + "\n"
				+ "}";

		System.out.println(insertSparql);

		try {
			UpdateAction.parseExecute(insertSparql, aModel);
		} catch (final Exception e) {
			System.out.println("Falha ao executar UPDATE...");
			System.out.println(e.getMessage());
		}

	}
	
	public ArrayList<robos> selectRobos() {
		final ArrayList<robos> result = new ArrayList<robos>();
		
		final String owl = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
		final String rdf = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
		final String rdfs = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
		final String xsd = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		final String ont = "PREFIX ont: <http://www.semanticweb.org/robotemação/ontologies/2021/8/untitled-ontology-31#>\n";
		String sparqlQuery = owl + rdf + rdfs + xsd + ont;
		sparqlQuery += "SELECT DISTINCT ?pecas  ?DescricaoRobo ?Descricao ?quantidade\r\n" + "WHERE {\r\n"
				+ "ont:RoboEducador owl:equivalentClass/owl:intersectionOf ?list .\r\n"
				+ "?Robos rdfs:comment ?DescricaoRobo.\r\n" + "?list rdf:rest*/rdf:first ?element .\r\n"
				+ "?element owl:someValuesFrom ?montagem .\r\n" + "?montagem owl:intersectionOf ?pecasLista1 .\r\n"
				+ "?montagem owl:intersectionOf ?pecasLista2 .\r\n" + "?pecasLista1 rdf:rest*/rdf:first ?pecas .\r\n"
				+ "?pecasLista2 rdf:rest*/rdf:first ?peca2 .\r\n" + "?peca2 owl:hasValue ?quantidade.\r\n"
				+ "?pecas rdfs:comment ?Descricao\r\n" + "FILTER NOT EXISTS {?pecas owl:onProperty ont:TemQuantidade}}";

		try {
			final robos robot = new robos();
			final QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, aModel);
			final ResultSet results = qexec.execSelect();
			final QuerySolution sol = results.nextSolution();
			results.hasNext();
			robot.setPecas(sol.getResource("?pecas").getLocalName().toString());
			robot.setDescricao(sol.getLiteral("?Descricao").getString());
			robot.setQuantidade(sol.getLiteral("?quantidade").getString());
			robot.setNome(sol.getLiteral("?DescricaoRobo").getString());

			result.add(robot);
			while (results.hasNext()) {

				final robos robos = new robos();
				final QuerySolution soln = results.nextSolution();

				robos.setPecas(soln.getResource("?pecas").getLocalName().toString());
				robos.setDescricao(soln.getLiteral("?Descricao").getString());
				robos.setQuantidade(soln.getLiteral("?quantidade").getString());
				robos.setNome(soln.getLiteral("?DescricaoRobo").getString());

				if (result.get(0).getPecas().contains(robos.getPecas())) {
					qexec.close();

					return result;

				}
				result.add(robos);

			}
			qexec.close();

			return result;
		} catch (final QueryExecException e) {
			System.err.println("ERROR" + e.getMessage());
			e.printStackTrace();
			
		}
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
