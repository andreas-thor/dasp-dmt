package dmt;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dmt.task.Task;
import dmt.task.TaskFactory;
import dmt.task.TaskResult;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class Controller {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping("/dmt/gettaskinfo")
	public Task getTaskInfo(@RequestParam("taskid") final String taskid) throws ResponseStatusException {

		Task t = null;
		Logger logger = LoggerFactory.getLogger(Controller.class);
		ObjectMapper Obj = new ObjectMapper(); 

		try {
			t = TaskFactory.createTask(taskid, jdbcTemplate.getDataSource());
			t.computeStatus();
			logger.info("request:{}, taskid:{}, task:{}", "gettaskinfo", taskid, Obj.writeValueAsString(t));
			return t;	
		} catch (final Exception e) {
			logger.error("request:{}, taskid:{}, exception:{}", "gettaskinfo", taskid, e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} finally {
			if (t != null)
				t.close();
		}

	}



	@RequestMapping("/dmt/gettaskresult")
	public TaskResult getTaskResult(
		@RequestParam("taskid") final String taskid, 
		@RequestParam("answer") final String answer,
		@RequestParam(value="points_max", defaultValue = "1") final int points_max) throws ResponseStatusException {
	
		Task t = null;
		TaskResult tr = null;
		Logger logger = LoggerFactory.getLogger(Controller.class);
		ObjectMapper Obj = new ObjectMapper(); 

		try {
			t = TaskFactory.createTask(taskid, jdbcTemplate.getDataSource());
			tr = t.getTaskResult (answer, points_max);
			logger.info("request:{}, taskid:{}, answer:{}, points_max:{}, taskresult:{}", "gettaskresult", taskid, answer, points_max, Obj.writeValueAsString(tr));
			return tr;
		} catch (Exception e) {
			logger.info("request:{}, taskid:{}, answer:{}, points_max:{}, exception:{}", "gettaskresult", taskid, answer, points_max, e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} finally {
			if (t!=null) t.close();
		}
	}

}