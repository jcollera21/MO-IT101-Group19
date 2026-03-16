package com.mycompany.motorph_payroll_system;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
 MotorPH Payroll System

 This program:
 1. Asks for username and password.
 2. Checks whether the user is an employee or payroll staff.
 3. Reads employee and attendance data from CSV files.
 4. Computes semi-monthly hours worked from June to December.
 5. Computes gross salary and mandatory deductions.
 6. Applies deductions only to the second cutoff.

 Files used:
 - Employee_Details.csv
 - Employee_Attendance_Record.csv
 - SSS_Contribution.csv
*/

public class MotorPH_Payroll_System {

    public static void main(String[] args) {

        // File paths
        String empDetailsFile = "resources/Employee_Details.csv";
        String attendanceFile = "resources/Employee_Attendance_Record.csv";
        String sssFile = "resources/SSS_Contribution.csv";

        Scanner scan = new Scanner(System.in);

        // =========================
        // LOGIN
        // =========================
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();

        // Valid usernames:
        // employee
        // payroll_staff
        // Valid password:
        // 12345
        if ((!username.equals("employee") && !username.equals("payroll_staff")) || !password.equals("12345")) {
            System.out.println("Incorrect username and/or password.");
            scan.close();
            return;
        }

        // =========================
        // EMPLOYEE MENU
        // =========================
        if (username.equals("employee")) {

            System.out.println();
            System.out.println("1. Enter your employee number");
            System.out.println("2. Exit the program");
            System.out.print("Choose an option: ");
            String option = scan.nextLine().trim();

            if (option.equals("1")) {
                System.out.print("Enter employee number: ");
                String empID = scan.nextLine().trim();

                String[] employee = getEmployeeData(empDetailsFile, empID);

                if (employee[0] == null) {
                    System.out.println("Employee number does not exist.");
                } else {
                    System.out.println();
                    System.out.println("Employee Number: " + employee[0]);
                    System.out.println("Employee Name: " + employee[2] + " " + employee[1]);
                    System.out.println("Birthday: " + employee[3]);
                }

            } else if (option.equals("2")) {
                scan.close();
                return;
            } else {
                System.out.println("Invalid option.");
            }

            scan.close();
            return;
        }

        // =========================
        // PAYROLL STAFF MENU
        // =========================
        System.out.println();
        System.out.println("1. Process Payroll");
        System.out.println("2. Exit the program");
        System.out.print("Choose an option: ");
        String mainOption = scan.nextLine().trim();

        if (mainOption.equals("2")) {
            scan.close();
            return;
        }

        if (!mainOption.equals("1")) {
            System.out.println("Invalid option.");
            scan.close();
            return;
        }

        System.out.println();
        System.out.println("1. One employee");
        System.out.println("2. All employees");
        System.out.println("3. Exit the program");
        System.out.print("Choose an option: ");
        String payrollOption = scan.nextLine().trim();

        if (payrollOption.equals("1")) {

            System.out.print("Enter employee number: ");
            String empID = scan.nextLine().trim();

            String[] employee = getEmployeeData(empDetailsFile, empID);

            if (employee[0] == null) {
                System.out.println("Employee number does not exist.");
                scan.close();
                return;
            }

            processOneEmployeePayroll(attendanceFile, sssFile, employee);

        } else if (payrollOption.equals("2")) {

            processAllEmployeesPayroll(empDetailsFile, attendanceFile, sssFile);

        } else if (payrollOption.equals("3")) {

            scan.close();
            return;

        } else {
            System.out.println("Invalid option.");
        }

        scan.close();
    }

    // ==================================================
    // PROCESS ONE EMPLOYEE PAYROLL
    // ==================================================
    public static void processOneEmployeePayroll(String attendanceFile, String sssFile, String[] employee) {

        String empID = employee[0];
        String lastName = employee[1];
        String firstName = employee[2];
        String birthdate = employee[3];
        double hourlyRate = Double.parseDouble(employee[4]);

        // Arrays to store total worked minutes
        // 0 = June, 1 = July, ..., 6 = December
        int[] firstCutoffMinutes = new int[7];
        int[] secondCutoffMinutes = new int[7];

        String[] monthNames = {
            "June", "July", "August", "September", "October", "November", "December"
        };

        String[] secondCutoffEndDates = {
            "30", "31", "31", "30", "31", "30", "31"
        };

        boolean attendanceFound = loadAttendanceForEmployee(attendanceFile, empID, firstCutoffMinutes, secondCutoffMinutes);

        System.out.println();
        System.out.println("Employee #: " + empID);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Birthday: " + birthdate);
        System.out.println();

        if (!attendanceFound) {
            System.out.println("No attendance records found from June to December.");
            return;
        }

        // Display all months from June to December
        for (int i = 0; i < 7; i++) {

            double firstHours = firstCutoffMinutes[i] / 60.0;
            double secondHours = secondCutoffMinutes[i] / 60.0;

            double firstGross = firstHours * hourlyRate;
            double secondGross = secondHours * hourlyRate;

            // Add 1st and 2nd cutoff gross first before deductions
            double monthlyGross = firstGross + secondGross;

            double sss = computeSSSFromCSV(sssFile, monthlyGross);
            double philHealth = computePhilHealth(monthlyGross);
            double pagIbig = computePagIbig(monthlyGross);

            // Tax is based on taxable income after mandatory deductions
            double taxableIncome = monthlyGross - sss - philHealth - pagIbig;
            double tax = computeWithholdingTax(taxableIncome);

            double totalDeductions = sss + philHealth + pagIbig + tax;

            // Deductions are applied only on the second cutoff
            double firstNet = firstGross;
            double secondNet = secondGross - totalDeductions;

            System.out.println("Cutoff Date: " + monthNames[i] + " 1 to " + monthNames[i] + " 15");
            System.out.println("Total Hours Worked: " + firstHours);
            System.out.println("Gross Salary: " + firstGross);
            System.out.println("Net Salary: " + firstNet);
            System.out.println();

            System.out.println("Cutoff Date: " + monthNames[i] + " 16 to " + monthNames[i] + " " + secondCutoffEndDates[i]);
            System.out.println("Total Hours Worked: " + secondHours);
            System.out.println("Gross Salary: " + secondGross);
            System.out.println();

            System.out.println("Each Deduction:");
            System.out.println("SSS: " + sss);
            System.out.println("PhilHealth: " + philHealth);
            System.out.println("Pag-IBIG: " + pagIbig);
            System.out.println("Tax: " + tax);
            System.out.println();

            System.out.println("Total Deductions: " + totalDeductions);
            System.out.println("Net Salary: " + secondNet);
            System.out.println("----------------------------------------------------");
        }
    }

