#!/bin/bash
cat > sample-data.json << EOF
{
    "timestamp": 1234567890,
    "report": "Age Report",
    "results": [
        { "name": "John", "age": 43, "city": "TownA" },
        { "name": "Joe",  "age": 10, "city": "TownB" }
    ]
}
EOF
#
echo -e "Sample data written to file system."
#
echo -e "Running jq '. | {timestamp,report}' sample-data.json"
echo -e "----------------------------------------"
jq '. | { timestamp, report }' sample-data.json
echo -e "----------------------------------------"
jq '.results[] | { name, age }' sample-data.json
echo -e "----------------------------------------"
jq -r '.results[] | { name, age } | join(" ")' sample-data.json
echo -e "----------------------------------------"
jq '.results[] | select(.name == "John") | {age}' sample-data.json          # Get age for 'John'
echo -e "----------------------------------------"
jq '.results[] | select((.name == "Joe") and (.age = 10))' sample-data.json # Get complete records for all 'Joe' aged 10
echo -e "----------------------------------------"
jq '.results[] | select(.name | contains("Jo"))' sample-data.json           # Get complete records for all names with 'Jo'
echo -e "----------------------------------------"
jq '.results[] | select(.name | test("Joe|John"))' sample-data.json         # Get complete records for all names matching PCRE regex 'Joe\+Smith'
echo -e "----------------------------------------"
