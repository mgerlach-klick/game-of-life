var request = require("request")
var randomColor = require("randomColor")
var R = require("ramda")

var WIDTH = 25
var HEIGHT = 25

var isValid = cell => {
    var [x,y] = cell
    return (x > -1) && (x < WIDTH) && (y > -1) && (y < HEIGHT)
}

var neighbourPositions = cell => {
    var inc = x => x+1
    var dec = x => x-1

    var [x,y] = cell
    var neighbours = [[dec(x), dec(y)] //above left
                      ,[x, dec(y)] //above
                      ,[inc(x), dec(y)] //above right
                      ,[dec(x), y] //left
                      ,[inc(x), y] //right
                      ,[dec(x), inc(y)] //below left
                      ,[x, inc(y)] //below
                      ,[inc(x), inc(y)] //below right
                     ]
    return R.filter(isValid, neighbours)
}

var get = (map, key) => map[JSON.stringify(key)]
var set = (map, key, value) => map[JSON.stringify(key)] = value

var getNeighbours = (world, neighbours) => {
    // var getWorld = R.partial(get, [world])
    return R.map((cell) => get(world, cell), neighbours)
}

var isAlive = (world, cell) => true == get(world,cell)
var isDead = (world, cell) => false == get(world,cell)

var makeRandomWorld = () => {
    var randomElement = myArray => myArray[Math.floor(Math.random() * myArray.length)]
    var world = {}

    for (let y of R.range(0, HEIGHT))
        for (let x of R.range(0, WIDTH))
            set(world, [x,y], randomElement([true, false]))

    return world
}

var countAliveNeighbours = (world, cell) => {
    var isTrue = x => true == x
    var a = neighbourPositions(cell)
    var b = getNeighbours(world, a)
    var c = R.filter(isTrue, b)
    return c.length;
}

// Any live cell with two or three live neighbours lives on to the next generation.
// Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
// Any live cell with more than three live neighbours dies, as if by overpopulation.
// Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

var decideFate = (world, cell) => {
    var A = countAliveNeighbours(world, cell)
    var same = get(world, cell)
    var dead = false
    var alive = true

    if(A < 2)
        return dead
    else if (A >= 2 && A <= 3 && isAlive(world, cell))
        return alive
    else if (A > 3 && isAlive(world, cell))
        return dead
    else if (A == 3 && isDead(world, cell))
        return alive
    else
        return same
}

var tick = world => {
    var newWorld = {}
    for (let jcell in world) {
        var cell = JSON.parse(jcell)
        var state = get(world, cell)
        var fate = decideFate(world, cell)
        console.log(`${cell}: ${state} => ${fate} because ${countAliveNeighbours(world,cell)} alive neighbours from neighbour positions: ${JSON.stringify(neighbourPositions(cell))}`)
        set(newWorld,cell,fate)
    }
    return newWorld
    }

var createRequest = world => {
    var makeCell = (jcell) => {
        var cell = JSON.parse(jcell)
        var [x,y] = cell
        var alive = get(world, cell)
        return {x: x,
                y: y,
                color: alive ? randomColor({luminosity: "bright"}) : "#000"}
    }
    var result = [] // I couldn't think of a nice way of mapping over object keys
    for (let jcell in world)
        result.push(makeCell(jcell))
    return result
}

// only side effecting function!
var makeRequest = req => {
    var reqObj = {url: "http://localhost:5000/"
                  ,method: "POST"
                  ,json: {cells: req}
                 }
    request(reqObj)
}


// simplifed from clojurescript because i don't care about stopping
var tickTock = (world) => {
    makeRequest(createRequest(world))
    setTimeout(() => tickTock(tick(world)), 1000)
}

tickTock(makeRandomWorld())
