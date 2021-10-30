import java.util.List;

public abstract class ParseTreeNode
{
    public static class Program extends ParseTreeNode
    {
        public DeclSeq declSeq;
        public List<Stmt> stmtSeq;
    }

    public static class DeclSeq
    {
        public List<VarDecl> declSeq;
        public List<FuncDecl> funcDeclSeq;
    }

    public static abstract class Stmt extends ParseTreeNode {}

    public static class Assign extends Stmt
    {
        public String id;
        public Expr exprRHS;
        public String idRHS;
    }

    public static class If extends Stmt
    {
        public Cond condition;
        public List<Stmt> ifBody;
        public List<Stmt> elseBody;
    }

    public static class Loop extends Stmt
    {
        public Cond condition;
        public List<Stmt> body;
    }

    public static class Input extends Stmt
    {
        public String id;
    }

    public static class Output extends Stmt
    {
        public Expr expr;
    }

    public static class VarDecl extends Stmt
    {
        public VarType type;
        public List<String> ids;
    }

    public static class FuncCall extends Stmt
    {
        public String id;   
        public List<String> params;
    }

    public static class FuncDecl extends ParseTreeNode
    {
        public String id;
        public List<String> params;
        public List<Stmt> body;
    }

    public static class Cond extends ParseTreeNode
    {
        public Cmpr cmpr;
        public Cond cond;
    }

    public static class Cmpr extends ParseTreeNode
    {
        public Expr lhs;
        public Core comparison;
        public Expr rhs;
    }

    public static class Expr extends ParseTreeNode
    {
        public Term lhs;
        public Core operator;
        public Expr rhs;
    }

    public static class Term extends ParseTreeNode
    {
        public Factor lhs;
        public Term rhs;
    }

    public static class Factor extends ParseTreeNode
    {
        public String id;
        public int CONST;
        public Expr expr;
    }
}
