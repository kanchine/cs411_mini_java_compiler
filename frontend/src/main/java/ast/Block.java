package ast;

import visitor.Visitor;

import java.util.List;


public class Block extends Statement {

    public final NodeList<Statement> statements;
    public BlockType blockType;
    public Block(NodeList<Statement> statements) {
        super();
        this.statements = statements;
    }

    public Block(List<Statement> statements) {
        this(new NodeList<Statement>(statements));
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }


}
