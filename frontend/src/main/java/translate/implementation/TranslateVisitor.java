package translate.implementation;

import ast.AST;
import ast.And;
import ast.ArrayAssign;
import ast.ArrayLength;
import ast.ArrayLookup;
import ast.Assign;
import ast.Block;
import ast.BlockType;
import ast.BooleanLiteral;
import ast.BooleanType;
import ast.Call;
import ast.ClassDecl;
import ast.ClassType;
import ast.Conditional;
import ast.Expression;
import ast.FunctionDecl;
import ast.FunctionType;
import ast.IdentifierExp;
import ast.If;
import ast.IntArrayType;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.MainClass;
import ast.MethodDecl;
import ast.MethodType;
import ast.Minus;
import ast.NewArray;
import ast.NewObject;
import ast.NodeList;
import ast.Not;
import ast.ObjectType;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.Statement;
import ast.This;
import ast.Times;
import ast.Type;
import ast.UnknownType;
import ast.VarDecl;
import ast.While;
import ir.frame.Access;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.BINOP.Op;
import ir.tree.CJUMP;
import ir.tree.CJUMP.RelOp;
import ir.tree.IR;
import ir.tree.IRExp;
import ir.tree.IRStm;
import ir.tree.TEMP;
import translate.Fragments;
import translate.ProcFragment;
import util.FunTable;
import util.List;
import util.Lookup;
import visitor.Visitor;

import static ir.tree.IR.CALL;
import static ir.tree.IR.CMOVE;
import static ir.tree.IR.ESEQ;
import static ir.tree.IR.FALSE;
import static ir.tree.IR.JUMP;
import static ir.tree.IR.LABEL;
import static ir.tree.IR.MOVE;
import static ir.tree.IR.SEQ;
import static ir.tree.IR.TEMP;
import static ir.tree.IR.TRUE;
import static translate.TranslatorLabels.L_MAIN;
import static translate.TranslatorLabels.L_PRINT;


/**
 * This visitor builds up a collection of IRTree code fragments for the body
 * of methods in a Functions program.
 * <p>
 * Methods that visit statements and expression return a TRExp, other methods
 * just return null, but they may add Fragments to the collection by means
 * of a side effect.
 *
 * @author kdvolder
 */
public class TranslateVisitor implements Visitor<TRExp> {
    /**
     * We build up a list of Fragment (pieces of stuff to be converted into
     * assembly) here.
     */
    private Fragments frags;

    /**
     * We use this factory to create Frame's, without making our code dependent
     * on the target architecture.
     */
    private Frame frameFactory;

    /**
     * The symbol table may be needed to find information about classes being
     * instantiated, or methods being called etc.
     */
    private Frame frame;

    private FunTable<Access> currentEnv;

    public TranslateVisitor(Lookup<Type> table, Frame frameFactory) {
        this.frags = new Fragments(frameFactory);
        this.frameFactory = frameFactory;
    }

    /////// Helpers //////////////////////////////////////////////

    private boolean atGlobalScope() {
        return frame.getLabel().equals(L_MAIN);
    }

    /**
     * Create a frame with a given number of formals. We assume that
     * no formals escape, which is the case in MiniJava.
     */
    private Frame newFrame(Label name, int nFormals) {
        return frameFactory.newFrame(name, nFormals);
    }

    /**
     * Creates a label for a function (used by calls to that method).
     * The label name is simply the function name.
     */
    private Label functionLabel(String functionName) {
        return Label.get("_" + functionName);
    }


    private void putEnv(String name, Access access) {
        currentEnv = currentEnv.insert(name, access);
    }

    ////// Visitor ///////////////////////////////////////////////

    @Override
    public <T extends AST> TRExp visit(NodeList<T> ns) {
        IRStm result = IR.NOP;
        for (int i = 0; i < ns.size(); i++) {
            AST nextStm = ns.elementAt(i);
            TRExp e = nextStm.accept(this);
            // e will be null if the statement was in fact a function declaration
            // just ignore these as they generate Fragments
            if (e != null)
                result = IR.SEQ(result, e.unNx());
        }
        return new Nx(result);
    }

