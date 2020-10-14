package dmt.task;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import dmt.task.sql.TaskCheck;
import dmt.task.sql.TaskSchema;
import dmt.task.sql.TaskSelect;
import dmt.task.sql.TaskTable;
import dmt.task.sql.TaskView;

@Service
public class TaskFactory {
	
	public static enum TASKTYPE {
		SELECT, VIEW, TABLE, CHECK, SCHEMA, XPATH, XQUERY
	};

	public static Task createTask(String taskId, DataSource dbSource) throws Exception {

		if (taskId == null) {
			throw new Exception("No TaskId.");
		}

		String[] split = taskId.split(":", 2);
		if (split.length != 2) {
			throw new Exception("Wrong TaskId format; must follow <repo>:<id>");
		}
		final String repo = split[0];
		final String id = split[1];

		// load repository
		JsonReader jread = Json.createReader(new ClassPathResource("repo/" + repo + ".json").getInputStream());
		
		JsonObject jrepo = jread.readObject();
		JsonObject repoData = jrepo.getJsonObject("_repo");
		if (repoData == null) {
			throw new Exception("No data found for repo " + repo);
		}
		
		// load task information and task type
		JsonObject taskData = jrepo.getJsonObject(id);
		if (taskData == null) {
			throw new Exception("No data found for task " + taskId);
		}
		String typeStr = taskData.getString("type", "<not found>");
		TASKTYPE type = TASKTYPE.valueOf(typeStr);
		if (type == null) {
			throw new Exception(String.format("Unknown type %s for task %s.", typeStr, taskId)) ;
		}
		

		// create tasks based on type
		switch (type) {
		case SELECT:
			return new TaskSelect(repo, id, repoData, taskData, dbSource);
		case VIEW:
			return new TaskView(repo, id, repoData, taskData, dbSource);
		case CHECK:
			return new TaskCheck(repo, id, repoData, taskData, dbSource);
		case TABLE:
			return new TaskTable(repo, id, repoData, taskData, dbSource);
		case SCHEMA:
			return new TaskSchema(repo, id, repoData, taskData, dbSource);
		default:
			throw new Exception(String.format ("Unsupported type %s for task %s.", type.toString(), taskId));
		}

		
	}
}
