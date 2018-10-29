module.exports = function (RED) {
    function LogReplayNode(config) {
        RED.nodes.createNode(this, config);
        let node = this;

        const filename = config.path;
        const freq = config.freq; // Default 1
        const loop = config.loop; // default true;
        const verbose = config.verbose; // default false;

        if (verbose === true) {
            console.log(">> Will " + (loop ? "" : "not ") + "loop.");
        }
        let fs = require('fs'),
            lineByLine = require('n-readlines');

        var reader;

        function readNext() {
            let ok = true;
            let line = reader.next();
            if (line !== undefined && line !== null && line !== false) {
//              console.log(">>> Read " + line);
                try {
                    let str = Buffer.from(line);
                    node.send({'payload': str.toString(), 'verbose': verbose});
                } catch (err) {
                    console.log(err);
                }
            } else {
                ok = false;
                console.log(loop  ? ">>>>>>> Reseting <<<<<<<<" : "Done reading");
                if (loop) {
                    readLoop();
                }

            }
            if (ok === true) {
                setTimeout(readNext, freq * 1000);
            }
        };

        function readLoop() {
            reader = new lineByLine(filename);
            readNext();
        };

        readLoop();
        console.log("log-replay running...");

        this.on('close', function() {
            // close the file here
            console.log("-- Closing");

        });
    }

    RED.nodes.registerType("log-replay", LogReplayNode);
}
