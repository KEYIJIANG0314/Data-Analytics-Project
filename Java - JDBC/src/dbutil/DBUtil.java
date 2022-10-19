package dbutil;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class DBUtil {
    private static final String DRIVER_NAME = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://localhost/postgres";
    private static final String USER_NAME = "postgres";
    private static final String PASSWORD = "SelfC0704.";
    private Connection conn = null;

    public DBUtil() {
        try {
            Class.forName(DRIVER_NAME);
            this.conn = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet runSelectQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    void runInsertQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
    }

    public void addForest() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Adding forest... Plz input the following fields");
        System.out.print("Forest no: ");
        String no = sc.next();
        sc.nextLine();
        System.out.print("Forest name: ");
        String name = sc.nextLine().strip();
        System.out.print("Area: ");
        Double area = sc.nextDouble();
        System.out.print("Acid level: ");
        Double acidLevel = sc.nextDouble();
        System.out.print("X min: ");
        Double xMin = sc.nextDouble();
        System.out.print("X max: ");
        Double xMax = sc.nextDouble();
        System.out.print("Y min: ");
        Double yMin = sc.nextDouble();
        System.out.print("Y max: ");
        Double yMax = sc.nextDouble();
        System.out.print("State(Abbreviation): ");
        String abbr = sc.next();

        try {
            conn.setAutoCommit(false);
            // As name has no key constraint, check it separately
            if (runSelectQuery(String.format("SELECT * FROM FOREST WHERE name = %s", name)).next()) {
                System.out.println("â�Œ Insertion error: name already exists");
                return;
            }
            // Insert
            runInsertQuery(
                    String.format(
                            "INSERT INTO FOREST VALUES ('%s','%s', %f, %f, %f, %f, %f, %f)",
                            no, name, area, acidLevel, xMin, xMax, yMin, yMax
                    )
            );
            runInsertQuery(
                    String.format(
                            "INSERT INTO COVERAGE VALUES ('%s', '%s', 1, %f)",
                            no, abbr ,area
                    )
            );
            conn.commit();
            System.out.println("===============================");
            System.out.printf("âœ… Forest '%s' added successfully%n", name);
            System.out.println("===============================");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            System.out.println("â�Œ Insertion error: " + e);
        }
    }

    public void addWorker() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Adding worker... Plz input the following fields");
        System.out.print("Worker SSN: ");
        String ssn = sc.next();
        System.out.print("Worker name: ");
        String name = sc.next();
        System.out.print("Worker rank: ");
        int rank = sc.nextInt();
        System.out.print("Employing state abbreviation: ");
        String abbr = sc.next();

        try {
            conn.setAutoCommit(false);
            // Insert
            runInsertQuery(
                    String.format(
                            "INSERT INTO WORKER VALUES ('%s', '%s', %d, '%s')",
                            ssn, name, rank, abbr
                    )
            );
            conn.commit();
            System.out.println("===============================");
            System.out.printf("âœ… Worker '%s' added successfully%n", name);
            System.out.println("===============================");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            System.out.println("â�Œ Insertion error: " + e);
        }
    }

    public void addSensor() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Adding sensor... Plz input the following fields");
        System.out.print("Sensor ID: ");
        int id = sc.nextInt();
        System.out.print("X coord: ");
        Double x = sc.nextDouble();
        System.out.print("Y coord: ");
        Double y = sc.nextDouble();
        System.out.print("Last time charged(mm/dd/yyyy hh24:mi): ");
        String chargeTime = sc.next() + sc.next(); // white space
        System.out.print("Maintainer(SSN, or NULL for null): ");
        String ssn = sc.next();
        System.out.print("Last time report(mm/dd/yyyy hh24:mi): ");
        String reportTime = sc.next() + sc.next(); // white space
        System.out.print("Energy level: ");
        double energy = sc.nextDouble();

        try {
            conn.setAutoCommit(false);
            // As coordinate has no key constraint, check it separately
            if (runSelectQuery(String.format("SELECT * FROM SENSOR WHERE x = %f and y = %f", x, y)).next()) {
                System.out.println("â�Œ Insertion error: coordinate already exists");
                return;
            }
            // Insert
            if (ssn.equals("NULL")) {
                runInsertQuery(
                        String.format(
                                "INSERT INTO SENSOR VALUES (%d,%f,%f,TO_TIMESTAMP('%s', 'mm/dd/yyyy hh24:mi'), NULL,TO_TIMESTAMP('%s', 'mm/dd/yyyy hh24:mi'),%f)",
                                id, x, y, chargeTime, reportTime, energy
                        )
                );

            } else {
                runInsertQuery(
                        String.format(
                                "INSERT INTO SENSOR VALUES (%d,%f,%f,TO_TIMESTAMP('%s', 'mm/dd/yyyy hh24:mi'),'%s',TO_TIMESTAMP('%s', 'mm/dd/yyyy hh24:mi'),%f)",
                                id, x, y, chargeTime, ssn, reportTime, energy
                        )
                );
            }
            conn.commit();
            System.out.println("===============================");
            System.out.printf("âœ… Sensor '%s', %f, %f added successfully%n", id, x, y);
            System.out.println("===============================");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            System.out.println("â�Œ Insertion error: " + e);
        }
    }

    public void switchWorkersDuties() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Switching workers duties... Plz input the two workers' names");
        System.out.print("Worker A: ");
        String nameA = sc.next();
        System.out.print("Worker B: ");
        String nameB = sc.next();

        try {
            conn.setAutoCommit(false);
            // Make sure that A sensors can are in the same employing state of B
            ResultSet workerA = runSelectQuery("SELECT * FROM WORKER WHERE name = '" + nameA + "'");
            if (!workerA.next()) {
                System.out.println("â�Œ Worker A does not exist");
                return;
            }
            ResultSet workerB = runSelectQuery("SELECT * FROM WORKER WHERE name = '" + nameB + "'");
            if (!workerB.next()) {
                System.out.println("â�Œ Worker B does not exist");
                return;
            }

            if (!workerA.getString("employing_state").equals(workerB.getString("employing_state"))) {
                System.out.println("â�Œ Worker A and B are not in same employing state, cannot switch duties");
                return;
            }

            ResultSet workerASensor = runSelectQuery(String.format(
                    "SELECT * FROM SENSOR WHERE maintainer = '%s'",
                    workerA.getString("ssn")
            ));

            // Memorize workerB's sensors' ids
            ArrayList<Integer> workerASensorIds = new ArrayList<Integer>();
            while (workerASensor.next()) {
                workerASensorIds.add(workerASensor.getInt("sensor_id"));
            }

            runInsertQuery(String.format(
                    "UPDATE SENSOR SET maintainer = '%s' WHERE maintainer = '%s'",
                    workerA.getString("ssn"),
                    workerB.getString("ssn")
            ));

            // Set A's sensors' maintainer to B
            PreparedStatement sensor = conn.prepareStatement(
                    "UPDATE SENSOR SET maintainer = ? WHERE sensor_id = ?"
            );
            for (Integer id : workerASensorIds) {
                sensor.setString(1, workerB.getString("ssn"));
                sensor.setInt(2, id);
                sensor.addBatch();
            }
            sensor.executeBatch();
            conn.commit();
            System.out.println("âœ… Duties successfully switched");
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            System.out.println("â�Œ Operation error: " + e);
        }

    }

    public void updateSensorStatus() {
        System.out.println("Updating sensor status... Plz input the following fields: ");
        Scanner sc = new Scanner(System.in);
        System.out.print("X coord: ");
        Double x = sc.nextDouble();
        System.out.print("Y coord: ");
        Double y = sc.nextDouble();
        System.out.print("Last time charged(mm/dd/yyyy hh24:mi): ");
        String chargeTime = sc.next() + sc.next(); // white space
        System.out.print("Energy level: ");
        double energy = sc.nextDouble();
        System.out.print("Temperature: ");
        double temperature = sc.nextDouble();

        try {
            conn.setAutoCommit(false);
            ResultSet sensor = runSelectQuery(String.format(
                    "SELECT * FROM SENSOR WHERE x = %f and y = %f", x, y
            ));
            sensor.next();
            int sensor_id = sensor.getInt("sensor_id");

            runInsertQuery(String.format(
                "UPDATE SENSOR SET energy = %f, last_charged = TO_TIMESTAMP('%s', 'mm/dd/yyyy hh24:mi') WHERE x = %f and y = %f",
                energy, chargeTime, x, y
            ));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            runInsertQuery(String.format(
                    "INSERT INTO REPORT VALUES (%s, TO_TIMESTAMP('%s', 'mm/dd/yyyy hh24:mi'), %f)",
                    sensor_id, sdf.format(new java.util.Date()), temperature
            ));
            conn.commit();
            if (temperature > 100) {
                System.out.println("========= âš ï¸� âš ï¸� âš ï¸� ==========");
                System.out.println("âš ï¸� Sensor's temperature is higher than 100 degree !!! âš ï¸�");
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            System.out.println("â�Œ Update error: " + e);
        }
    }

    public void updateForestCoveredArea() {
        System.out.println("Updating forest covered area... Plz input the following fields: ");
        Scanner sc = new Scanner(System.in);
        System.out.print("Forest name: ");
        String name = sc.nextLine().strip();
        System.out.print("Area: ");
        double area = sc.nextDouble();
        System.out.print("State abbreviation spans: ");
        String abbr = sc.next();

        try {
            conn.setAutoCommit(false);
            ResultSet forest = runSelectQuery("SELECT * FROM FOREST WHERE name = '" + name + "'");
            forest.next();
            double oldArea = forest.getDouble("area");
            String forestNo = forest.getString("forest_no");
            System.out.println("Forest no is " + forestNo + " , area before update is " + oldArea);

            // Update forest
            runInsertQuery(
                    String.format(
                            "UPDATE FOREST SET AREA = %f WHERE NAME = '%s'",
                            area, name
                    )
            );

            // Update coverage
            ResultSet coverages = runSelectQuery(String.format(
                        "SELECT * FROM COVERAGE WHERE forest_no = '%s'",
                        forestNo
                    )
            );
            boolean areaUpdated = false;
            while (coverages.next()) {
                String covAbbr = coverages.getString("state");
                if (covAbbr.equals(abbr)) {
                    areaUpdated = true;
                    runInsertQuery(String.format(
                            "UPDATE COVERAGE SET area = %f WHERE forest_no = '%s' and state = '%s'",
                            coverages.getDouble("area") + area - oldArea,
                            forestNo,
                            abbr
                    ));
                    break;
                }
            }
            if (!areaUpdated) {
                runInsertQuery(String.format(
                        "INSERT INTO COVERAGE VALUES ('%s', '%s', 1, %f)",
                        forestNo, abbr, area - oldArea
                    )
                );
            }
            runInsertQuery(String.format(
                    "UPDATE COVERAGE SET percentage = area / %f",
                    area
            ));
            conn.commit();
            System.out.println("===============================");
            System.out.printf("âœ… Forest '%s' area updated successfully%n", name);
            System.out.println("===============================");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            System.out.println("â�Œ Update error: " + e);
        }
    }

    public void findTopKBusyWorkers() {
        System.out.println("Finding top K busy workers... Plz input the number K: ");
        Scanner sc = new Scanner(System.in);
        int k = sc.nextInt();
        try {
            ResultSet rs = runSelectQuery(
                    String.format("SELECT WORKER.name, COUNT(*) FROM SENSOR INNER JOIN WORKER ON SENSOR.maintainer = WORKER.ssn WHERE SENSOR.energy <= 2 GROUP BY WORKER.name ORDER BY count DESC LIMIT %d", k)
            );
            if (!rs.next()) {
                System.out.println("âš ï¸� There's no busy worker now!");
            } else {
                System.out.println("=============================");
                System.out.println("Busy workers are as follows(with sensor count): ");
                do {
                    System.out.printf("%s(%s)%n", rs.getString(1), rs.getString(2));
                }
                while (rs.next());
                System.out.println("=============================");
            }
        } catch (Exception e) {
            System.out.println("â�Œ Operation error: " + e);
        }
    }

    public void displaySensorsRanking() {
        System.out.println("Displaying sensor's ranking...");
        try {
            ResultSet rs = runSelectQuery(
                    "SELECT SENSOR.sensor_id, COUNT(*) FROM SENSOR INNER JOIN REPORT ON SENSOR.sensor_id = REPORT.sensor_id GROUP BY SENSOR.sensor_id ORDER BY SENSOR.sensor_id"
            );
            System.out.println("=======================");
            System.out.println("SensorID     Count");
            // Extract data from result set
            while (rs.next()) {
                // Retrieve by column name
                System.out.printf("%s            %s%n", rs.getString(1), rs.getString(2));
            }
            System.out.println("=======================");
        } catch (Exception e) {
            System.out.println("â�Œ Operation error: " + e);
        }
    }
}