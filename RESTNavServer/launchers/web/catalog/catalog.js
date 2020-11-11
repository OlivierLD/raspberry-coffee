// Composite Catalog (Weather Wizard)
// A nice NOAA Chart list at
// Pacific: https://www.nws.noaa.gov/om/marine/hfhi_mobile.htm
// Atlantic: https://www.nws.noaa.gov/om/marine/hfmarsh_mobile.htm
const faxColors = { // Must match ImageColor, in PullTxManager
	black: "BLACK",
	white: "WHITE",
	red: "RED",
	green: "GREEN",
	darkgreen: "DARKGREEN",
	pink: "PINK",
	orange: "ORANGE",
	magenta: "MAGENTA",
	blue: "BLUE",
	navy: "NAVY",
	cyan: "CYAN",
	violet: "VIOLET"
};

const faxEffects = {
	blur: "BLUR",
	sharp: "SHARP",
	none: "NONE"
};

const mapProjections = {
	globe: "GLOBE",
	mercator: "MERCATOR"
	// TODO LAMBERT and others
};

const compositeCatalog = [
	{
		key: "PAC-0001-NORTH-FINE",
		name: "North Pacific, current analysis (fine)",
		map: {
			projection: mapProjections.mercator,
			north: 66.5,
			south: 10,
			east: -92,
			west: 127.5
		},
		canvas: {
			w: 1200,
			h: 800
		},
		faxData: [
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PYBA90.gif",
				name: "North Pac surface analysis, East",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				rotation: 90,
				zoom: 0.4172253717050488,
				location: {
					x: 573,
					y: 36
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PYBA91.gif",
				name: "North Pac surface analysis, West",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				rotation: 90,
				zoom: 0.4163207653306388,
				location: {
					x: 74,
					y: 36
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PPBA10.gif",
				name: "North Pac 500mb analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.6382782963114088,
				location: {
					x: 78,
					y: 57
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PJBA99.gif",
				name: "North Pac Sea State analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.darkgreen
				},
				effect: faxEffects.blur,
				zoom: 0.6382782963114088,
				location: {
					x: 79,
					y: 60
				}
			}
			//
		],
		gribRequest: 'GFS:65N,10N,130E,110W|1,1|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	},
	{
		key: "PAC-0001-NEW",
		name: "All Pacific, current analysis (fine)",
		map: {
			projection: mapProjections.mercator,
			north: 66.5,
			south: -48.5,
			east: -92,
			west: 127.5
		},
		canvas: {
			w: 1000,
			h: 1200
		},
		faxData: [
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PYBA90.gif",
				name: "North Pac surface analysis, East",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				rotation: 90,
				zoom: 0.3432523458477064,
				location: {
					x: 471,
					y: 31
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PYBA91.gif",
				name: "North Pac surface analysis, West",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				rotation: 90,
				zoom: 0.3442206645644906,
				location: {
					x: 58,
					y: 30
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PPBA10.gif",
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
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PWFA11.gif",
				name: "Central Pac Streamlines",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.violet
				},
				effect: faxEffects.blur,
				rotation: 90,
				zoom: 0.41601580228005985,
				location: {
					x: 22,
					y: 402
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PJBA99.gif",
				name: "North Pac Sea State analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.darkgreen
				},
				effect: faxEffects.blur,
				zoom: 0.5251131339813421,
				location: {
					x: 66,
					y: 48
				}
			}
			//
		],
		gribRequest: 'GFS:65N,45S,130E,110W|1,1|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	},
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
		key: "PAC-0002",
		name: "All Pacific, current analysis (WIP)",
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
				faxUrl: "https://ocean.weather.gov/P_sfc_full_ocean_color.png",
				name: "North Pac surface analysis",
				transp: faxColors.white,
				effect: faxEffects.blur,
				zoom: 0.37096066812224876,
				location: {
					x: 62,
					y: 27
				}
			},
			{
				faxUrl: "https://ocean.weather.gov/shtml/P_00hr500.gif",
				name: "North Pac 500mb analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.7589960618086322,
				location: {
					x: 64,
					y: 47
				}
			},
			{
				faxUrl: "https://www.prh.noaa.gov/hnl/graphics/stream.gif",
				name: "Central Pac Streamlines",
				transp: faxColors.white,
				effect: faxEffects.blur,
				zoom: 1.0091025114598056,
				location: {
					x: -30,
					y: 482
				}
			}
		],
		gribRequest: 'GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	},
	{
		key: "PAC-0003",
		name: "All Pacific, current analysis (WIP, 2)",
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
				faxUrl: "https://ocean.weather.gov/P_sfc_full_ocean_color.png",
				name: "North Pac surface analysis",
				transp: faxColors.white,
				effect: faxEffects.blur,
				zoom: 0.37096066812224876,
				location: {
					x: 62,
					y: 27
				}
			},
			{
				faxUrl: "https://ocean.weather.gov/shtml/P_00hr500.gif",
				name: "North Pac 500mb analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.7589960618086322,
				location: {
					x: 64,
					y: 47
				}
			},
			{
				faxUrl: "https://www.prh.noaa.gov/hnl/graphics/stream.gif",
				name: "Central Pac Streamlines",
				transp: faxColors.white,
				effect: faxEffects.blur,
				zoom: 1.0091025114598056,
				location: {
					x: -30,
					y: 482
				}
			}
		],
		gribRequest: 'GFS:65N,45S,130E,110W|1,1|0,6..72|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
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
		key: "ATL-0001-FINE",
		name: "North Atlantic, current analysis (fine)",
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
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PYAA12.gif",
				name: "North-West Atl surface analysis",
				transp: faxColors.white,
				rotation: 90,
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
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PYAA11.gif",
				name: "North-East Atl surface analysis",
				transp: faxColors.white,
				rotation: 90,
				tx: {
					from: faxColors.black,
					to: faxColors.red
				},
				effect: faxEffects.blur,
				zoom: 0.32291855775920775,
				location: {
					x: 401,
					y: 8
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PPAA10.gif",
				name: "North Atl 500mb analysis",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.blue
				},
				effect: faxEffects.blur,
				zoom: 0.49330500605497085,
				location: {
					x: 26,
					y: 27
				}
			},
			{
				faxUrl: "https://tgftp.nws.noaa.gov/fax/PJAA99.gif",
				name: "North Atl Sea state",
				transp: faxColors.white,
				tx: {
					from: faxColors.black,
					to: faxColors.darkgreen
				},
				effect: faxEffects.blur,
				zoom: 0.4928121938611098,
				location: {
					x: 26,
					y: 27
				}
			}
		],
		gribRequest: 'GFS:65N,10N,100W,10E|1,1|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
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
			h: 600
		},
		gribRequest: 'GFS:45N,30N,10W,40E|0.5,0.5|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN'
	}
];
