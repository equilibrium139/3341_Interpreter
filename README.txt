Zaid Al-ruwaishan

Main.java- similar to Project 2 but now also initializes Executor

Executor.java- Contains all the Project 3 functionality. Structured in the same way as the Parser and SemanticChecker classes. Contains data structures
for static memory, heap memory, and stack memory.

Scope.java- Contains all the the scope logic. Templated on the data needed for each variable. The semantic checker only needs to store the type for each 
variable while the executor needs the type as well as the value. They both use the Scope class but store different data.

Parser.java- unchanged
Core.java- unchanged
ParseTreeNode.java- unchanged
Scanner.java- unchanged
SemantiChecker.java- unchanged
VarType.java- unchanged

Similar to the previous project with the parser and semantic checker, the functionality for executing the program is all in a single file: Executor.java.

The Executor class relies on the Scope class to handle static and stack variables. The scope class is simple: it contains a reference to it's parent scope
and a HashMap to store it's variables. 
The heap is managed with an integer array. The value of ref variables are indices into this heap.

I tested the interpretor with the provided test script and some debugging to work through other issues.
