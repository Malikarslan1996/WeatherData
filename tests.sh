#!/bin/sh

# Variables
HOST="localhost"
PORT=4567
FILEPATH="test_data.txt"
FILEPATH1="data1.txt"


# Test Case: HTTP request

echo "\nTest Case 1: HTTP request..."
java -cp ./bin:"./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar" AggregationServer $PORT &
AGGREGATION_PID=$!
sleep 2
java -cp ./bin:"./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar" ContentServer "$HOST:$PORT" $FILEPATH &
CONTENT_PID=$!
sleep 10
kill $CONTENT_PID
sleep 2
RESPONSE=$(curl -X GET "http://$HOST:$PORT")
echo "Received response: $RESPONSE"
kill $AGGREGATION_PID
sleep 2

# Test Case: Data deletion after 35 seconds
echo "\nTest Case 2: Data deletion after 35 seconds..."
java -cp ./bin:"./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar" AggregationServer $PORT &
AGGREGATION_PID=$!
sleep 2


# Now, we will sleep for 35 seconds and check the content of data1.txt
echo "Waiting for 35 seconds..."
sleep 50
if [ ! -f FILEPATH1 ]; then
    echo "data1.txt is deleted after 35 seconds."
else
    CONTENT=$(cat FILEPATH1)
    if [ -z "$CONTENT" ]; then
        echo "data1.txt is empty after 35 seconds. Data removal successful."
    else
        echo "data1.txt still contains data after 35 seconds."
    fi
fi
kill $AGGREGATION_PID
sleep 2

# Test Case: Lamport Clock
echo "\nTest Case 3: Lamport Clock..."
java -cp ./bin:"./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar" AggregationServer $PORT &
AGGREGATION_PID=$!
sleep 2
CLOCK_VALUE=$(curl -X GET "http://$HOST:$PORT/clock")
echo "Current Lamport Clock value: $CLOCK_VALUE"
kill $AGGREGATION_PID
sleep 2

# Test Case: Content Server Loading
echo "\nTest Case 4: Content Server Loading..."
java -cp ./bin:"./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar" AggregationServer $PORT &
AGGREGATION_PID=$!
sleep 2
java -cp ./bin:"./lib/jackson/jackson-annotations-2.13.3.jar:./lib/jackson/jackson-core-2.13.3.jar:./lib/jackson/jackson-databind-2.13.3.jar" ContentServer "$HOST:$PORT" $FILEPATH &
CONTENT_PID=$!
sleep 5
RESPONSE=$(curl -X GET "http://$HOST:$PORT")
echo "Received response after Content Server loading: $RESPONSE"
kill $AGGREGATION_PID
kill $CONTENT_PID

echo "\nAll test cases completed!"
