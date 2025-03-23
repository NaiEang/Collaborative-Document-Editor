# Collaborative-Document-Editor
The goal of this project is to develop a console-based collaborative document editor that allows multiple users to open and edit documents simultaneously. The project will be completed by a team of two students within a 4-week deadline. Below is the project plan, estimation, and progress tracking.

  ## Developers
Srun Nai Eang (p20230006), Taing VengChhay (p20230022)

  ## Table of Content
  - [Developers](#developers)
  - [Features](#features)
  - [Prerequisites](#prerequisites)
  - [System Requirements](#system-requirements)
  - [Knowledge Requirements](#knowledge-requirements)
  - [User Manual](#user-manual)
  - [Training](#training)

  ## Features
  - User Authentication:
    - Users can register and log in to the system.
  - File Operations:
    - Create, save, edit and delete files
    - Files are saved with a "Last Modified" timestamp.
  - Collaboration:
    - Multiple users can connect to the server and work on files.
  - Real-Time Communication:
    - The system supports real-time communication for file sharing (future versions).
    - The version3 can be communicate in real time
    - This last version can create and save file but not yet able to edit at the same time
  

  ## Prerequisites
  ### System Requirements
  - Operating System (OS): Window 10 or later
  - Ram: 5GB
  - IDE: Visual Studio Code(extension pack for java)
  - Tools: Github to clone repository
  - Disk space: Minimun 9.23 MB 

  ### Knowledge Requirements
  - Basic understanding of Java programming
  - Familiarity with Git and GitHub
  - Basic networking knowledge (e.g., understanding of IPv4)

  ## User Manual
  - Get your current ipv4 by ruuning ipconfig in your termial
  - Run and compile server file with your ipv4
  - Run and compile client file with your ipv4
  - After creating or editing file, you need to save otherwise you'll lose your updates

  ## Training
  - Clone the repository using `git clone https://github.com/NaiEang/Collaborative-Document-Editor.git`.
  - Compile and run the server:
    ```bash
    javac EditorServerPAD.java
    java EditorServerPAD <server-ip>
    ```
  - Compile and run the client:
    ```bash
    javac EditorClientPAD.java
    java EditorClientPAD <server-ip>
    ```
  - Practice the following:
    - Create, save, edit, and delete files.
    - Test collaboration by running multiple clients.
  - Refer to the [Features](#features) and [User Manual](#user-manual) sections for more details.
