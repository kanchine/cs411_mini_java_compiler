package codegen.x86_64;

import codegen.assem.A_LABEL;
import codegen.assem.A_MOVE;
import codegen.assem.A_OPER;
import codegen.assem.Instr;
import codegen.muncher.MunchRule;
import codegen.muncher.Muncher;
import codegen.muncher.MuncherRules;
import codegen.patterns.Matched;
import codegen.patterns.Pat;
import codegen.patterns.Wildcard;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.CJUMP.RelOp;
import ir.tree.IR;
import ir.tree.IRExp;
import ir.tree.IRStm;
import util.IndentingWriter;
import util.List;

import static codegen.patterns.IRPat.CALL;
import static codegen.patterns.IRPat.CJUMP;
import static codegen.patterns.IRPat.CMOVE;
import static codegen.patterns.IRPat.CONST;
import static codegen.patterns.IRPat.EXP;
import static codegen.patterns.IRPat.JUMP;
import static codegen.patterns.IRPat.LABEL;
import static codegen.patterns.IRPat.MEM;
import static codegen.patterns.IRPat.MINUS;
import static codegen.patterns.IRPat.MOVE;
import static codegen.patterns.IRPat.MUL;
import static codegen.patterns.IRPat.NAME;
import static codegen.patterns.IRPat.PLUS;
import static codegen.patterns.IRPat.TEMP;
import static ir.frame.x86_64.X86_64Frame.RAX;
import static ir.frame.x86_64.X86_64Frame.RDX;
import static ir.frame.x86_64.X86_64Frame.RV;
import static ir.frame.x86_64.X86_64Frame.arguments;
import static ir.frame.x86_64.X86_64Frame.callerSave;
import static ir.frame.x86_64.X86_64Frame.special;
import static util.List.list;

/**
 * This Muncher implements the munching rules for a subset
 * of X86 instruction set.
 *
 * @author kdvolder
 */
// Note: making a trivial change to verify scoreboard
public class X86_64Muncher extends Muncher {

    /**
     * If this flag is false, then we only use a bare minimum of small
     * tiles. This should be enough to generate working code, but it
     * generates a lot of instructions (all things operated on are
     * first loaded into a temp).
     */
    private static final List<Temp> noTemps = List.empty();

    private static MuncherRules<IRStm, Void> sm = new MuncherRules<IRStm, Void>();
    private static MuncherRules<IRExp, Temp> em = new MuncherRules<IRExp, Temp>();
    private static MuncherRules<IRExp, Void> dm = new MuncherRules<IRExp, Void>();

    public X86_64Muncher(Frame frame) {
        super(frame, sm, em, dm);
    }

    public X86_64Muncher(Frame frame, boolean beVerbose) {
        super(frame, sm, em, beVerbose);
    }

    //////////// The munching rules ///////////////////////////////

    static { //Done only once, at class loading time.

        // Pattern "variables" (used by the rules below)

        final Pat<IRExp> _e_ = Pat.any();
        final Pat<IRExp> _l_ = Pat.any();
        final Pat<IRExp> _r_ = Pat.any();

        final Pat<List<IRExp>> _es_ = Pat.any();

        final Pat<Label> _lab_ = Pat.any();
        final Pat<Label> _thn_ = Pat.any();
        final Pat<Label> _els_ = Pat.any();

        final Pat<RelOp> _relOp_ = Pat.any();

        final Pat<Temp> _t_ = Pat.any();

        final Pat<Integer> _i_ = Pat.any();

        final Pat<Integer> _scale_ = new Wildcard<Integer>() {
            @Override
            public void match(Integer toMatch, Matched matched) throws Failed {
                int value = (Integer) toMatch;
                if (value == 1 || value == 2 || value == 4 || value == 8)
                    super.match(toMatch, matched);
                else
                    fail();
            }

            public void dump(IndentingWriter out) {
                out.print("1|2|4|8");
            }
        };

        // =====================================================================
        // Pattern variables created by William

        final Pat<Integer> _offset_ = Pat.any();

        // =====================================================================
        // Larger tiles created by William

        // x = x + 1
        dm.add(new MunchRule<IRExp, Void>(
                PLUS(_l_, CONST(1))
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp value = m.munch(c.get(_l_));
                m.emit(A_INC(value));

                return null;
            }
        });

