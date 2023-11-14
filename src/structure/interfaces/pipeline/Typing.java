package structure.interfaces.pipeline;

import structure.datatypes.AnnotatedDocument;

public interface Typing extends PipelineComponent {

    public AnnotatedDocument ner(final AnnotatedDocument document) throws Exception;

    public default AnnotatedDocument ner(final String text) throws Exception {
        final AnnotatedDocument document = new AnnotatedDocument(text);
        return ner(document);
    }
}
