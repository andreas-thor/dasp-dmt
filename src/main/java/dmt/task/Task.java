package dmt.task;

import javax.json.JsonObject;

import dmt.task.TaskFactory.TASKTYPE;

public abstract class Task {

	protected TASKTYPE type = null;
	
	private final String repo;
	private final String id;
	private final String question;
	private final String defaultAnswer;
	private String status;

	private TaskException exception = null;

	public Task(final String repo, final String id, final JsonObject repoData, final JsonObject taskData) throws Exception {
		super();
		this.repo = repo;
		this.id = id;
		this.question = taskData.getString("question");
		this.defaultAnswer = taskData.getString("defaultAnswer", "");
		this.status = null;
	}

	@Override
	public String toString() {
		return String.format("TASK[repo=%s, id=%s, type=%s]", this.repo, this.id, this.type.toString());
	}

	protected void setException(final Exception e, final String info) {
		this.exception = new TaskException(e, info);
	}

	/**
	 * @return taskId = "repo:id"
	 */
	public String getTaskid() {
		return this.repo + ":" + this.id;
	}

	public String getTasktype() {
		return this.type.toString();
	}

	public String getQuestion() {
		return this.question;
	}

	public String getStatus() {
		return this.status;
	}

	public void computeStatus() {
		this.status = computeAndGetStatus();
	}

	public abstract String computeAndGetStatus();

	public String getDefaultAnswer() {
		return this.defaultAnswer;
	}


	public TaskException xxxgetExeption() {
		return this.exception;
	}


	public abstract void close(); 
	

	public TaskResult getTaskResult(final String answer, final int max_points) {
		TaskResult result = new TaskResult(this, answer, max_points);
		computeAssessment(result);
		computeStatus();
		return result;
	};

	protected abstract void computeAssessment(TaskResult taskResult);


}
