#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

ensure_on_path() {
  command -v "$1" >/dev/null 2>&1 || { echo >&2 "I require '$1' but it's not installed. Aborting."; exit 1; }
}

ensure_on_path curl
ensure_on_path git

URL=$(curl -s https://aws.amazon.com/architecture/icons/ | grep 'Asset Package&nbsp;<i class="icon-download"></i>' | head -n1 | grep -oEi '//.*\.zip' | while read line; do echo "https:$line";  done)
echo "Latest URL: ${URL}"

# Clone the git repo
ssh-keyscan github.com >> ~/.ssh/known_hosts
git config --global user.name "Justin Derby"
git config --global user.email "justderb@gmail.com"
git clone git@github.com:JustDerb/yed-aws-palettes.git
cd "${DIR}/yed-aws-palettes"

# Grab our metadata
. ./metadata.config

if [[ "${URL}" != "${ASI_url}" ]]; then
  echo "AWS Simple Icons outdated..."
  echo "New: ${URL}"
  echo "Old: ${ASI_url}"
  ./update.sh "$URL" true
  git push
else
  echo "AWS Simple Icons are up to date!"
  echo "${URL}"
fi
