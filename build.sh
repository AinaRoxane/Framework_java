#!/bin/bash

# Définition des variables
project_dir="/home/toffee/Documents/school/S5/Naina/SPRINT/v2/Framework"
test_dir="/home/toffee/Documents/school/S5/Naina/SPRINT/v3/flyght"
web_dir="/opt/tomcat"
gson_jar="$web_dir/lib/com.google.gson_2.10.1.v20230109-0753.jar"
lib_dir="$web_dir/lib/servlet-api.jar"

src_dir="$project_dir/src"
bin_dir="$project_dir/bin"
temp_dir="$project_dir/temp"

# Vérification et création des répertoires si nécessaire
mkdir -p "$bin_dir" "$temp_dir"

# Trouver et compiler tous les fichiers Java
find "$src_dir" -name "*.java" > "$temp_dir/sources.txt"
javac -cp "$lib_dir:$gson_jar:$test_dir/lib/*" -d "$bin_dir" @"$temp_dir/sources.txt"

# Vérification si la compilation a réussi
if [ $? -ne 0 ]; then
    echo "Erreur : Compilation échouée."
    exit 1
fi

# Création du JAR
jar -cvf "$test_dir/lib/framework.jar" -C "$bin_dir" .

echo "Déploiement terminé avec succès !"
