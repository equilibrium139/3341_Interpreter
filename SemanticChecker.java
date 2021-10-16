import java.util.HashMap;
import java.util.List;

public class SemanticChecker {
    private enum VarType
    {
        INT, REF
    }

    private class Scope
    {
        HashMap<String, VarType> variables;
        Scope parent;

        public Scope()
        {
            variables = new HashMap<>();
            parent = null;
        }

        public VarType GetType(String name)
        {
            VarType type = variables.get(name);
            if (type == null && parent != null)
            {
                return parent.GetType(name);
            }
            return type;
        }

        public void Add(String name, VarType type)
        {
            variables.put(name, type);
        }

        public boolean Contains(String name)
        {
            return variables.containsKey(name);
        }
    }

    private static void error(String message)
    {
        System.out.println(message);
        System.exit(1);
    }

    Scope currentScope;

    private void pushScope()
    {
        Scope newScope = new Scope();
        newScope.parent = currentScope;
        currentScope = newScope;
    }

    private void popScope()
    {
        currentScope = currentScope.parent;
    }

    public SemanticChecker(ParseTreeNode.Program root)
    {
        currentScope = new Scope();
        program(root);
    }

    void program(ParseTreeNode.Program program)
    {
        declSeq(program.declSeq);
        stmtSeq(program.stmtSeq);
    }

    void declSeq(List<ParseTreeNode.Decl> decls)
    {
        for (ParseTreeNode.Decl decl : decls)
        {
            decl(decl);
        }
    }

    void stmtSeq(List<ParseTreeNode.Stmt> stmts)
    {
        for (ParseTreeNode.Stmt stmt : stmts)
        {
            stmt(stmt);
        }
    }

    void decl(ParseTreeNode.Decl decl)
    {
        VarType type = decl.type == ParseTreeNode.Decl.Type.INT ? VarType.INT : VarType.REF;
        for (String id : decl.ids)
        {
            if (currentScope.Contains(id))
            {
                error("Variable with name " + id + " can't be declared twice.");
            }
            else
            {
                currentScope.Add(id, type);
            }
        }
    }

    void stmt(ParseTreeNode.Stmt stmt)
    {
        if (stmt instanceof ParseTreeNode.Assign)
        {
            assign((ParseTreeNode.Assign)stmt);
        }
        else if (stmt instanceof ParseTreeNode.If)
        {
            ifStmt((ParseTreeNode.If)(stmt));
        }
        else if (stmt instanceof ParseTreeNode.Loop)
        {
            loop((ParseTreeNode.Loop)stmt);
        }
        else if (stmt instanceof ParseTreeNode.Input)
        {
            input((ParseTreeNode.Input)stmt);
        }
        else if (stmt instanceof ParseTreeNode.Output)
        {
            output((ParseTreeNode.Output)stmt);
        }
        else if (stmt instanceof ParseTreeNode.Decl)
        {
            decl((ParseTreeNode.Decl)stmt);
        }
    }

    private void output(ParseTreeNode.Output stmt) {
        expression(stmt.expr);
    }

    private void input(ParseTreeNode.Input stmt) {
        if (currentScope.GetType(stmt.id) == null)
        {
            error("Attempting to get input into undeclared variable '" + stmt.id + "'.");
        }
    }

    private void loop(ParseTreeNode.Loop stmt) {
        condition(stmt.condition);
        pushScope();
        stmtSeq(stmt.body);
        popScope();
    }

    private void ifStmt(ParseTreeNode.If stmt) {
        condition(stmt.condition);
        pushScope();
        stmtSeq(stmt.ifBody);
        popScope();
        if (stmt.elseBody != null) 
        {
            pushScope();
            stmtSeq(stmt.elseBody);
            popScope();
        }
    }

    private void assign(ParseTreeNode.Assign stmt) {
        VarType type = currentScope.GetType(stmt.id);

        if (type == null)
        {
            error("Attempting to assign to undeclared variable '" + stmt.id + "'.");
        }
        else
        {
            // id = new;
            if (stmt.exprRHS == null && stmt.idRHS == null)
            {
                if (type != VarType.REF)
                {
                    error("Attempting to new an int variable '" + stmt.id + "'.");
                }
            }
            // id = ref id
            else if (stmt.exprRHS == null)
            {
                if (type != VarType.REF)
                {
                    error("Attempting to store a reference in int variable '" + stmt.id + "'.");
                }
                if (currentScope.GetType(stmt.idRHS) != VarType.REF)
                {
                    error("Attemping to store a reference to an int or undeclared variable '" + stmt.idRHS + "'.");
                }
            }
            // id = expr
            else
            {
                expression(stmt.exprRHS);
            }
        }
    }
    
    private void condition(ParseTreeNode.Cond condition) {
        if (condition.cmpr != null) comparison(condition.cmpr);
        if (condition.cond != null) condition(condition.cond);
    }

    private void comparison(ParseTreeNode.Cmpr cmpr) {
        expression(cmpr.lhs);
        expression(cmpr.rhs);
    }

    private void expression(ParseTreeNode.Expr expr) {
        term(expr.lhs);
        if (expr.rhs != null) expression(expr.rhs);
    }

    private void term(ParseTreeNode.Term term) {
        factor(term.lhs);
        if (term.rhs != null) term(term.rhs);
    }

    private void factor(ParseTreeNode.Factor factor) {
        if (factor.expr != null)
        {
            expression(factor.expr);
        }
        else if (factor.id != null)
        {
            if (currentScope.GetType(factor.id) == null)
            {
                error("Attempting to use undeclared variable '" + factor.id + "'.");
            }
        }
        // Nothing to do for constant factor
    }
}
