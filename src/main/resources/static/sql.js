
/**
 * Provides a text area for input of SQL code
 * @param {DOMNode} node the containing HTML node 
 */
function Answer_SQL(node) {

    this.node = node;

    this.createUI = function () {
        this.node.append('<textarea class="u-full-width" placeholder="Geben Sie Ihren SQL-Code hier ein ..." id="taskAnswer"></textarea>');
    }

    this.getAnswer = function () {
        return this.node.find("textarea").val();
    }

    this.setAnswer = function (answer) {
        this.node.find("textarea").val(answer);
    }

}