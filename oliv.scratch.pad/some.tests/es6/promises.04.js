// Async test
async function f() {
    console.group('In the async function');
    return 1;
}

console.log('Invoking async, and then...');
f().then(console.log);


// async, and await
async function f2() {
    let promise = new Promise((resolve, reject) => {
        setTimeout(() => resolve("Done!"), 1000);
    });

    let result = await promise; // Wait for the resolve
    console.log('await result:', result);
}

f2();
