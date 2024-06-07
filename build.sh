#!/bin/bash

# Define variables
project_dir="/home/Documents/Naina/test"
web_dir="/opt/tomcat/apache-tomcat-10.1.23"

lib_dir="$web_dir/lib/servlet-api.jar"
src_dir="$project_dir/src"
bin_dir="$project_dir/bin"

# Compile Java files
javac -cp "$bin_dir:$lib_dir" -d "$bin_dir" "$src_dir/annotation/*.java" "$src_dir/exception/*.java" "$src_dir/utils/*.java" "$src_dir/mg/itu/prom16/*.java"

# Change to the bin directory
cd "$bin_dir" || exit

# Create the jar file
jar -cvf "$project_dir/lib/framework.jar" *