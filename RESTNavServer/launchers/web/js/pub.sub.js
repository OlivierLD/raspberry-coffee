/**
 Subscribe like this:
 events.subscribe('topic', (val) => {
   doSomethingSmart(val);
 });

 Publish like that:
 events.publish('topic', val);

 Don't use arrow functions in the members below (ok when subscribing though, see the comment above).
 */
var events = {
    listener: [],

    subscribe: function (topic, action) {
        this.listener.push({
            'topic': topic,
            'actionListener': action
        });
    },

    publish: function (topic, value) {
        for (var i = 0; i < this.listener.length; i++) {
            if (this.listener[i].topic === topic) {
                this.listener[i].actionListener(value);
            }
        }
    }
};
