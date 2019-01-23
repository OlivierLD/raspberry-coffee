/**
 Subscribe like this:
 events.subscribe('topic', (val) => {
   doSomethingSmart(val);
 });

 Publish like that:
 events.publish('topic', val);
 */
let events = {

	listener: [],

	subscribe: function(topic, action) {
		this.listener.push({
			'topic': topic,
			'action': action
		});
	},

	publish: function(topic, value) {
		this.listener.forEach((lsnr, idx) => {
			if (lsnr.topic === topic) {
				try {
					lsnr.action(value);
				} catch (err) {
					console.log("Topic %s, index %d, err: %s", topic, idx, err);
				}
			}
		});
	}

};
