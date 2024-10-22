import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class Sql_training extends Frame {
    JPanel pnl1, pnl2, pnl3, pnlQuery, pnlResult;
    JLabel lblSrv, lblDb, lblLstTables, lblQuery, lblResult;
    JButton btnSrv, btnDb, btnExecute;
    JTextField txtServer, txtUsername, txtPassword, txtQuery;
    JTextArea txtaResult;
    JScrollPane sclTextArea;
    JComboBox<String> lstDbs;
    JList<String> tabelsDisplayer;
    Connection connection;
    Statement sttm;
    DefaultListModel<String> listTables;
    String selectedtable;

    public Sql_training() {
        super("SQL Training");
        this.setSize(900, 700);

        // Labels
        lblSrv = new JLabel("Server    :");
        txtServer = new JTextField("localhost:3306", 20);
        txtUsername = new JTextField("yassine", 20);
        txtPassword = new JTextField("yassine", 20);
        btnSrv = new JButton("Connect to server");
        lblDb = new JLabel("Data bases");
        lstDbs = new JComboBox<>();
        btnDb = new JButton("Connect to DB");

        // Panel 1 setup
        pnl1 = new JPanel();
        pnl1.setLayout(new GridLayout(5, 2));
        pnl1.add(lblSrv);
        pnl1.add(txtServer);
        pnl1.add(txtUsername);
        pnl1.add(txtPassword);
        pnl1.add(btnSrv);
        pnl1.add(new JPanel());
        pnl1.add(lblDb);
        pnl1.add(new JPanel());
        pnl1.add(lstDbs);
        pnl1.add(btnDb);

        // Buttons size
        btnSrv.setPreferredSize(new Dimension(150, 30));
        btnDb.setPreferredSize(new Dimension(150, 30));

        // Panel 2 setup
        lblLstTables = new JLabel("Tables List");
        tabelsDisplayer = new JList<>();
        pnl2 = new JPanel();
        pnl2.setLayout(new BorderLayout());
        pnl2.add(lblLstTables, BorderLayout.NORTH);
        pnl2.add(tabelsDisplayer, BorderLayout.CENTER);

        // Query setup
        lblQuery = new JLabel("|  Query :");
        txtQuery = new JTextField("", 100);

        // Query panel setup
        pnlQuery = new JPanel();
        pnlQuery.setLayout(new BorderLayout());
        pnlQuery.add(lblQuery, BorderLayout.NORTH);
        pnlQuery.add(txtQuery, BorderLayout.CENTER);

        // Execute button
        btnExecute = new JButton("Execute");
        btnExecute.setPreferredSize(new Dimension(150, 30));  // Set preferred size for Execute button

        // Result setup
        lblResult = new JLabel("  Result :");
        txtaResult = new JTextArea(20, 10);
        txtaResult.setEditable(false);
        sclTextArea = new JScrollPane(txtaResult);

        // Result panel setup
        pnlResult = new JPanel();
        pnlResult.setLayout(new BorderLayout());
        pnlResult.add(lblResult, BorderLayout.NORTH);
        pnlResult.add(sclTextArea, BorderLayout.CENTER); // Use JScrollPane instead of JTextArea directly

        // Main panel setup
        pnl3 = new JPanel();
        pnl3.setLayout(new GridLayout(3, 1));
        pnl3.add(pnlQuery);
        pnl3.add(btnExecute);
        pnl3.add(pnlResult);

        // Frame layout setup
        this.setLayout(new BorderLayout());
        this.add(pnl1, BorderLayout.NORTH);
        this.add(pnl2, BorderLayout.WEST);
        this.add(pnl3, BorderLayout.CENTER);

        // Window close operation
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int rep = JOptionPane.showConfirmDialog(Sql_training.this, "Are you sure you want to exit SQL Training?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (rep == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        this.setVisible(true);
        btnSrv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        btnDb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useDatabase();
            }
        });

        tabelsDisplayer.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ResultSet rs;

                selectedtable = tabelsDisplayer.getSelectedValue();
                String query = "Select * from " + selectedtable;
                try{
                    sttm = connection.createStatement();
                    rs = sttm.executeQuery(query);
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    int nbc = resultSetMetaData.getColumnCount();
                    String schema = "Table :" +selectedtable + "\n";
                    for (int i = 1; i < nbc; i++)
                    {
                        schema += "   |----" + resultSetMetaData.getColumnName(i) +" "+resultSetMetaData.getColumnTypeName(i) +" (" +resultSetMetaData.getPrecision(i) + ")\n";
                    }
                    txtaResult.setText(schema);
                }catch( SQLException ex)
                {
                    System.out.println("Error :" + ex.getMessage());
                }
            }
        });
        btnExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = txtQuery.getText().trim();
                if (!query.isEmpty()) {
                    try {
                        sttm = connection.createStatement();
                        boolean isResultSet = sttm.execute(query);

                        if (isResultSet) {
                            // It's a SELECT query, process the result set
                            ResultSet rs = sttm.getResultSet();
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            // Prepare result header
                            StringBuilder result = new StringBuilder();
                            for (int i = 1; i <= columnCount; i++) {
                                result.append(metaData.getColumnName(i)).append("\t");
                            }
                            result.append("\n");

                            // Append each row of data
                            while (rs.next()) {
                                for (int i = 1; i <= columnCount; i++) {
                                    result.append(rs.getString(i)).append("\t");
                                }
                                result.append("\n");
                            }

                            // Display the result in the text area
                            txtaResult.setText(result.toString());
                        } else {
                            // It's an UPDATE/INSERT/DELETE query, show the number of affected rows
                            int rowsAffected = sttm.getUpdateCount();
                            txtaResult.setText("Query executed successfully. Rows affected: " + rowsAffected);
                        }

                    } catch (SQLException ex) {
                        txtaResult.setText("Error executing query: " + ex.getMessage());
                    }
                } else {
                    txtaResult.setText("Please enter a query to execute.");
                }
            }
        });

    }

    public void useDatabase(){
        try{
            String selectDatabase = (String) lstDbs.getSelectedItem();
            System.out.println(selectDatabase);
            String query = "USE " + selectDatabase;
            Statement statement = connection.createStatement();
            statement.execute(query);
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet tables = metadata.getTables(selectDatabase, null, "%", new String[] {"TABLE"});
            String tableName;
            listTables = new DefaultListModel<>();
            while(tables.next()){
                tableName = tables.getString("TABLE_NAME");
                System.out.println(tableName);
                listTables.addElement(tableName);
            }
             tabelsDisplayer.setModel(listTables);
        }catch(SQLException ex){
            System.out.println("Error :" + ex.getMessage());
        }
    }

    public void connect() {
        Connection cnx;
        try {
            String url = "jdbc:mysql://" + txtServer.getText();  // Ensure the URL format is correct
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, txtUsername.getText(), txtPassword.getText());
            updateDatabaseList(connection);
            JOptionPane.showMessageDialog(this, "Connection successful!", "Connection Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (ClassNotFoundException ex) {
            System.out.println("MySQL JDBC Driver not found: " + ex.getMessage());
        } catch (SQLException ex) {
            System.out.println("Connection failed: " + ex.getMessage()); // Print detailed error message
        }
    }
    private void updateDatabaseList(Connection connection) {
        try{
            DatabaseMetaData metaData= connection.getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            lstDbs.removeAllItems();
            String databaseName;
            while (resultSet.next())
            {
                databaseName = resultSet.getString(1);
                lstDbs.addItem(databaseName);
            }

        } catch (SQLException ex) {
            System.out.println("Failed to retrieve database list.");
        }
    }

    public static void main(String[] args) {
        new Sql_training();
    }
}
