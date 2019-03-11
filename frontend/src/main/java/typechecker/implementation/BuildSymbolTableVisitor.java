package typechecker.implementation;

import ast.*;
import ir.interp.Array;
import typechecker.ErrorReport;
import util.ImpTable;
import util.ImpTable.DuplicateException;
import util.Pair;
import visitor.DefaultVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * This visitor implements Phase 1 of the TypeChecker. It constructs the symboltable.
 *
 * @author norm
 */
public class BuildSymbolTableVisitor extends DefaultVisitor<ImpTable<Type>> {

    private final ImpTable<Type> variables = new ImpTable<Type>();    // Symbol table for global scope that includes all classes declarations
    private final ErrorReport errors;
    private ImpTable<Type> classFields = null;                     // Symbol table for class fields
    private ImpTable<Type> classMethods = null;                    // Symbol table for class methods
    private ImpTable<Type> methodScope = null;                     // Symbol table for a particular method
    private ImpTable<Type> blockScope = null;                      // Symbol table for a particular block

    public BuildSymbolTableVisitor(ErrorReport errors) {
        this.errors = errors;
    }

    /////////////////// Phase 1 ///////////////////////////////////////////////////////
    // In our implementation, Phase 1 builds up a symbol table containing all the
    // global identifiers defined in a Functions program, as well as symbol tables
    // for each of the function declarations encountered.
    //
    // We also check for duplicate identifier definitions in each symbol table

    @Override
    public ImpTable<Type> visit(Program n) {
        n.mainClass.accept(this);
        n.classes.accept(this); // process all the "normal" classes.
        return variables;
    }

