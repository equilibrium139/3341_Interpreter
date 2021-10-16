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

    public void Add(String name, T type)
    {
        variables.put(name, type);
    }

    public void Set(String name, T newValue)
    {
        variables.put(name, newValue);
    }

    public boolean Contains(String name)
    {
        return variables.containsKey(name);
    }
}