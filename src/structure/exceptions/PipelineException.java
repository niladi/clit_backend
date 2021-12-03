package structure.exceptions;

public class PipelineException extends Exception {
	
	private String pipelineItemId; 
	
    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, String pipelineItemId) {
        super(message);
        this.pipelineItemId = pipelineItemId;
    }

	public String getPipelineItemId() {
		return pipelineItemId;
	}

}