    @Override
    public <T extends AST> ImpTable<Type> visit(NodeList<T> ns) {
        for (int i = 0; i < ns.size(); i++)
            ns.elementAt(i).accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(Assign n) {
        n.value.accept(this);
        if (lookup(n.name) == null)
            errors.undefinedId(n.name);
        // We don't need this be cause we need to declare the variable before assigning
//        def(methodScope, n.name, new UnknownType());
        return null;
    }


    @Override
    public ImpTable<Type> visit(IdentifierExp n) {
        if (lookup(n.name) == null)
            errors.undefinedId(n.name);
        return null;
    }

    @Override
    public ImpTable<Type> visit(BooleanType n) {
        return null;
    }

    @Override
    public ImpTable<Type> visit(IntegerType n) {
        return null;
    }

    @Override
    public ImpTable<Type> visit(Print n) {
        n.exp.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(LessThan n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(Conditional n) {
        n.e1.accept(this);
        n.e2.accept(this);
        n.e3.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(Plus n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(Minus n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(Times n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(IntegerLiteral n) {
        return null;
    }

    @Override
    public ImpTable<Type> visit(Not not) {
        not.e.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(UnknownType n) {
        return null;
    }


    @Override
    public ImpTable<Type> visit(VarDecl n) {
        if (n.kind == VarDecl.Kind.FIELD) {
            def(classFields, n.name, n.type);
        } else {
            def(methodScope, n.name, n.type);
        }
        return null;
    }

    @Override
    public ImpTable<Type> visit(Call n) {
        // TODO: the expression in the call node should be an identifier, how do we locate the symbol table for that
        if (n.receiver instanceof This) {
            // Check the method table in the current class
            if (classMethods.lookup(n.name) == null)
                errors.undefinedId(n.name);
        } else if (n.receiver instanceof IdentifierExp) {
            // retrieve the method table in the corresponding class
            String name = ((IdentifierExp) n.receiver).name;
            // we need to figure out the type here to check if the method exists in the corresponding class definition
            Type type = lookup(name);

            if (type == null) {
                errors.undefinedId(n.name);
                return null;
            }

            // TODO: this is a bit ugly
            if (!(type instanceof ObjectType)) {
                errors.typeError(n.receiver, new ObjectType(n.name), type);
                return null;
            }

            ClassType classType = (ClassType) variables.lookup(((ObjectType) type).name);
            if (classType == null) {
                errors.undefinedId(((ObjectType) type).name);
                return null;
            }

            if (classType.methods.lookup(n.name) == null)
                errors.undefinedId(n.name);
        }

        return null;
    }

    @Override
    public ImpTable<Type> visit(MainClass n) {
        n.statement.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(ClassDecl n) {
        ClassType classType = new ClassType();
        classFields = classType.locals;
        classMethods = classType.methods;
        classType.locals = classFields;
        classType.methods = classMethods;
        classType.name = n.name;
        classType.superName = n.superName;

        def(variables, n.name, classType);

        n.vars.accept(this);
        n.methods.accept(this);

        n.classType = classType;
        classFields = null;
        classMethods = null;

        return null;
    }

    @Override
    public ImpTable<Type> visit(MethodDecl n) {
        MethodType methodType = new MethodType();
        methodScope = methodType.locals;
        methodType.locals = methodScope;
        methodType.returnType = n.returnType;
        methodType.formals = n.formals;

        def(classFields, n.name, methodType);

        n.formals.accept(this);
        n.vars.accept(this);
        n.statements.accept(this);
        n.returnExp.accept(this);
        n.methodType = methodType;

        methodScope = null;
        return null;
    }

    @Override
    public ImpTable<Type> visit(Block n) {
        BlockType blockType = new BlockType();
        blockScope = blockType.locals;
        n.statements.accept(this);
        n.blockType = blockType;
        blockScope = null;
        return null;
    }

    @Override
    public ImpTable<Type> visit(If n) {
        n.tst.accept(this);
        n.thn.accept(this);
        n.els.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(While n) {
        BlockType blockType = new BlockType();
        blockScope = blockType.locals;
        n.tst.accept(this);
        n.body.accept(this);
        blockScope = null;
        return null;
    }

    @Override
    public ImpTable<Type> visit(ArrayAssign n) {
        n.value.accept(this);
        if (lookup(n.name) == null)
            errors.undefinedId(n.name);
        return null;
    }

    @Override
    public ImpTable<Type> visit(And n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(ArrayLookup n) {
        n.index.accept(this);
        n.array.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(ArrayLength n) {
        n.array.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(BooleanLiteral n) {
        return null;
    }

    @Override
    public ImpTable<Type> visit(This n) {
        // TODO: lets double check this
        return null;
    }

    @Override
    public ImpTable<Type> visit(NewArray n) {
        n.size.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(NewObject n) {
        if (variables.lookup(n.typeName) == null)
            errors.undefinedId(n.typeName);

        return null;
    }

    @Override
    public ImpTable<Type> visit(MethodType n) {
        return null;
    }


    ///////////////////// Helpers ///////////////////////////////////////////////

    /**
     * Add an entry to a table, and check whether the name already existed.
     * If the name already existed before, the new definition is ignored and
     * an error is sent to the error report.
     */
    private <V> void def(ImpTable<V> tab, String name, V value) {
        try {
            tab.put(name, value);
        } catch (DuplicateException e) {
            errors.duplicateDefinition(name);
        }
    }

    // lookup a name in local, class, or global scope. This lookup method is private to BuildSymbolTableVisitor
    private Type lookup(String name) {
        // first lookup in local symbol table, if not found, then lookup in global symbol table (variables and functions)
        List<ImpTable<Type>> scopes = new ArrayList<>();
        // set up scopes look up order
        scopes.add(blockScope);
        scopes.add(methodScope);
        scopes.add(classFields);
        Type type = null;  // the type associated with the input name

        for (ImpTable<Type> scope : scopes) {
            if (scope != null) {
                type = scope.lookup(name);
                if (type != null)
                    return type;
            }
        }

        if (type == null) {
            errors.undefinedId(name);
        }

        return type;
    }
}
