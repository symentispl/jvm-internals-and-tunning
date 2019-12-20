#!/bin/bash

if [[ -z "$1" ]]; then
  echo "presentation name is not set, pass it as argument"
  exit
fi

PRESENTATION_NAME=$1

if [[ -d "$PRESENTATION_NAME" ]]; then
  echo "directory $1 exists, I am done here"
  exit
fi

hg clone https://bitbucket.org/kcrimson/slideon "$PRESENTATION_NAME" && \
cd "$PRESENTATION_NAME" && \
source /usr/share/virtualenvwrapper/virtualenvwrapper.sh && \
mkvirtualenv --no-site-packages -p  /usr/bin/python3.5 "$PRESENTATION_NAME" && \
workon "$PRESENTATION_NAME" && \
pip install pybuilder && \
pyb install_dependencies
