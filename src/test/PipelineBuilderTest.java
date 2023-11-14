
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import experiment.EnumComponentType;
import experiment.ExperimentSettings;
import experiment.Pipeline;
import experiment.PipelineBuilder;
import experiment.PipelineItem;
import structure.config.constants.EnumPipelineType;
import structure.exceptions.PipelineException;
import structure.interfaces.pipeline.PipelineComponent;

public class PipelineBuilderTest {

	private static final String PATH = "test/resources/";
	private static final Map<String, Class<? extends PipelineComponent>> LINKER_CLASSES = ExperimentSettings
			.getLinkerClassesCaseInsensitive();

	private static enum TestFileEnum {
		STANDARD_LINKER("standard_linker.json"), //
		SIMPLE_PIPELINE("simple_pipeline.json"), //
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
		PipelineItem outputItem = pipeline.getOutputItem();
		assertEquals(1, outputItem.getDependencies().size());
		assertEquals(Pipeline.KEY_OUTPUT_ITEM, outputItem.getID());
		PipelineItem linkerItem = outputItem.getDependencies().iterator().next();
		assertNotNull(linkerItem);
		assertEquals(EnumComponentType.MD_CG_ED.id + "1", linkerItem.getID());
		assertEquals(1, linkerItem.getDependencies().size());
		PipelineItem startItem = linkerItem.getDependencies().iterator().next();
		assertNotNull(startItem);
		assertEquals(Pipeline.KEY_INPUT_ITEM, startItem.getID());
		assertEquals(EnumPipelineType.FULL, pipeline.getPipelineType());
	}

