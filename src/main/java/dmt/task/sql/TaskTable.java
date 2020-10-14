package dmt.task.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.json.JsonObject;
import javax.sql.DataSource;

import dmt.task.TaskResult;
import dmt.task.TaskFactory.TASKTYPE;

public class TaskTable extends TaskSQL {

	protected String solution = null;
	protected boolean sortRelevant = false;

	/**
	 * 
	 * @param taskId
	 */
	public TaskTable(String repo, String id, JsonObject repoData, JsonObject taskData, DataSource dbSource) throws Exception {
		super(repo, id, repoData, taskData, dbSource);
		this.type = TASKTYPE.TABLE;
		this.solution = taskData.getString("solution"); // correct query
		this.sortRelevant = taskData.getBoolean("sort", false);
	}



	@Override
	protected void computeAssessment(TaskResult taskResult) {

		QueryResult qrAnswer, qrSolution;

		qrAnswer = new QueryResult(taskResult.getAnswer());
		
		try {
			Statement stmt = this.createStatement();
			ResultSet rs = stmt.executeQuery(solution);
			qrSolution = new QueryResult(rs);
			rs.close();
		} catch (SQLException e) {
			this.setException(e, "Exception when checking solution " + solution);
			taskResult.setFeedback("Solution query could not be executed.\n" + e.toString());
			return;
		}

		QueryDiff qrDiff = qrSolution.getDifference(qrAnswer, sortRelevant, false, true);
		taskResult.setPoints(qrDiff.identical ? taskResult.getPoints_max() : 0);
		taskResult.setFeedback (qrDiff.diffExplanation);
		
	}

}
