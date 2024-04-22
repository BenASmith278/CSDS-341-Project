//class that defines my connection for simplicity
public class connectionURL {
    static String getConnectionString() {
        String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\rxb706;"
                + "database=CrossCountry;"
                + "user=sa;"
                + "password=yQb0OUoXKxtcQZ;"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=15;";
        return connectionUrl;
    }
}
