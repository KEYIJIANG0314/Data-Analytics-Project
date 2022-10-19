import dbutil.DBUtil;

import java.sql.ResultSet;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("Welcome to US Forest prototype system!");
        System.out.println("======================================");
        Scanner s = new Scanner(System.in);
        DBUtil db = new DBUtil();
        while (true) {
            System.out.println("Input the number of task you want to do, then press Enter:");
            System.out.println("1) Add Forest; 2) Add Worker; 3) Add Sensor 4) Switch Workers Duties;");
            System.out.println("5) Update Sensor Status; 6) Update Forest Covered Area;");
            System.out.println("7) Find Top-k Busy Workers; 8) Display Sensors Ranking");
            int idx = -1;
            try {
                idx = s.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid Input");
            } 
            try {
            	switch (idx) {
                case 1: db.addForest(); break;
                case 2: db.addWorker(); break;
                case 3: db.addSensor(); break;
                case 4: db.switchWorkersDuties(); break;
                case 5: db.updateSensorStatus(); break;
                case 6: db.updateForestCoveredArea(); break;
                case 7: db.findTopKBusyWorkers(); break;
                case 8: db.displaySensorsRanking(); break;
                default: System.out.println("Input must be int in 1-8");
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
