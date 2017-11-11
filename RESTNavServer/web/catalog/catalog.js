const faxColors = {
	black: "BLACK",
	white: "WHITE",
	red: "RED",
	green: "GREEN",
	pink: "PINK",
	blue: "BLUE"
};

const faxEffects = {
	blur: "BLUR",
	sharp: "SHARP",
	none: "NONE"
};

const mapProjections = {
	globe: "GLOBE",
	mercator: "MERCATOR"
};

const compositeCatalog = [
	{
		key: "PAC-0001",
		name: "All Pacific, current analysis",
		map: {
			projection: mapProjections.mercator,
			north: 66.5,
			south: -48.5,
			east: -102,
			west: 127.5
		},
		canvas: {
			w: 1000,
			h: 1200
		},
		faxData: [
			{
				faxUrl: "http://www.opc.ncep.noaa.gov/P_sfc_full_ocean.gif",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				zoom: 0.3418498710866215,
				location: {
					x: 10,
					y: 10
				}
			},
			{
				faxUrl: "http://www.opc.ncep.noaa.gov/shtml/P_06hr500bw.gif",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.5209310701416114,
				location: {
					x: 15,
					y: 30
				}
			},
			{
				faxUrl: "http://www.prh.noaa.gov/hnl/graphics/stream.gif",
				transp: faxColors.white,
				effect: faxEffects.blur,
				zoom: 1.0040821009550305,
				location: {
					x: -41,
					y: 460
				}
			}
		]
	}
];
