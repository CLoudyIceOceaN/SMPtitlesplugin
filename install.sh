#!/bin/bash
# SMPtitlesplugin one-command installer.
# Run this in your SERVER folder (the folder with the server jar in it):
#   curl -sL https://cloudyiceocean.github.io/SMPtitlesplugin/install.sh | bash
set -e

TITLES_URL="https://cloudyiceocean.github.io/SMPtitlesplugin/SMPtitlesplugin.jar"
MONEY_URL="https://cloudyiceocean.github.io/SMPmoneyplugin/SMPmoneyplugin.jar"

if [ -d "plugins" ]; then
  DEST="plugins"
elif [ -d "../plugins" ]; then
  DEST="../plugins"
elif ls *.jar >/dev/null 2>&1 || [ -f "server.properties" ]; then
  mkdir -p plugins
  DEST="plugins"
else
  echo "Hmm, I can't find your server here."
  echo "cd into your server folder (the one with the server jar or"
  echo "server.properties in it), then run this command again."
  exit 1
fi

echo "Downloading SMPtitlesplugin..."
curl -sL "$TITLES_URL" -o "$DEST/SMPtitlesplugin.jar"

if [ ! -f "$DEST/SMPmoneyplugin.jar" ]; then
  echo "Downloading SMPmoneyplugin (titles are bought with its money)..."
  curl -sL "$MONEY_URL" -o "$DEST/SMPmoneyplugin.jar"
fi

echo ""
echo "  Installed to $DEST/"
echo ""
echo "  Now restart your server, then add your titles in"
echo "  $DEST/SMPtitlesplugin/config.yml (examples are in there)"
echo "  and type /titles reload in game. Players use /titles."
