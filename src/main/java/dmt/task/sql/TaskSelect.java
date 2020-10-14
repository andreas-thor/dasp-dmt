package dmt.task.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.json.JsonObject;
import javax.sql.DataSource;

import dmt.task.TaskResult;
import dmt.task.TaskFactory.TASKTYPE;

public class TaskSelect extends TaskSQL {

	private String solution = null;
	private boolean sortRelevant = false;


	public TaskSelect(final String repo, final String id, final JsonObject repoData, final JsonObject taskData, DataSource dbSource) throws Exception {
		super(repo, id, repoData, taskData, dbSource);
		this.type = TASKTYPE.SELECT;
		this.solution = taskData.getString("solution"); // correct query
		this.sortRelevant = taskData.getBoolean("sort", false);
	}

	@Override
	protected void computeAssessment(final TaskResult taskResult) {

		QueryResult qrAnswer, qrSolution;

		try {
			Statement stmt = this.createStatement();
			ResultSet rs = stmt.executeQuery(taskResult.getAnswer());
			qrAnswer = new QueryResult(rs);
			rs.close();
		} catch (final SQLException e) {
			this.setException(e, "Exception when checking answer " + taskResult.getAnswer());
			taskResult.setFeedback("Query could not be executed.\n" + e.toString());
			return;
		}

		try {
			Statement stmt = this.createStatement();
			ResultSet rs = stmt.executeQuery(solution);
			qrSolution = new QueryResult(rs);
			rs.close();
		} catch (final SQLException e) {
			this.setException(e, "Exception when checking solution " + solution);
			taskResult.setFeedback("Solution query could not be executed.\n" + e.toString());
			return;
		}

		QueryDiff qrDiff = qrSolution.getDifference(qrAnswer, sortRelevant, false, false);
		taskResult.setPoints(qrDiff.identical ? taskResult.getPoints_max() : 0);
		taskResult.setFeedback(qrDiff.diffExplanation);
	}

	
	
	
}
