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
        
        String empNo = null;
        String fname = null;
        String lname = null;
        String bday = null;
        
        
        double hourlyRate = 0.0;
        
        
        System.out.print("Enter Employee ID#: ");
        String empID = scan.nextLine();
        
        // This variable checks whether the employee exists in the file
        boolean isEmp = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(empDetails))){ // Open the CSV file for reading
            br.readLine(); // Skip the first line because it contains column headers
            String line;
            
            while ((line = br.readLine()) != null) { // Read each row of the file until the end
                
                String[] col = line.split(","); // Split the row into columns using comma as separator
                
                if (col.length >= 18 && col[0].trim().equals(empID)){ // Check if the row has enough columns and if the ID matches
                    
                    // Save the first 4 columns of the employee data
                    empNo = col[0];
                    lname = col[1];
                    fname = col[2];
                    bday = col[3];
                    
                    String rateText = col[col.length - 1].replace("\"", "").trim(); // Hourly Rate is the last column
                    hourlyRate = Double.parseDouble(rateText); // Converts rateText into double

                    isEmp = true; // Mark that the employee was found
                    break; // Stop searching because we already found the employee
                }
            }
        }catch (IOException e){
            System.out.println("Error reading employee file!"); // This happens if the program cannot open or read the file
            return;
        }
        
        if(isEmp){ // Display the employee information if found
            System.out.println("Welcome " + fname + " " + lname + "!");
            System.out.println("Employee #: " + empNo);
            System.out.println("Birthdate: " + bday);
            System.out.println("Hourly Rate: " + hourlyRate);
        } else {
            System.out.println("Employee not found!");
        }
        
        
        

    }
}
