package dmt.task.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.sql.DataSource;

import dmt.task.TaskResult;
import dmt.task.TaskFactory.TASKTYPE;

public class TaskCheck extends TaskSQL {

	
	private JsonArray checks = null;

	public TaskCheck(String repo, String id, JsonObject repoData, JsonObject taskData, DataSource dbSource) throws Exception {
		super(repo, id, repoData, taskData, dbSource);
		this.type = TASKTYPE.CHECK;
		this.checks = taskData.getJsonArray("check");
	}

	@Override
	protected void computeAssessment(TaskResult taskResult) {

		taskResult.setPoints(0);
		Statement stmt = null;
		try {
			stmt = this.createStatement();
			stmt.execute(taskResult.getAnswer());
		} catch (Exception e) {
			this.setException(e, "Exception when executing answer " + taskResult.getAnswer());
			taskResult.setFeedback(String.format("Could not execute SQL!\n%s", e.toString()));
			return;
		}

		for (int i = 0; i < this.checks.size(); i++) {
			String sql = this.checks.getJsonObject(i).getString("sql");
			String sqlCompare = this.checks.getJsonObject(i).getString("compare", null);
			String state = this.checks.getJsonObject(i).getString("state", "0");

			if (sqlCompare == null) {	// check for correct sqlState
			
				try {
					stmt.execute(sql);
	
					if (!state.equalsIgnoreCase("0")) {
						taskResult.setFeedback(String.format("Wrong SQLState for '%s' (was 0 but expected %s).", sql, state));
						return;
					}
					
				} catch (SQLException e) {
	
					if (!e.getSQLState().equalsIgnoreCase(state)) {
						this.setException(e, "Exception when executing answer " + taskResult.getAnswer());
						taskResult.setFeedback(String.format("Wrong SQLState for '%s' (was %s but expected %s).\n%s", sql, e.getSQLState(), state, e.toString()));
						return;
					}
	
				}
				
				
				
			} else {	// check for query equivalence
				
				
				QueryResult qrAnswer, qrSolution;

				try {
					ResultSet rs = stmt.executeQuery(sql);
					qrAnswer = new QueryResult(rs);
					rs.close();
				} catch (SQLException e) {
					this.setException(e, "Exception when checking answer using " + sql);
					taskResult.setFeedback("Query could not be executed.\n" + e.toString());
					return;
				}

				try {
					ResultSet rs = stmt.executeQuery(sqlCompare);
					qrSolution = new QueryResult(rs);
					rs.close();
				} catch (SQLException e) {
					this.setException(e, "Exception when checking solution " + sqlCompare);
					taskResult.setFeedback("Solution query could not be executed.\n" + e.toString());
					return;
				}

				QueryDiff qrDiff = qrSolution.getDifference(qrAnswer, this.checks.getJsonObject(i).getBoolean("sort", false), false, false);
				
				if (!qrDiff.identical) {
					taskResult.setFeedback("Check query " + sql + " generated wrong result. " + qrDiff.diffExplanation);
					return;
				}
			}

		}

		taskResult.setPoints(taskResult.getPoints_max());

		return;

	}

}
