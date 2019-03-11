package ast;

import util.ImpTable;
import visitor.Visitor;

public class BlockType extends Type {
    public ImpTable<Type> locals = new ImpTable<Type>();
    @Override
    public boolean equals(Object other) {
        return this.getClass() == other.getClass();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
