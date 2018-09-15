## To build
For now (Sep-2018), build the components one by one.

We are using `webpack` to bundle _all_ the required resources into a _single_ file, minimized, uglified, etc., to minimize
the IOs and the size of the HTTP response.

> Note: No `lint` here. Maybe later...

Requires `node` and `npm`. `yarn` if you want.

### Example: World Map
```
 $ cd worldmap
```
The _**first time**_ (only):
```
 $ yarn
```
or
```
 $ npm install
```

Then, every time you want to start a new build
```
 $ yarn build
```
or
```
 $ npm run build
```
This will `webpack` all the required bundled artifacts in the `lib` directory (sibling of `widgets`).
The bundle contains all the necessary dependencies.
