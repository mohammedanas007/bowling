#!/usr/bin/env bash
# Prints the numbers you need for the report after `mvn clean verify`.
# Usage (from the project root):  bash scripts/summary.sh
set -euo pipefail

SURE=target/surefire-reports
CSV=target/site/jacoco/jacoco.csv

[ -d "$SURE" ] || { echo "No surefire reports. Run: mvn clean verify"; exit 1; }
[ -f "$CSV" ]  || { echo "No JaCoCo CSV. Run: mvn clean verify"; exit 1; }

sum_attr() {  # sum an attribute of every <testsuite ...> across the XML reports
  grep -h -o "<testsuite [^>]*" "$SURE"/TEST-*.xml \
    | grep -o " $1=\"[0-9.]*\"" | grep -o '[0-9.]*' \
    | awk '{s+=$1} END {printf "%g", s}'
}

TESTS=$(sum_attr tests); FAIL=$(sum_attr failures); ERR=$(sum_attr errors)
SKIP=$(sum_attr skipped); TIME=$(sum_attr time)

echo "== Test execution =="
echo "Tests run : $TESTS"
echo "Passed    : $((TESTS - FAIL - ERR - SKIP))"
echo "Failed    : $FAIL"
echo "Errors    : $ERR"
echo "Skipped   : $SKIP"
echo "Time (s)  : $TIME  (sum of per-class times)"
echo

echo "== JaCoCo coverage (bundle) =="
awk -F, 'NR>1 {
  im+=$4; ic+=$5; bm+=$6; bc+=$7; lm+=$8; lc+=$9; cm+=$10; cc+=$11; mm+=$12; mc+=$13
}
function pct(c, m) { return (c+m) ? sprintf("%.1f%%", 100*c/(c+m)) : "n/a" }
END {
  printf "Instruction : %d/%d covered (%s)\n", ic, ic+im, pct(ic, im)
  printf "Branch      : %d/%d covered (%s)\n", bc, bc+bm, pct(bc, bm)
  printf "Line        : %d/%d covered (%s)\n", lc, lc+lm, pct(lc, lm)
  printf "Complexity  : %d/%d covered (%s)\n", cc, cc+cm, pct(cc, cm)
  printf "Method      : %d/%d covered (%s)\n", mc, mc+mm, pct(mc, mm)
}' "$CSV"
