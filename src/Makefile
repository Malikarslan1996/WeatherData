# Variables
JAVAC = javac
JAVA = java
CP = -cp
#javac -cp "jackson/*" src/AggregationServer.java
JSON_JAR = "lib/jackson/jackson-annotations-2.13.3.jar"
TYRUS_JAR = "lib/jackson/jackson-core-2.13.3.jar"
ORG_JSON_JAR = "lib/jackson/jackson-databind-2.13.3.jar"
CLASSPATH = ".:lib/jackson/jackson-annotations-2.13.3.jar:lib/jackson/jackson-core-2.13.3.jar:lib/jackson/jackson-databind-2.13.3.jar"

SRC_DIR = .
BIN_DIR = ./bin

all: compile

prepare:
	mkdir -p $(BIN_DIR)

compile: prepare
	$(JAVAC) $(CP) "$(CLASSPATH)" -d $(BIN_DIR) $(SRC_DIR)/*.java

runContentServer:
	$(JAVA) $(CP) $(BIN_DIR):$(JSON_JAR):$(TYRUS_JAR):$(ORG_JSON_JAR) ContentServer "$(HOST):$(PORT)" "$(FILEPATH)"

runAggregationServer:
	$(JAVA) $(CP) $(BIN_DIR):$(JSON_JAR):$(TYRUS_JAR):$(ORG_JSON_JAR) AggregationServer

runGETClient:
	$(JAVA) $(CP) $(BIN_DIR):$(JSON_JAR):$(TYRUS_JAR):$(ORG_JSON_JAR) GETClient "$(HOST):$(PORT)" "$(FILEPATH)"

clean:
	rm -rf $(BIN_DIR)
