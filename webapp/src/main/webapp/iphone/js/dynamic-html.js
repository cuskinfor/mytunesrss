function createPager(middlebutton, totalResults, itemsPerPage) {
    html = "<table border='0' cellspacing='0px' cellpadding='0px' width='100%'><tr>";
    if (first > 0) {
        html += "<td align='center' onclick='first=0;buildList();return false'><input type=submit style='font-size:14px; width:40px' value='|<'></td>";
        html += "<td align='center' onclick='first=" + (first - pageSize > 0 ? first - pageSize : 0) + ";buildList();return false'><input type=submit style='font-size:14px; width:40px' value='<'></td>";
    } else {
        html += "<td align='center'><input type=submit style='font-size:14px; width:40px' value='|<'></td>";
        html += "<td align='center'><input type=submit style='font-size:14px; width:40px' value='<'></td>";
    }
    html += middlebutton;
    if (first + 10 < totalResults) {
        var lastPageFirstItem = first;
        while (lastPageFirstItem + 10 < totalResults) {
            lastPageFirstItem += 10;
        }
        html += "<td align='center' onclick='first=" + (first + pageSize) + ";buildList();return false'><input type=submit style='font-size:14px; width:40px' value='>'></td>";
        html += "<td align='center' onclick='first=" + lastPageFirstItem + ";buildList();return false'><input type=submit style='font-size:14px; width:40px' value='>|'></td>";
    } else {
        html += "<td align='center'><input type=submit style='font-size:14px; width:40px' value='>'></td>";
        html += "<td align='center'><input type=submit style='font-size:14px; width:40px' value='>|'></td>";
    }
    html += "</tr></table>";
    return html;    
}

function createRegister() {
    var html = '<table border="0" cellspacing="0px" cellpadding="0px" width="100%">';
    html += '<tr>';
    html += '<td align="center" onclick="first=0;loadItems(0)"><input type=submit style="font-size:14px; width:50px" value="0-9"></td>';
    html += '<td align="center" onclick="first=0;loadItems(1)"><input type=submit style="font-size:14px; width:50px" value="A-C"></td>';
    html += '<td align="center" onclick="first=0;loadItems(2)"><input type=submit style="font-size:14px; width:50px" value="D-F"></td>';
    html += '<td align="center" onclick="first=0;loadItems(3)"><input type=submit style="font-size:14px; width:50px" value="G-I"></td>';
    html += '<td align="center" onclick="first=0;loadItems(4)"><input type=submit style="font-size:14px; width:50px" value="J-L"></td>';
    html += '</tr>';
    html += '<tr>';
    html += '<td align="center" onclick="first=0;loadItems(5)"><input type=submit style="font-size:14px; width:50px" value="M-O"></td>';
    html += '<td align="center" onclick="first=0;loadItems(6)"><input type=submit style="font-size:14px; width:50px" value="P-S"></td>';
    html += '<td align="center" onclick="first=0;loadItems(7)"><input type=submit style="font-size:14px; width:50px" value="T-V"></td>';
    html += '<td align="center" onclick="first=0;loadItems(8)"><input type=submit style="font-size:14px; width:50px" value="W-Z"></td>';
    html += '<td align="center" onclick="first=0;loadItems(9)"><input type=submit style="font-size:14px; width:50px" value="All"></td>';
    html += '</tr>';
    html += '</table>';
    return html;
}

function getDisplayName(name) {
    if (name == 'undefined' || name == null || name == '!') {
        return "Unknown";
    }
    return name;
}
