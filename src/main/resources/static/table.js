/**
 * Provides an editable HTML table for specifying table content (e.g., result of a query)
 * @param {DOMNode} node the containing HTML node 
 */

function Answer_Table(node) {

    this.node = node;
    this.colSize = 5;
    this.rowSize = 8;

    this.createUI = function () {
        this.node.append('<table class="u-full-width"><tbody id="tableBody"></tbody></table>');

        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {

            // first row is table header (bold font + gray background)
            var style = (rowIdx == 0) ? "font-weight:bold; border-bottom:1px solid; " : "border-bottom:0pt;";
            var row = $("<tr></tr>");
            for (var colIdx = 0; colIdx < this.colSize; colIdx++) {

                var placeholder = (colIdx > 0) ? "" : ((rowIdx > 1) ? "" : ((rowIdx == 0) ? "Attributname..." : "Attributwert..."));
                row.append(`
                    <td style="padding:0px; margin:0px; border-left:1px solid; border-right:1px solid; ${style}">
                        <input placeholder="${placeholder}" style="background-color:#FFFFFF; border-width:1px; margin:0.5em; width:90%" type="text" name="cell_${rowIdx}_${colIdx}">
                    </td>`);
            }
            $('#tableBody').append(row);
        }
    }

    this.getAnswer = function () {

        // read data from HTML table and identify empty rows and columns
        var data = new Array();
        var isEmptyRow = Array(this.rowSize).fill(true);
        var isEmptyCol = Array(this.colSize).fill(true);

        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {
            var row = new Array();
            for (var colIdx = 0; colIdx < this.colSize; colIdx++) {
                row[colIdx] = this.node.find(`input[name='cell_${rowIdx}_${colIdx}']`).val().trim();
                if (row[colIdx].length > 0) {
                    isEmptyRow[rowIdx] = false;
                    isEmptyCol[colIdx] = false; 
                }
            }
            data[rowIdx] = row;
        }

        // generate CSV but ignore empty rows and empty lines
        var result = "";
        var isFirstRow = true;
        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {
            if (isEmptyRow[rowIdx]) continue;

            result += (isFirstRow) ? "" : "\n";
            isFirstRow = false;

            var isFirstCol = true;
            for (var colIdx = 0; colIdx < this.colSize; colIdx++) {
                if (isEmptyCol[colIdx]) continue;

                result += (isFirstCol) ? "" : "\t";
                result += data[rowIdx][colIdx];
                isFirstCol = false;
            }

        }

        return result;
    }

    this.setAnswer = function (answer) {
        var rows = answer.split("\n");
        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {
            var cols = (rowIdx<rows.length) ? rows[rowIdx].split("\t") : [];
            for (var colIdx = 0; colIdx < this.colSize; colIdx++) {
                var value = (colIdx<cols.length) ? cols[colIdx] : "";
                this.node.find(`input[name='cell_${rowIdx}_${colIdx}']`).val(value);
            }

        }
    }


}