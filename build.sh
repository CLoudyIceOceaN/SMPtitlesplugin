#!/bin/bash
# Builds SMPtitlesplugin.jar — run:  ./build.sh
set -e
cd "$(dirname "$0")"

JDK=/opt/homebrew/opt/openjdk@21/bin
API=libs/spigot-api-1.8.8.jar
MONEY=libs/SMPmoneyplugin.jar

mkdir -p libs
if [ ! -f "$API" ]; then
  echo "Downloading Spigot 1.8.8 API..."
  curl -sL "https://repo.codemc.io/repository/nms/org/spigotmc/spigot-api/1.8.8-R0.1-SNAPSHOT/spigot-api-1.8.8-R0.1-20190527.160122-6.jar" -o "$API"
fi
if [ -f "../SMPmoneyplugin/SMPmoneyplugin.jar" ]; then
  cp ../SMPmoneyplugin/SMPmoneyplugin.jar "$MONEY"
elif [ ! -f "$MONEY" ]; then
  echo "Downloading SMPmoneyplugin..."
  curl -sL "https://cloudyiceocean.github.io/SMPmoneyplugin/SMPmoneyplugin.jar" -o "$MONEY"
fi

rm -rf target/classes
mkdir -p target/classes
"$JDK/javac" --release 8 -Xlint:-options -encoding UTF-8 -cp "$API:$MONEY" -d target/classes src/main/java/dev/bladesmp/titles/*.java
cp src/main/resources/*.yml target/classes/
(cd target/classes && "$JDK/jar" cf ../SMPtitlesplugin.jar .)
cp target/SMPtitlesplugin.jar SMPtitlesplugin.jar
echo "Done! Plugin is at: SMPtitlesplugin.jar"
