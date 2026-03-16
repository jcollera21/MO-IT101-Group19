MotorPH Payroll Processing System
Project Summary
The MotorPH Payroll Processing System is a Java console application designed to automate the computation of employee payroll. The program reads employee information, hourly rates, and attendance records directly from CSV files and calculates the total hours worked and the corresponding salary for each cutoff period.
The system supports semi-monthly payroll computation and applies the required government deductions such as SSS, PhilHealth, Pag-IBIG, and withholding tax. The deductions are computed based on the employee’s total monthly salary, and they are applied during the second cutoff payout.
The program includes two user roles:
Employee – allows employees to view their personal information.
Payroll Staff – allows payroll personnel to process payroll for one employee or all employees.
The system ensures that all payroll computations follow the required business rules, including:
Reading all data from CSV files.
Processing attendance records from June to December.
Calculating work hours only between 8:00 AM and 5:00 PM.
Applying government deductions correctly based on payroll policies.

Team Contributions
Action Item
Person Assigned
Implement core system functions
Jose
Apply business rules and computations
Bryan
Validate system outputs
Bryan / Chona
Fix identified issues
Jose
Finalize system logic
Chona
Create QA review questions
Bryan
Conduct system testing
Chona
Import project to GitHub repository
Jose
Create README details
Bryan


Program Details
The system performs the following operations:
User Authentication
The program requires a username and password before accessing the system.
Valid usernames:
employee
payroll_staff
Password for both accounts: 12345
Employee Mode
The user enters their employee number.
The program displays:
Employee Number
Employee Name
Birthday
If the employee number does not exist, the system displays an error message.
Payroll Staff Mode
Allows payroll processing for:
One employee
All employees
The system calculates:
Total hours worked per cutoff
Gross salary
Government deductions
Net salary
Payroll Rules
Payroll is calculated semi-monthly:
1st cutoff: Day 1–15
2nd cutoff: Day 16–end of month
Only working hours between 8:00 AM and 5:00 PM are counted.
Extra hours beyond 5:00 PM are not included.
Government deductions are applied during the second cutoff.

How to Run the Program
Clone or download this repository.
Open the project using NetBeans IDE or any Java IDE.
Make sure the required CSV files are located in the resources folder:
Employee_Details.csv
Employee_Attendance_Record.csv
SSS_Contribution.csv

Run the file:
MotorPH_Payroll_System.java
Log in using one of the following accounts:
Username
Password
Access
employee
12345
View employee information
payroll_staff
12345
Process payroll


Follow the menu prompts displayed in the console.

Project Plan
Project Plan Link:
https://github.com/jcollera21/MO-IT101-Group19
