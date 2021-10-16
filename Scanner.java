import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

class Scanner {
	
	private String fileContents;
	private int line = 1;
	private int startIndex = 0;
	private int currentIndex = 0;
	private Core current;
	private String ID;
	private int CONST;

	private static final HashMap<String, Core> keywords;
	private static final HashMap<String, Core> specials;

	static {
		keywords = new HashMap<>();
		keywords.put("program", Core.PROGRAM);
		keywords.put("begin", Core.BEGIN);
		keywords.put("end", Core.END);
		keywords.put("new", Core.NEW);
		keywords.put("int", Core.INT);
		keywords.put("define", Core.DEFINE);
		keywords.put("endfunc", Core.ENDFUNC);
		keywords.put("class", Core.CLASS);
		keywords.put("extends", Core.EXTENDS);
		keywords.put("endclass", Core.ENDCLASS);
		keywords.put("if", Core.IF);
		keywords.put("then", Core.THEN);
		keywords.put("else", Core.ELSE);
		keywords.put("while", Core.WHILE);
		keywords.put("endwhile", Core.ENDWHILE);
		keywords.put("endif", Core.ENDIF);
		keywords.put("or", Core.OR);
		keywords.put("input", Core.INPUT);
		keywords.put("output", Core.OUTPUT);
		keywords.put("ref", Core.REF);

		specials = new HashMap<>();
		specials.put(";", Core.SEMICOLON);
		specials.put("(", Core.LPAREN);
		specials.put(")", Core.RPAREN);
		specials.put(",", Core.COMMA);
		specials.put("=", Core.ASSIGN);
		specials.put("!", Core.NEGATION);
		specials.put("==", Core.EQUAL);
		specials.put("<", Core.LESS);
		specials.put("<=", Core.LESSEQUAL);
		specials.put("+", Core.ADD);
		specials.put("-", Core.SUB);
		specials.put("*", Core.MULT);
	}

	// Constructor should open the file and find the first token
	Scanner(String filename) throws IOException {
		Path path = Paths.get(filename);
		fileContents = Files.readString(path, StandardCharsets.US_ASCII);
		fileContents += "\n"; // ensure there is whitespace at the end of the file. this makes handling EOF easier (only needed in skipWhitespace function)
		nextToken();
	}

	private char currentChar() {
		return fileContents.charAt(currentIndex);
	}

	private char nextChar() {
		return fileContents.charAt(++currentIndex);
	}

	private void skipWhitespace() {
		char curr = currentChar();
		while (Character.isWhitespace(curr)) {
			if (curr == '\n') line++;
			if (atEnd()) {
				current = Core.EOF;
				break;
			}
			curr = nextChar();
		}
	}

	private boolean atEnd() {
		return currentIndex >= fileContents.length() - 1;
	}

	private void scanConstant() {
		char curr = nextChar();
		while (Character.isDigit(curr)) {
			curr = nextChar();
		}
		String constantSubstr = fileContents.substring(startIndex, currentIndex);
		if (constantSubstr.length() <= 4) { 
			CONST = Integer.parseInt(constantSubstr);
			if (CONST >= 0 && CONST <= 1023) {
				current = Core.CONST;
			}
			else {
				current = Core.ERROR;
				System.out.println("ERROR (" + line + "): Constants must be in the range 0-1023");	
			}
		}
		else {
			current = Core.ERROR;
			System.out.println("ERROR (" + line + "): Constants can have a maximum of 4 digits");
		}
	}

	private void scanIDOrKeyword() {
		char curr = nextChar();
		while (Character.isAlphabetic(curr) || Character.isDigit(curr)) {
			curr = nextChar();
		}

		String IDOrKeyword = fileContents.substring(startIndex, currentIndex);
		Core keywordToken = keywords.get(IDOrKeyword);
		if (keywordToken != null) {
			current = keywordToken;
		}
		else {
			current = Core.ID;
			ID = IDOrKeyword;
		}
	}

	private void scanSpecial() {
		char curr = currentChar();
		// These are the only cases where special takes up two chars
		if (curr == '<' || curr == '=') {
			curr = nextChar();
			if (curr == '=') {
				currentIndex++;
			}
		}
		else currentIndex++;

		String special = fileContents.substring(startIndex, currentIndex);
		Core specialToken = specials.get(special);
		if (specialToken != null) {
			current = specialToken;
		}
		else {
			current = Core.ERROR;
			System.out.println("ERROR (" + line + "): invalid token '" + special + "'");
		}
	}

	// nextToken should advance the scanner to the next token
	public void nextToken() {
		skipWhitespace();
		if (current == Core.EOF) {
			return;
		}

		startIndex = currentIndex; // store the start of the current token
		char curr = currentChar();

		if (Character.isDigit(curr)) {
			scanConstant();
		}
		else if (Character.isAlphabetic(curr)) {
			scanIDOrKeyword();
		}
		else {
			scanSpecial();
		}
	}

	// currentToken should return the current token
	public Core currentToken() {
		return current;
	}

	// If the current token is ID, return the string value of the identifier
	// Otherwise, return value does not matter
	public String getID() {
		return ID;
	}

	// If the current token is CONST, return the numerical value of the constant
	// Otherwise, return value does not matter
	public int getCONST() {
		return CONST;
	}

}