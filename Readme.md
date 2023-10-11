# Weather Data Aggregation System

## Table of Contents
1. [Introduction](#introduction)
2. [System Architecture](#system-architecture)
    - [Directories](#directories)
    - [Java Files Classification](#java-files-classification)
    - [Makefile Targets](#makefile-targets)
3. [Special Features](#special-features)
4. [Getting Started](#getting-started)
    - [AggregationServer](#aggregationserver)
    - [JsonFormatter](#jsonformatter)
    - [ContentServer](#contentserver)
    - [GETClient](#getclient)
    - [LamportClock](#lamportclock)
    - [Modify Class](#modify-class)
    - [Testing](#testing)
5. [Conclusion](#conclusion)
## Introduction

The Weather Data Aggregation System is a comprehensive tool designed to aggregate, process, and display weather data from various sources. This system is built using Java and integrates several components, including the Aggregation Server, Content Server, and a GET client.

## System Architecture

The system's architecture is divided into the following main components:

1. **Aggregation Server**: Central server responsible for aggregating data from multiple content servers.
2. **Content Server**: Reads weather data from files, processes it, and sends it to the Aggregation Server.
3. **GET Client**: Connects to the Aggregation Server to retrieve and display weather data.

### Directories
   1. ./src/: Contains all the source code files. 
   2. ./bin/: Destination for compiled bytecode files. 
   3. ./lib/jackson/: Contains the Jackson library JAR files used for JSON operations.

### Java Files Classification
   1. **Server Side:**
      -   AggregationServer.java: Manages data from content servers, serves client requests.
   2. **Client Side:**
      - ContentServer.java: Sends data to the Aggregation Server.
      - GETClient.java: Retrieves data from the Aggregation Server.
   3. **Utilities:**
      - LamportClock.java: Clock synchronization utility.
      - JsonFormatter.java: For JSON data formatting.
      - Modify.java: Modifies data file content periodically.

### Makefile Targets
   The provided Makefile has several targets to ease compilation and execution:

1. compile: Compiles all source files.
2. runContentServer: Runs the ContentServer.
3. runAggregationServer: Initiates the AggregationServer.
4. runModify: Executes the Modify utility.
5. runGETClient: Starts the GETClient.
6. test: Tests will run.

## Special Features

1. **Jackson Library**: Enables efficient serialization and deserialization of JSON data.
2. **Lamport Clock**: Ensures synchronized communication across system components.
3. **Data Modifier**: Updates the system's data.
4. **Testing Framework**: System testing.

## Getting Started

To run the code using the provided Makefile:

1. **Compile the Code**:
   ```bash
   make compile
   ```

2. **Run the AggregationServer**:
   ```bash
   make runAggregationServer
   ```

3. **Run the ContentServer**:
   ```bash
   make runContentServer "$(HOST):$(PORT)" "$(FILEPATH)"
   ```
   
4. **Access the data using GETCLient**
    1. **Fetch all data**: 
    ```
    make runGETClient "$(HOST):$(PORT)"
   ```
    2. **Fetch data for a specific station**:
    ```
    make runGETClient "$(HOST):$(PORT)" "$(ID)"
   ```

5. **Access the HTTP Request in  Browser**
    1. **Fetch all data**:
   ```
       http://localhost:4567/
   ```
    2. **Fetch data for a specific station**:
    ```
    http://localhost:4567/ID=IDS60912
   ```

6. **Run DataModifier**:
   ```bash
   make runModify
   ```
7. **Run Tests**:
   ```bash
   make test
   ```
### AggregationServer
A server that aggregates data from various clients.

**Key Features:**
- Uses the Jackson library for JSON manipulation.
- Maintains a Lamport logical clock.
- Keeps track of connected servers and their associated data.
- Periodically cleans up old server data.
- Responds to client requests for:
- Lamport clock time.
- Data of specific IDs.
- Data updates.
- Persists the aggregated data to a file.

**Global Data Structures:**
- lamportClock: Logical clock for synchronization.
- connectedServers: List of servers that have connected.
- serverTimestamps: Last known timestamps for each server.
- serverToIds: Mapping of server to their associated data IDs.
- timestampedDataMap: Most recent data from servers with timestamps.

**Concurrency:**
Uses Java's concurrent utilities to handle multiple client requests and scheduled cleanups efficiently.

### JsonFormatter
Utility class to convert content from a file into a structured JSON format.

**Key Features:**
- Reads a specified file and processes its key:value formatted content.
- Transforms the content into a JSON structure using the Jackson library.
- Handles "id" keys specially to create new JSON objects.
- Adds numeric or string values based on the content.

**Usage:**
```
JsonNode formattedData = JsonFormatter.format("path_to_file.txt");
```
**Where:**
"path_to_file.txt" should be replaced with the path to your file.

### ContentServer
This server connects to the AggregationServer and sends content from a specific file in a structured JSON format.

**Key Features:**
- Uses the Jackson library for JSON operations.
- Establishes a connection to the specified AggregationServer.
- Reads and formats the content from a given file path into structured JSON.
- Sends this content periodically (every 30 seconds) to the AggregationServer.
- Displays the response received from the AggregationServer.

**Here's a more detailed breakdown:**

**Imports:**
- JSON handling using the Jackson library.
- Basic Java I/O classes for reading, writing, and handling socket-based communications.
- Java's networking libraries for creating and handling socket communications.

**Main Function:**
- Command-line arguments are expected for server details and the path of the file.
- Parses the server name and port from the first argument.
**Enters a loop to:**
- Connect to the AggregationServer.
- Use JsonFormatter to fetch and format content from the file.
- Sends this data to the AggregationServer using a "PUT" command.
- Reads and prints the response from the AggregationServer.
- Waits for 30 seconds before starting the next iteration.

**Exception Handling:**
- If an IOException is encountered, it is printed.
- If the thread is interrupted during sleep, it interrupts the current thread and exits the loop.

**Usage:**
```
java ContentServer <ServerName:Port> <FilePath>
```
**Where:**
<ServerName:Port> refers to the name and port of the AggregationServer (e.g., localhost:4567).
<FilePath> denotes the path to the file containing the content to be formatted and sent.

### GETClient
A client that connects to a specified server, sends GET requests, and neatly displays the returned JSON data.

**Key Features:**
- Uses the Jackson library for JSON operations.
- Maintains a Lamport logical clock.
- Automatically retries in case of connection failures, up to a specified limit (MAX_RETRIES).
- Supports fetching data for a specific station ID or a generic GET request.
- Processes and displays JSON data, either as individual objects or arrays of objects.

**Here's a more detailed breakdown:**

**Imports:**
- JSON utilities from the Jackson library for processing and handling JSON data.
- Basic Java I/O classes for reading, writing, and handling socket-based communications.
- Java's networking libraries for creating and handling socket communications.

**Main Function:**
- Parses the server's URL and port number from the command-line argument.
- Optionally captures a station ID from the arguments.
- For a maximum of MAX_RETRIES times:
- Connects to the specified server.
- Ticks the Lamport clock.
- Sends a GET request (with or without the station ID) and the Lamport clock's current time.
- Reads the server's response.
- If the response is valid JSON, formats and displays the data. If the data represents an array of objects, each object is processed and displayed.
- In case of communication failures, retries until the maximum number of retries is reached.

**Utility Functions:**
formatAndPrintData(JsonNode jsonObject): Overloaded methods to format and display JSON data. Supports nested objects by recursively processing and displaying inner objects with indentation for clarity.
isValidJSON(String test): Checks if a string represents valid JSON data.

**Usage:**
```
java GETClient <ServerURL:Port> [StationID]
```
**Where:**
<ServerURL:Port> refers to the URL and port of the target server (e.g., http://localhost:4567).
[StationID] is an optional argument specifying a station ID for more specific requests.

### LamportClock
A class implementing the Lamport logical clock, designed to maintain a consistent order of events in distributed systems.

**Key Features:**
- Represents the clock using a simple integer counter.
**Supports:**
- Ticking the clock to signify internal events or sent messages.
- Updating the clock using another clock's time to ensure consistency upon receiving messages.
- Fetching the current clock time.

**Usage:**
```
Increment the clock for internal events: lamportClock.tick().
Synchronize with another clock: lamportClock.update(receivedTime).
Get the current time: int currentTime = lamportClock.getTime().
```

### Modify Class
A utility designed to automatically modify specific content within the src/data.txt file every 30 seconds.

**Key Features:**
- Reads and processes the content of src/data.txt.
- Modifies the content by:
- Incrementing air temperature values.
- Randomly updating wind speeds.
- Altering IDs by incrementing their last digit.
- Advancing date-time values by an hour.
- Writes the modified content back to the file.
- Automatically and periodically repeats the process.

**Usage:**
```
java Modify
```
Upon execution, the program will periodically read, modify, and write content to src/data.txt every 30 seconds.

### Testing
The system includes a comprehensive set of tests, organized into different test cases to ensure its robustness and reliability. To run these tests, a test.sh script has been provided.

**Test Procedure**

**Dependencies:**

Ensure you have curl and java installed, as they are required to run the tests.

**Setting Variables:**
- HOST: Set to "localhost"
- PORT: 4567 (or any available port)
- FILEPATH: Path to your test data (e.g., "test_data.txt")
- FILEPATH1: Path to additional data file (e.g., "data1.txt")

**Executing the Test Script:**

Run the test.sh script using the command:
```
./test.sh or make test
```
**Test Cases**

**HTTP Request Test:**

This test involves starting the AggregationServer, sending data to it using the ContentServer, making an HTTP request to fetch the data, and checking the response.

**Data Deletion After 30 Seconds:**

The test starts the AggregationServer, waits for 30 seconds, and checks if the data in the specified file (data1.txt) has been deleted or if it's empty.

**Lamport Clock:**

This test starts the AggregationServer and makes an HTTP request to fetch the current Lamport Clock value.

**Content Server Loading:**

This test checks if the AggregationServer correctly receives and loads content when the ContentServer is initiated and sends data.

At the end of these tests, you should see an "All test cases completed!" message, which indicates that all the test cases have been executed.

## Conclusion

The Weather Data Aggregation System is a robust tool designed for efficient data aggregation and display. By following the instructions provided, users can easily compile, run, and test the system components.
