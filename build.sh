#!/bin/bash

# Define variables
project_dir="/home/roxane/Documents/GitHub/Framework_java"
test_dir="/home/roxane/Documents/Naina/test"
web_dir="/opt/tomcat/apache-tomcat-10.1.23"

lib_dir="$web_dir/lib/servlet-api.jar"
src_dir="$project_dir/src"
bin_dir="$project_dir/bin"

# Find all .java files in the specified directories
java_files=$(find "$src_dir/annotation" "$src_dir/exception" "$src_dir/utils" "$src_dir/mg/itu/prom16" -name "*.java")

# Compile Java files
javac -cp "$bin_dir:$lib_dir" -d "$bin_dir" $java_files

# Create the jar file directly in the desired location
jar -cvf "$test_dir/lib/framework.jar" -C "$bin_dir" .
