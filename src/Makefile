# Variables
JAVAC = javac
JAVA = java
CP = -cp
CLASSPATH = $(shell echo "'.:/Weather Data/json.jar:/Weather Data/tyrus-standalone-client-1.9.jar:/Weather Data/json-20230618.jar'")
# Change this to your actual JSON and Tyrus JAR paths
JSON_JAR = "/Weather Data/json.jar"
TYRUS_JAR = "/Weather Data/tyrus-standalone-client-1.9.jar"
ORG_JSON_JAR = "/Weather Data/json-20230618.jar"
#HOST = localhost
#PORT = 8887
FILEPATH = /Weather Data/src/data.txt

SRC_DIR = .
BIN_DIR = ./bin

all: compile

prepare:
	mkdir -p $(BIN_DIR)

compile: prepare
	$(JAVAC) $(CP) $(CLASSPATH) -d $(BIN_DIR) $(SRC_DIR)/*.java

runContentSer:
	$(JAVA) $(CP) $(BIN_DIR):$(CLASSPATH) ContentSer "$(HOST):$(PORT)" "$(FILEPATH)"

runAggregationS:
	$(JAVA) $(CP) $(BIN_DIR):$(JSON_JAR):$(TYRUS_JAR):$(ORG_JSON_JAR) AggregationS

runClient:
	$(JAVA) $(CP) $(BIN_DIR):$(JSON_JAR):$(TYRUS_JAR):$(ORG_JSON_JAR) Client "$(HOST):$(PORT)" "$(FILEPATH)"

clean:
	rm -rf $(BIN_DIR)
