To generate the openapi spec from the TypeSpec, you must first install tsp.

`npm install -g tsp`

Then from this directory, run `npm install` then `tsp compile main.tsp --emit @typespec/openapi3`.