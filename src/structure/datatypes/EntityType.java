package structure.datatypes;

public class EntityType {
    private String uri;
    private String text;

    public EntityType() {
    }

    public EntityType(String uri, String text) {
        this.uri = uri;
        this.text = text;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String id) {
        this.uri = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
