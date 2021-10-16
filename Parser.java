import java.util.ArrayList;
import java.util.List;

public class Parser {
    private Scanner scanner;
    public ParseTreeNode.Program root;

    Parser(Scanner scanner)
    {
        this.scanner = scanner;
        root = program();
    }

    Core expect(String errorMessage, Core ...expectedTokens)
    {
        Core current = scanner.currentToken();
        for (Core token : expectedTokens)
        {
            if (current == token) 
            {
                scanner.nextToken();
                return current;
            }
        }
        System.out.println(errorMessage);
        System.exit(1);
        return null; // Dummy return so java doesn't complain about missing return
    }

    boolean consume(Core token)
    {
        Core current = scanner.currentToken();
        if (current != token)
        {
            return false;
        }
        else 
        {
            scanner.nextToken();
            return true;
        }
    }

    boolean matches(Core ...tokensToMatch)
    {
        Core current = scanner.currentToken();
        for (Core token : tokensToMatch)
        {
            if (current == token)
            {
                return true;
            }
        }
        return false;
    }

    String expectID(String errorMessage)
    {
        if (matches(Core.ID))
        {
            return id();
        }
        else 
        {
            System.out.println(errorMessage);
            System.exit(1);
            return null; // dummy return
        }
    }

    ParseTreeNode.Program program() 
    {
        var program = new ParseTreeNode.Program();
        expect("Program must start with 'program' keyword.", Core.PROGRAM);
        if (matches(Core.INT, Core.REF))
        {
            program.declSeq = declSeq();
        }
        expect( "Program body must start with 'begin' keyword.", Core.BEGIN);
        program.stmtSeq = stmtSeq();
        expect("Program body must end with 'end' keyword.", Core.END);
        expect("Invalid tokens after program end.", Core.EOF);
        return program;
    }

    List<ParseTreeNode.Decl> declSeq() 
    {
        var decls = new ArrayList<ParseTreeNode.Decl>();
        decls.add(decl());
        while (matches(Core.INT, Core.REF))
        {
            decls.add(decl());
        }
        return decls;
    }

    List<ParseTreeNode.Stmt> stmtSeq() 
    {
        var stmts = new ArrayList<ParseTreeNode.Stmt>();
        stmts.add(stmt());
        while (matches(Core.ID, Core.IF, Core.WHILE, Core.INPUT, Core.OUTPUT, Core.INT, Core.REF))
        {
            stmts.add(stmt());
        }
        return stmts;
    }

    ParseTreeNode.Decl decl() 
    {
        var decl = new ParseTreeNode.Decl();
        if (consume(Core.INT))
        {
            decl.type = VarType.INT;
        }
        else
        {
            consume(Core.REF);
            decl.type = VarType.REF;
        }
        decl.ids = idList();
        expect("Declaration must end with ';'.", Core.SEMICOLON);
        return decl;
    }

    List<String> idList()
    {
        var ids = new ArrayList<String>();
        ids.add(id());
        while (consume(Core.COMMA))
        {
            ids.add(id());
        }
        return ids;
    }

    String id()
    {
        String id = scanner.getID();
        scanner.nextToken();
        return id;
    }

    int CONST()
    {
        int CONST = scanner.getCONST();
        scanner.nextToken();
        return CONST;
    }

    Core currentToken()
    {
        Core token = scanner.currentToken();
        scanner.nextToken();
        return token;
    }

    ParseTreeNode.Stmt stmt()
    {
        if (consume(Core.IF))
        {
            return ifStmt();
        }
        else if (matches(Core.ID))
        {
            return assign();
        }
        else if (consume(Core.WHILE))
        {
            return loop();
        }
        else if (consume(Core.INPUT))
        {
            return input();
        }
        else if (matches(Core.INT, Core.REF))
        {
            return decl();
        }
        else 
        {
            expect("Invalid statement.", Core.OUTPUT);
            return output();
        }
    }