    @Override
    public TRExp visit(Program n) {
        frame = newFrame(L_MAIN, 0);
        currentEnv = FunTable.theEmpty();

        TRExp classes = n.classes.accept(this);
        TRExp mainClass = n.mainClass.accept(this);

        IRStm body = SEQ(
                classes.unNx(),
                mainClass.unNx()
        );
        frags.add(new ProcFragment(frame, frame.procEntryExit1(body)));

        return null;   // TODO: this was based off of Functions. need to check if this is correct.
    }

    @Override
    public TRExp visit(BooleanType n) {
        return null;   // TODO: should we return null for all Types?
    }

    @Override
    public TRExp visit(IntegerType n) {
        return null;
    }

    @Override
    public TRExp visit(UnknownType n) {
        return null;
    }

    private TRExp visitStatements(NodeList<Statement> statements) {
        IRStm result = IR.NOP;
        for (int i = 0; i < statements.size(); i++) {
            Statement nextStm = statements.elementAt(i);
            result = IR.SEQ(result, nextStm.accept(this).unNx());
        }
        return new Nx(result);
    }

    @Override
    public TRExp visit(Conditional n) {
        // TODO: not sure what there is to do here.
        //TODO: This is eager, as in functions-starter!
        TRExp c = n.e1.accept(this);
        TRExp t = n.e2.accept(this);
        TRExp f = n.e3.accept(this);

        TEMP v = TEMP(new Temp());
        return new Ex(ESEQ(SEQ(
                MOVE(v, f.unEx()),
                CMOVE(RelOp.EQ, c.unEx(), TRUE, v, t.unEx())),
                v));
    }

    @Override
    public TRExp visit(MethodType n) {
        return null;
    }

    @Override
    public TRExp visit(ClassType n) {
        return null;
    }

    @Override
    public TRExp visit(BlockType n) {
        return null;
    }

    @Override
    public TRExp visit(Print n) {
        TRExp arg = n.exp.accept(this);
        return new Ex(CALL(L_PRINT, arg.unEx()));
    }

    @Override
    public TRExp visit(Assign n) {
        Access var = frame.allocLocal(false);
        putEnv(n.name, var);
        TRExp val = n.value.accept(this);
        return new Nx(MOVE(var.exp(frame.FP()), val.unEx()));
    }

    private TRExp relOp(final CJUMP.RelOp op, AST ltree, AST rtree) {
        final TRExp l = ltree.accept(this);
        final TRExp r = rtree.accept(this);
        return new TRExp() {
            @Override
            public IRStm unCx(Label t, Label f) {
                return IR.CJUMP(op, l.unEx(), r.unEx(), t, f);
            }

            @Override
            public IRStm unCx(IRExp dst, IRExp src) {
                return IR.CMOVE(op, l.unEx(), r.unEx(), dst, src);
            }

            @Override
            public IRExp unEx() {
                TEMP v = TEMP(new Temp());
                return ESEQ(SEQ(
                        MOVE(v, FALSE),
                        CMOVE(op, l.unEx(), r.unEx(), v, TRUE)),
                        v);
            }

            @Override
            public IRStm unNx() {
                Label end = Label.gen();
                return SEQ(unCx(end, end),
                        LABEL(end));
            }

        };

    }

    @Override
    public TRExp visit(LessThan n) {
        return relOp(RelOp.LT, n.e1, n.e2);
    }

    //////////////////////////////////////////////////////////////

    private TRExp numericOp(Op op, Expression e1, Expression e2) {
        TRExp l = e1.accept(this);
        TRExp r = e2.accept(this);
        return new Ex(IR.BINOP(op, l.unEx(), r.unEx()));
    }

    @Override
    public TRExp visit(Plus n) {
        return numericOp(Op.PLUS, n.e1, n.e2);
    }

    @Override
    public TRExp visit(Minus n) {
        return numericOp(Op.MINUS, n.e1, n.e2);
    }

    @Override
    public TRExp visit(Times n) {
        return numericOp(Op.MUL, n.e1, n.e2);
    }

