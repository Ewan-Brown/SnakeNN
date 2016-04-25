# SnakeNN
This program is a Snake game that contains a Snake Neural Network trainer using a genetic algorithm. You can either play Snake normally
or you can let the Neural Network trainer take place and keep improving its population.

I made this project because I wanted to learn both JavaFX and how to use Neural Networks, and this project gave me a better understanding
of both these things. Thanks [lestard](https://github.com/lestard) for helping me with the JavaFX part and for design inspiration! Here's lestard's [SnakeFX](https://github.com/lestard/SnakeFX)
which I took inspiration from :)

![](https://github.com/NanoVash/SnakeNN/blob/master/screenshot.png)
# Neural Network structure
The Neural Networks contains 3 layers, the input layer (4 input neurons), a hidden layer (3 neurons) and an output layer(1 neuron).
The inputs of the network are:
* The distance deferential from the food if it will do its next move (anywhere from -1 to 1)
* The object on the Snake's left (i.e. a wall or a tail is -1, nothing is 0 and food is 1)
* The object infront of the Snake
* The object on the Snake's right

Here's a visual example of how the network works: ![](https://github.com/NanoVash/SnakeNN/blob/master/nn.png)

The output of the network is its evaluation for how good that move is, so this process is repeated 3 times, 1 time for each possible 
direction that the Snake can move to (note that the snake can't move backwards) and then the best direction is chosen.
For example: if the Snake is facing up, we will input the information as if it is looking to the left, and will move to the left 
(food distance deferential), then we will input as if it is looking up and will move up, and then we will input as if it is looking to
the right and will move to the right. After that we will compare all evaluations and choose the direction with the best evaluation for
the next move.
#Genetic algorithm
The genetic algorithm I use here is quite simple, every generation update I split the population to best 50% and worst 50% and I replace
the worst 50% with children of the best 50%. This ensures that the best genetic information wont be lost but progress will be made.
The crossover method is a normal 2 cutpoint crossover and every child has a 10% chance to mutate.
#IO
The game stores its population and each generation's best networks in %APPDATA%/.SnakeNN
#Dependencies
The only dependency in this project is [lombok](https://projectlombok.org/), its a great dependency for projects in general and I
advise anyone to check it out!
#Download
You can check the "releases" section of this repository for a downloadable jar file.
