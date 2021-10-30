import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Executor {
    private class VariableInfo
    {
        VarType type;
        Integer value;
        VariableInfo(VarType type, Integer value)
        {
            this.type = type;
            this.value = value;
        }
    }

    private class FuncInfo
    {
        List<String> params;
        List<ParseTreeNode.Stmt> body;
        FuncInfo(List<String> params, List<ParseTreeNode.Stmt> body)
        {
            this.params = params;
            this.body = body;
        }
    }

    Frame<VariableInfo> currentFrame;
    Frame<VariableInfo> globalFrame;
    Scope<VariableInfo> globalScope;
    // Scope<VariableInfo> staticVariables;
    // Scope<VariableInfo> currentScope;
    List<Integer> heap;
    HashMap<String, FuncInfo> functions;
    Scanner dataScanner;

    Executor(ParseTreeNode.Program p, String dataFilename) throws IOException
    {
        globalFrame = new Frame<>();
        currentFrame = globalFrame;
        globalScope = globalFrame.currentScope;
        heap = new ArrayList<>();
        functions = new HashMap<>();
        dataScanner = new Scanner(dataFilename);
        program(p);
    }

    private void executeFunc(String funcName, List<String> args)
    {
        // Push new frame
        var funcInfo = functions.get(funcName);
        List<VariableInfo> paramsValues = new ArrayList<>();
        for (int i = 0; i < args.size(); i++)
        {
            String arg = args.get(i);
            paramsValues.add(currentScope().Get(arg));
        }
        Frame<VariableInfo> newFrame = new Frame<>(globalScope, funcInfo.params, paramsValues, currentFrame);
        currentFrame = newFrame;

        // Execute function body
        stmtSeq(funcInfo.body);

        popFrame();
    }

    private void popFrame()
    {
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

    private Scope<VariableInfo> currentScope()
    {
        return currentFrame.currentScope;
    }

    private void newHeapVar(String name)
    {
        heap.add(0);
        var newValue = new VariableInfo(VarType.REF, heap.size() - 1);
        currentScope().Assign(name, newValue);
    }

    private void setHeapVar(String name, Integer newValue)
    {
        var varInfo = currentScope().Get(name);
        int index = varInfo.value;
        heap.set(index, newValue);
    }

    private Integer getNextInputValue()
    {
        if (dataScanner.currentToken() == Core.EOF) return null;
        Integer value = dataScanner.getCONST();
        dataScanner.nextToken();
        return value;
    }

    private void error(String message)
    {
        System.out.println(message);
        System.exit(1);
    }

    void program(ParseTreeNode.Program p)
    {
        declSeq(p.declSeq);
        stmtSeq(p.stmtSeq);
    }

    void declSeq(ParseTreeNode.DeclSeq decls)
    {
        // All decls here are static variables since declSeq can only occur in global scope
        for (var d : decls.declSeq)
        {
            decl(d);
        }

        for (var d : decls.funcDeclSeq)
        {
            funcDecl(d);
        }
    }

    void stmtSeq(List<ParseTreeNode.Stmt> stmts)
    {
        pushScope();
        for (var s : stmts)
        {
            stmt(s);
        }
        popScope();
    }

    void decl(ParseTreeNode.VarDecl decl)
    {
        Integer value = decl.type == VarType.INT ? 0 : null;

        for (var id : decl.ids)
        {
            currentScope().Declare(id, new VariableInfo(decl.type, value));
        }
    }

    void funcDecl(ParseTreeNode.FuncDecl decl)
    {
        FuncInfo value = new FuncInfo(decl.params, decl.body);
        functions.put(decl.id, value);
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
        else if (stmt instanceof ParseTreeNode.VarDecl)
        {
            decl((ParseTreeNode.VarDecl)stmt);
        }
        else if (stmt instanceof ParseTreeNode.FuncCall)
        {
            funcCall((ParseTreeNode.FuncCall)stmt);
        }
    }

    private void funcCall(ParseTreeNode.FuncCall stmt) {
        executeFunc(stmt.id, stmt.params);
    }

    private void output(ParseTreeNode.Output stmt) {
        System.out.println(expr(stmt.expr));
    }

    private Integer expr(ParseTreeNode.Expr expr) {
        Integer lhs = term(expr.lhs);
        if (expr.rhs != null)
        {
            Integer rhs = expr(expr.rhs);
            if (expr.operator == Core.ADD)
            {
                lhs += rhs;
            }
            else lhs -= rhs;
        }
        return lhs;
    }

    private Integer term(ParseTreeNode.Term term) {
        Integer lhs = factor(term.lhs);
        if (term.rhs != null)
        {
            Integer rhs = term(term.rhs);
            lhs *= rhs;
        }
        return lhs;
    }

    private Integer factor(ParseTreeNode.Factor lhs) {
        if (lhs.id != null)
        {
            var varInfo = currentScope().Get(lhs.id);
            if (varInfo.type == VarType.INT) return varInfo.value;
            else 
            {
                if (varInfo.value == null) return null;
                else return heap.get(varInfo.value);
            }
        }
        else if (lhs.expr != null)
        {
            return expr(lhs.expr);
        }
        else return lhs.CONST;
    }

    private void input(ParseTreeNode.Input stmt) {
        var inputValue = getNextInputValue();
        if (inputValue == null)
        {
            error("No more input values available in data file");
        }

        var varInfo = currentScope().Get(stmt.id);
        if (varInfo.type == VarType.INT)
        {
            var newValue = new VariableInfo(VarType.INT, inputValue);
            currentScope().Assign(stmt.id, newValue);
        }
        else
        {
            setHeapVar(stmt.id, inputValue);
        }
    }

    private void loop(ParseTreeNode.Loop stmt) {
        while (condition(stmt.condition))
        {
            stmtSeq(stmt.body);
        }
    }

    private boolean condition(ParseTreeNode.Cond condition) {
        if (condition.cond == null)
        {
            return comparison(condition.cmpr);
        }
        else if (condition.cmpr == null)
        {
            return !condition(condition.cond);
        }
        else return comparison(condition.cmpr) || condition(condition.cond);
    }

    private boolean comparison(ParseTreeNode.Cmpr cmpr) {
        var lhs = expr(cmpr.lhs);
        var rhs = expr(cmpr.rhs);
        if (cmpr.comparison == Core.EQUAL) return lhs == rhs;
        else if (cmpr.comparison == Core.LESS) return lhs < rhs;
        else return lhs <= rhs;
    }

    private void ifStmt(ParseTreeNode.If stmt) {
        if (condition(stmt.condition))
        {
            stmtSeq(stmt.ifBody);
        }
        else if (stmt.elseBody != null) 
        {
            stmtSeq(stmt.elseBody);
        }
    }

    private void assign(ParseTreeNode.Assign stmt) {
        var varInfo = currentScope().Get(stmt.id);

        if (varInfo.type == VarType.INT)
        {
            var newValue = new VariableInfo(VarType.INT, 0);
            newValue.value = expr(stmt.exprRHS);
            currentScope().Assign(stmt.id, newValue);
        }
        else
        {
            // id = new
            if (stmt.exprRHS == null && stmt.idRHS == null)
            {
                newHeapVar(stmt.id);
            }
            // id = ref id
            else if (stmt.exprRHS == null)
            {
                var rhsVarInfo = currentScope().Get(stmt.idRHS);
                var newValue = new VariableInfo(VarType.REF, rhsVarInfo.value);
                currentScope().Assign(stmt.id, newValue);
            }
            // id = expr
            else
            {
                if (varInfo.value == null)
                {
                    error("Can't assign a value to a null reference.");
                }
                else setHeapVar(stmt.id, expr(stmt.exprRHS));
            }
        }
    }
}
