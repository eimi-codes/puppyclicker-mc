#!/usr/bin/env bash

set -euo pipefail

manifest="${1:-.github/release-targets.json}"

jq -e '
  type == "array"
  and length == 15
  and all(.[];
    (.display | type == "string" and length > 0)
    and (.loader | IN("neoforge", "forge", "fabric"))
    and (.minecraft | type == "string" and length > 0)
    and (.java | IN("17", "21", "25"))
    and (.artifact | type == "string" and contains("{version}"))
  )
  and ([.[].artifact] | unique | length == 15)
' "$manifest" > /dev/null

while IFS= read -r template; do
  module_directory="${template%%/build/libs/*}"
  if [[ "$module_directory" == "$template" || ! -f "$module_directory/build.gradle" ]]; then
    echo "Release target does not map to a Gradle module: $template" >&2
    exit 1
  fi
done < <(jq -r '.[].artifact' "$manifest")
