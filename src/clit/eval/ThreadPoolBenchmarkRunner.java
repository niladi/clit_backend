package clit.eval;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolBenchmarkRunner {
  public static void main(String[] args) {
    String home = System.getProperty("user.home");
    Map<String, String> paths = Map.of(
        "AIDA-YAGO2-dataset.tsv_nif",
        home + "/Desktop/dataset/datasets/conll_aida-yago2-dataset/AIDA-YAGO2-dataset.tsv_nif",
        "KORE_50_DBpedia.ttl", home + "/Desktop/dataset/datasets/KORE50/KORE_50_DBpedia.ttl",
        "News-100.ttl", home + "/Desktop/dataset/datasets/News-100.ttl",
        "RSS-500.ttl", home + "/Desktop/dataset/datasets/RSS-500.ttl",
        "Reuters-128.ttl", home + "/Desktop/dataset/datasets/Reuters-128.ttl");
    Map<String, Boolean> linkers = new HashMap<>();
    linkers.put("AIDA", false); // Error: MD1: https://gate.d5.mpi-inf.mpg.de/aida/service/disambiguate
    linkers.put("Babelfy", true);
    linkers.put("CLOCQ", true);
    linkers.put("DBpediaSpotlight", true);
    linkers.put("EntityClassifierEULinker", false);
    linkers.put("FOX", false);
    linkers.put("Falcon 2.0", true);
    linkers.put("OpenTapioca", true);
    linkers.put("REL", false); // for now
    linkers.put("TagMe", true);
    linkers.put("TextRazor", true);
    linkers.put("spaCy", false); // Error: MD1: Request failed: 503 Service Unavailable
    ExecutorService es = Executors.newFixedThreadPool(linkers.size() * paths.size());
    for (Map.Entry<String, String> pathEntry : paths.entrySet()) {
      String datasetName = pathEntry.getKey();
      String datasetPath = pathEntry.getValue();
      for (Map.Entry<String, Boolean> linkerEntry : linkers.entrySet()) {
        if (!linkerEntry.getValue())
          continue;
        String linker = linkerEntry.getKey();
        BenchmarkMentionDetectionTemplateTest rt = new BenchmarkMentionDetectionTemplateTest(datasetName, datasetPath,
            linker);
        es.submit(rt);
      }
    }
    es.shutdown();


  }

}
