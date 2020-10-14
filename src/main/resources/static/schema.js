/**
 * Provides a simple schema definition: attribute names + is (part of) primary key and/or (part of any) foreign key
 * @param {DOMNode} node the containing HTML node 
 */

function Answer_Schema(node) {

    this.node = node;
    this.rowSize = 6;

    this.createUI = function () {
        this.node.append(
            `<table class="u-full-width">
                <thead>
                    <tr><th>Attributname</th><th style="text-align:center;">Primärschlüssel</th><th style="text-align:center;">Fremdschlüssel</th></tr>
                </thead>
                <tbody id="tableBody"></tbody>
            
            </table>`);

        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {

            var placeholder = (rowIdx > 0) ? "" : "Attributname...";

            $('#tableBody').append(`
                <tr>
                    <td style="padding:0px; margin:0px; border-width:0px; ">
                        <input placeholder="${placeholder}" style="background-color:#FFFFFF; margin:0.5em; width:90%" type="text" name="attribute_${rowIdx}">
                    </td>
                    <td style="text-align:center; padding:0px; margin:0px; border-width:0px; ">
                        <input type="checkbox" name="pk_${rowIdx}">
                    </td>
                    <td style="text-align:center; padding:0px; margin:0px; border-width:0px; ">
                        <input type="checkbox" name="fk_${rowIdx}">
                    </td>
                </tr>`);
        }
    }

    this.getAnswer = function () {

        var result = "";
        var isFirstRow = true;

        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {
            
            var attribute = this.node.find(`input[name='attribute_${rowIdx}']`).val().trim();
            if (attribute.length == 0) continue;
            
            result += (isFirstRow) ? "" : "\n";
            isFirstRow = false;

            result += attribute;
            result += "\t";
            console.log(this.node.find(`input[name='pk_${rowIdx}']`));  
            result += this.node.find(`input[name='pk_${rowIdx}']`).prop('checked') ? "1" : "0";
            result += "\t";
            result += this.node.find(`input[name='fk_${rowIdx}']`).prop('checked') ? "1" : "0";
        }

        return result;
    }

    this.setAnswer = function (answer) {
        var rows = answer.split("\n");
        for (var rowIdx = 0; rowIdx < this.rowSize; rowIdx++) {
            var data = (rowIdx<rows.length) ? rows[rowIdx].split("\t") : ["", "0", "0"];
            this.node.find(`input[name='attribute_${rowIdx}']`).val(data[0]);
            this.node.find(`input[name='pk_${rowIdx}']`).prop('checked', data[1]=="1");
            this.node.find(`input[name='fk_${rowIdx}']`).prop('checked', data[2]=="1");
        }
    }


}