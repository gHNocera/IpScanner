# IP Scanner - Network Tool

A Java application for scanning and monitoring network devices.

## Features

- List IPs and MACs in the network
- Track packets from specific IP addresses
- Simple command-line interface

## Requirements

- Java 8 or higher
- Maven (for building)

## Building the Application

```bash
mvn clean package
```

## Running the Application

### Using the provided scripts

#### Windows (Command Prompt)
```
run.bat
```

#### Windows (PowerShell)
```
.\run.ps1
```

### Using Maven
```bash
mvn exec:java
```

### Using Java directly
```bash
java -cp target/classes com.ghartmann.App
```

## Available Commands

- `help` - Display help information
- `iplist` - List IPs and MACs in the network
- `iptracker <IP>` - Monitor packets from a specific IP address
- `exit` - Exit the program