    //////////////////////////////////////////////////////////////////

    @Override
    public TRExp visit(IntegerLiteral n) {
        return new Ex(IR.CONST(n.value));
    }

    @Override
    public TRExp visit(IdentifierExp n) {
        Access var = currentEnv.lookup(n.name);
        return new Ex(var.exp(frame.FP()));
    }

    @Override
    public TRExp visit(Not n) {
        final TRExp negated = n.e.accept(this);
        return new Cx() {
            @Override
            public IRStm unCx(Label ifTrue, Label ifFalse) {
                return negated.unCx(ifFalse, ifTrue);
            }

            @Override
            public IRStm unCx(IRExp dst, IRExp src) {
                return new Ex(IR.BINOP(Op.MINUS, IR.CONST(1), negated.unEx())).unCx(dst, src);
            }

            @Override
            public IRExp unEx() {
                return new Ex(IR.BINOP(Op.MINUS, IR.CONST(1), negated.unEx())).unEx();
            }
        };
    }

    @Override
    public TRExp visit(FunctionDecl n) {
        throw new Error("Not implemented");
    }


    @Override
    public TRExp visit(VarDecl n) {
        Access var = frame.getInArg(n.index);
        putEnv(n.name, var);
        return null;
    }


    @Override
    public TRExp visit(Call n) {
        String name = n.name;
        Label label = functionLabel(name);

        List<IRExp> arguments = List.list();
        TRExp receiver = n.receiver.accept(this);
        arguments.add(receiver.unEx());

        for (int index = 0; index < n.rands.size(); index++) {
            Expression expression = n.rands.elementAt(index);
            TRExp argument = expression.accept(this);

            arguments.add(argument.unEx());
        }

        return new Ex(CALL(label, arguments));

        // TODO: if we use this pattern of
        // passing the object we called the method on as the first argument,
        // we need to follow the pattern when implementing method declaration.
    }

    @Override
    public TRExp visit(FunctionType n) {
        return null;
    }

    /**
     * After the visitor successfully traversed the program,
     * retrieve the built-up list of Fragments with this method.
     */
    public Fragments getResult() {
        return frags;
    }

    @Override
    public TRExp visit(MainClass n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(ClassDecl n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(MethodDecl n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(IntArrayType n) {
        return null;
    }

    @Override
    public TRExp visit(ObjectType n) {
        return null;
    }

    @Override
    public TRExp visit(Block n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(If n) {
        TRExp test = n.tst.accept(this);
        TRExp then = n.thn.accept(this);
        TRExp _else = n.els.accept(this);

        Label thenLabel = Label.gen();
        Label elseLabel = Label.gen();
        Label doneLabel = Label.gen();

        return new Nx(SEQ(
                test.unCx(thenLabel, elseLabel),
                LABEL(thenLabel),
                then.unNx(),
                JUMP(doneLabel),
                LABEL(elseLabel),
                _else.unNx(),
                JUMP(doneLabel),
                LABEL(doneLabel)
        ));
    }

    @Override
    public TRExp visit(While n) {
        TRExp test = n.tst.accept(this);
        TRExp body = n.body.accept(this);

        Label startLabel = Label.gen();
        Label bodyLabel = Label.gen();
        Label doneLabel = Label.gen();

        return new Nx(SEQ(
                LABEL(startLabel),
                test.unCx(bodyLabel, doneLabel),
                LABEL(bodyLabel),
                body.unNx(),
                JUMP(startLabel),
                LABEL(doneLabel)
        ));
    }

    @Override
    public TRExp visit(ArrayAssign n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(And n) {
        return numericOp(Op.AND, n.e1, n.e2);
    }

    @Override
    public TRExp visit(ArrayLookup n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(ArrayLength n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(BooleanLiteral n) {
        if (n.value) {
            return new Ex(IR.TRUE);
        } else {
            return new Ex(IR.FALSE);
        }
    }

    @Override
    public TRExp visit(This n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(NewArray n) {
        throw new Error("Not implemented");
    }

    @Override
    public TRExp visit(NewObject n) {
        throw new Error("Not implemented");
    }
}
