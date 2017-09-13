# SnakeNN
This program is a Snake game that contains a Snake Neural Network trainer using a genetic algorithm. You can either play Snake normally
or you can let the Neural Network trainer take place and keep improving its population.

I made this project because I wanted to learn both JavaFX and how to use Neural Networks, and this project gave me a better understanding
of both these things. Thanks [lestard](https://github.com/lestard) for helping me with the JavaFX part and for design inspiration! Here's lestard's [SnakeFX](https://github.com/lestard/SnakeFX)
which I took inspiration from :)

![](https://github.com/NanoVash/SnakeNN/blob/master/screenshot.png)

# How to train the networks
To train the neural network population, you need to switch the player type to "NN Training" and start the game, it is also
recommended that you change the speed to "Fast" or "NN Training" because with "Normal" it will take a lot of time. In the beginning
you'll notice the networks are pretty dumb but after a few generations they'll begin to collect more and more food, up to a maximum
of about 60 or 70 from my experience because we only supply the network with limited information. It doesn't really matter if you
enable or disable walls during NN Training because there is a monitor running in case a network gets stuck in an infinite loop, but I recommend to turn walls on. The "Loaded NN" player by default is just a randomly generated neural network but you can load different ones using the Load Network input on the left, to load a network copy it from either population.txt or best.txt (only whats after the colon without the space for best.txt or a whole line for population.txt) and paste it in the input field and press load, now that network is the Loaded NN and when "Loaded NN" is selected that network will make the moves.
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
