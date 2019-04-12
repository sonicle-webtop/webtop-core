## Building

Make sure to have `npm` installed in your system and search-string project cloned from [https://github.com/mixmaxhq/search-string](https://github.com/mixmaxhq/search-string).

```shell

# Install broserify for use in command-line
$ npm install -g browserify

# Install yui compressor for use in command-line
$ npm -g install yuglify

# Install project's dev-dependencies
$ npm install --only=dev

# Build ES5 compatible code
$ npm run babelBuild

# Convert module into a browser suitable js bundle
$ browserify index.my.js > search-string.js

# Create the compressed bundle
yuglify search-string.js

```
