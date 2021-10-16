import java.util.List;

public abstract class ParseTreeNode
{
    static String tabs(int n)
    {
        String s = "";
        for (int i = 0; i < n; i++) s += '\t';
        return s;
    }

    static <T extends ParseTreeNode> void PrintSeq(List<T> seq, int indentationLevel)
    {
        for (T t : seq) t.print(indentationLevel);
    }

    public abstract void print(int indentationLevel);

    public static class Program extends ParseTreeNode
    {
        public List<Decl> declSeq;
        public List<Stmt> stmtSeq;

        public Program() {}

        @Override
        public void print(int indentationLevel) {
            System.out.println("program");
            PrintSeq(declSeq, 1);
            System.out.println("begin");
            PrintSeq(stmtSeq, 1);
            System.out.println("end");
        }
    }

    public static abstract class Stmt extends ParseTreeNode {}

    public static class Assign extends Stmt
    {
        public String id;
        public Expr exprRHS;
        public String idRHS;

        @Override
        public void print(int indentationLevel) {
            String indent = tabs(indentationLevel);
            System.out.print(indent + id + "=");
            if (exprRHS != null) exprRHS.print(0);
            else if (idRHS != null) System.out.print("ref " + idRHS);
            else System.out.print("new");
            System.out.println(";");
        }
    }

    public static class If extends Stmt
    {
        Cond condition;
        List<Stmt> ifBody;
        List<Stmt> elseBody;

        @Override
        public void print(int indentationLevel) {
            String indent = tabs(indentationLevel);
            System.out.print(indent + "if ");
            condition.print(0);
            System.out.println(" then");
            PrintSeq(ifBody, indentationLevel + 1);
            if (elseBody != null)
            {
                System.out.println(indent + "else");
                PrintSeq(elseBody, indentationLevel + 1);    
            }
            System.out.println(indent + "endif");
        }
        
    }

    public static class Loop extends Stmt
    {
        Cond condition;
        List<Stmt> body;
        @Override
        public void print(int indentationLevel) {
            String indent = tabs(indentationLevel);
            System.out.print(indent + "while ");
            condition.print(0);
            System.out.println(" begin");
            PrintSeq(body, indentationLevel + 1);
            System.out.println(indent + "endwhile");
        }
        
    }

    public static class Input extends Stmt
    {
        String id;

        @Override
        public void print(int indentationLevel) {
            String indent = tabs(indentationLevel);
            System.out.println(indent + "input " + id + ";");
        }
        
    }

    public static class Output extends Stmt
    {
        Expr expr;

        @Override
        public void print(int indentationLevel) {
            String indent = tabs(indentationLevel);
            System.out.print(indent + "output ");
            expr.print(0);
            System.out.println(";");
        }
        
    }

    public static class Decl extends Stmt
    {
        public enum Type
        {
            INT, REF
        }

        public Type type;
        public List<String> ids;

        @Override
        public void print(int indentationLevel) {
            String indent = tabs(indentationLevel);
            System.out.print(indent);
            if (type == Type.INT) System.out.print("int ");
            else System.out.print("ref ");
            for (int i = 0; i < ids.size() - 1; i++) System.out.print(ids.get(i) + ",");
            System.out.println(ids.get(ids.size() - 1) + ";");
        }
    }

    public static class Cond extends ParseTreeNode
    {
        public Cmpr cmpr;
        public Cond cond;

        @Override
        public void print(int indentationLevel) {
            if (cmpr == null)
            {
                System.out.print("!(");
                cond.print(0);
                System.out.print(")");
            }
            else if (cond == null)
            {
                cmpr.print(0);
            }
            else 
            {
                cmpr.print(0);
                System.out.print(" or ");
                cond.print(0);
            }
        }
        
    }

    public static class Cmpr extends ParseTreeNode
    {
        public Expr lhs;
        public Core comparison;
        public Expr rhs;

        @Override
        public void print(int indentationLevel) {
            lhs.print(0);
            if (comparison == Core.EQUAL) System.out.print("==");
            else if (comparison == Core.LESS) System.out.print("<");
            else System.out.print("<=");
            rhs.print(0);
        }
    }

    public static class Expr extends ParseTreeNode
    {
        public Term lhs;
        public Core operator;
        public Expr rhs;
        
        @Override
        public void print(int indentationLevel) {
            lhs.print(0);
            if (operator != null)
            {
                if (operator == Core.ADD) System.out.print("+");
                else System.out.print("-");
                rhs.print(0);
            }
        }
    }

    public static class Term extends ParseTreeNode
    {
        public Factor lhs;
        public Term rhs;
        
        @Override
        public void print(int indentationLevel) {
            lhs.print(0);
            if (rhs != null)
            {
                System.out.print("*");
                rhs.print(0);
            }
        }
    }

    public static class Factor extends ParseTreeNode
    {
        public String id;
        public int CONST;
        public Expr expr;
        
        @Override
        public void print(int indentationLevel) {
            if (id != null)
            {
                System.out.print(id);
            }
            else if (expr != null)
            {
	    	System.out.print('(');
                expr.print(0);
		System.out.print(')');
            }
            else 
            {
                System.out.print(CONST);
            }
        }
    }
}
