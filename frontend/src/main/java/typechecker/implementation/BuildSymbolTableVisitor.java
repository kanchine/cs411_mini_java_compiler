package typechecker.implementation;

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
import ast.IdentifierExp;
import ast.If;
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
import ast.This;
import ast.Times;
import ast.Type;
import ast.UnknownType;
import ast.VarDecl;
import ast.While;
import typechecker.ErrorReport;
import util.ImpTable;
import util.ImpTable.DuplicateException;
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
    private String className = null;

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
            // Prevent re-declaring the same variable name multiple times
            if (classFields.lookup(n.name) != null) {
                errors.duplicateDefinition(n.name);
                return null;
            }
            def(classFields, n.name, n.type);
        } else {
            if (classFields.lookup(n.name) != null) {
                errors.duplicateDefinition(n.name);
                return null;
            }
            def(methodScope, n.name, n.type);
        }
        return null;
    }


    @Override
    public ImpTable<Type> visit(Call n) {
        String namePtr;
        ImpTable<Type> tablePtr;
        if (n.receiver instanceof This) {
            namePtr = className;
            tablePtr = classMethods;
        } else {
            Type type;
            if (n.receiver instanceof NewObject) {
                String name = ((NewObject) n.receiver).typeName;
                type = lookup(name);
            } else {
                n.receiver.accept(this);   // typecheck the receiver
                type = n.receiver.getType();
            }

            if (type == null) {
                errors.undefinedId(n.name);
                return null;
            }

            if (!(type instanceof ObjectType)) {
                errors.typeError(n.receiver, new ObjectType(n.name), type);
                return null;
            }

            ObjectType objectType = (ObjectType) type;
            ClassType classType = (ClassType) variables.lookup(objectType.name);
            if (classType == null) {
                errors.undefinedId(objectType.name);
                return null;
            }

            namePtr = classType.name;
            tablePtr = classType.methods;
        }

        while (namePtr != null) {
            if (tablePtr.lookup(n.name) != null) {
                return null;   // found the method name
            }

            ClassType classType = (ClassType) variables.lookup(namePtr);
            namePtr = classType.superName;
            if (namePtr != null) {
                // update tablePtr
                ClassType parentType = (ClassType) variables.lookup(namePtr);
                tablePtr = parentType.methods;
            }
        }

        errors.undefinedId(n.name);
        return null;
    }

    @Override
    public ImpTable<Type> visit(MainClass n) {
        // TODO: we need a scope here as well
        ClassType classType = new ClassType();
        n.statement.accept(this);
        return null;
    }

    @Override
    public ImpTable<Type> visit(ClassDecl n) {
        if (variables.lookup(n.name) != null) {
            errors.duplicateDefinition(n.name);
            return null;
        }

        ClassType classType = new ClassType();
        classFields = classType.locals;
        classMethods = classType.methods;
        classType.name = n.name;
        classType.superName = n.superName;

        className = n.name;

        def(variables, n.name, classType);

        n.vars.accept(this);
        n.methods.accept(this);

        n.classType = classType;
        classFields = null;
        classMethods = null;
        className = null;

        return null;
    }

    @Override
    public ImpTable<Type> visit(MethodDecl n) {
        if (classMethods.lookup(n.name) != null) {
            errors.duplicateDefinition(n.name);
            return null;
        }

        MethodType methodType = new MethodType();
        methodScope = methodType.locals;
        methodType.returnType = n.returnType;
        methodType.formals = n.formals;

        def(classMethods, n.name, methodType);

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

        errors.undefinedId(name);

        return null;
    }
}
