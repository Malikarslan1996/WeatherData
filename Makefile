# Variables
JAVAC = javac
JAVA = java
CP = -cp
CLASSPATH = ".:./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar"

SRC_DIR = src
BIN_DIR = ./bin

all: compile

prepare:
	mkdir -p $(BIN_DIR)

compile: prepare
	$(JAVAC) $(CP) "$(CLASSPATH)" -d $(BIN_DIR) $(SRC_DIR)/*.java

runContentServer:
	$(JAVA) $(CP) $(BIN_DIR):"$(CLASSPATH)" ContentServer "$(HOST):$(PORT)" "$(FILEPATH)"

runAggregationServer:
	$(JAVA) $(CP) $(BIN_DIR):"$(CLASSPATH)" AggregationServer

runModify:
	$(JAVA) $(CP) $(BIN_DIR):"$(CLASSPATH)" Modify

runGETClient:
	$(JAVA) $(CP) $(BIN_DIR):"$(CLASSPATH)" GETClient "$(HOST):$(PORT)" "$(ID)"

clean:
	rm -rf $(BIN_DIR)

# Test targets
prepare-test:
	chmod +x tests.sh

test: compile prepare-test
	./tests.sh
