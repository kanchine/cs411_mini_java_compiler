package ir.interp;

/**
 * A type of word that points to some data in memory.
 */
public abstract class Ptr extends Word {

    public abstract void set(Word newValue);

    public abstract Word get();

    @Override
    public abstract Ptr add(int bytesOffset);

    // TODO
    public boolean isEQ(Word r) {
        if (r.asInt() == 0) {
            return false;
        } else {
            throw new Error("EQ on " + this + " only works to compare to 0, not " + r);
        }
    }

}
