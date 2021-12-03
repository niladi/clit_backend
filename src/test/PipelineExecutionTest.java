package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import experiment.ExperimentSettings;
import experiment.Pipeline;
import experiment.PipelineBuilder;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.exceptions.PipelineException;
import structure.interfaces.pipeline.PipelineComponent;

public class PipelineExecutionTest {

	private static final String PATH = "test/resources/";
	private static final Map<String, Class<? extends PipelineComponent>> LINKER_CLASSES = ExperimentSettings
			.getLinkerClassesCaseInsensitive();
	private static final String TEXT = "Napoleon was the emperor of the First French Empire.";
	private static final AnnotatedDocument DOCUMENT = new AnnotatedDocument(TEXT);

	private static enum TestFileEnum {
		STANDARD_LINKER("standard_linker.json"), //
		SIMPLE_PIPELINE("simple_pipeline.json"), //
		COMPLEX_PIPELINE_TRANSLATOR("complex_pipeline_translator.json"), //
		COMPLEX_PIPELINE_COMBINED_CG_ED("complex_pipeline_with_combined_cg_ed.json"), //
		COMPLEX_PIPELINE_MD_ONLY("complex_pipeline_with_md_only.json"), //
		COMPLEX_PIPELINE_TWO_MD_SP_CO("complex_pipeline_with_two_md_and_splitter_and_combiner.json"), //
		COMPLEX_PIPELINE_TWO_CG_ED_SP_CO("complex_pipeline_with_two_cg_ed_and_splitter_and_combiner.json"), //
		COMPLEX_PIPELINE_DEFAULT("complex_pipeline_with_md_cg_ed.json"), //
		COMPLEX_PIPELINE_MD_CG("complex_md_cg_only.json"), //

		;

		final String path;

		TestFileEnum(String path) {
			this.path = path;
		}
	};

	private JSONObject loadPipelineJson(String jsonFileName) {
		String path = PATH + jsonFileName;
		JSONParser jsonParser = new JSONParser();
		try {
			FileReader reader = new FileReader(path);
			Object obj = jsonParser.parse(reader);
			JSONObject pipelineJson = (JSONObject) obj;
			return pipelineJson;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void testLoadTestFiles() {
		for (TestFileEnum testFile : TestFileEnum.values()) {
			JSONObject pipelineJson = loadPipelineJson(testFile.path);
			assertNotNull(pipelineJson);
		}
	}

	@Test
	public void testStandardLinker() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.STANDARD_LINKER.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
	}

	@Test
	public void testSimplePipeline() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.SIMPLE_PIPELINE.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
	}

	@Test
	public void testComplexPipelineWithCombinedCgEd() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_COMBINED_CG_ED.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
	}

	@Test
	public void testComplexPipelineCgEdTranslator() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_TRANSLATOR.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
		assertTrue(mentions.size() > 1);
		System.out.println(mentions);
	}

	@Test
	public void testComplexPipelineWithMdOnly() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_MD_ONLY.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
	}

	// TODO tests for CG only, ED only, CG & ED only, CG_ED only

	@Test
	public void testComplexPipelineWithMdAndCgOnly() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_MD_CG.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		// assertEquals(1, result.size());
		assertTrue(result.size() >= 1);
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
		for (Mention m : mentions) {
			assertTrue(m.getPossibleAssignments() != null);
			assertTrue(m.getPossibleAssignments().size() > 0);
		}
	}

	@Test
	public void testComplexPipelineWithTwoMdAndSplitterAndCombiner() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_TWO_MD_SP_CO.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
	}

	@Test
	public void testComplexPipelineWithTwoCgEdAndSplitterAndCombiner() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_TWO_CG_ED_SP_CO.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		pipeline.execute(DOCUMENT);
		Collection<AnnotatedDocument> result = pipeline.getResults(DOCUMENT);
		assertNotNull(result);
		assertEquals(1, result.size());
		Collection<Mention> mentions = result.iterator().next().getMentions();
		assertNotNull(mentions);
	}

	@Test
	public void testComplexPipelineWithUnspecifiedComponent() {
		// TODO
	}

	@Test
	public void testComplexPipelineWithWrongComponent() {
		// TODO
	}

	@Test
	public void testComplexPipelineWithConnectionToUndefinedComponent() {
		// TODO
	}

	@Test
	public void testComplexPipelineWithInvalidDependency() {
		// TODO
	}

}
