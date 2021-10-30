import java.util.List;

public class Frame<T> {
    Scope<T> currentScope;
    Frame<T> parent;

    Frame()
    {
        currentScope = new Scope<>();
        parent = null;
    }

    Frame(Scope<T> globalScope, List<String> params, T paramsValue, Frame<T> parent)
    {
        Scope<T> paramsScope = new Scope<>();
        for (String id : params)
        {
            paramsScope.Declare(id, paramsValue);
        }
        paramsScope.parent = globalScope;
        currentScope = paramsScope;

        this.parent = parent;
    }

    Frame(Scope<T> globalScope, List<String> params, List<T> paramsValues, Frame<T> parent)
    {
        Scope<T> paramsScope = new Scope<>();
        for (int i = 0; i < params.size(); i++)
        {
            paramsScope.Declare(params.get(i), paramsValues.get(i));
        }
        paramsScope.parent = globalScope;
        currentScope = paramsScope;

        this.parent = parent;
    }

    public void pushScope() {
        Scope<T> newScope = new Scope<>();
        newScope.parent = currentScope;
        currentScope = newScope;
    }

    public void popScope() {
        currentScope = currentScope.parent;
    }
}
