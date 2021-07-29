# Kevin Bacon Game

This console based game is all about the "Bacon number", i.e. the number of degrees of separation an actor/actrees has from Kevin Bacon. If the graph is properly formatted, this can also be used to perform some basic graph analysis.

*The graph operations are supported by the graph lib written by Chris Bailey-Kellogg during previous offerings.

## Format of inputs
See `inputs`.

## Supported Operations
`c <#>` - list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation

`d <low> <high>` - list actors sorted by degree, with degree between low and high

`h` - show the instructions again

`i` - list actors with infinite separation from the current center

`p <name>` - find path from <name> to current center of the universe

`s <low> <high>` - list actors sorted by non-infinite separation from the current center, with separation between low and high

`u <name>` - make <name> the center of the universe

`q` - quit game

## Testing
Screenshots can be found in `./test`.  