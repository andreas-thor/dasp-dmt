package dmt.task;


public class TaskException {



	private final String string;
	private final String message;
	private final String info;

    public TaskException(final Exception e, final String info) {
		this.info = info; 
		this.string = e.toString();
		this.message = e.getMessage();
	}
    
    public String getInfo() {
        return info;
    }

    public String getMessage() {
        return message;
    }

    public String getString() {
        return string;
    }
}