        // x = x - 1
        dm.add(new MunchRule<IRExp, Void>(
                MINUS(_l_, CONST(1))
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp value = m.munch(c.get(_l_));
                m.emit(A_DEC(value));

                return null;
            }
        });

        // x = 0 - x
        dm.add(new MunchRule<IRExp, Void>(
                MINUS(CONST(0), _r_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp value = m.munch(c.get(_r_));
                m.emit(A_NEG(value));

                return null;
            }
        });

        // x = -1 * x
        dm.add(new MunchRule<IRExp, Void>(
                MUL(CONST(-1), _r_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp value = m.munch(c.get(_r_));
                m.emit(A_NEG(value));

                return null;
            }
        });

        // x = 0
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(_e_, CONST(0))
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp value = m.munch(c.get(_e_));
                m.emit(A_XOR(value, value));

                return null;
            }
        });

        // x = a[i]
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(_e_, MEM(PLUS(_l_, MUL(CONST(_scale_), CONST(_i_)))))
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp receiver = m.munch(c.get(_e_));
                Temp base = m.munch(c.get(_l_));
                int scale = c.get(_scale_);
                int index = c.get(_i_);

                m.emit(A_MOVE_IM2R_index(base, index, scale, receiver));

                return null;
            }
        });

        // a[i] = x
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(MEM(PLUS(_l_, MUL(CONST(_scale_), CONST(_i_)))), _e_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp base = m.munch(c.get(_l_));
                int scale = c.get(_scale_);
                int index = c.get(_i_);
                Temp value = m.munch(c.get(_e_));

                m.emit(A_MOVE_R2IM_index(base, index, scale, value));

                return null;
            }
        });

        // x = y.z
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(_e_, MEM(PLUS(_l_, CONST(_offset_))))
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp receiver = m.munch(c.get(_e_));
                Temp base = m.munch(c.get(_l_));
                int offset = c.get(_offset_);

                m.emit(A_MOVE_M2R(base, offset, receiver));

                return null;
            }
        });

        // y.z = x
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(MEM(PLUS(_l_, CONST(_offset_))), _e_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp base = m.munch(c.get(_l_));
                int offset = c.get(_offset_);
                Temp value = m.munch(c.get(_e_));

                m.emit(A_MOVE_R2M(base, offset, value));

                return null;
            }
        });

        // x = y.a[i]
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(_e_, MEM(PLUS(_l_, CONST(_offset_), MUL(CONST(_scale_), CONST(_i_)))))
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp receiver = m.munch(c.get(_e_));
                Temp base = m.munch(c.get(_l_));
                int offset = c.get(_offset_);
                int scale = c.get(_scale_);
                int index = c.get(_i_);

                m.emit(A_MOVE_IM2R_offset(base, offset, index, scale, receiver));

                return null;
            }
        });

        // y.a[i] = x
        sm.add(new MunchRule<IRStm, Void>(
                MOVE(MEM(PLUS(_l_, CONST(_offset_), MUL(CONST(_scale_), CONST(_i_)))), _e_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp base = m.munch(c.get(_l_));
                int offset = c.get(_offset_);
                int scale = c.get(_scale_);
                int index = c.get(_i_);
                Temp value = m.munch(c.get(_e_));

                m.emit(A_MOVE_R2IM_offset(base, offset, index, scale, value));

                return null;
            }
        });

        /*

        // compare immediate and memory (CJUMP case)
        sm.add(new MunchRule<IRStm, Void>(
                CJUMP(_relOp_, CONST(_i_), MEM(PLUS(_r_, CONST(_offset_))), _thn_, _els_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                RelOp relOp = c.get(_relOp_);
                int immediate = c.get(_i_);

                Temp base = m.munch(c.get(_r_));
                int offset = c.get(_offset_);

                Label thn = c.get(_thn_);
                Label els = c.get(_els_);

                m.emit(A_CMP_I2M(immediate, base, offset));
                m.emit(A_CJUMP(relOp, thn, els));

                return null;
            }
        });

        // compare immediate and memory (CMOVE case)
        sm.add(new MunchRule<IRStm, Void>(
                CMOVE(_relOp_, CONST(_i_), MEM(PLUS(_r_, CONST(_offset_))), TEMP(_t_), _e_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                RelOp relOp = c.get(_relOp_);
                int immediate = c.get(_i_);

                Temp base = m.munch(c.get(_r_));
                int offset = c.get(_offset_);

                Temp receiver = c.get(_t_);
                Temp value = m.munch(c.get(_e_));

                m.emit(A_CMP_I2M(immediate, base, offset));
                m.emit(A_CMOV(relOp, receiver, value));

                return null;
            }
        });

        // compare memory and register (CJUMP case)
        sm.add(new MunchRule<IRStm, Void>(
                CJUMP(_relOp_, MEM(PLUS(_l_, CONST(_offset_))), _r_, _thn_, _els_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                RelOp relOp = c.get(_relOp_);

                Temp base = m.munch(c.get(_l_));
                int offset = c.get(_offset_);

                Temp register = m.munch(c.get(_r_));

                Label thn = c.get(_thn_);
                Label els = c.get(_els_);

                m.emit(A_CMP_M2R(base, offset, register));
                m.emit(A_CJUMP(relOp, thn, els));

                return null;
            }
        });

        // compare memory and register (CMOVE case)
        sm.add(new MunchRule<IRStm, Void>(
                CMOVE(_relOp_, MEM(PLUS(_l_, CONST(_offset_))), _r_, TEMP(_t_), _e_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                RelOp relOp = c.get(_relOp_);

                Temp base = m.munch(c.get(_l_));
                int offset = c.get(_offset_);

                Temp register = m.munch(c.get(_r_));

                Temp receiver = c.get(_t_);
                Temp value = m.munch(c.get(_e_));

                m.emit(A_CMP_M2R(base, offset, register));
                m.emit(A_CMOV(relOp, receiver, value));

                return null;
            }
        });

        // compare register and memory (CJUMP)
        sm.add(new MunchRule<IRStm, Void>(
                CJUMP(_relOp_, _l_, MEM(PLUS(_r_, CONST(_offset_))), _thn_, _els_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                RelOp relOp = c.get(_relOp_);
                Temp register = m.munch(c.get(_l_));

                Temp base = m.munch(c.get(_r_));
                int offset = c.get(_offset_);

                Label thn = c.get(_thn_);
                Label els = c.get(_els_);

                m.emit(A_CMP_R2M(base, offset, register));
                m.emit(A_CJUMP(relOp, thn, els));

                return null;
            }
        });

        sm.add(new MunchRule<IRStm, Void>(
                CMOVE(_relOp_, _l_, MEM(PLUS(_r_, CONST(_offset_))), TEMP(_t_), _e_)
        ) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                RelOp relOp = c.get(_relOp_);
                Temp register = m.munch(c.get(_l_));

                Temp base = m.munch(c.get(_r_));
                int offset = c.get(_offset_);

                Temp receiver = c.get(_t_);
                Temp value = m.munch(c.get(_e_));

                m.emit(A_CMP_R2M(base, offset, register));
                m.emit(A_CMOV(relOp, receiver, value));

                return null;
            }
        });

        */

        // =====================================================================

        // A basic set of small tiles.

        dm.add(new MunchRule<IRExp, Void>(CONST(_i_)) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                m.emit(A_QUAD(c.get(_i_)));
                return null;
            }
        });
        dm.add(new MunchRule<IRExp, Void>(NAME(_lab_)) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                m.emit(A_QUAD(c.get(_lab_)));
                return null;
            }
        });

        sm.add(new MunchRule<IRStm, Void>(LABEL(_lab_)) {
            @Override
            protected Void trigger(Muncher m, Matched children) {
                m.emit(A_LABEL(children.get(_lab_)));
                return null;
            }
        });
        sm.add(new MunchRule<IRStm, Void>(JUMP(_e_)) {
            protected Void trigger(Muncher m, Matched children) {
                // Expression shouldn't need to emit indirect jumps.
                // (assuming there's a rule to match JUMP(NAME(*))
                throw new Error("Not implemented");
            }
        });
        sm.add(new MunchRule<IRStm, Void>(EXP(_e_)) {
            @Override
            protected Void trigger(Muncher m, Matched children) {
                IRExp exp = children.get(_e_);
                m.munch(exp);
                return null;
            }
        });
        sm.add(new MunchRule<IRStm, Void>(MOVE(TEMP(_t_), _e_)) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                m.emit(A_MOV(c.get(_t_),
                        m.munch(c.get(_e_))));
                return null;
            }
        });
        sm.add(new MunchRule<IRStm, Void>(MOVE(MEM(_l_), _r_)) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                Temp d = m.munch(c.get(_l_));
                Temp s = m.munch(c.get(_r_));
                m.emit(A_MOV_TO_MEM(d, s));
                return null;
            }
        });
        sm.add(new MunchRule<IRStm, Void>(JUMP(NAME(_lab_))) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                m.emit(A_JMP(c.get(_lab_)));
                return null;
            }
        });
        sm.add(new MunchRule<IRStm, Void>(CJUMP(_relOp_, _l_, _r_, _thn_, _els_)) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                m.emit(A_CMP(m.munch(c.get(_l_)), m.munch(c.get(_r_))));
                m.emit(A_CJUMP(c.get(_relOp_), c.get(_thn_), c.get(_els_)));
                return null;
            }
        });
        sm.add(new MunchRule<IRStm, Void>(CMOVE(_relOp_, _l_, _r_, TEMP(_t_), _e_)) {
            @Override
            protected Void trigger(Muncher m, Matched c) {
                m.emit(A_CMP(m.munch(c.get(_l_)), m.munch(c.get(_r_))));
                m.emit(A_CMOV(c.get(_relOp_), c.get(_t_), m.munch(c.get(_e_))));
                return null;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(CALL(_l_, _es_)) {
            @Override
            protected Temp trigger(Muncher m, Matched children) {
                // Expressions shouldn't need to emit indirect calls ( unless we implement VMT and inheritance )
                throw new Error("Not implemented");
            }
        });
        em.add(new MunchRule<IRExp, Temp>(CONST(_i_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Temp t = new Temp();
                m.emit(A_MOV(t, c.get(_i_)));
                return t;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(PLUS(_l_, _r_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Temp sum = new Temp();
                m.emit(A_MOV(sum, m.munch(c.get(_l_))));
                m.emit(A_ADD(sum, m.munch(c.get(_r_))));
                return sum;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(MINUS(_l_, _r_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Temp res = new Temp();
                m.emit(A_MOV(res, m.munch(c.get(_l_))));
                m.emit(A_SUB(res, m.munch(c.get(_r_))));
                return res;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(MUL(_l_, _r_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Temp res = new Temp();
                m.emit(A_MOV(res, m.munch(c.get(_l_))));
                m.emit(A_IMUL(res, m.munch(c.get(_r_))));
                return res;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(TEMP(_t_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                return c.get(_t_);
            }
        });
        em.add(new MunchRule<IRExp, Temp>(NAME(_lab_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Temp t = new Temp();
                m.emit(A_MOV(t, c.get(_lab_)));
                return t;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(MEM(_e_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Temp r = new Temp();
                m.emit(A_MOV_FROM_MEM(r, m.munch(c.get(_e_))));
                return r;
            }
        });
        em.add(new MunchRule<IRExp, Temp>(CALL(NAME(_lab_), _es_)) {
            @Override
            protected Temp trigger(Muncher m, Matched c) {
                Frame frame = m.getFrame();
                Label name = c.get(_lab_);
                List<IRExp> args = c.get(_es_);
                for (int i = args.size() - 1; i >= 0; i--) {
                    IRExp outArg = frame.getOutArg(i).exp(frame.FP());
                    m.munch(IR.MOVE(outArg, args.get(i)));
                }
                m.emit(A_CALL(name, args.size()));
                return RV;
            }

        });
    }

    // =========================================================================
    // Helper methods created by William

    private static Instr A_CMP_I2M(int immediate, Temp base, int offset) {
        return new A_OPER(
                "\tcmpq\t$" + immediate + ", " + offset + "(" + base + ")",
                noTemps,
                list(base)
        );
    }

    private static Instr A_CMP_M2R(Temp base, int offset, Temp register) {
        return new A_OPER(
                "\tcmpq\t" + offset + "(" + base + "), " + register,
                noTemps,
                list(base, register)
        );
    }

    private static Instr A_CMP_R2M(Temp base, int offset, Temp register) {
        return new A_OPER(
                "\tcmpq\t" + register + ", " + offset + "(" + base + ")",
                noTemps,
                list(register, base)
        );
    }

    private static Instr A_DEC(Temp value) {
        return new A_OPER(
                "\tdecq\t" + value,
                list(value),
                list(value)
        );
    }

    private static Instr A_INC(Temp value) {
        return new A_OPER(
                "\tincq\t" + value,
                list(value),
                list(value)
        );
    }

    private static Instr A_MOVE_IM2R_index(Temp base, int index, int scale, Temp receiver) {
        return new A_OPER(
                "\tmovq\t" + "(" + base + ", " + index + ", " + scale + "), " + receiver,
                list(receiver),
                list(base)
        );
    }

    private static Instr A_MOVE_IM2R_offset(Temp base, int offset, int index, int scale, Temp receiver) {
        return new A_OPER(
                "\tmovq\t" + (offset == 0 ? "" : offset) + "(" + base + ", " + index + ", " + scale + "), " + receiver,
                list(receiver),
                list(base)
        );
    }

    private static Instr A_MOVE_M2R(Temp base, int offset, Temp receiver) {
        return new A_OPER(
                "\tmovq\t" + (offset == 0 ? "" : offset) + "(" + base + "), " + receiver,
                list(receiver),
                list(base)
        );
    }

    private static Instr A_MOVE_R2IM_index(Temp base, int index, int scale, Temp value) {
        return new A_OPER(
                "\tmovq\t" + value + ", (" + base + ", " + index + ", " + scale + ")",
                list(base),
                list(value)
        );
    }

    private static Instr A_MOVE_R2IM_offset(Temp base, int offset, int index, int scale, Temp value) {
        return new A_OPER(
                "\tmovq\t" + value + ", " + (offset == 0 ? "" : offset) + "(" + base + ", " + index + ", " + scale + ")",
                list(value),
                list(base)
        );
    }

    private static Instr A_MOVE_R2M(Temp base, int offset, Temp value) {
        return new A_OPER(
                "\tmovq\t" + value + ", " + (offset == 0 ? "" : offset) + "(" + base + ")",
                list(base),
                list(value)
        );
    }

    private static Instr A_NEG(Temp value) {
        return new A_OPER(
                "\tnegq\t" + value,
                list(value),
                list(value)
        );
    }

    private static Instr A_XOR(Temp source, Temp target) {
        return new A_OPER(
                "\txorq\t" + source + ", " + target,
                list(target),
                list(source, target)
        );
    }

    // =========================================================================

    ///////// Helper methods to generate X86 assembly instructions //////////////////////////////////////

    private static Instr A_QUAD(int i) {
        return new A_OPER(".quad    " + i, noTemps, noTemps);
    }

    private static Instr A_QUAD(Label l) {
        return new A_OPER(".quad    " + l, noTemps, noTemps);
    }

    private static Instr A_ADD(Temp dst, Temp src) {
        return new A_OPER("addq    `s0, `d0",
                list(dst),
                list(src, dst));
    }

    private static Instr A_CALL(Label fun, int nargs) {
        List<Temp> args = List.empty();
        for (int i = 0; i < Math.min(arguments.size(), nargs); ++i) {
            args.add(arguments.get(i));
        }
        return new A_OPER("call    " + fun, callerSave.append(arguments), special.append(args));
    }

    private static Instr A_CJUMP(RelOp relOp, Label thn, Label els) {
        String opCode;
        switch (relOp) {
            case EQ:
                opCode = "je ";
                break;
            case NE:
                opCode = "jne";
                break;
            case GE:
                opCode = "jge";
                break;
            case LT:
                opCode = "jl ";
                break;
            case LE:
                opCode = "jle";
                break;
            case GT:
                opCode = "jg";
                break;
            case ULT:
                opCode = "jb";
                break;
            case UGT:
                opCode = "ja";
                break;
            case ULE:
                opCode = "jbe";
                break;
            case UGE:
                opCode = "jae";
                break;
            default:
                throw new Error("Missing case?");
        }
        return new A_OPER(opCode + "     `j0", noTemps, noTemps, list(thn, els));
    }

    private static Instr A_CMP(Temp l, Temp r) {
        return new A_OPER("cmpq    `s1, `s0", noTemps, list(l, r));
    }

    private static Instr A_IMUL(Temp dst, Temp src) {
        return new A_OPER("imulq   `s0, `d0",
                list(dst),
                list(src, dst));
    }

    private static Instr A_IDIV(Temp dst, Temp src) {
        return new A_OPER("movq    `d0, %rax\n" +
                "   cqto\n" +
                "   idivq   `s0\n" +
                "   movq    %rax, `d0",
                list(dst, RAX, RDX),
                list(src, dst));
    }

    private static Instr A_JMP(Label target) {
        return new A_OPER("jmp     `j0", noTemps, noTemps, List.list(target));
    }

    private static Instr A_LABEL(Label name) {
        return new A_LABEL(name + ":", name);
    }

    private static Instr A_MOV(Temp t, int value) {
        if (value == 0)
            return new A_OPER("xorq    `d0, `d0", list(t), noTemps);
        else
            return new A_OPER("movq    $" + value + ", `d0", list(t), noTemps);
    }

    private static Instr A_MOV(Temp d, Temp s) {
        return new A_MOVE("movq    `s0, `d0", d, s);
    }

    private static Instr A_CMOV(RelOp relOp, Temp d, Temp s) {
        String opCode;
        switch (relOp) {
            case EQ:
                opCode = "cmove ";
                break;
            case NE:
                opCode = "cmovne";
                break;
            case GE:
                opCode = "cmovge";
                break;
            case LT:
                opCode = "cmovl ";
                break;
            case LE:
                opCode = "cmovle";
                break;
            case GT:
                opCode = "cmovg";
                break;
            case ULT:
                opCode = "cmovb";
                break;
            case UGT:
                opCode = "cmova";
                break;
            case ULE:
                opCode = "cmovbe";
                break;
            case UGE:
                opCode = "cmovae";
                break;
            default:
                throw new Error("Missing case?");
        }

        return new A_OPER(opCode + "    `s0, `d0", list(d), list(s, d));
    }

    private static Instr A_MOV(Temp d, Label l) {
        return new A_OPER("leaq    " + l + "(%rip), `d0", list(d), noTemps);
    }

    private static Instr A_MOV_TO_MEM(Temp ptr, Temp s) {
        return new A_OPER("movq    `s1, (`s0)", noTemps, list(ptr, s));
    }

    private static Instr A_MOV_FROM_MEM(Temp d, Temp ptr) {
        return new A_OPER("movq    (`s0), `d0", list(d), list(ptr));
    }

    private static Instr A_SUB(Temp dst, Temp src) {
        return new A_OPER("subq    `s0, `d0",
                list(dst),
                list(src, dst));
    }

    public static void dumpRules() {
        System.out.println("StmMunchers: " + sm);
        System.out.println("ExpMunchers: " + em);
    }
}
