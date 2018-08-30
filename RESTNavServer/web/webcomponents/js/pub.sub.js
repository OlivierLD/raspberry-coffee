/**
 Subscribe like this:
 events.subscribe('topic', function(val) {
   doSomethingSmart(val);
 });

 Publish like that:
 events.publish('topic', val);
 */
let events = {
	listener: [],

	subscribe: function (topic, action) {
		this.listener.push({
			'topic': topic,
			'actionListener': action
		});
	},

	publish: function (topic, value) {
		this.listener.forEach((lsn, idx) => {
			if (lsn.topic === topic) {
				try {
					lsn.actionListener(value);
				} catch (err) {
					console.log("Topic %s, index %d, err: %s", topic, idx, err);
				}
			}
		});
	}
};
