package dmt.task.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.json.JsonObject;
import javax.sql.DataSource;

import dmt.task.TaskResult;
import dmt.task.TaskFactory.TASKTYPE;

public class TaskSchema extends TaskSQL {

	protected String table = null;
	protected boolean sortRelevant = false;

	/**
	 * 
	 * @param taskId
	 */
	public TaskSchema(String repo, String id, JsonObject repoData, JsonObject taskData, DataSource dbSource) throws Exception {
		super(repo, id, repoData, taskData, dbSource);
		this.type = TASKTYPE.SCHEMA;
		
		// make table and schema lowercase to access metadata later
		this.table = taskData.getString("table").toLowerCase(); 
		this.readSchema = this.readSchema.toLowerCase();	
	}



	@Override
	protected void computeAssessment(TaskResult taskResult) {

		HashMap<String, int[]> correctSchema = new HashMap<String, int[]>();	// attribute -> [is PK?, is FK?] 
		
		try {
			// get all attributes
			Statement stmt = this.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.table + " LIMIT 0");
			ResultSetMetaData metaAttr = rs.getMetaData();
			for (int i=1; i<=metaAttr.getColumnCount(); i++) {
				correctSchema.put(metaAttr.getColumnName(i).toLowerCase(), new int[] { 0, 0 });
			}
			rs.close();

			// get primary key attributes
			DatabaseMetaData meta = stmt.getConnection().getMetaData();
			ResultSet primaryKeys = meta.getPrimaryKeys(null, this.readSchema, this.table);
			while (primaryKeys.next()) {
				correctSchema.get(primaryKeys.getString("COLUMN_NAME").toLowerCase())[0] = 1;
			}
			primaryKeys.close();

			// get foreign key attributes
			ResultSet foreignKeys = meta.getImportedKeys(null, this.readSchema, this.table);
            while (foreignKeys.next()) {
				correctSchema.get(foreignKeys.getString("fkcolumn_name").toLowerCase())[1] = 1;
            }
			foreignKeys.close();
			
		} catch (Exception e) {
			this.setException(e, "Exception when checking schema for " + table);
			taskResult.setFeedback("Schema query could not be executed.\n" + e.toString());
			return;
		}
		
		String header = "attribute" + "\t" + "pk" + "\t" + "fk";
        StringBuffer correctString = new StringBuffer(header);
        for (Entry<String, int[]> entry: correctSchema.entrySet()) {
       		correctString.append("\n");
    		correctString.append(entry.getKey());
    		correctString.append("\t");
    		correctString.append(entry.getValue()[0]);
    		correctString.append("\t");
    		correctString.append(entry.getValue()[1]);
        }
		
    	QueryResult qrAnswer, qrSolution;
    	qrSolution = new QueryResult (correctString.toString());
		qrAnswer = new QueryResult(header + "\n" + taskResult.getAnswer()).distinct();
		
		QueryDiff qrDiff = qrSolution.getDifference(qrAnswer, false, false, false);
		taskResult.setPoints (qrDiff.identical ? taskResult.getPoints_max() : 0);
		taskResult.setFeedback (qrDiff.diffExplanation);
	}

}
