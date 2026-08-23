# APU_Automotive_Service_Centre

# 🛠️ APU Appliance Service Center (ASC) Management System
**Module Code:** CT038-3-2-OODJ-L-3 (Object-Oriented Development with Java)  
**Intake Code:** APU2F2602SE & APD2F2602SE

## 📘 Project Overview

The **APU ASC Management System** is an end-to-end desktop application developed in Java for managing appliance service center operations. The system streamlines service scheduling, customer management, technician task assignments, billing and payment processing, feedback management, and executive report generation.  

Built strictly using Object-Oriented Programming (OOP) principles, the application implements robust security standards including two-factor authentication (OTP), password hashing, and role-based authorization. 

## 👥 Group Members

* **Serene Loh Zi Ting** TP075920  
* **Siew Zhen Lynn** TP076386  
* **Lim Ying Ying** TP076348  
* **Yap Xin Ling** TP077224  

## 🔑 Key Features & Role-Based Access Control
The application enforces strict Role-Based Access Control (RBAC) across four distinct primary user roles:

### 👨‍💼 Manager
* **User Management:** Register technician, counter staff, and manager accounts, edit user details, and manage user access permissions.
* **Service Price Configuration:** Define service types, create new services, and set service baseline prices.
* **Reporting & Analytics:** Generate, view, and export comprehensive operational reports.
* **Feedback Monitoring:** Review customer and technician feedback and service evaluations.

### 👔 Counter Staff
* **Customer Registration:** Register new customer accounts in the system.
* **Appointment Scheduling:** Book, view, and manage appointments for customers.
* **Payment Processing:** Process cash or credit card payments for completed services, generate receipts, and automatically email receipts to customers.
* **Password Resets:** Assist customers with authorized password resets.

### 🔧 Technician
* **Service Tracking:** View individual assigned appointment details and updates.
* **Status Updates:** Update assigned appointments to "Completed" status upon service fulfillment.
* **Feedback Management:** Submit, view, edit, and manage comments and technical feedback on completed appointments.

### 👤 Customer
* **Appointment Management:** View individual appointment details.
* **Payment History:** Access and view personal payment records and receipt history.
* **Feedback Submission:** Submit, edit, view, and delete feedback or ratings for completed service appointments.

## 🏗️ Object-Oriented Programming (OOP) Architecture
The application extensively applies core and advanced OOP principles to achieve modular, reusable, and secure code:

* **Inheritance:** Hierarchical user modeling branching from base classes to specialized roles.
* **Encapsulation:** Visibility control (private/protected attributes) exposed via standard getter and setter methods.
* **Polymorphism:** Dynamic method execution across role-specific workflows.
* **Abstraction:** Hiding complex underlying implementations (e.g., payment engines, email services) behind simplified execution methods.
* **Composition & Aggregation:** Structuring complex relationships between Users, Appointments, Payments, Services, and Reports.
* **Abstract Classes & Interfaces:** Defining standardized behavioral contracts for user components, data processors, and UI structures.
* **Method Overriding & Overloading:** Tailoring behaviors for object rendering, equality checks, and method execution variations.
* **Nested Classes:** Utilizing inner classes to handle event listeners and modular UI sub-components.

## 🚀 Additional Technical Features

* **Email Notification System:** Automated sending of generated receipts and One-Time Passwords (OTP) for Two-Factor Authentication (2FA).
* **Excel Data Export:** Exporting system reports and operational analytics into Excel spreadsheets for offline record-keeping.
* **Security Layer:** SHA-256 password hashing, OTP 2FA verification, password strength enforcement, and email format validation.

## ⚙️ Core System Rules & Assumptions

* **Authentication:** All users must authenticate via valid user credentials and pass role-based checks before accessing dashboard features.
* **Account Creation:** Self-registration is disabled. Counter staff register customer accounts; managers register staff, technician, and manager accounts.
* **Appointment Rules:** Working hours are enforced between 9:30 AM and 6:30 PM. Normal service duration is 1 hour, while Major service is 3 hours.
* **Appointment Statuses:** Managed across three states: Pending, Completed, and Cancelled. Counter staff can only cancel Pending appointments.
* **Feedback Eligibility:** Feedback and comments can only be submitted for Completed appointments.  

> 📥 **[Click here to access the full project documentation](https://github.com/XinLing8/APU_Automotive_Service_Centre/blob/main/Documentation/OODJ%20Assignment.pdf)**

## 📄 Academic Disclaimer

This project is an academic group submission for the Object-Oriented Development with Java (CT038-3-2-OODJ) module at Asia Pacific University of Technology & Innovation (APU).
