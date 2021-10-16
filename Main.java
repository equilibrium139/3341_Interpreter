import java.io.IOException;

class Main {
	public static void main(String[] args) throws IOException {
		// Initialize the scanner with the input file
		Scanner S = new Scanner(args[0]);
		Parser parser = new Parser(S);
		SemanticChecker checker = new SemanticChecker(parser.root);
		// parser.root.print(0);
		Executor executor = new Executor(parser.root, args[1]);

		// Print the token stream
		// while (S.currentToken() != Core.EOF && S.currentToken() != Core.ERROR) {
		// 	// Pring the current token, with any extra data needed
		// 	System.out.print(S.currentToken());
		// 	if (S.currentToken() == Core.ID) {
		// 		String value = S.getID();
		// 		System.out.print("[" + value + "]");
		// 	} else if (S.currentToken() == Core.CONST) {
		// 		int value = S.getCONST();
		// 		System.out.print("[" + value + "]");
		// 	}
		// 	System.out.print("\n");

		// 	// Advance to the next token
		// 	S.nextToken();
		// }
	}
}