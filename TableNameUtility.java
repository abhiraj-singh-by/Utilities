// Utility to extract table name from any SQL query.
// Works for complex SQL queries.
// Disclaimer: Functionality to extract from Create SQL query is not added.

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableNameUtility {

    public static void main(String[] args) {
        String input ="CREATE OR REPLACE VIEW [Brazil Customers] AS SELECT CustomerName, ContactName, City FROM Customers, TableA, TableB;";
//        String input = "CREATE OR REPLACE VIEW TEST_ITEM_APPVIEW AS SELECT item.PRODUCTHIERARCHYL2,item.PRODUCTHIERARCHYL3 ,location.locationId FROM A.A.CTRD_ITEM_MASTER_VIEW as item, CTRD_LOCATION_MASTER_VIEW as location, CTRD_LOCATION as location;";
        String tables = utility(input);
        System.out.println(tables);
    }

    private static String utility(String query) {
        query = query.substring(0, query.length() - 1);        // remove semi-colon
        query = query.replaceAll("\\s+", " "); // remove extra spaces
        query = query.replace(", ", "_next_"); // for multiple tables
        query = query.replace(",", "_next_");  // for multiple tables
        query = query.replace(")", " where ");
        query = query.replace("(", " where ");
        query = query.replace("-", "");
        query = query.replace("\"", "");
//        System.out.println(query);

        // From "from" to "keywords"
        Pattern p1 = Pattern.compile("from\\s+(?:\\w+\\.)*(\\w+)(\\s*$|\\s+(AS|WHERE|JOIN|START|LEFT|RIGHT\\s+WITH|ORDER|ON\\s+BY|GROUP\\s+BY))",Pattern.CASE_INSENSITIVE);
        String str = "";
        Matcher m1 = p1.matcher(query);
        while(m1.find())
            str = str + m1.group(1) + " ";

        // From "join" to "keywords"  for multiple table joins
        Pattern p2 = Pattern.compile("join\\s+(?:\\w+\\.)*(\\w+)(\\s*$|\\s+(AS|ON\\s+))",Pattern.CASE_INSENSITIVE);
        str+=" ";
        Matcher m2 = p2.matcher(query);
        while(m2.find())
            str = str + m2.group(1) + " ";

        // From "into" to "keywords"  for multiple table joins
        Pattern p3 = Pattern.compile("to\\s+(?:\\w+\\.)*(\\w+)(\\s*$|\\s+(where\\s+))",Pattern.CASE_INSENSITIVE);
        str+=" ";
        Matcher m3 = p3.matcher(query);
        while(m3.find())
            str = str + m3.group(1) + " ";

        // From "update" to "keywords"  for multiple table joins
        Pattern p4 = Pattern.compile("update\\s+(?:\\w+\\.)*(\\w+)(\\s*$|\\s+(set\\s+))",Pattern.CASE_INSENSITIVE);
        str+=" ";
        Matcher m4 = p4.matcher(query);
        while(m4.find())
            str = str + m4.group(1) + " ";

        // _next_ at places where commas so that we don't miss multiple tables after from
        // @ is _next_  # is as
        // @table_name# is a format to cover the scenario as mentioned below
        // "from tableA as a, tableB as b
        query = query.replace("_next_", " @");
        query = query.replace(" as ", "# ");
        query = query.replace(" AS ", "# ");
        query = query.replace(" As ", "# ");

        for(int i = 0; i < query.length(); i++) {
            if(query.charAt(i) == '@') {
                query = query.substring(i);
                break;
            }
        }

        // extract table names between @ and #
        // if there is space in between it will not be a table name
        if(query.contains("#")) {
            String[] view = StringUtils.substringsBetween(query, "@","#");

            for (int i = 0; i < view.length; i++) {
                if(!view[i].contains(" ")) str = str + " " + view[i];
            }
        }

        // replace _next_ in string we finally get to bring spaces between table names
        str = str.replace("_next_", " ");
        str = str.replaceAll("\\s+", " "); // remove extra spaces

        // there can be spaces in starting and ending of string
        return str.trim();
    }
}
