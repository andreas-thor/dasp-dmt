package dmt.task.sql;

import java.sql.ResultSet;
import java.sql.Statement;

import javax.json.JsonObject;
import javax.sql.DataSource;

import dmt.task.TaskFactory.TASKTYPE;
import dmt.task.TaskResult;

public class TaskView extends TaskSQL {

	private String solution = null;
	private String viewName = null;

	public TaskView(String repo, String id, JsonObject repoData, JsonObject taskData, DataSource dbSource)
			throws Exception {
		super(repo, id, repoData, taskData, dbSource);
		this.type = TASKTYPE.VIEW;
		this.solution = taskData.getString("solution"); // correct query
		this.viewName = taskData.getString("viewname"); // name of view
	}

	protected void computeAssessment(TaskResult taskResult) {

		Statement stmt = null;
		try {
			stmt = this.createStatement();
			stmt.execute(taskResult.getAnswer());
		} catch (Exception e) {
			this.setException(e, "Exception when executing answer " + taskResult.getAnswer());
			taskResult.setFeedback(String.format("Could not execute SQL!\n%s", e.toString()));
			return;
		}

		QueryResult qrAnswer, qrSolution;

		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.viewName);
			qrAnswer = new QueryResult(rs);
			rs.close();
		} catch (Exception e) {
			this.setException(e, "Exception when reading view " + this.viewName);
			taskResult.setFeedback(String.format("Could not find view %s.\n%s", this.viewName, e.toString()));
			return;
		}

		try {
			ResultSet rs = stmt.executeQuery(this.solution);
			qrSolution = new QueryResult(rs);
			rs.close();
		} catch (Exception e) {
			this.setException(e, "Exception when checking solution " + this.solution);
			taskResult.setFeedback(String.format("Solution query could not be executed.\n%s", e.toString()));
			return;
		}

		QueryDiff qrDiff = qrSolution.getDifference(qrAnswer, false, false, false);
		taskResult.setPoints(qrDiff.identical ? taskResult.getPoints_max() : 0);
		taskResult.setFeedback(qrDiff.diffExplanation);


	}



}
