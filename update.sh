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

ICON_DIR=$(cd "${TMP_DIR}"/AWS-Architecture-* && pwd)
ICON_VERSION_DIR=${ICON_DIR#"${TMP_DIR}"/AWS-Architecture-}
ICON_VERSION=''
REGEX_NUMBER='([0-9]+)'
if ! [[ ${ICON_VERSION_DIR} =~ ${REGEX_NUMBER} ]]; then
  echo >&2 "Couldn't parse version number from directory section: ${ICON_VERSION_DIR}"
  exit 1
else
  ICON_VERSION="${BASH_REMATCH[1]}"
fi

if [ -z "$ICON_VERSION" ]; then
  echo >&2 "Couldn't parse version number from directory section: ${ICON_VERSION_DIR}"
  exit 1
fi

echo "Detected version: ${ICON_VERSION}"

# Compile our translator
pushd "${DIR}/translator"
mvn package
popd

# Clear our old .graphml files
rm -f "${DIR}"/*.graphml

fix_mistakes() {
  case "$(echo $1 | tr '[:upper:]' '[:lower:]')" in
    # Someone messed up the spelling :O
    lot|"internet of things")
      echo "IoT"
      ;;
    # Misspelling
    "customer enagagement")
      echo "Customer Engagement"
      ;;
    app*integration)
      echo "Application Integration"
      ;;
    migration*transfer)
      echo "Migration and Transfer"
      ;;
    networking*content*)
      echo "Networking and Content Delivery"
      ;;
    security*identity*compliance)
      echo "Security Networking and Compliance"
      ;;
    *)
      echo "$1"
      ;;
  esac
}


find "${ICON_DIR}" -type d -name "*Light" -o -type d -name "*32" | sort | while read section; do
  SECTION_NAME="$(echo ${section} | tr ' ' '_')"
  SECTION_NAME="$(dirname ${SECTION_NAME})"
  SECTION_NAME="$(basename ${SECTION_NAME})"
  SECTION_NAME="${SECTION_NAME#Res_}"
  SECTION_NAME="${SECTION_NAME#Arch_}"
  SECTION_NAME="$(echo ${SECTION_NAME} | tr '_-' ' ' | tr -s ' ' ' ')"
  SECTION_NAME="$(fix_mistakes "${SECTION_NAME}")"
  if [ -z "$SECTION_NAME" ]; then
    echo >&2 "Got empty section when parsing ${section}"
    exit 1
  fi
  SECTION_NAME="AWS - ${SECTION_NAME}"
  echo "Found: ${SECTION_NAME}"
  mkdir -p "${TMP_DIR}/sections/${SECTION_NAME}"
  cp "${section}"/*.svg "${TMP_DIR}/sections/${SECTION_NAME}"
done

for section in "${TMP_DIR}/sections/"*; do
  SECTION_NAME="${section#${TMP_DIR}/sections/}"
  if [ -z "$SECTION_NAME" ]; then
    echo >&2 "Got empty section when parsing ${section}"
    exit 1
  fi
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
