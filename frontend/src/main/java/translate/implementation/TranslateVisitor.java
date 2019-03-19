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
import ir.tree.*;
import ir.tree.BINOP.Op;
import ir.tree.CJUMP.RelOp;
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
import static translate.TranslatorLabels.*;


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

    private FunTable<IRExp> currentEnv;

    private Lookup<Type> allClasses;
    private ClassType currClass = null;

    public TranslateVisitor(Lookup<Type> table, Frame frameFactory) {
        this.frags = new Fragments(frameFactory);
        this.frameFactory = frameFactory;
        this.allClasses = table;
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
        currentEnv = currentEnv.insert(name, access.exp(frame.FP()));
    }

    private void putEnv(String name, IRExp exp) {
        currentEnv = currentEnv.insert(name, exp);
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
                mainClass.unNx(),
                classes.unNx()
        );
        frags.add(new ProcFragment(frame, frame.procEntryExit1(body)));

        return null;
    }

    @Override
    public TRExp visit(BooleanType n) {
        return null;
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
        TRExp c = n.e1.accept(this);
        TRExp t = n.e2.accept(this);
        TRExp f = n.e3.accept(this);

        TEMP v = TEMP(new Temp());
        Label tl = Label.gen();
        Label fl = Label.gen();
        Label j = Label.gen();

        return new Ex(ESEQ(SEQ(SEQ(SEQ(SEQ(SEQ(SEQ(SEQ(c.unCx(tl, fl),LABEL(tl)), MOVE(v, t.unEx())),JUMP(j)), LABEL(fl)), MOVE(v, f.unEx())), JUMP(j)), LABEL(j)), v));
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
        IRExp var = currentEnv.lookup(n.name);

        if (var == null) {
            IRExp base = frame.getFormal(0).exp(frame.FP());
            IRExp offset = IR.CONST(getFieldOffset(n.name, currClass));
            TRExp value = n.value.accept(this);

            return new Nx(IR.MOVE(IR.MEM(getMemLocation(base, offset)), value.unEx()));
        } else {
            TRExp value = n.value.accept(this);
            return new Nx(IR.MOVE(var, value.unEx()));
        }
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
        IRExp var = currentEnv.lookup(n.name);

        if (var == null) {
            IRExp base = frame.getFormal(0).exp(frame.FP());
            IRExp offset = IR.CONST(getFieldOffset(n.name, currClass));
            return new Ex(IR.MEM(getMemLocation(base, offset)));
        } else
            return new Ex(var);

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

        return null;
    }


    @Override
    public TRExp visit(VarDecl n) {
//        if (isInMethodScope) {
//            Access var = frame.getInArg(n.index);
//            putEnv(n.name, var);
//        } else {
//            Label label = Label.get(n.name);
//            IRData data = new IRData(label, List.list(IR.CONST(0)));  // can we directly initialize the location to the value specified in the program rather than CONST zero ???
//
//            // append this DataFragment to fragment list
//            DataFragment dataFragment = new DataFragment(frame, data);
//            frags.add(dataFragment);
//
//            currentEnv = currentEnv.insert(n.name, IR.MEM(IR.NAME(label)));
//        }

        return null;
    }


    @Override
    public TRExp visit(Call n) {
        String recType = "";
        ClassType currClass = (ClassType) allClasses.lookup(n.receiver.getType().toString());
        while (currClass != null) {
            if (currClass.methods.lookup(n.name) != null) {
                recType = currClass.name;
                break;
            }

            currClass = (ClassType) allClasses.lookup(currClass.superName);
        }
        String name = recType + "#" + n.name;
        Label label = functionLabel(name);

        List<IRExp> arguments = List.list();
        TRExp receiver = n.receiver.accept(this);
        arguments.add(receiver.unEx());

        for (int index = 0; index < n.rands.size(); index++) {
            Expression expression = n.rands.elementAt(index);
            TRExp argument = expression.accept(this);

            arguments.add(argument.unEx());
        }

        return new Ex(IR.CALL(label, arguments));
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
        return n.statement.accept(this);
    }

    @Override
    public TRExp visit(ClassDecl n) {
        currClass = (ClassType) allClasses.lookup(n.name);
        for (int i = 0; i < n.vars.size(); ++i) {
            n.vars.elementAt(i).accept(this);
        }

        n.methods.accept(this);

        currClass = null;
        return null;
    }

    @Override
    public TRExp visit(MethodDecl n) {
        Frame oldFrame = frame;
        frame = newFrame(functionLabel(currClass.name + "#" + n.name), n.formals.size() + 1);
        FunTable<IRExp> saveEnv = currentEnv;

        //Get the access information for each regular formal and add it to the environment.
        for (int i = 0; i < n.formals.size(); i++) {
            putEnv(n.formals.elementAt(i).name, frame.getFormal(i + 1));
        }

        for (int i = 0; i < n.vars.size(); i++) {
            Access var = frame.allocLocal(false);
            putEnv(n.vars.elementAt(i).name, var);
        }

        TRExp stats = visitStatements(n.statements);
        TRExp exp = n.returnExp.accept(this);

        IRStm body = IR.SEQ(
                stats.unNx(),
                MOVE(frame.RV(), exp.unEx()));
        body = frame.procEntryExit1(body);
        frags.add(new ProcFragment(frame, body));

        frame = oldFrame;
        currentEnv = saveEnv;

        return null;
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
        return n.statements.accept(this);
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
        IRExp var = currentEnv.lookup(n.name);

        if (var == null) {
            IRExp base = frame.getFormal(0).exp(frame.FP());
            IRExp offset = IR.CONST(getFieldOffset(n.name, currClass));

            var = IR.MEM(getMemLocation(base, offset));
        }

        IRExp index = n.index.accept(this).unEx();
        IRExp value = n.value.accept(this).unEx();
        IRExp size = IR.MEM(getMemLocation(var, IR.CONST(-1)));

        Label validIndex = Label.gen();
        Label invalidIndex = Label.gen();
        Label done = Label.gen();

        Temp temp = new Temp();
        return new Nx(
                SEQ(IR.CJUMP(RelOp.LT, index, size, validIndex, invalidIndex),

                        LABEL(invalidIndex),
                        MOVE(TEMP(temp), CALL(L_ERROR, IR.CONST(1))),
                        JUMP(done),

                        LABEL(validIndex),
                        MOVE(IR.MEM(getMemLocation(var, index)), value),
                        LABEL(done)
                )
        );
    }

    @Override
    public TRExp visit(And n) {
        return relOp(RelOp.EQ, n.e1, n.e2);
    }

    @Override
    public TRExp visit(ArrayLookup n) {
        IRExp offset = n.index.accept(this).unEx();
        IRExp base = n.array.accept(this).unEx();
        IRExp size = IR.MEM(getMemLocation(base, IR.CONST(-1)));

        Label validIndex = Label.gen();
        Label invalidIndex = Label.gen();
        Label done = Label.gen();

        Temp temp = new Temp();
        return new Ex(
                ESEQ(
                        SEQ(IR.CJUMP(RelOp.LT, offset, size, validIndex, invalidIndex),

                            LABEL(invalidIndex),
                            MOVE(TEMP(temp), CALL(L_ERROR, IR.CONST(1))),
                            JUMP(done),

                            LABEL(validIndex),
                            MOVE(TEMP(temp), IR.MEM(getMemLocation(base, offset))),
                            LABEL(done)
                        ),
                        TEMP(temp)
                )
        );
    }

    @Override
    public TRExp visit(ArrayLength n) {
        IRExp base = n.array.accept(this).unEx();
        return new Ex(IR.MEM(getMemLocation(base, IR.CONST(-1))));
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
        Access var =  frame.getFormal(0);
        return new Ex(var.exp(frame.FP()));
    }

    @Override
    public TRExp visit(NewArray n) {
        TRExp size = n.size.accept(this);
        return new Ex(IR.CALL(L_NEW_ARRAY, size.unEx()));
    }

    @Override
    public TRExp visit(NewObject n) {
        ClassType curr = (ClassType) allClasses.lookup(n.typeName);
        int numFields = curr.fields.size();

        while (curr != null) {
            numFields += curr.fields.size();
            curr = (ClassType) allClasses.lookup(curr.superName);
        }

        TRExp size = new Ex(IR.CONST(numFields * 8));

        return new Ex(IR.CALL(L_NEW_OBJECT, size.unEx()));
    }

    private IRExp getMemLocation(IRExp base, IRExp offset) {
        return IR.BINOP(Op.PLUS, IR.BINOP(Op.MUL, offset, IR.CONST(8)), base);
    }

    private int getFieldOffset(String name, ClassType currClass) {
        int offset = 0;

        ClassType curr = currClass;

        while (curr != null) {
            for (int idx = 0; idx < currClass.fields.size(); ++idx) {
                if (name.equals(curr.fields.elementAt(idx).name))
                    return offset;

                ++offset;
            }
            curr = (ClassType) allClasses.lookup(curr.superName);
        }

        return 0;
    }
}
