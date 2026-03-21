package com.mycompany.motorph_payroll_system;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * MotorPH Payroll System
 *
 * <p>This is a Java console application for processing employee payroll using CSV files.
 * The program supports two user roles: employee and payroll_staff.</p>
 *
 * <p>The program reads employee details, attendance records, and SSS contribution data
 * from CSV files. It calculates semi-monthly hours worked, gross salary, government
 * deductions, and net salary.</p>
 *
 * <p>This program is limited to attendance and payroll display from June to December
 * based on the course requirement.</p>
 *
 * <p>Course constraints followed:
 * - One Java file only
 * - Procedural programming only
 * - No OOP concepts used in the program design
 * </p>
 */
public class MotorPH_Payroll_System {

    public static void main(String[] args) {

        // File paths
        String empDetailsFile = "resources/Employee_Details.csv";
        String attendanceFile = "resources/Employee_Attendance_Record.csv";
        String sssFile = "resources/SSS_Contribution.csv";

        // Formatter for money output
        DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

        Scanner scan = new Scanner(System.in);

        // =========================
        // LOGIN
        // =========================
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();

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
            int option = getMenuOption(scan, 1, 2);

            if (option == 1) {
                String empId = getEmployeeIdInput(scan);

                String[] employee = getEmployeeData(empDetailsFile, empId);

                if (employee[0] == null) {
                    System.out.println("Employee number does not exist.");
                } else {
                    System.out.println();
                    System.out.println("Employee Number: " + employee[0]);
                    System.out.println("Employee Name: " + employee[2] + " " + employee[1]);
                    System.out.println("Birthday: " + employee[3]);
                }

            } else if (option == 2) {
                scan.close();
                return;
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
        int mainOption = getMenuOption(scan, 1, 2);

        if (mainOption == 2) {
            scan.close();
            return;
        }

        System.out.println();
        System.out.println("1. One employee");
        System.out.println("2. All employees");
        System.out.println("3. Exit the program");
        System.out.print("Choose an option: ");
        int payrollOption = getMenuOption(scan, 1, 3);

        if (payrollOption == 1) {

            String empId = getEmployeeIdInput(scan);

            String[] employee = getEmployeeData(empDetailsFile, empId);

            if (employee[0] == null) {
                System.out.println("Employee number does not exist.");
                scan.close();
                return;
            }

            processOneEmployeePayroll(attendanceFile, sssFile, employee, moneyFormat);

        } else if (payrollOption == 2) {

            processAllEmployeesPayroll(empDetailsFile, attendanceFile, sssFile, moneyFormat);

        } else if (payrollOption == 3) {
            scan.close();
            return;
        }

        scan.close();
    }

    /**
     * Reads a valid numeric menu option from the user.
     *
     * @param scan the Scanner used for input
     * @param min the minimum valid option
     * @param max the maximum valid option
     * @return the validated numeric menu option
     */
    public static int getMenuOption(Scanner scan, int min, int max) {
        while (true) {
            String input = scan.nextLine().trim();

            if (input.matches("\\d+")) {
                int option = Integer.parseInt(input);

                if (option >= min && option <= max) {
                    return option;
                }
            }

            System.out.print("Invalid option. Please enter a number between " + min + " and " + max + ": ");
        }
    }

    /**
     * Reads a valid employee number from the user.
     *
     * @param scan the Scanner used for input
     * @return the validated employee number as a string
     */
    public static String getEmployeeIdInput(Scanner scan) {
        while (true) {
            System.out.print("Enter employee number: ");
            String empId = scan.nextLine().trim();

            if (empId.matches("\\d+")) {
                return empId;
            }

            System.out.println("Invalid input. Please enter numbers only.");
        }
    }

    /**
     * Processes payroll for one employee from June to December.
     *
     * @param attendanceFile the attendance CSV file path
     * @param sssFile the SSS contribution CSV file path
     * @param employee the employee record array
     * @param moneyFormat the DecimalFormat used for salary output
     * @return nothing
     */
    public static void processOneEmployeePayroll(String attendanceFile, String sssFile, String[] employee, DecimalFormat moneyFormat) {

        String empId = employee[0];
        String lastName = employee[1];
        String firstName = employee[2];
        String birthdate = employee[3];
        double hourlyRate = Double.parseDouble(employee[4]);

        int[] firstCutoffMinutes = new int[7];
        int[] secondCutoffMinutes = new int[7];

        String[] monthNames = {
            "June", "July", "August", "September", "October", "November", "December"
        };

        String[] secondCutoffEndDates = {
            "30", "31", "31", "30", "31", "30", "31"
        };

        boolean attendanceFound = loadAttendanceForEmployee(attendanceFile, empId, firstCutoffMinutes, secondCutoffMinutes);

        System.out.println();
        System.out.println("Employee #: " + empId);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Birthday: " + birthdate);
        System.out.println();

        if (!attendanceFound) {
            System.out.println("No attendance records found from June to December.");
            return;
        }

        for (int i = 0; i < 7; i++) {

            double firstHours = firstCutoffMinutes[i] / 60.0;
            double secondHours = secondCutoffMinutes[i] / 60.0;

            double firstGross = firstHours * hourlyRate;
            double secondGross = secondHours * hourlyRate;

            double monthlyGross = firstGross + secondGross;

            double sss = computeSSSFromCSV(sssFile, monthlyGross);
            double philHealth = computePhilHealth(monthlyGross);
            double pagIbig = computePagIbig(monthlyGross);

            double taxableIncome = monthlyGross - sss - philHealth - pagIbig;
            double tax = computeWithholdingTax(taxableIncome);

            double totalDeductions = sss + philHealth + pagIbig + tax;

            double firstNet = firstGross;
            double secondNet = secondGross - totalDeductions;

            System.out.println("Cutoff Date: " + monthNames[i] + " 1 to " + monthNames[i] + " 15");
            System.out.println("Total Hours Worked: " + firstHours);
            System.out.println("Gross Salary: " + moneyFormat.format(firstGross));
            System.out.println("Net Salary: " + moneyFormat.format(firstNet));
            System.out.println();

            System.out.println("Cutoff Date: " + monthNames[i] + " 16 to " + monthNames[i] + " " + secondCutoffEndDates[i]);
            System.out.println("Total Hours Worked: " + secondHours);
            System.out.println("Gross Salary: " + moneyFormat.format(secondGross));
            System.out.println();

            System.out.println("Each Deduction:");
            System.out.println("SSS: " + moneyFormat.format(sss));
            System.out.println("PhilHealth: " + moneyFormat.format(philHealth));
            System.out.println("Pag-IBIG: " + moneyFormat.format(pagIbig));
            System.out.println("Tax: " + moneyFormat.format(tax));
            System.out.println();

            System.out.println("Total Deductions: " + moneyFormat.format(totalDeductions));
            System.out.println("Net Salary: " + moneyFormat.format(secondNet));
            System.out.println("----------------------------------------------------");
        }
    }

    /**
     * Processes payroll for all employees in the employee file.
     *
     * @param empDetailsFile the employee details CSV file path
     * @param attendanceFile the attendance CSV file path
     * @param sssFile the SSS contribution CSV file path
     * @param moneyFormat the DecimalFormat used for salary output
     * @return nothing
     */
    public static void processAllEmployeesPayroll(String empDetailsFile, String attendanceFile, String sssFile, DecimalFormat moneyFormat) {

        try (BufferedReader br = new BufferedReader(new FileReader(empDetailsFile))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = parseCSVLine(line);

                if (col.length >= 19) {
                    String[] employee = new String[5];
                    employee[0] = col[0].trim();
                    employee[1] = col[1].trim();
                    employee[2] = col[2].trim();
                    employee[3] = col[3].trim();
                    employee[4] = cleanNumber(col[18]);

                    processOneEmployeePayroll(attendanceFile, sssFile, employee, moneyFormat);
                    System.out.println();
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading employee file.");
        }
    }

    /**
     * Reads employee information from the employee CSV file.
     *
     * @param empDetailsFile the employee details CSV file path
     * @param empId the employee number to search for
     * @return a String array containing employee number, last name, first name, birthday, and hourly rate
     */
    public static String[] getEmployeeData(String empDetailsFile, String empId) {

        String[] employee = new String[5];

        try (BufferedReader br = new BufferedReader(new FileReader(empDetailsFile))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = parseCSVLine(line);

                if (col.length >= 19 && col[0].trim().equals(empId)) {
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

    /**
     * Loads attendance records for one employee from June to December
     * and stores worked minutes in the proper cutoff arrays.
     *
     * @param attendanceFile the attendance CSV file path
     * @param empId the employee number
     * @param firstCutoffMinutes array storing total minutes for days 1–15
     * @param secondCutoffMinutes array storing total minutes for days 16–end of month
     * @return true if attendance records were found, otherwise false
     */
    public static boolean loadAttendanceForEmployee(String attendanceFile, String empId, int[] firstCutoffMinutes, int[] secondCutoffMinutes) {

        boolean attendanceFound = false;

        try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] col = parseCSVLine(line);

                if (col.length >= 6) {
                    String employeeNo = col[0].trim();
                    String date = col[3].trim();
                    String logIn = col[4].trim();
                    String logOut = col[5].trim();

                    if (employeeNo.equals(empId)) {
                        int month = getMonth(date);
                        int day = getDay(date);

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

    /**
     * Computes worked minutes for one attendance record using payroll rules.
     *
     * @param logIn the login time in HH:mm format
     * @param logOut the logout time in HH:mm format
     * @return the total valid worked minutes after applying payroll rules
     */
    public static int computeDailyWorkedMinutes(String logIn, String logOut) {

        int loginMinutes = convertTimeToMinutes(logIn);
        int logoutMinutes = convertTimeToMinutes(logOut);

        int officialStart = 8 * 60;
        int graceEnd = 8 * 60 + 10;
        int officialEnd = 17 * 60;
        int lunchBreak = 60;

        if (loginMinutes >= officialStart && loginMinutes <= graceEnd) {
            loginMinutes = officialStart;
        }

        if (loginMinutes < officialStart) {
            loginMinutes = officialStart;
        }

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

    /**
     * Converts a time string in HH:mm format into total minutes.
     *
     * @param time the time string in HH:mm format
     * @return the total number of minutes
     */
    public static int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());
        return (hour * 60) + minute;
    }

    /**
     * Extracts the month from a date in MM/dd/yyyy format.
     *
     * @param date the date string in MM/dd/yyyy format
     * @return the month value as an integer
     */
    public static int getMonth(String date) {
        String[] parts = date.split("/");
        return Integer.parseInt(parts[0].trim());
    }

    /**
     * Extracts the day from a date in MM/dd/yyyy format.
     *
     * @param date the date string in MM/dd/yyyy format
     * @return the day value as an integer
     */
    public static int getDay(String date) {
        String[] parts = date.split("/");
        return Integer.parseInt(parts[1].trim());
    }

    /**
     * Parses a CSV line while correctly handling values enclosed in quotation marks.
     *
     * @param line the CSV line to parse
     * @return an array of parsed field values
     */
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

    /**
     * Removes commas from numeric text so it can be parsed safely.
     *
     * @param text the numeric text that may contain commas
     * @return the cleaned numeric text
     */
    public static String cleanNumber(String text) {
        return text.replace(",", "").trim();
    }

    /**
     * Computes SSS contribution by reading the SSS bracket table from CSV.
     *
     * @param sssFile the SSS contribution CSV file path
     * @param monthlyGross the employee's monthly gross salary
     * @return the matching SSS contribution amount
     */
    public static double computeSSSFromCSV(String sssFile, double monthlyGross) {

        try (BufferedReader br = new BufferedReader(new FileReader(sssFile))) {
            br.readLine();
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

                    if (fromText.equalsIgnoreCase("Below")) {
                        double upperLimit = Double.parseDouble(toText);

                        if (monthlyGross < upperLimit) {
                            return contribution;
                        }
                    } else if (toText.equalsIgnoreCase("Over")) {
                        double lowerLimit = Double.parseDouble(fromText);

                        if (monthlyGross >= lowerLimit) {
                            return contribution;
                        }
                    } else {
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

    /**
     * Computes the employee's PhilHealth contribution.
     *
     * @param monthlyGross the employee's monthly gross salary
     * @return the employee share of the PhilHealth contribution
     */
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

    /**
     * Computes the employee's Pag-IBIG contribution.
     *
     * @param monthlyGross the employee's monthly gross salary
     * @return the Pag-IBIG contribution amount
     */
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

    /**
     * Computes withholding tax based on taxable income.
     *
     * @param taxableIncome the employee's taxable income after mandatory deductions
     * @return the withholding tax amount
     */
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