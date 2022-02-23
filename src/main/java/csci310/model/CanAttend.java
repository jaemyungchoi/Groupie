package csci310.model;

public enum CanAttend {
    NONE(0), // empty value
    YES(1),
    NO(2),
    MAYBE(3);

    private final int code;

    CanAttend(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    static public CanAttend fromInt(int code) {
        switch (code) {
            case 0:
                return NONE;
            case 1:
                return YES;
            case 2:
                return NO;
            case 3:
                return MAYBE;
        }
        return null;
    }
}
