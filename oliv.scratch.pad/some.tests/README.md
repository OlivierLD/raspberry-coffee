# Project 1-PAAS-UI

## First step: 1Paas Branding Bar

Embeddable Branding Bar based on OJ CCA Components

![Branding Bar](./branding-bar.png)

Find out more about [CCA](https://blogs.oracle.com/groundside/cca)

## How to Consume it?

### Get it

The first step is to download the bundle, you can automate it using BowerJS or manually download it.

#### Bower

Below there is an snippet on how to download it with Bower. 
This snippet should be added to a file named `bower.json`, located in the directory you run `bower` from.

```json
{
  "name": "my-1paas-app",
  "dependencies": {
    "requirejs": "2.1.16",
    "knockout": "3.4.0",
    "ic-components": "git+https://orahub.oraclecorp.com/fmw-bpm-composer/ic-components.git#ojet/2.3"
  },
  "devDependencies": {
  },
  "ignore": [
    "node_modules/",
    ".npmignore",
    "package.json",
    "hooks",
    ".idea"
  ],
  "resolutions": {
    "jquery": "*",
    "es6-promise" : "*"
  }
}

```

Once you've edited the `bower.json` file, run the following command:
```bash
 $ bower install
```
_Warning:_ If you are behind a firewall, some external resources (like JQuery, Underscore, etc) will require a proxy to be set.
Bower requires some specific environment variable to be set (fifferent from `git` and `npm`...):
```bash
 $ export http_proxy=http://www-proxy.us.oracle.com:80
 $ export https_proxy=http://www-proxy.us.oracle.com:80
 $ bower install
```
Once the `bower install` command is completed successfully, you should see a `bower_components` directory 
where you ran the command from.

![Bower Copmponents](./docimg/bower.components.png)

### Configure it
#### Dependencies

The component has the following external dependencies:

* Knockout 3.4.0
* OJET V2.3
* Jquery 2.1.3

#### RequireJS Configuration

You will need to specify all the external dependencies in the RequireJS Configuration (the file is `require-config.js`):

```javascript
requirejs.config({
	baseUrl: './',
	// Path mappings for the logical module names
	paths: {
		'knockout': 'bower_components/knockout/dist/knockout.debug',
		'ojs/ojcore': 'bower_components/oraclejet/dist/js/libs/oj/debug/ojcore',
		'ojs/ojbutton': 'bower_components/oraclejet/dist/js/libs/oj/debug/ojbutton',
		'ojs/ojfilmstrip': 'bower_components/oraclejet/dist/js/libs/oj/debug/ojfilmstrip',
		'ojL10n': 'bower_components/oraclejet/dist/js/libs/oj/ojL10n',
		'ojtranslations': 'bower_components/oraclejet/dist/js/libs/oj/resources',
		'jquery': 'bower_components/jquery/dist/jquery',
		'promise': 'bower_components/es6-promise/promise',
		'knockout-amd-helpers': 'bower_components/jet/js/libs/knockout/knockout-amd-helpers.min',
    'paas-branding': 'bower_components/ic-components/dist/paas-branding',
		'suiteComponentsMsg': 'bower_components/ic-components/src/main/js/suiteComponentsMsg'
	}
});
``` 

Notice that the `paths` element (combined with the `baseUrl`) of the `json` above refers to data downloaded during the `bower install` steps.
Notice the way `require-config.js` is mentioned in then code.

### Add it to your page

Below there is an example on how this can be embedded.
Create a file named - for example - `index.html`, as follow:
```html
<!DOCTYPE html>
<html>
<head>
	<title>My 1-PAAS App</title>
	<link rel="icon" type="image/jpg" href="oracle.jpeg">

	<meta charset="UTF-8">
	<meta name="viewport" content="user-scalable=yes,width=device-width,initial-scale=1.0,maximum-scale=1.0" />

	<link rel="stylesheet" href="bower_components/oraclejet/dist/css/alta/oj-alta.css">

	<script type="text/javascript" src="bower_components/requirejs/require.js"></script>
	<script type="text/javascript" src="require-config.js"></script>

	<script src="runner.js"></script>
</head>
<body class="oj-sm-flex-wrap-nowrap oj-flex oj-flex-item">

  <div id="page" class="oj-web-applayout-page" style="position: relative; width:100%; height: 100%">

    <paas-branding title="{{title}}" user-menu="{{menu}}" user-name="{{userName}}" help="{{help}}"
                   data-bind="event:{'mainMenu':toggleMainMenu}">
    </paas-branding>

  </div>

</body>
</html>
```

Notice in the code above the reference to `runner.js`, here is its content:
```javascript
require(['paas-branding', 'knockout'], function(PaasBranding, ko) {

	'use strict';
		
	 //initialize the bundle specifying where the images are located.
   PaasBranding.init('bower_components/ic-components/dist/');

   var model = {
       title: 'My 1PAAS App',
       userName: 'Tony Stark',
       help: [{
           label: 'Get Started',
           type: 'URL',
           properties: {
               url: 'http://oracle.com'
           }
       },
       {
           label: 'More Help',
           type: 'URL',
           properties: {
               url: 'http://www.oracle.com/technetwork/indexes/downloads/index.html'
           }
       }],
       menu: [{
           label: 'Forum',
           type: 'URL',
           properties: {
               url: 'http://www.oracle.com/technetwork/index.html'
           }
       },
       {
           label: 'Logout',
           type: 'URL',
           properties: {
               url: 'logout.html'
           }
       }],
       toggleMainMenu: function () {
           console.log("main menu clicked");
       }
   };
   ko.applyBindings(model);
});
```

You should by now be able to take a look at the result of your work. You need a web server to render it.

##### Using Python
```bash
 $ python -m SimpleHTTPServer
```

##### Using `node.js`
Tentative approach (WIP). Default port is `8080`, default `verbose` is `false`. 
```bash
 $ node server.js [--verbose:true|false --port:8080]
```

Then your page is ready for you to take a look at it, reach `http://localhost:8080/`.

### Full Example

Get the [full example](https://docs-documentsus.documents.osn.oraclecorp.com/documents/link/LD8BFA09DFFA5C82B1392019T0000DEFAULT00000000/fileview/DB26B30D0AEC22FCF34B83F7T0000DEFAULT00000000/_example.zip)

Unzip it an run it in any Server.

We usually do it with `python -m SimpleHTTPServer`, it will start a server on localhost:8000.

### Branding Bar Definition

#### Parameters

All the parameters are optional. Below there is a list of them:

* title: Specify an application title
* user-name: provide the logged in user name
* help: list of help items.
* menu: list of items to be shown under the user menu.

Each help or menu link has the following structure:

```json
{
   "label": "Forum",
   "type": "URL",
   "properties": {
	   "url": "http://oracle.com"
   }
}
```
* label: the text to be shown in the link.
* type: to provide more flexibility there are 4 types of links supported: NONE, ADF, JQUERY, URL.
* properties: metadata needed for each link type.

##### Link Types

###### NONE

A type NONE link will not execute any action when clicked. No properties are required for this type.

###### ADF

When a user clicks on an ADF link, an ADF server listener will be called. The following properties are required:
* source: and id to the container that is executing the server listener.
* listener: the name of the listener to be called.

**NOTE:** any extra property defined will be passed to the server listener. 

###### JQUERY

When a user clicks on a JQUERY link, a jquery event is triggered in the document element. The following properties are required: 
* event: the event name to be triggered.

**NOTE:** any extra property defined will be passed to the listeners.
 
Register to listen for the event using jquery:

```javascript
var myEventCallback = function(event, properties){
    //do any necessary steps to navigate
}
$(document).on('myEvent', myEventCallback);
  
//at a given point the listener might need to be removed.
$(document).off('myEvent', myEventCallback);
```

###### URL

When the user clicks on a URL link, he will navigate to the specified URL. The following properties are supported:
* url: the url where the user will navigate
* target [optional]: specify "_blank" if you want to open the link in a new window/tab.

#### Events

There is one event supported that is triggered when the main menu icon (hamburger menu) is clicked. The event is called mainMenu. 
Register to it by defining a callback in the element:

```html
        <paas-branding data-bind="event:{'mainMenu':toggleMainMenu}">
        </paas-branding>
```

```javascript
const toggleMainMenu = function () {
   console.log("main menu clicked");
};
```
#### More Information

Check the [CCA definition](paas-branding.json) to understand the supported parameters, methods and events.
