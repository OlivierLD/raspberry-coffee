// Composite Catalog (Weather Wizard)
const faxColors = {
	black: "BLACK",
	white: "WHITE",
	red:   "RED",
	green: "GREEN",
	pink:  "PINK",
	blue:  "BLUE"
};

const faxEffects = {
	blur:  "BLUR",
	sharp: "SHARP",
	none:  "NONE"
};

const mapProjections = {
	globe:    "GLOBE",
	mercator: "MERCATOR"
};

const compositeCatalog = [
	{
		key: "PAC-0001",
		name: "All Pacific, current analysis",
		map: {
			projection: mapProjections.mercator,
			north:  66.5,
			south: -48.5,
			east: -102,
			west:  127.5
		},
		canvas: {
			w: 1000,
			h: 1200
		},
		faxData: [
			{
				faxUrl: "http://www.opc.ncep.noaa.gov/P_sfc_full_ocean.gif",
				name: "North Pac surface analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				zoom: 0.34390610490333456,
				location: {
					x: 61,
					y: 30
				}
			},
			{
				faxUrl: "http://www.opc.ncep.noaa.gov/shtml/P_00hr500bw.gif",
				name: "North Pac 500mb analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.5251131339813421,
				location: {
					x: 66,
					y: 48
				}
			},
			{
				faxUrl: "http://www.prh.noaa.gov/hnl/graphics/stream.gif",
				name: "Central Pac Streamlines",
				transp: faxColors.white,
				effect: faxEffects.blur,
				zoom: 1.0040821009550305,
				location: {
					x: 14,
					y: 482
				}
			}
		],
		gribRequest: 'GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	},
	{
		key: "ATL-0001",
		name: "North Atlantic, current analysis",
		map: {
			projection: mapProjections.mercator,
			north: 65.5,
			south: 10,
			east: 28.2,
			west: -101.8
		},
		canvas: {
			w: 900,
			h: 600
		},
		faxData: [
			{
				faxUrl: "http://www.opc.ncep.noaa.gov/A_sfc_full_ocean.gif",
				name: "North Atl surface analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				zoom: 0.32291855775920775,
				location: {
					x: 13,
					y: 8
				}
			},
			{
				faxUrl: "http://www.opc.ncep.noaa.gov/shtml/A_00hr500bw.gif",
				name: "North Atl 500mb analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.4928121938611098,
				location: {
					x: 26,
					y: 27
				}
			}
		],
		gribRequest: 'GFS:65N,10N,100W,10E|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	},
	{
		key: "MED-0001",
		name: 'Mediterranean GRIBs',
		map: {
			projection: mapProjections.mercator,
			north: 50,
			south: 20,
			east: 45,
			west: -20
		},
		canvas: {
			w: 1000,
			h:  600
		},
		gribRequest: 'GFS:45N,30N,10W,40E|0.5,0.5|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	}
];
