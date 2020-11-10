#!/bin/bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

ensure_on_path() {
  command -v "$1" >/dev/null 2>&1 || { echo >&2 "I require '$1' but it's not installed. Aborting."; exit 1; }
}

display_help() {
  echo    >&2 "$0 <url> <version> <commit>"
  echo -e >&2 "\turl: URL to AWS Simple Icons"
  echo -e >&2 "\tcommit: Auto-commit changes"
}

ensure_on_path curl
ensure_on_path java
ensure_on_path mvn
ensure_on_path git
ensure_on_path unzip

if [[ $# -lt 1 ]]; then
  display_help
  exit 1
fi

URL=$1
COMMIT=$2

TMP_DIR=$(mktemp -d)
trap "{ rm -rf ${TMP_DIR}; }" EXIT

echo >&2 "Downloading $1"
curl "${URL}" --output "${TMP_DIR}/aws-simple-icons.zip"
unzip -o "${TMP_DIR}/aws-simple-icons.zip" -d "${TMP_DIR}"

ICON_DIR=$(cd "${TMP_DIR}"/AWS-Architecture-Icons_SVG_* && pwd)
ICON_VERSION=${ICON_DIR#"${TMP_DIR}"/AWS-Architecture-Icons_SVG_}
REGEX_NUMBER='^[0-9]+$'
if ! [[ ${ICON_VERSION} =~ ${REGEX_NUMBER} ]]; then
  echo >&2 "Couldn't parse version number from URL: ${ICON_VERSION}"
  exit 1
fi

echo "Detected version: ${ICON_VERSION}"

# Compile our translator
pushd "${DIR}/translator"
mvn package
popd

# Clear our old .graphml files
rm -f "${DIR}"/*.graphml

find "${ICON_DIR}/SVG Light/" -mindepth 1 -type d | sort | while read section; do
  SECTION_NAME="${section#${ICON_DIR}/SVG Light/}"
  SECTION_NAME="AWS - ${SECTION_NAME/\// - }"
  echo "${SECTION_NAME}"
  java -jar "${DIR}/translator/target/yed-translator-1.0-SNAPSHOT.jar" \
      --out "${DIR}/${SECTION_NAME}.graphml" \
      --url "${URL}" \
      --version "${ICON_VERSION}" \
      "${section}/"*.svg
done

echo "ASI_version=${ICON_VERSION}" > "${DIR}/metadata.config"
echo "ASI_url=${URL}" >> "${DIR}/metadata.config"

if [[ -n "${COMMIT}" ]]; then
  git add "${DIR}/"*.graphml
  git add "${DIR}/metadata.config"
  git commit -m "Updating .graphml files to version ${ICON_VERSION}"
  echo "git: Updating .graphml files to version ${ICON_VERSION}"
fi
