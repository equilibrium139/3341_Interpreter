import java.util.HashMap;

public class Scope<T>
{
    HashMap<String, T> variables;
    Scope<T> parent;

    public Scope()
    {
        variables = new HashMap<>();
        parent = null;
    }

    public T Get(String name)
    {
        T t = variables.get(name);
        if (t == null && parent != null)
        {
            return parent.Get(name);
        }
        return t;
    }

    public void Declare(String name, T newValue)
    {
        variables.put(name, newValue);
    }

    public void Assign(String name, T newValue)
    {
        if (variables.containsKey(name))
        {
            variables.put(name, newValue);
            return;
        }

        assert parent != null : "Attempting to assign to non-existent variable " + name;
        parent.Assign(name, newValue);
    }

    public boolean Contains(String name)
    {
        return variables.containsKey(name);
    }
}