	@Test
	public void testSimplePipeline() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.SIMPLE_PIPELINE.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		PipelineItem outputItem = pipeline.getOutputItem();
		assertEquals(1, outputItem.getDependencies().size());
		assertEquals(Pipeline.KEY_OUTPUT_ITEM, outputItem.getID());
		PipelineItem cgEdItem = outputItem.getDependencies().iterator().next();
		assertNotNull(cgEdItem);
		assertEquals(EnumComponentType.CG_ED.id + "1", cgEdItem.getID());
		assertEquals(1, cgEdItem.getDependencies().size());
		PipelineItem mdItem = cgEdItem.getDependencies().iterator().next();
		assertNotNull(mdItem);
		assertEquals(EnumComponentType.MD.id + "1", mdItem.getID());
		assertEquals(1, mdItem.getDependencies().size());
		PipelineItem startItem = mdItem.getDependencies().iterator().next();
		assertNotNull(startItem);
		assertEquals(Pipeline.KEY_INPUT_ITEM, startItem.getID());
		assertEquals(EnumPipelineType.FULL, pipeline.getPipelineType());
	}

	@Test
	public void testComplexPipelineWithCombinedCgEd() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_COMBINED_CG_ED.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		PipelineItem outputItem = pipeline.getOutputItem();
		assertEquals(Pipeline.KEY_OUTPUT_ITEM, outputItem.getID());
		assertEquals(1, outputItem.getDependencies().size());
		PipelineItem cgEdItem = outputItem.getDependencies().iterator().next();
		assertNotNull(cgEdItem);
		assertEquals(EnumComponentType.CG_ED.id + "1", cgEdItem.getID());
		assertEquals(1, cgEdItem.getDependencies().size());
		PipelineItem mdItem = cgEdItem.getDependencies().iterator().next();
		assertNotNull(mdItem);
		assertEquals(EnumComponentType.MD.id + "1", mdItem.getID());
		assertEquals(1, mdItem.getDependencies().size());
		PipelineItem startItem = mdItem.getDependencies().iterator().next();
		assertNotNull(startItem);
		assertEquals(Pipeline.KEY_INPUT_ITEM, startItem.getID());
		assertEquals(EnumPipelineType.FULL, pipeline.getPipelineType());
	}

	@Test(expected = PipelineException.class)
	public void testComplexPipelineMissingStartComponent() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_COMBINED_CG_ED.path);
		json.put("startComponents", new JSONArray()); // remove start components
		PipelineBuilder builder = new PipelineBuilder(json, null);
		builder.buildPipeline();
	}

	@Test(expected = PipelineException.class)
	public void testComplexPipelineMissingEndComponent() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_COMBINED_CG_ED.path);
		json.put("endComponents", new JSONArray()); // remove end components
		PipelineBuilder builder = new PipelineBuilder(json, null);
		builder.buildPipeline();
	}

	@Test
	public void testComplexPipelineWithMdOnly() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_MD_ONLY.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		PipelineItem outputItem = pipeline.getOutputItem();
		assertEquals(Pipeline.KEY_OUTPUT_ITEM, outputItem.getID());
		assertEquals(1, outputItem.getDependencies().size());
		PipelineItem mdItem = outputItem.getDependencies().iterator().next();
		assertNotNull(mdItem);
		assertEquals(EnumComponentType.MD.id + "1", mdItem.getID());
		assertEquals(1, mdItem.getDependencies().size());
		PipelineItem startItem = mdItem.getDependencies().iterator().next();
		assertNotNull(startItem);
		assertEquals(Pipeline.KEY_INPUT_ITEM, startItem.getID());
		assertEquals(EnumPipelineType.MD, pipeline.getPipelineType());
	}

	// TODO tests for CG only, ED only, MD & CG only, CG & ED only, CG_ED only

	@Test
	public void testComplexPipelineWithTwoMdAndSplitterAndCombiner() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_TWO_MD_SP_CO.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		// Output
		PipelineItem outputItem = pipeline.getOutputItem();
		assertEquals(Pipeline.KEY_OUTPUT_ITEM, outputItem.getID());
		assertEquals(1, outputItem.getDependencies().size());
		// CG_ED
		PipelineItem cgEdItem = outputItem.getDependencies().iterator().next();
		assertNotNull(cgEdItem);
		assertEquals(EnumComponentType.CG_ED.id + "1", cgEdItem.getID());
		assertEquals(1, cgEdItem.getDependencies().size());
		// Combiner
		PipelineItem coItem = cgEdItem.getDependencies().iterator().next();
		assertNotNull(coItem);
		assertEquals(EnumComponentType.COMBINER.id + "1", coItem.getID());
		assertEquals(2, coItem.getDependencies().size());
		// MD 1
		Iterator<PipelineItem> iter = coItem.getDependencies().iterator();
		PipelineItem mdItem1 = iter.next();
		assertNotNull(mdItem1);
		assertEquals(EnumComponentType.MD.id + "1", mdItem1.getID());
		assertEquals(1, mdItem1.getDependencies().size());
		// MD 2
		PipelineItem mdItem2 = iter.next();
		assertNotNull(mdItem2);
		assertEquals(EnumComponentType.MD.id + "2", mdItem2.getID());
		assertEquals(1, mdItem2.getDependencies().size());
		// Splitter
		PipelineItem spItem1 = mdItem1.getDependencies().iterator().next();
		PipelineItem spItem2 = mdItem2.getDependencies().iterator().next();
		assertNotNull(spItem1);
		assertNotNull(spItem2);
		assertEquals(spItem1, spItem2); // continue for spItem1 only as they should be the same
		assertEquals(EnumComponentType.SPLITTER.id + "1", spItem1.getID());
		assertEquals(1, spItem1.getDependencies().size());
		// Input
		PipelineItem startItem = spItem1.getDependencies().iterator().next();
		assertNotNull(startItem);
		assertEquals(Pipeline.KEY_INPUT_ITEM, startItem.getID());
		assertEquals(EnumPipelineType.FULL, pipeline.getPipelineType());
	}

	@Test
	public void testComplexPipelineWithTwoCgEdAndSplitterAndCombiner() throws PipelineException {
		JSONObject json = loadPipelineJson(TestFileEnum.COMPLEX_PIPELINE_TWO_CG_ED_SP_CO.path);
		PipelineBuilder builder = new PipelineBuilder(json, null);
		Pipeline pipeline = builder.buildPipeline();
		// Output
		PipelineItem outputItem = pipeline.getOutputItem();
		assertEquals(Pipeline.KEY_OUTPUT_ITEM, outputItem.getID());
		assertEquals(1, outputItem.getDependencies().size());
		// Combiner
		PipelineItem coItem = outputItem.getDependencies().iterator().next();
		assertNotNull(coItem);
		assertEquals(EnumComponentType.COMBINER.id + "1", coItem.getID());
		assertEquals(2, coItem.getDependencies().size());
		// CG_ED 1
		Iterator<PipelineItem> iter = coItem.getDependencies().iterator();
		PipelineItem cgEdItem1 = iter.next();
		assertNotNull(cgEdItem1);
		assertEquals(EnumComponentType.CG_ED.id + "1", cgEdItem1.getID());
		assertEquals(1, cgEdItem1.getDependencies().size());
		// CG_ED 2
		PipelineItem cgEdItem2 = iter.next();
		assertNotNull(cgEdItem2);
		assertEquals(EnumComponentType.CG_ED.id + "2", cgEdItem2.getID());
		assertEquals(1, cgEdItem2.getDependencies().size());
		// Splitter
		PipelineItem spItem1 = cgEdItem1.getDependencies().iterator().next();
		PipelineItem spItem2 = cgEdItem2.getDependencies().iterator().next();
		assertNotNull(spItem1);
		assertNotNull(spItem2);
		assertEquals(spItem1, spItem2);
		PipelineItem spItem = spItem1; // continue for spItem1 only as they should be the same
		assertEquals(EnumComponentType.SPLITTER.id + "1", spItem.getID());
		assertEquals(1, spItem.getDependencies().size());
		// MD
		PipelineItem mdItem = spItem.getDependencies().iterator().next();
		assertNotNull(mdItem);
		assertEquals(EnumComponentType.MD.id + "1", mdItem.getID());
		assertEquals(1, mdItem.getDependencies().size());
		// Input
		PipelineItem startItem = mdItem.getDependencies().iterator().next();
		assertNotNull(startItem);
		assertEquals(Pipeline.KEY_INPUT_ITEM, startItem.getID());
		assertEquals(EnumPipelineType.FULL, pipeline.getPipelineType());
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
