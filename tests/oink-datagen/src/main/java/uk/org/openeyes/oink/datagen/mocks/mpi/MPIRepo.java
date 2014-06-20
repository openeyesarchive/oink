/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package uk.org.openeyes.oink.datagen.mocks.mpi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;
import uk.org.openeyes.oink.datagen.generators.person.PersonGeneratorFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class MPIRepo {
	
	public class GetAllContext {
		public GetAllContext(IndexSearcher searcher,
				TopScoreDocCollector collector, BooleanQuery bq, int size, TopDocs hits) {
			this.searcher = searcher;
			this.collector = collector;
			this.query = bq;
			this.totalSize = size;
			this.hits = hits;
		}
		public TopScoreDocCollector collector;
		public IndexSearcher searcher;
		public ScoreDoc lastDoc;
		public Query query;
		public int totalSize;
		public TopDocs hits;
	}

	private static Logger logger = LoggerFactory.getLogger(MPIRepo.class);

	private Directory directory = null;
	private IndexWriterConfig config = null;

	public void init() throws Exception {

		logger.info("Initialising repo...");

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

		boolean init = false;

		File f = new File("target/oink/datastore/");
		if (!f.exists()) {

			logger.info("Data store does not exist, creating...");

			f.mkdirs();

			init = true;
		}

		directory = FSDirectory.open(f);
		config = new IndexWriterConfig(Version.LUCENE_48, analyzer);

		if (init) {
			PersonGenerator g = PersonGeneratorFactory.getInstance("uk");
			List<Person> patients = g.generate(100);
			addPatients(patients);

			logger.info("Data store created and is ready.");
		} else {
			logger.info("Data store exists and is ready.");
		}

		logger.info("Initialising repo complete.");
	}

	public void close() throws IOException {
		logger.info("Closing repo...");
		directory.close();
		logger.info("Repo closed.");
	}

	public void deleteAll() {

		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(directory, config.clone());
			iwriter.deleteAll();
		} catch (IOException e) {
			logger.error("Failed to delete all patients. Exception = {}", e);
		} finally {
			try {
				if (iwriter != null) {
					iwriter.close();
				}
			} catch (IOException e) {
				logger.error("Failed to delete all patients. Exception = {}", e);
			}
		}
	}

	public void addPatient(Person patient) {

		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(directory, config.clone());
			Document doc = new Document();
			upsertPatientDocument(doc, patient);
			iwriter.addDocument(doc);
		} catch (IOException e) {
			logger.error("Failed to add patient. Exception = {}", e);
		} finally {
			try {
				if (iwriter != null) {
					iwriter.close();
				}
			} catch (IOException e) {
				logger.error("Failed to add patient. Exception = {}", e);
			}
		}
	}

	public void addPatients(List<Person> patients) {

		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(directory, config.clone());
		} catch (IOException e1) {
			logger.error("Failed to add patient. Exception = {}", e1);
			return;
		}
		for (Person patient : patients) {
			try {
				Document doc = new Document();
				upsertPatientDocument(doc, patient);
				iwriter.addDocument(doc);
			} catch (IOException e) {
				logger.error("Failed to add patient. Exception = {}", e);
			}
		}
		try {
			iwriter.close();
		} catch (IOException e) {
			logger.error("Failed to add patient. Exception = {}", e);
		}
	}

	private void upsertPatientDocument(Document doc, Person patient) {

		logger.debug("================================");
		logger.debug(" Patient ");
		logger.debug("================================");
		logger.debug("Name = {} {}", patient.getFirstName(),
				patient.getLastName());

		doc.add(new StringField("type", Person.class.getName(), Store.YES));

		for (Identifier i : patient.getIdentifiers()) {
			String identifierField = buildIdentifierField(i);
			doc.add(new StringField(identifierField, i.getValue(), Store.YES));
			logger.debug("'{}' = '{}'", identifierField, i.getValue());
			;
		}

		ObjectMapper m = new ObjectMapper();
		m.registerModule(new JodaModule());
		try {
			String json = m.writeValueAsString(patient);
			doc.add(new StoredField("data.json", json));
		} catch (JsonProcessingException e) {
			logger.error("Failed to create patient JSON. Exception = {}", e);
		}
	}

	private String buildIdentifierField(Identifier i) {
		String identifierField = "identifier";
		if (StringUtils.hasText(i.getCodeSystem())) {
			identifierField += "_" + i.getCodeSystem();
		}
		identifierField = identifierField.replaceAll("[^A-Za-z0-9]", "_");
		return identifierField.toLowerCase();
	}

	public Person getPatientByIdentifier(String usage, String codeSystem,
			String value) throws IOException, ParseException {
		return getPatientByIdentifier(new Identifier(usage, codeSystem, value));
	}

	public Person getPatientByIdentifier(Identifier i) throws IOException,
			ParseException {

		logger.debug("================================");
		logger.debug(" Query ");
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		String identifierField = buildIdentifierField(i);

		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);

		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(new Term("type", Person.class.getName())),
				Occur.MUST);
		bq.add(new TermQuery(new Term(identifierField, i.getValue())),
				Occur.MUST);

		logger.debug("Lucene query = '{}'", bq);

		TopDocs hits = searcher.search(bq, null, 100);

		logger.debug("hits = '{}'", hits.totalHits);

		if (hits.totalHits > 0) {
			Document doc = searcher.doc(hits.scoreDocs[0].doc);

			String json = doc.getField("data.json").stringValue();
			ObjectMapper m = new ObjectMapper();
			Person patient = m.readValue(json, Person.class);

			return patient;
		}

		return null;
	}

	public int getSize() throws IOException {
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);

		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(new Term("type", Person.class.getName())),
				Occur.MUST);

		final org.apache.lucene.search.Query luceneQuery = new ConstantScoreQuery(
				bq);

		final TotalHitCountCollector hitCountCollector = new TotalHitCountCollector();

		searcher.search(luceneQuery, hitCountCollector);

		return hitCountCollector.getTotalHits();
	}

	public GetAllContext getAllInit() throws IOException,
			ParseException {

		logger.debug("================================");
		logger.debug(" Query ");
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);

		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(new Term("type", Person.class.getName())),
				Occur.MUST);

		logger.debug("Lucene query = '{}'", bq);
		
		int size = getSize();
		TopScoreDocCollector collector = TopScoreDocCollector.create(size, false);
		
		searcher.search(bq, collector);
		
		logger.debug("Docs = '{}'", collector.getTotalHits());
		
		TopDocs hits = collector.topDocs();
		
		return new GetAllContext(searcher, collector, bq, size, hits);
	}
	
	public List<Person> getAllByPage(GetAllContext context, int page, int pageSize) throws IOException {
		
		List<Person> patients = new ArrayList<Person>();
		
		for(int i = 0; i < pageSize; i++) {
			int index = page * pageSize + i;
			if(index >= context.hits.scoreDocs.length) {
				break;
			}
			ScoreDoc scoreDoc = context.hits.scoreDocs[index];
			Document doc = context.searcher.doc(scoreDoc.doc);
			String json = doc.getField("data.json").stringValue();
			ObjectMapper m = new ObjectMapper();
			Person patient = m.readValue(json, Person.class);
			patients.add(patient);
		}
		
		return patients;
	}

}
