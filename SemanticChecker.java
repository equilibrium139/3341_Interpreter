import java.util.HashMap;
import java.util.List;

public class SemanticChecker {
    private static void error(String message) {
        System.out.println(message);
        System.exit(1);
    }

    Frame<VarType> currentFrame;
    Frame<VarType> globalFrame;
    Scope<VarType> globalScope;
    // key: func name, value: num args
    HashMap<String, Integer> funcDeclsData;

    private void pushFrame(List<String> params) {
        Frame<VarType> newFrame = new Frame<>(globalScope, params, VarType.REF, currentFrame);
        currentFrame = newFrame;
    }

    private void popFrame() {
        currentFrame = currentFrame.parent;
    }

    private void pushScope()
    {
        currentFrame.pushScope();
    }

    private void popScope()
    {
        currentFrame.popScope();
    }

    private Scope<VarType> currentScope()
    {
        return currentFrame.currentScope;
    }

    public SemanticChecker(ParseTreeNode.Program root) {
        globalFrame = new Frame<>();
        currentFrame = globalFrame;
        globalScope = globalFrame.currentScope;
        funcDeclsData = new HashMap<>();
        program(root);
    }

    void program(ParseTreeNode.Program program) {
        declSeq(program.declSeq);
        stmtSeq(program.stmtSeq);
    }

    void declSeq(ParseTreeNode.DeclSeq decls) {
        for (ParseTreeNode.VarDecl decl : decls.declSeq) {
            decl(decl);
        }

        for (ParseTreeNode.FuncDecl decl : decls.funcDeclSeq) {
            funcDecl(decl);
        }
    }

    void stmtSeq(List<ParseTreeNode.Stmt> stmts) {
        pushScope();
        for (ParseTreeNode.Stmt stmt : stmts) {
            stmt(stmt);
        }
        popScope();
    }

    void decl(ParseTreeNode.VarDecl decl) {
        for (String id : decl.ids) {
            if (currentScope().Contains(id)) {
                error("Variable with name " + id + " can't be declared twice.");
            } else {
                currentScope().Declare(id, decl.type);
            }
        }
    }

    void funcDecl(ParseTreeNode.FuncDecl decl) {
        if (funcDeclsData.containsKey(decl.id)) {
            error("Function with name " + decl.id + " can't be declared twice.");
        } else {
            // Check for duplicate parameters
            for (int i = 0; i < decl.params.size() - 1; i++) {
                for (int j = i + 1; j < decl.params.size(); j++) {
                    if (decl.params.get(i).equals(decl.params.get(j))) {
                        error("Function with name " + decl.id + " has duplicate parameters.");
                    }
                }
            }
            // TODO: maybe need to check if params is not null
            funcDeclsData.put(decl.id, decl.params.size());
            pushFrame(decl.params);
            stmtSeq(decl.body);
            popFrame();
        }
    }

    void stmt(ParseTreeNode.Stmt stmt) {
        if (stmt instanceof ParseTreeNode.Assign) {
            assign((ParseTreeNode.Assign) stmt);
        } else if (stmt instanceof ParseTreeNode.If) {
            ifStmt((ParseTreeNode.If) (stmt));
        } else if (stmt instanceof ParseTreeNode.Loop) {
            loop((ParseTreeNode.Loop) stmt);
        } else if (stmt instanceof ParseTreeNode.Input) {
            input((ParseTreeNode.Input) stmt);
        } else if (stmt instanceof ParseTreeNode.Output) {
            output((ParseTreeNode.Output) stmt);
        } else if (stmt instanceof ParseTreeNode.VarDecl) {
            decl((ParseTreeNode.VarDecl) stmt);
        } else if (stmt instanceof ParseTreeNode.FuncCall) {
            funcCall((ParseTreeNode.FuncCall) stmt);
        }
    }

    private void funcCall(ParseTreeNode.FuncCall stmt) {
        var funcNumParams = funcDeclsData.get(stmt.id);
        if (funcNumParams == null)
        {
            error("Attempting to call undeclared function '" + stmt.id + "'.");
        }
        else if (funcNumParams != stmt.params.size())
        {
            error("Function '" + stmt.id + "' expects " + funcNumParams + " parameters.");
        }
    }

    private void output(ParseTreeNode.Output stmt) {
        expression(stmt.expr);
    }

    private void input(ParseTreeNode.Input stmt) {
        if (currentScope().Get(stmt.id) == null) {
            error("Attempting to get input into undeclared variable '" + stmt.id + "'.");
        }
    }

    private void loop(ParseTreeNode.Loop stmt) {
        condition(stmt.condition);
        stmtSeq(stmt.body);
    }

    private void ifStmt(ParseTreeNode.If stmt) {
        condition(stmt.condition);
        stmtSeq(stmt.ifBody);
        if (stmt.elseBody != null) {
            stmtSeq(stmt.elseBody);
        }
    }

    private void assign(ParseTreeNode.Assign stmt) {
        VarType type = currentScope().Get(stmt.id);

        if (type == null) {
            error("Attempting to assign to undeclared variable '" + stmt.id + "'.");
        } else {
            // id = new;
            if (stmt.exprRHS == null && stmt.idRHS == null) {
                if (type != VarType.REF) {
                    error("Attempting to new an int variable '" + stmt.id + "'.");
                }
            }
            // id = ref id
            else if (stmt.exprRHS == null) {
                if (type != VarType.REF) {
                    error("Attempting to store a reference in int variable '" + stmt.id + "'.");
                }
                if (currentScope().Get(stmt.idRHS) != VarType.REF) {
                    error("Attemping to store a reference to an int or undeclared variable '" + stmt.idRHS + "'.");
                }
            }
            // id = expr
            else {
                expression(stmt.exprRHS);
            }
        }
    }

    private void condition(ParseTreeNode.Cond condition) {
        if (condition.cmpr != null)
            comparison(condition.cmpr);
        if (condition.cond != null)
            condition(condition.cond);
    }

    private void comparison(ParseTreeNode.Cmpr cmpr) {
        expression(cmpr.lhs);
        expression(cmpr.rhs);
    }

    private void expression(ParseTreeNode.Expr expr) {
        term(expr.lhs);
        if (expr.rhs != null)
            expression(expr.rhs);
    }

    private void term(ParseTreeNode.Term term) {
        factor(term.lhs);
        if (term.rhs != null)
            term(term.rhs);
    }

    private void factor(ParseTreeNode.Factor factor) {
        if (factor.expr != null) {
            expression(factor.expr);
        } else if (factor.id != null) {
            if (currentScope().Get(factor.id) == null) {
                error("Attempting to use undeclared variable '" + factor.id + "'.");
            }
        }
        // Nothing to do for constant factor
    }
}
