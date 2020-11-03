package com.bingo.sdk.string2pic;

/**
 * @author #Suyghur.
 * @date 2020/6/9
 */
public class BlockUtils {

    /**
     * IHDR标志位
     */
    private static final String IHDR_FLAG = "49484452";

    /**
     * PLTE标志位
     */
    private static final String PLTE_FLAG = "504C5445";

    /**
     * IDAT标志位
     */
    private static final String IDAT_FLAG = "49444154";

    /**
     * IEND标志位
     */
    private static final String IEND_FLAG = "49454E44";

    /**
     * sRGB标志位
     */
    private static final String SRGB_FLAG = "73524742";

    /**
     * tEXt标志位
     */
    private static final String TEXT_FLAG = "74455874";

    /**
     * PHYS标志位
     */
    private static final String PHYS_FLAG = "70485973";

    /**
     * tRNS标志位
     */
    private static final String TRNS_FLAG = "74524E53";

    public static boolean isIHDRBlock(String hexCode) {
        return IHDR_FLAG.equals(hexCode);
    }

    public static boolean isPLTEBlock(String hexCode) {
        return PLTE_FLAG.equals(hexCode);
    }

    public static boolean isIDATBlock(String hexCode) {
        return IDAT_FLAG.equals(hexCode);
    }

    public static boolean isIENDBlock(String hexCode) {
        return IEND_FLAG.equals(hexCode);
    }

    public static boolean isSRGBBlock(String hexCode) {
        return SRGB_FLAG.equals(hexCode);
    }

    public static boolean isTEXTBlock(String hexCode) {
        return TEXT_FLAG.equals(hexCode);
    }

    public static boolean isPHYSBlock(String hexCode) {
        return PHYS_FLAG.equals(hexCode);
    }

    public static boolean isTRNSBlock(String hexCode) {
        return TRNS_FLAG.equals(hexCode);
    }


}