    // ==================================================
    // PROCESS ALL EMPLOYEES PAYROLL
    // ==================================================
    public static void processAllEmployeesPayroll(String empDetailsFile, String attendanceFile, String sssFile) {

        try (BufferedReader br = new BufferedReader(new FileReader(empDetailsFile))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = parseCSVLine(line);

                if (col.length >= 19) {
                    String[] employee = new String[5];
                    employee[0] = col[0].trim();        // Employee #
                    employee[1] = col[1].trim();        // Last Name
                    employee[2] = col[2].trim();        // First Name
                    employee[3] = col[3].trim();        // Birthday
                    employee[4] = cleanNumber(col[18]); // Hourly Rate

                    processOneEmployeePayroll(attendanceFile, sssFile, employee);
                    System.out.println();
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading employee file.");
        }
    }

    // ==================================================
    // GET EMPLOYEE DATA
    // Returns:
    // [0] Employee #
    // [1] Last Name
    // [2] First Name
    // [3] Birthday
    // [4] Hourly Rate
    // ==================================================
    public static String[] getEmployeeData(String empDetailsFile, String empID) {

        String[] employee = new String[5];

        try (BufferedReader br = new BufferedReader(new FileReader(empDetailsFile))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = parseCSVLine(line);

                if (col.length >= 19 && col[0].trim().equals(empID)) {
                    employee[0] = col[0].trim();
                    employee[1] = col[1].trim();
                    employee[2] = col[2].trim();
                    employee[3] = col[3].trim();
                    employee[4] = cleanNumber(col[18]);
                    return employee;
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading employee file.");
        }

        return employee;
    }

    // ==================================================
    // LOAD ATTENDANCE FOR ONE EMPLOYEE
    // Stores worked minutes from June to December
    // ==================================================
    public static boolean loadAttendanceForEmployee(String attendanceFile, String empID, int[] firstCutoffMinutes, int[] secondCutoffMinutes) {

        boolean attendanceFound = false;

        try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = line.split(",");

                if (col.length >= 6) {
                    String employeeNo = col[0].trim();
                    String date = col[3].trim();
                    String logIn = col[4].trim();
                    String logOut = col[5].trim();

                    if (employeeNo.equals(empID)) {
                        int month = getMonth(date);
                        int day = getDay(date);

                        // Only process June to December
                        if (month >= 6 && month <= 12) {
                            int monthIndex = month - 6;
                            int workedMinutes = computeDailyWorkedMinutes(logIn, logOut);

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
        }

        return attendanceFound;
    }

    // ==================================================
    // COMPUTE DAILY WORKED MINUTES
    // Rules:
    // - Count only from 8:00 AM to 5:00 PM
    // - 10-minute grace period
    // - No extra hours before 8:00 or after 5:00
    // - Deduct 1 hour lunch break based on the examples
    // ==================================================
    public static int computeDailyWorkedMinutes(String logIn, String logOut) {

        int loginMinutes = convertTimeToMinutes(logIn);
        int logoutMinutes = convertTimeToMinutes(logOut);

        int officialStart = 8 * 60;      // 8:00 AM
        int graceEnd = 8 * 60 + 10;      // 8:10 AM
        int officialEnd = 17 * 60;       // 5:00 PM
        int lunchBreak = 60;             // 1 hour lunch break

        // Grace period
        if (loginMinutes >= officialStart && loginMinutes <= graceEnd) {
            loginMinutes = officialStart;
        }

        // Ignore time before 8:00 AM
        if (loginMinutes < officialStart) {
            loginMinutes = officialStart;
        }

        // Ignore time after 5:00 PM
        if (logoutMinutes > officialEnd) {
            logoutMinutes = officialEnd;
        }

        if (logoutMinutes <= loginMinutes) {
            return 0;
        }

        int workedMinutes = logoutMinutes - loginMinutes - lunchBreak;

        if (workedMinutes < 0) {
            return 0;
        }

        return workedMinutes;
    }

    // ==================================================
    // CONVERT HH:mm TO TOTAL MINUTES
    // ==================================================
    public static int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());
        return (hour * 60) + minute;
    }

    // ==================================================
    // DATE HELPERS
    // Attendance file format is MM/dd/yyyy
    // ==================================================
    public static int getMonth(String date) {
        String[] parts = date.split("/");
        return Integer.parseInt(parts[0].trim());
    }

    public static int getDay(String date) {
        String[] parts = date.split("/");
        return Integer.parseInt(parts[1].trim());
    }

    // ==================================================
    // PARSE CSV LINE
    // Used for files that contain quoted values with commas
    // ==================================================
    public static String[] parseCSVLine(String line) {

        String[] tempFields = new String[30];
        String currentField = "";
        boolean inQuotes = false;
        int fieldCount = 0;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {
                inQuotes = !inQuotes;
            } else if (currentChar == ',' && !inQuotes) {
                tempFields[fieldCount] = currentField.trim();
                fieldCount++;
                currentField = "";
            } else {
                currentField += currentChar;
            }
        }

        tempFields[fieldCount] = currentField.trim();
        fieldCount++;

        String[] finalFields = new String[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            finalFields[i] = tempFields[i];
        }

        return finalFields;
    }

    // ==================================================
    // REMOVE COMMAS FROM NUMBER TEXT
    // Example: 1,125.00 -> 1125.00
    // ==================================================
    public static String cleanNumber(String text) {
        return text.replace(",", "").trim();
    }

    // ==================================================
    // SSS DEDUCTION FROM CSV
    // CSV format:
    // Column 0 = lower bound or "Below"
    // Column 1 = "-"
    // Column 2 = upper bound or "Over"
    // Column 3 = contribution
    // ==================================================
    public static double computeSSSFromCSV(String sssFile, double monthlyGross) {

        try (BufferedReader br = new BufferedReader(new FileReader(sssFile))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = parseCSVLine(line);

                if (col.length >= 4) {
                    String fromText = cleanNumber(col[0]);
                    String toText = cleanNumber(col[2]);
                    double contribution = Double.parseDouble(cleanNumber(col[3]));

                    // Case 1: Below
                    if (fromText.equalsIgnoreCase("Below")) {
                        double upperLimit = Double.parseDouble(toText);

                        if (monthlyGross < upperLimit) {
                            return contribution;
                        }
                    }

                    // Case 2: Over
                    else if (toText.equalsIgnoreCase("Over")) {
                        double lowerLimit = Double.parseDouble(fromText);

                        if (monthlyGross >= lowerLimit) {
                            return contribution;
                        }
                    }

                    // Case 3: Normal range
                    else {
                        double lowerLimit = Double.parseDouble(fromText);
                        double upperLimit = Double.parseDouble(toText);

                        if (monthlyGross >= lowerLimit && monthlyGross < upperLimit) {
                            return contribution;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading SSS file.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format in SSS file.");
        }

        return 0.0;
    }

    // ==================================================
    // PHILHEALTH DEDUCTION
    // Premium rate = 3%
    // Minimum premium = 300
    // Maximum premium = 1800
    // Employee pays half
    // ==================================================
    public static double computePhilHealth(double monthlyGross) {

        double premium = monthlyGross * 0.03;

        if (premium < 300) {
            premium = 300;
        }

        if (premium > 1800) {
            premium = 1800;
        }

        return premium / 2.0;
    }

    // ==================================================
    // PAG-IBIG DEDUCTION
    // 1% for 1000 to 1500
    // 2% for over 1500
    // Maximum contribution = 100
    // ==================================================
    public static double computePagIbig(double monthlyGross) {

        double contribution;

        if (monthlyGross >= 1000 && monthlyGross <= 1500) {
            contribution = monthlyGross * 0.01;
        } else if (monthlyGross > 1500) {
            contribution = monthlyGross * 0.02;
        } else {
            contribution = 0.0;
        }

        if (contribution > 100) {
            contribution = 100;
        }

        return contribution;
    }

    // ==================================================
    // WITHHOLDING TAX
    // Tax is based on taxable income after SSS,
    // PhilHealth, and Pag-IBIG
    // ==================================================
    public static double computeWithholdingTax(double taxableIncome) {

        if (taxableIncome <= 20832) {
            return 0.0;
        } else if (taxableIncome < 33333) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome < 66667) {
            return 2500 + ((taxableIncome - 33333) * 0.25);
        } else if (taxableIncome < 166667) {
            return 10833 + ((taxableIncome - 66667) * 0.30);
        } else if (taxableIncome < 666667) {
            return 40833.33 + ((taxableIncome - 166667) * 0.32);
        } else {
            return 200833.33 + ((taxableIncome - 666667) * 0.35);
        }
    }
}