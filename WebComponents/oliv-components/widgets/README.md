# Preparing for Production
In order to minimize
- the amount of work at deployment phase
- the errors about the accuracy of deployment (deploy _all_ the required components _and their dependencies_, but nothing more)
- The size of the bundle to download at runtime
- possible browser incompatibilities
- the number of requests the client has to make to get to a component and run it
we will bundle each component and its dependencies into a single resource. This resource
is the one (and only one) to invoke at runtime from its client. Samples will be provided.
We will be using the `npm` module `webpack` to achieve this goal, along with a transpilation.


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