    private ParseTreeNode.Output output() {
        ParseTreeNode.Output out = new ParseTreeNode.Output();
        out.expr = expression();
        expect("Missing ';' at end of output statement.", Core.SEMICOLON);
        return out;
    }

    private ParseTreeNode.Input input() {
        ParseTreeNode.Input in = new ParseTreeNode.Input();
        in.id = expectID("Missing identifier in input statement.");
        expect("Missing ';' at end of input statement.", Core.SEMICOLON);
        return in;
    }

    private ParseTreeNode.Loop loop() {
        ParseTreeNode.Loop loop = new ParseTreeNode.Loop();
        loop.condition = condition();
        expect("Loop body must start with 'begin' keyword.", Core.BEGIN);
        loop.body = stmtSeq();
        expect("Loop body must end with 'endwhile' keyword.", Core.ENDWHILE);
        return loop;
    }

    private ParseTreeNode.Assign assign() {
        ParseTreeNode.Assign stmt = new ParseTreeNode.Assign();
        stmt.id = id();
        expect("Identifier must be followed by '=' in assignment.", Core.ASSIGN);
        if (consume(Core.NEW))
        {
            // nothing to do, stmt's other fields are already null which implies id = new
        }
        else if (consume(Core.REF))
        {
            stmt.idRHS = expectID("ref must be followed by identifier in assignment.");
        }
        else
        {
            stmt.exprRHS = expression();
        }
        expect("Missing ';' at end of assignment.", Core.SEMICOLON);
        return stmt;
    }

    ParseTreeNode.If ifStmt()
    {
        ParseTreeNode.If stmt = new ParseTreeNode.If();
        stmt.condition = condition();
        expect("If statement body must begin with 'then' keyword.", Core.THEN);
        stmt.ifBody = stmtSeq();
        if (consume(Core.ELSE))
        {
            stmt.elseBody = stmtSeq();
        }
        expect("If statement body must end with 'endif' keyword.", Core.ENDIF);
        return stmt;
    }

    private ParseTreeNode.Cond condition()  {
        ParseTreeNode.Cond cond = new ParseTreeNode.Cond();

        if (consume(Core.NEGATION))
        {
            expect("Negation must be followed by '('.", Core.LPAREN);
            cond.cond = condition();
            expect("Missing ')' at end of negation.", Core.RPAREN);
        }
        else
        {
            cond.cmpr = comparison();
            if (consume(Core.OR))
            {
                cond.cond = condition();
            }
        }

        return cond;
    }

    private ParseTreeNode.Cmpr comparison() {
        ParseTreeNode.Cmpr cmpr = new ParseTreeNode.Cmpr();
        cmpr.lhs = expression();
        cmpr.comparison = expect("Invalid comparison operator.", Core.EQUAL, Core.LESSEQUAL, Core.LESS);
        cmpr.rhs = expression();
        return cmpr;
    }

    private ParseTreeNode.Expr expression() {
        ParseTreeNode.Expr expr = new ParseTreeNode.Expr();
        expr.lhs = term();
        if (matches(Core.ADD, Core.SUB))
        {
            expr.operator = currentToken();
            expr.rhs = expression();
        }
        return expr;
    }

    private ParseTreeNode.Term term() {
        ParseTreeNode.Term term = new ParseTreeNode.Term();
        term.lhs = factor();
        if (consume(Core.MULT))
        {
            term.rhs =  term();
        }
        return term;
    }

    private ParseTreeNode.Factor factor() {
        ParseTreeNode.Factor factor = new ParseTreeNode.Factor();
        if (matches(Core.ID))
        {
            factor.id = id();
        }
        else if (matches(Core.CONST))
        {
            factor.CONST = CONST();
        }
        else 
        {
            expect("Factor must be an identifier, a constant, or a parenthesis enclosed expression", 
                    Core.LPAREN);
            factor.expr = expression();
            expect("Missing ')' at end of factor expression.", Core.RPAREN);
        }
        return factor;
    }

    
}
