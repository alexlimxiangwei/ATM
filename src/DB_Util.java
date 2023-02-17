import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class DB_Util {
    public static ArrayList<Bank> fetchBanks(Connection conn){
        ArrayList<Bank> banks = new ArrayList<>();

        try {
//         Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
            Statement stmt = conn.createStatement();
            // Step 3: Write a SQL query string. Execute the SQL query via the 'Statement'.
            //  The query result is returned in a 'ResultSet' object called 'rset'.
            String strSelect = "select * from bank";
//            System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging

            ResultSet rset = stmt.executeQuery(strSelect);

            // Step 4: Process the 'ResultSet' by scrolling the cursor forward via next().
            //  For each row, retrieve the contents of the cells with getXxx(columnName).
//            System.out.println("The records selected are:");
//            int rowCount = 0;
            // Row-cursor initially positioned before the first row of the 'ResultSet'.
            // rset.next() inside the whole-loop repeatedly moves the cursor to the next row.
            // It returns false if no more rows.
            while (rset.next()) {   // Repeatedly process each row
                int idBank = rset.getInt("idBank");  // retrieve a 'String'-cell in the row
                String name = rset.getString("name");  // retrieve a 'double'-cell in the row
                boolean local = rset.getBoolean("local");       // retrieve a 'int'-cell in the row
                banks.add(new Bank(idBank, name, local));
//                System.out.println(idBank + ", " + name + ", " + local);
//                ++rowCount;
            }
//            System.out.println("Total number of records = " + rowCount);
        }
         catch(SQLException ex) {
            ex.printStackTrace();
        }
        return banks;
    }
}
