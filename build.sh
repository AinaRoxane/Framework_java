#!/bin/bash

# Define variables
project_dir="/home/toffee/Documents/GitHub/Framework_java"
test_dir="/home/toffee/Documents/S5/Naina/test"
web_dir="/opt/tomcat"
gson_jar="/home/toffee/Documents/apis/com.google.gson_2.10.1.v20230109-0753.jar"

lib_dir="$web_dir/lib/servlet-api.jar"
src_dir="$project_dir/src"
bin_dir="$project_dir/bin"

# Find all .java files in the specified directories
java_files=$(find "$src_dir/annotation" "$src_dir/exception" "$src_dir/utils" "$src_dir/mg/itu/prom16" -name "*.java")

# Compile Java files with gson.jar included
javac -cp "$bin_dir:$lib_dir:$gson_jar" -d "$bin_dir" $java_files

# Create the jar file directly in the desired location
jar -cvf "$test_dir/lib/framework.jar" -C "$bin_dir" .
