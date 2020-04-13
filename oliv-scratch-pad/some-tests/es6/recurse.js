var model = [
	{
		stuff: 'Whatever',
		items: [{
			stuff: 'Level 2',
			items: [{
				stuff: 'Elem 2-1'
			}, {
				stuff: 'Elem 2-2',
				items: [{
					stuff: 'Elem 2-2-1'
				}, {
					stuff: 'Elem 2-2-2'
				}]
			}]
		}, {
			stuff: 'Level 2'
		}]
	}];

getMaxDepth = function(treeArray) {
	let depth = 0;
	for (let nl=0; nl<treeArray.length; nl++) {
		depth = Math.max(depth, drillDown(treeArray[nl].items));
	}
	return depth;
};

drillDown = function(items) {
	let depth = 0;
	if (items !== undefined) {
		depth = 1;
		for (let i=0; i<items.length; i++) {
			depth += drillDown(items[i].items);
		}
	}
	return depth;
};

console.log('Max depth:', getMaxDepth(model));
