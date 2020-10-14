package dmt.task;

/**
 * TaskResult
 */
public class TaskResult {

    private final Task task;
    private final String answer;
    private final int points_max;
    private int points;
    private String feedback;

    public TaskResult(final Task task, final String answer, final int points_max) {
        this.task = task;
        this.answer = answer;
        this.points_max = points_max;
        this.setFeedback(null);
        this.setPoints(0);
    }

    public String getTaskid() {
        return this.task.getTaskid();
    }

    public String getTasktype() {
        return this.task.getTasktype();
    }

    public String getQuestion() {
        return this.task.getQuestion();
    }

    public String getStatus() {
        return this.task.getStatus();
    };

    public String getDefaultAnswer() {
        return this.task.getDefaultAnswer();
    }

    public String getFeedback() {
        return feedback;
    }

    public String getAnswer() {
        return answer;
    }

    public int getPoints_max() {
        return points_max;
    }

    public int getPoints() {
        return points;
    }

    public void setFeedback(final String feedback) {
        this.feedback = feedback;
    }

    public void setPoints(final int points) {
        this.points = points;
    }
}