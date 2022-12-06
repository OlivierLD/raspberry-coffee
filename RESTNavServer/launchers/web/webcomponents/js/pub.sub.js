/**
 Subscribe like this:
 events.subscribe('topic', (val) => {
   doSomethingSmart(val);
 });

 Publish like that:
 events.publish('topic', val);
 */
let events = {

	// topic names
	topicNames: {
		POS: 'pos',
		BSP: 'bsp',
		LOG: 'log',
		DAILY_LOG: 'daily-log',
		GPS_DATE_TIME: 'gps-time',
		GPS_SAT: 'gps-sat',
		TRUE_HDG: 'hdg',
		TWD: 'twd',
		TWA: 'twa',
		TWS: 'tws',
		WATER_TEMP: 'wt',
		AIR_TEMP: 'at',
		PRMSL: "prmsl",
		REL_HUM: 'hum',
		AWS: 'aws',
		AWA: 'awa',
		CURRENT_DIR: 'cdr',
		CURRENT_SPEED: 'csp',
		COG: 'cog',
		SOG: 'sog',
		CMG: 'cmg',
		MAX_LEEWAY_ANGLE: 'max-leeway',
		LEEWAY_ANGLE: 'leeway',
		DAMP_CSP_PREFIX: 'csp-',
		DAMP_CDR_PREFIX: 'cdr-',
		TO_WP: 'wp',
		VMG: 'vmg',
		LAST_NMEA: 'nmea',
		DECL: 'decl',
		DEV: 'dev',
		BSP_COEFF: 'bsp-coeff',
		AWS_COEFF: 'aws-coeff',
		HDG_OFFSET: 'hdg-offset',
		AWA_OFFSET: 'awa-offset'
	},

	listener: [],

	subscribe: function(topic, action) {
		this.listener.push({
			'topic': topic,
			'action': action
		});
	},

	commonPublish: function(topic, value) {
		// Empty by default
	},

	publish: function(topic, value) {
		this.commonPublish(topic, value);
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
