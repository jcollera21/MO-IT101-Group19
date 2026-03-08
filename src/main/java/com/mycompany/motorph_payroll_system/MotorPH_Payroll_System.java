/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.motorph_payroll_system;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/**
 *
 * @author AM5
 * 
 * MotorPH Payroll System (Basic Version)

 This program searches for an employee in a CSV file using their Employee ID.
 A CSV file is like an Excel sheet where each row contains employee data
 separated by commas.

 The program will:
 1. Ask the user to enter an Employee ID.
 2. Open the Employee_Details.csv file.
 3. Read the file line by line.
 4. Split each row into columns.
 5. Check if the Employee ID matches the one entered by the user.
 6. If found, display the employee's name and birthdate.
 */

public class MotorPH_Payroll_System {

    public static void main(String[] args) {
        // Path to the file that contains employee information
        String empDetails = "resources/Employee_Details.csv";
        
        // File that will be used later for attendance records
        String empAttendance = "resources/Employee_Attendance_Record.csv";
        
        // Scanner is used to get input from the user
        Scanner scan = new Scanner(System.in);
        
        String employeeID = null;
        String firstname = null;
        String lastname = null;
        String birthdate = null;
        
        
        double hourlyRate = 0.0;
        
        
        System.out.print("Enter Employee ID#: ");
        String empID = scan.nextLine();
        
        // This variable checks whether the employee exists in the file
        boolean isEmployee = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(empDetails))){ // Open the CSV file for reading
            br.readLine(); // Skip the first line because it contains column headers
            String line;
            
            while ((line = br.readLine()) != null) { // Read each row of the file until the end
                
                String[] col = line.split(","); // Split the row into columns using comma as separator
                
                if (col.length >= 19 && col[0].trim().equals(empID)){ // Check if the row has enough columns and if the ID matches
                    
                    // Save the first 4 columns of the employee data
                    employeeID = col[0];
                    lastname = col[1];
                    firstname = col[2];
                    birthdate = col[3];
                    
                    String rateText = col[col.length - 1].replace("\"", "").trim(); // Hourly Rate is the last column
                    hourlyRate = Double.parseDouble(rateText); // Converts rateText into double

                    isEmployee = true; // Mark that the employee was found
                    break; // Stop searching because we already found the employee
                }
            }
        }catch (IOException e){
            System.out.println("Error reading employee file!"); // This happens if the program cannot open or read the file
            return;
        }
        
        if(!isEmployee){ // Display the employee information if found
            System.out.println("Employee not found!");
            scan.close();
            return;
        }
        
        /* STORE TOTAL MINUTES PER MONTH AND PER CUTOFF
        Index 0 = June, 1 = July, ..., 6 = December */
        int[] firstCutoffMinutes = new int[7];
        int[] secondCutoffMinutes = new int[7];

        String[] monthNames = {
            "June", "July", "August", "September", "October", "November", "December"
        };

        boolean attendanceFound = false;

        // ==================================================
        // READ ATTENDANCE FILE
        // ==================================================
        try (BufferedReader br = new BufferedReader(new FileReader(empAttendance))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                String[] col = line.split(",");

                if (col.length >= 6) {
                    String employeeNo = col[0].trim();
                    String date = col[3].trim();
                    String logIn = col[4].trim();
                    String logOut = col[5].trim();

                    if (employeeNo.equals(empID)) {
                        int day = getDay(date);
                        int month = getMonth(date);

                        // Process only June to December
                        if (month >= 6 && month <= 12) {
                            int workedMinutes = computeDailyWorkedMinutes(logIn, logOut);

                            int monthIndex = month - 6;

                            if (day <= 15) {
                                firstCutoffMinutes[monthIndex] += workedMinutes;
                            } else {
                                secondCutoffMinutes[monthIndex] += workedMinutes;
                            }

                            attendanceFound = true;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading attendance file.");
            return;
        }

        if (!attendanceFound) {
            System.out.println("No attendance records found for employee " + empID + " from June to December.");
            scan.close();
            return;
        }

        // ==================================================
        // DISPLAY RESULTS
        // ==================================================
        System.out.println();
        System.out.println("Employee ID: " + employeeID);
        System.out.println("Employee Name: " + firstname + " " + lastname);
        System.out.println("Birthdate: " + birthdate);
        System.out.printf("Hourly Rate: %.2f%n", hourlyRate);
        System.out.println();
        System.out.println("SEMI-MONTHLY HOURS WORKED AND GROSS SALARY");
        System.out.println("====================================================");

        for (int i = 0; i < 7; i++) {
            int firstHoursPart = firstCutoffMinutes[i] / 60;
            int firstMinutesPart = firstCutoffMinutes[i] % 60;
            double firstHoursDecimal = firstCutoffMinutes[i] / 60.0;
            double firstGross = firstHoursDecimal * hourlyRate;

            int secondHoursPart = secondCutoffMinutes[i] / 60;
            int secondMinutesPart = secondCutoffMinutes[i] % 60;
            double secondHoursDecimal = secondCutoffMinutes[i] / 60.0;
            double secondGross = secondHoursDecimal * hourlyRate;

            System.out.println(monthNames[i]);
            System.out.println("1st Cutoff Hours Worked: " + firstHoursPart + " hour(s) and " + firstMinutesPart + " minute(s)");
            System.out.printf("1st Cutoff Gross Salary: %.2f%n", firstGross);

            System.out.println("2nd Cutoff Hours Worked: " + secondHoursPart + " hour(s) and " + secondMinutesPart + " minute(s)");
            System.out.printf("2nd Cutoff Gross Salary: %.2f%n", secondGross);

            System.out.println("----------------------------------------------------");
        }

        scan.close();
    }
    
    // COMPUTE DAILY WORKED MINUTES
    public static int computeDailyWorkedMinutes(String logIn, String logOut) {
        int loginMinutes = convertTimeToMinutes(logIn);
        int logoutMinutes = convertTimeToMinutes(logOut);

        int officialStart = 8 * 60;      // 8:00 AM = 480
        int graceEnd = 8 * 60 + 10;      // 8:10 AM = 490
        int officialEnd = 17 * 60;       // 5:00 PM = 1020
        int lunchBreak = 60;             // 1 hour lunch break

        // Grace period: 8:00 to 8:10 counts as 8:00
        if (loginMinutes >= officialStart && loginMinutes <= graceEnd) {
            loginMinutes = officialStart;
        }

        // Do not count before 8:00 AM
        if (loginMinutes < officialStart) {
            loginMinutes = officialStart;
        }

        // Do not count after 5:00 PM
        if (logoutMinutes > officialEnd) {
            logoutMinutes = officialEnd;
        }

        // No valid worked time
        if (logoutMinutes <= loginMinutes) {
            return 0;
        }

        int workedMinutes = logoutMinutes - loginMinutes - lunchBreak;

        if (workedMinutes < 0) {
            return 0;
        }

        return workedMinutes;
    }

    // Convert HH:mm to total minutes
    public static int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());
        return (hour * 60) + minute;
    }

    // Get month from MM/dd/yyyy
    public static int getMonth(String date) {
        String[] parts = date.split("/");
        return Integer.parseInt(parts[0].trim());
    }

    // Get day from MM/dd/yyyy
    public static int getDay(String date) {
        String[] parts = date.split("/");
        return Integer.parseInt(parts[1].trim());
    }

}
