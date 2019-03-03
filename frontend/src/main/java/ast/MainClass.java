package ast;

import visitor.Visitor;

// represent the class with a main function (main class has only one method: the main function)
public class MainClass extends AST {

    public final String className;
    public final String argName;
    public final Statement statement;

    public MainClass(String className, String argName, Statement statement) {
        super();
        this.className = className;
        this.argName = argName;
        this.statement = statement;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
