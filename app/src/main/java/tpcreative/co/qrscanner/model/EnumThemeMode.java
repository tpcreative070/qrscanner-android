package tpcreative.co.qrscanner.model;

public enum EnumThemeMode {
    LIGHT(0),
    DARK(1),
    DEFAULT(2);
    int positionOrdinal = 0;
    EnumThemeMode(int ord) {
        this.positionOrdinal = ord;
    }
    public static EnumThemeMode byPosition(int ord) {
        return EnumThemeMode.values()[ord]; // less safe
    }
}
