let re = new RegExp("^([a-z0-9]{5,})$");
let term = "sample1";
console.log(`${term} with ${re}`);
if (re.test(term)) {
    console.log("Match");
} else {
    console.log("No match");
}

re = new RegExp("\\/gridpoints\\/[A-Z]{3}\\/[0-9]*,[0-9]*\\/forecast");
term = "/rest/gridpoints/MTR/85,126/forecast";
console.log(`${term} with ${re}`);
if (re.test(term)) {
    console.log("Match");
} else {
    console.log("No match");
}

