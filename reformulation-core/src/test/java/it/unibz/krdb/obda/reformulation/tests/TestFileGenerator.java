package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFileGenerator {

	private static final String															ONTOURI								= "http://obda.inf.unibz.it/ontologies/tests/dllitef/";

	private static final String															SPARQL_MODE							= "sparql";
	private static final String															DATALOG_MODE						= "datalog";

	private String																		obdalocation						= null;
	private String																		xmllocation							= null;
	private String																		testlocation						= null;
	private String																		mode								= null;

	private OWLOntologyManager															manager								= null;
	private OWLDataFactory																dataFactory							= null;
	private OBDAModel																	apic								= null;
	private it.unibz.krdb.obda.reformulation.tests.XMLResultWriter						writer								= null;
	private Vector<String>																tests								= null;

	private Vector<it.unibz.krdb.obda.reformulation.tests.OntologyGeneratorExpression>	expressions							= null;

	private int																			linenr								= 1;

	private boolean																		GENERATE_ALTERNATIVES_FOR_EXISTS	= false;

	Logger																				log									= LoggerFactory
																																	.getLogger(this
																																			.getClass());

	public TestFileGenerator(String obdalocation, String xmllocation, String tl, String mode) {
		this.obdalocation = obdalocation;
		this.xmllocation = xmllocation;
		this.testlocation = tl + "it/unibz/krdb/obda/reformulation/tests/";
		this.mode = mode;
		tests = new Vector<String>();
		expressions = new Vector<it.unibz.krdb.obda.reformulation.tests.OntologyGeneratorExpression>();
		manager = OWLManager.createOWLOntologyManager();
		log.info("Main Location: {}", tl);
		log.info("Location for OBDA files: {}", obdalocation);
		log.info("Location for XML files: {}", xmllocation);
		log.info("Mode: {}", mode);

	}

	public static void main(String[] args) throws Exception {

		if (args.length != 5) {
			throw new Exception("The program requires 5 parameter.\n" + "1. path to location of obda files\n"
					+ "2. path to location of xml files\n" + "3. path to the location where to put the test files\n"
					+ "4. path to the input file\n" + "5. the query mode. Can either be 'sparql' or 'datalog'");
		}

		String obdaloc = args[0];
		String xmlloc = args[1];
		String tl = args[2];
		String inputfile = args[3];
		String mode = args[4];

		TestFileGenerator gen = new TestFileGenerator(obdaloc, xmlloc, tl, mode);
		gen.parseInputFile(inputfile);
	}

	public void parseInputFile(String f) throws Exception {

		File file = new File(f);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(file);

			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			it.unibz.krdb.obda.reformulation.tests.OntologyGeneratorExpression expression = null;

			while (dis.available() != 0) {

				String line = dis.readLine().trim();
				log.debug("Input: {}", line);
				if (line.contains("#END#")) {
					log.debug("Found end. Ignoring the rest of the file");
					break;
				}

				if (line.startsWith("||")) {
					line = line.substring(2, line.length() - 2);
					line.trim();
					line = line.replace("||", "#");
					String[] array = line.split("#");
					if (array.length != 7) {
						throw new Exception(" Input at " + linenr + " is not in proper format!");
					} else {
						if (lineContainsScenario(array[0].trim())) {
							if (array[0].trim().length() > 0) {
								if (expression != null) {
									expressions.add(expression);
								}
								String id = array[0].trim();
								String tbox = array[2].trim();
								String abox = array[3].trim();
								expression = new OntologyGeneratorExpression(id, tbox, abox);
							}
							String queryid = array[1].trim();
							String query = array[4].trim();
							String result = array[5].trim();
							expression.addQuery(query, result);
							expression.addQueryID(query, queryid);
						}
					}
				}
				linenr++;
			}
			if (expression != null) {
				expressions.add(expression);
			}
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<OntologyGeneratorExpression> it = expressions.iterator();
		int t = 1;
		while (it.hasNext()) {
			OntologyGeneratorExpression ex = it.next();
			String abox = ex.getAbox();
			String tbox = ex.getTbox();
			String id = ex.getId();
			Vector<OWLOntology> o = null;
			try {
				o = createOntology(id, abox, tbox);
			} catch (Exception e) {
				log.error(e.getMessage());
				log.error("Processing ontology: {} {}", id, abox + " " + tbox);
				throw e;
			}
			HashMap<String, String> queriesMap = ex.getQueries();
			HashMap<String, String> ids = ex.getQueriesIds();
			Set<String> queries = queriesMap.keySet();
			Iterator<OWLOntology> oit = o.iterator();
			while (oit.hasNext()) {
				OWLOntology onto = oit.next();

				OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
				apic = obdafac.getOBDAModel();

				String ontoname = onto.getURI().toString();
				int z = ontoname.lastIndexOf("/");
				ontoname = ontoname.substring(z + 1, ontoname.length() - 4);
				writer = new XMLResultWriter(ontoname);

				Iterator<String> qit = queries.iterator();
				while (qit.hasNext()) {
					String query = qit.next();

					String genQuery = "";
					try {
						if (mode.equals(SPARQL_MODE))
							genQuery = getSPARQLQuery(query);
						else if (mode.equals(DATALOG_MODE))
							genQuery = getDatalogQuery(query);
						else
							throw new Exception("Cannot generate the proper query");
					} catch (Exception e) {
						log.error("Processing query: {}", query);
						log.error(e.getMessage());
						throw e;
					}

					apic.getQueryController().addQuery(genQuery, ids.get(query));
					String[] queryhead = getQueryHead(query);
					String[] results = getResults(queriesMap.get(query));
					writer.addResult(ids.get(query), queryhead, results);
				}
				String loc = obdalocation + ontoname + ".obda";
				DataManager ioManager = new DataManager(apic);
				ioManager.saveOBDAData(URI.create(loc), apic.getPrefixManager());

				/* we start from 7 because the location must start with file:// */
				String owluri = obdalocation.substring(7) + ontoname + ".owl";
				manager.saveOntology(onto, new File(owluri).toURI());
				String xmluri = xmllocation + ontoname + ".xml";
				writer.saveXMLResults(xmluri);
				tests.add(ontoname);
				t++;
			}
		}

		generateJUnitTestClass();
	}

	private String[] getQueryHead(String query) throws Exception {

		String[] aux = query.split(":-");
		if (aux.length != 2) {
			throw new Exception("Wrong query format at " + linenr);
		}
		String head = aux[0].trim();
		int i1 = head.indexOf("(");
		int i2 = head.indexOf(")");
		String headvars = head.substring(i1 + 1, i2);
		if (headvars.trim().length() > 0) {
			String vars[] = headvars.split(",");
			return vars;
		} else {
			return new String[0];
		}
	}

	private Vector<OWLDescription> getOWLEntity(URI ontouri, String e) {
		e = e.trim();
		Vector<OWLDescription> desc = new Vector<OWLDescription>();
		if (e.startsWith("E")) {
			e = e.substring(1);
			if (e.contains("-")) {
				e = e.substring(0, e.length() - 5);// remove ^^-^^ from end
				OWLObjectProperty op = dataFactory.getOWLObjectProperty(URI.create(ontouri.toString() + "#" + e));
				OWLObjectPropertyInverse inv = dataFactory.getOWLObjectPropertyInverse(op);
				if (GENERATE_ALTERNATIVES_FOR_EXISTS) {
					desc.add(dataFactory.getOWLObjectMinCardinalityRestriction(inv, 1));
				}
				desc.add(dataFactory.getOWLObjectSomeRestriction(inv, dataFactory.getOWLThing()));
			} else {
				OWLObjectProperty op = dataFactory.getOWLObjectProperty(URI.create(ontouri.toString() + "#" + e));
				if (GENERATE_ALTERNATIVES_FOR_EXISTS) {
					desc.add(dataFactory.getOWLObjectMinCardinalityRestriction(op, 1));
				}
				desc.add(dataFactory.getOWLObjectSomeRestriction(op, dataFactory.getOWLThing()));
			}
		} else {
			desc.add(dataFactory.getOWLClass(URI.create(ontouri.toString() + "#" + e)));
		}
		return desc;
	}

	private OWLObjectPropertyExpression getOWLObjectProperty(URI ontouri, String name) {
		name = name.trim();
		if (name.contains("-")) {
			name = name.substring(0, name.length() - 5);// remove ^^-^^ from end
			OWLObjectProperty op = dataFactory.getOWLObjectProperty(URI.create(ontouri.toString() + "#" + name));
			return dataFactory.getOWLObjectPropertyInverse(op);
		} else {
			return dataFactory.getOWLObjectProperty(URI.create(ontouri.toString() + "#" + name));
		}
	}

	private Vector<OWLOntology> createOntology(String testid, String abox, String tbox) throws Exception {

		URI uri = URI.create(ONTOURI + "test.owl");
		Vector<OWLOntology> vec = new Vector<OWLOntology>();
		dataFactory = manager.getOWLDataFactory();

		if (tbox.contains("ISA")) {
			String tboxcontent[] = tbox.split(",");
			if (tboxcontent.length == 1) {
				for (int i = 0; i < tboxcontent.length; i++) {
					String exp = tboxcontent[i].trim();
					String[] entities = null;
					boolean isRoleInclusion = false;
					if (exp.contains(" RISA ")) {
						entities = exp.split("RISA");
						isRoleInclusion = true;
					} else {
						entities = exp.split("ISA");
					}
					if (entities.length != 2) {
						throw new Exception(" TBox at " + linenr + " is not in proper format!");
					} else {
						String s1 = entities[0].trim();
						String s2 = entities[1].trim();
						if (isRoleInclusion) {

							OWLObjectPropertyExpression role1 = getOWLObjectProperty(uri, s1);
							OWLObjectPropertyExpression role2 = getOWLObjectProperty(uri, s2);
							OWLAxiom ax = dataFactory.getOWLSubObjectPropertyAxiom(role1, role2);
							URI u = URI.create(ONTOURI + "test_" + testid.trim() + ".owl");
							OWLOntology o = manager.createOntology(u);
							manager.addAxiom(o, ax);
							vec.add(o);
						} else {
							Vector<OWLDescription> left = getOWLEntity(uri, s1);
							Vector<OWLDescription> right = getOWLEntity(uri, s2);
							int c = 1;
							for (int j = 0; j < left.size(); j++) {
								OWLDescription d1 = left.get(j);
								int indj = j;
								if (right.size() > 1) {
									indj = j + 1;
								}
								for (int k = 0; k < right.size(); k++) {
									OWLDescription d2 = right.get(k);
									int indk = k;
									if (right.size() > 1) {
										indk = k + 1;
									}
									URI u = URI.create(ONTOURI + "test_" + testid.trim() + "_" + indj + "_" + indk + ".owl");
									OWLOntology o = manager.createOntology(u);
									OWLAxiom ax = dataFactory.getOWLSubClassAxiom(d1, d2);
									manager.addAxiom(o, ax);
									c++;
									vec.add(o);
								}
							}
							if (left.size() > 1) {// means the left side
								// contains an exist

								OWLDescription d1 = left.get(0);
								OWLObjectPropertyExpression op = null;
								if (d1 instanceof OWLObjectSomeRestriction) {
									OWLObjectSomeRestriction rest = (OWLObjectSomeRestriction) d1;
									op = rest.getProperty();
								} else if (d1 instanceof OWLObjectMinCardinalityRestriction) {
									OWLObjectMinCardinalityRestriction card = (OWLObjectMinCardinalityRestriction) d1;
									op = card.getProperty();
								} else {
									throw new Exception("Error: Unexpected owldescription!");
								}
								if (op instanceof OWLObjectPropertyInverse) {
									op = ((OWLObjectPropertyInverse) op).getNamedProperty();
									for (int j = 0; j < right.size(); j++) {
										OWLDescription d2 = right.get(j);
										int indj = j;
										if (right.size() > 1) {
											indj = j + 1;
										}
										URI u = URI.create(ONTOURI + "test_" + testid.trim() + "_" + indj + "_3.owl");
										OWLOntology o = manager.createOntology(u);
										OWLAxiom ax = dataFactory.getOWLObjectPropertyRangeAxiom(op, d2);
										manager.addAxiom(o, ax);
										c++;
										vec.add(o);
									}
								} else {
									for (int j = 0; j < right.size(); j++) {
										OWLDescription d2 = right.get(j);
										int indj = j;
										if (right.size() > 1) {
											indj = j + 1;
										}
										URI u = URI.create(ONTOURI + "test_" + testid.trim() + "_" + indj + "_3.owl");
										OWLOntology o = manager.createOntology(u);
										OWLAxiom ax = dataFactory.getOWLObjectPropertyDomainAxiom(op, d2);
										manager.addAxiom(o, ax);
										c++;
										vec.add(o);
									}
								}
							}
						}
					}
				}
			} else {
				List<Vector<OWLAxiom>> axioms = getAxiomsFromTBox(tboxcontent, uri);
				vec = getOntolgoies(axioms, testid);
			}
		}
		if (vec.size() == 0) {
			URI u = URI.create(ONTOURI + "test_" + testid.trim() + ".owl");
			OWLOntology o = manager.createOntology(u);
			vec.add(o);
		}
		Iterator<OWLOntology> it = vec.iterator();
		while (it.hasNext()) {
			OWLOntology onto = it.next();
			String[] individuals = abox.trim().split(" ");
			OWLAxiom axiom = null;
			for (int i = 0; i < individuals.length; i++) {
				String aux = individuals[i].trim();
				if (aux.length() > 0) {
					int i1 = aux.indexOf("(");
					int i2 = aux.indexOf(")");
					String entity = aux.substring(0, i1);
					String parameters = aux.substring(i1 + 1, i2);
					String[] names = parameters.split(",");
					axiom = null;
					if (names.length == 2) {
						String objprop = uri.toString() + "#" + entity;
						OWLObjectProperty op = dataFactory.getOWLObjectProperty(URI.create(objprop));
						String ind1uri = onto.getURI() + "#" + names[0];
						String ind2uri = onto.getURI() + "#" + names[1];
						OWLIndividual ind1 = dataFactory.getOWLIndividual(URI.create(ind1uri));
						OWLIndividual ind2 = dataFactory.getOWLIndividual(URI.create(ind2uri));
						axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(ind1, op, ind2);
					} else {
						String clazz = uri.toString() + "#" + entity;
						OWLClass cl = dataFactory.getOWLClass(URI.create(clazz));
						String induri = onto.getURI() + "#" + parameters;
						OWLIndividual ind = dataFactory.getOWLIndividual(URI.create(induri));
						axiom = dataFactory.getOWLClassAssertionAxiom(ind, cl);
					}
				}
				if (axiom != null)
					manager.addAxiom(onto, axiom);
			}

		}
		return vec;
	}

	private Vector<OWLOntology> getOntolgoies(List<Vector<OWLAxiom>> axioms, String testid) throws Exception {

		Vector<OWLOntology> ontos = new Vector<OWLOntology>();
		int nr = 1;
		for (int i = 0; i < axioms.size(); i++) {
			Vector<OWLAxiom> vex = axioms.get(i);
			nr = nr * vex.size();
		}
		for (int i = 0; i < nr; i++) {
			ontos.add(manager.createOntology(URI.create(ONTOURI + "test_" + testid.trim() + "_" + i + ".owl")));
		}

		for (int i = 0; i < axioms.size(); i++) {
			Vector<OWLAxiom> vex = axioms.get(i);
			int j = nr / vex.size();
			int h = 0;
			for (int a = 0; a < vex.size(); a++) {
				OWLAxiom ax = vex.get(a);
				for (int k = 0; k < j; k++) {
					OWLOntology onto = ontos.get(h);
					manager.addAxiom(onto, ax);
					h++;
				}
			}
			if (h != ontos.size()) {
				throw new Exception("Not all ontologies were filled properly");
			}
		}
		return ontos;
	}

	private List<Vector<OWLAxiom>> getAxiomsFromTBox(String[] tbox, URI uri) throws Exception {

		List<Vector<OWLAxiom>> result = new Vector<Vector<OWLAxiom>>();
		for (int i = 0; i < tbox.length; i++) {
			Vector<OWLAxiom> axioms = new Vector<OWLAxiom>();
			String exp = tbox[i].trim();
			String[] entities = null;
			boolean isRoleInclusion = false;
			if (exp.contains(" RISA ")) {
				entities = exp.split("RISA");
				isRoleInclusion = true;
			} else {
				entities = exp.split("ISA");
			}
			if (entities.length != 2) {
				throw new Exception(" TBox at " + linenr + " is not in proper format!");
			} else {
				String s1 = entities[0].trim();
				String s2 = entities[1].trim();
				if (isRoleInclusion) {
					OWLObjectPropertyExpression role1 = getOWLObjectProperty(uri, s1);
					OWLObjectPropertyExpression role2 = getOWLObjectProperty(uri, s2);
					OWLAxiom ax = dataFactory.getOWLSubObjectPropertyAxiom(role1, role2);
					axioms.add(ax);
				} else {
					Vector<OWLDescription> left = getOWLEntity(uri, s1);
					Vector<OWLDescription> right = getOWLEntity(uri, s2);
					int c = 1;
					for (int j = 0; j < left.size(); j++) {
						OWLDescription d1 = left.get(j);
						for (int k = 0; k < right.size(); k++) {
							OWLDescription d2 = right.get(k);
							OWLAxiom ax = dataFactory.getOWLSubClassAxiom(d1, d2);
							axioms.add(ax);
						}
					}
					if (left.size() > 1) {// means the left side contains an
						// exist

						OWLDescription d1 = left.get(0);
						OWLObjectPropertyExpression op = null;
						if (d1 instanceof OWLObjectSomeRestriction) {
							OWLObjectSomeRestriction rest = (OWLObjectSomeRestriction) d1;
							op = rest.getProperty();
						} else if (d1 instanceof OWLObjectMinCardinalityRestriction) {
							OWLObjectMinCardinalityRestriction card = (OWLObjectMinCardinalityRestriction) d1;
							op = card.getProperty();
						} else {
							throw new Exception("Error: Unexpected owldescription!");
						}
						if (op instanceof OWLObjectPropertyInverse) {
							op = ((OWLObjectPropertyInverse) op).getNamedProperty();
							for (int j = 0; j < right.size(); j++) {
								OWLDescription d2 = right.get(j);
								OWLAxiom ax = dataFactory.getOWLObjectPropertyRangeAxiom(op, d2);
								axioms.add(ax);
								;
							}
						} else {
							for (int j = 0; j < right.size(); j++) {
								OWLDescription d2 = right.get(j);
								OWLAxiom ax = dataFactory.getOWLObjectPropertyDomainAxiom(op, d2);
								axioms.add(ax);
							}
						}
					}
				}
			}
			result.add(axioms);
		}
		return result;
	}

	private String getSPARQLQuery(String query) throws Exception {

		String[] aux = query.split(":-");
		if (aux.length != 2) {
			throw new Exception("Wrong query format at " + linenr);
		}
		String head = aux[0].trim();
		String body = aux[1].trim();
		int i1 = head.indexOf("(");
		int i2 = head.indexOf(")");
		String headvars = head.substring(i1 + 1, i2);
		String vars[] = headvars.split(",");
		StringBuffer sb = new StringBuffer();
		if (headvars.trim().length() > 0) {
			StringBuffer hv = new StringBuffer();
			for (int i = 0; i < vars.length; i++) {
				if (hv.length() > 0) {
					hv.append(" ");
				}
				hv.append("$");
				hv.append(vars[i]);
			}
			sb.append("SELECT ");
			sb.append(hv.toString());
		} else {
			sb.append("ASK");
		}

		String atoms[] = body.split(" ");
		StringBuffer sb1 = new StringBuffer();
		for (int i = 0; i < atoms.length; i++) {
			String atom = atoms[i];

			if (sb1.length() > 0) {
				sb1.append(" . ");
			}

			i1 = atom.indexOf("(");
			i2 = atom.indexOf(")");
			String name = atom.substring(0, i1);
			String parameters = atom.substring(i1 + 1, i2);
			if (parameters.contains(",")) {
				String[] p = parameters.split(",");
				if (p.length != 2) {
					throw new Exception("Wrong query format at line nr " + linenr);
				}
				sb1.append(getSPARQLTerm(p[0]));
				// if (!(p[0].startsWith("'") && p[0].endsWith("'"))) {
				// sb1.append("$");
				// }
				// sb1.append(p[0]);
				sb1.append(" dllite:");
				sb1.append(name);

				sb1.append(getSPARQLTerm(p[1]));
				// if (!(p[1].startsWith("") && p[1].endsWith("'"))) {
				// sb1.append("$");
				// }
				// sb1.append(p[1]);
			} else {
				sb1.append(getSPARQLTerm(parameters));
				sb1.append(" rdf:type dllite:");
				sb1.append(name);
			}
		}
		sb.append(" WHERE {");
		sb.append(sb1);
		sb.append("}");

		return sb.toString();
	}

	private String getSPARQLTerm(String parameters) {
		StringBuffer bf = new StringBuffer();
		if ((parameters.startsWith("<")) && parameters.endsWith(">")) {
			bf.append(":" + parameters.substring(1, parameters.length() - 1));
		} else {
			if (!(parameters.startsWith("'") && parameters.endsWith("'"))) {
				bf.append("$");
			}
			bf.append(parameters);
		}
		return bf.toString();
	}

	private String getDatalogQuery(String query) throws Exception {

		StringBuffer ds = new StringBuffer(); // datalog string

		String[] aux = query.split(":-");
		if (aux.length != 2)
			throw new Exception("Wrong query format at " + linenr);

		// The head
		String head = aux[0].trim();
		int i1 = head.indexOf("(");
		int i2 = head.indexOf(")");

		String headName = head.substring(0, i1);
		String headVars = head.substring(i1 + 1, i2);
		String[] vars = headVars.split(",");

		StringBuffer as = new StringBuffer(); // atom string
		as.append("ucq:").append(headName);
		as.append("(");
		if (headVars.trim().length() > 0) {
			StringBuffer av = new StringBuffer(); // atom variables
			for (String var : vars) {
				if (av.length() > 0) {
					av.append(", ");
				}
				av.append("$").append(var); // $x, $y
			}
			as.append(av.toString());
		}
		as.append(")");

		ds.append(as).append(" :- ");

		// The body
		String body = aux[1].trim();
		String atoms[] = body.split(" ");

		as = new StringBuffer(); // atom string
		for (String atom : atoms) {
			if (as.length() > 0) {
				as.append(", ");
			}
			i1 = atom.indexOf("(");
			i2 = atom.indexOf(")");

			String atomName = atom.substring(0, i1);
			String atomVars = atom.substring(i1 + 1, i2);
			vars = atomVars.split(",");

			as.append("dllite:").append(atomName);
			as.append("(");
			if (atomVars.trim().length() > 0) {
				StringBuffer av = new StringBuffer(); // atom variables
				for (String var : vars) {
					if (av.length() > 0) {
						av.append(", ");
					}
					av.append("$").append(var);
				}
				as.append(av);
			}
			as.append(")");
		}
		ds.append(as);

		return ds.toString();
	}

	private String[] getResults(String res) {
		res = res.trim();
		if (!res.equals("{}") && res.length() > 2) {
			res = res.substring(1, res.length() - 1);
			res = res.replace("(", "");
			res = res.replace(")", "");
			String[] results = res.split(" ");
			return results;
		} else {
			return new String[0];
		}
	}

	private void generateJUnitTestClass() {
		String outfile = testlocation + "ReformulationTest.java";
		log.info("Generating JUnit test file: {}", outfile);
		try {
			FileWriter fstream = new FileWriter(outfile);

			BufferedWriter out = new BufferedWriter(fstream);

			out.append("package it.unibz.krdb.obda.reformulation.tests;\n");
			out.append("import junit.framework.TestCase;\n");
			out.append("import java.util.Set;\n");
			out.append("import java.util.Iterator;\n");
			out.append("import java.util.LinkedList;\n");
			out.append("import java.util.Collections;\n");
			out.append("import java.util.List;\n");
			out.append("import org.slf4j.Logger;\n");
			out.append("import org.slf4j.LoggerFactory;\n");
			out.append("import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;\n");

			out.newLine();
			out.newLine();
			out.append("public class ReformulationTest extends TestCase {\n\n");
			out.append("\tprivate Tester tester = null;\n");
			out.append("\tLogger	log	= LoggerFactory.getLogger(this.getClass());\n");
			out.append("\tprivate String propfile = \"src/test/resources/test.properties\";\n");
			out.append("\tpublic ReformulationTest(){\n");
			out.append("\t\ttester = new Tester(propfile);\n");
			out.append("\t}\n");

			out.append("private void test_function(String ontoname) throws Exception {\n");
			out.append("     log.debug(\"Test case: {}\", ontoname);\n");
			out.append("     log.debug(\"Testing in-memory db/material abox\");\n");
			out.append("     tester.load(ontoname, QuestConstants.CLASSIC);\n");
			out.append("     for (String id : tester.getQueryIds()) {\n");
			out.append("         log.debug(\"Testing query: {}\", id);\n");
			out.append("         Set<String> exp = tester.getExpectedResult(id);\n");
			out.append("         Set<String> res = tester.executeQuery(id);\n");
			out.append("assertTrue(\"Expected \" + exp + \" Result \" + res, exp.size() == res.size());\n");
			// out.append("         assertTrue(exp.size() == res.size());\n");
			out.append("         for (String realResult : res) {\n");
			out.append("             assertTrue(\"expeted: \" +exp.toString() + \" obtained: \" + res.toString(), exp.contains(realResult));\n");
			out.append("         }\n");
			out.append("     }\n");
			out.append("\n");
			out.append("     log.debug(\"Testing in-memory db/vitual abox\");\n");
			out.append("     tester.load(ontoname, QuestConstants.VIRTUAL);\n");
			out.append("     for (String id : tester.getQueryIds()) {\n");
			out.append("         log.debug(\"Testing query: {}\", id);\n");
			out.append("         Set<String> exp = tester.getExpectedResult(id);\n");
			out.append("         Set<String> res = tester.executeQuery(id);\n");
			out.append("assertTrue(\"Expected \" + exp + \" Result \" + res, exp.size() == res.size());\n");
			// out.append("         assertTrue(exp.size() == res.size());\n");
			out.append("         for (String realResult : res) {\n");
			out.append("             assertTrue(\"expeted: \" +exp.toString() + \" obtained: \" + res.toString(),exp.contains(realResult));\n");
			out.append("         }\n");
			out.append("     }\n");
			out.append("     log.debug(\"Testing in-memory db/SemanticIndex\");\n");
			out.append("     tester.load(ontoname, QuestConstants.CLASSIC, QuestConstants.SEMANTIC);\n");
			out.append("     for (String id : tester.getQueryIds()) {\n");
			out.append("         log.debug(\"Testing query: {}\", id);\n");
			out.append("         Set<String> exp = tester.getExpectedResult(id);\n");
			out.append("         Set<String> res = tester.executeQuery(id);\n");
			out.append("         List<String> exp_list = new LinkedList<String>(exp);\n");
			out.append("         List<String> res_list = new LinkedList<String>(res);\n");
			out.append("         Collections.sort(exp_list);\n");
			out.append("         Collections.sort(res_list);\n");
			out.append("         assertEquals(exp_list, res_list);\n");
			out.append("     }\n");
			out.append(" }\n");
			out.append("\n");

			for (int i = 0; i < tests.size(); i++) {
				out.append("\tpublic void " + tests.get(i) + "() throws Exception {");
				out.newLine();
				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
				out.append("\t\ttest_function(ontoname);\n");
				out.append("\t}");
				out.newLine();
			}
			out.append("}");
			out.newLine();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean lineContainsScenario(String s) {
		if (s.length() > 0) {
			try {
				Integer.parseInt(s);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			return true;
		}
	}
}
