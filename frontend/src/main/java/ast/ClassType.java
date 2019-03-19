package ast;

import util.ImpTable;
import visitor.Visitor;

public class ClassType extends Type {
    public ImpTable<Type> locals = new ImpTable<Type>();
    public ImpTable<Type> methods = new ImpTable<Type>();
    public NodeList<VarDecl> fields;
    public String name;
    public String superName;

    @Override
    public boolean equals(Object other) {
        return this.getClass() == other.getClass();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return "ClassType {\n" +
                "locals=" + locals + "\n" +
                "methods=" + methods + "\n" +
                '}';
    }
}
