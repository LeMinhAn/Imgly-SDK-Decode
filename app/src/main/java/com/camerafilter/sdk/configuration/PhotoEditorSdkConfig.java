package com.camerafilter.sdk.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import com.camerafilter.R;
import com.camerafilter.sdk.filter.ColorFilterAD1920;
import com.camerafilter.sdk.filter.ColorFilterAncient;
import com.camerafilter.sdk.filter.ColorFilterBW;
import com.camerafilter.sdk.filter.ColorFilterBleached;
import com.camerafilter.sdk.filter.ColorFilterBleachedBlue;
import com.camerafilter.sdk.filter.ColorFilterBlueShadows;
import com.camerafilter.sdk.filter.ColorFilterBlues;
import com.camerafilter.sdk.filter.ColorFilterBreeze;
import com.camerafilter.sdk.filter.ColorFilterCelsius;
import com.camerafilter.sdk.filter.ColorFilterClassic;
import com.camerafilter.sdk.filter.ColorFilterColorful;
import com.camerafilter.sdk.filter.ColorFilterCool;
import com.camerafilter.sdk.filter.ColorFilterCottonCandy;
import com.camerafilter.sdk.filter.ColorFilterCreamy;
import com.camerafilter.sdk.filter.ColorFilterEighties;
import com.camerafilter.sdk.filter.ColorFilterElder;
import com.camerafilter.sdk.filter.ColorFilterEvening;
import com.camerafilter.sdk.filter.ColorFilterFall;
import com.camerafilter.sdk.filter.ColorFilterFixie;
import com.camerafilter.sdk.filter.ColorFilterFood;
import com.camerafilter.sdk.filter.ColorFilterFridge;
import com.camerafilter.sdk.filter.ColorFilterFront;
import com.camerafilter.sdk.filter.ColorFilterGlam;
import com.camerafilter.sdk.filter.ColorFilterHighCarb;
import com.camerafilter.sdk.filter.ColorFilterHighContrast;
import com.camerafilter.sdk.filter.ColorFilterK1;
import com.camerafilter.sdk.filter.ColorFilterK2;
import com.camerafilter.sdk.filter.ColorFilterK6;
import com.camerafilter.sdk.filter.ColorFilterKDynamic;
import com.camerafilter.sdk.filter.ColorFilterKeen;
import com.camerafilter.sdk.filter.ColorFilterLenin;
import com.camerafilter.sdk.filter.ColorFilterLitho;
import com.camerafilter.sdk.filter.ColorFilterLomo;
import com.camerafilter.sdk.filter.ColorFilterLomo100;
import com.camerafilter.sdk.filter.ColorFilterLucid;
import com.camerafilter.sdk.filter.ColorFilterMellow;
import com.camerafilter.sdk.filter.ColorFilterNeat;
import com.camerafilter.sdk.filter.ColorFilterNoGreen;
import com.camerafilter.sdk.filter.ColorFilterOrchid;
import com.camerafilter.sdk.filter.ColorFilterPale;
import com.camerafilter.sdk.filter.ColorFilterPola669;
import com.camerafilter.sdk.filter.ColorFilterPolaSx;
import com.camerafilter.sdk.filter.ColorFilterPro400;
import com.camerafilter.sdk.filter.ColorFilterQuozi;
import com.camerafilter.sdk.filter.ColorFilterSepiahigh;
import com.camerafilter.sdk.filter.ColorFilterSettled;
import com.camerafilter.sdk.filter.ColorFilterSeventies;
import com.camerafilter.sdk.filter.ColorFilterSin;
import com.camerafilter.sdk.filter.ColorFilterSoft;
import com.camerafilter.sdk.filter.ColorFilterSteel;
import com.camerafilter.sdk.filter.ColorFilterSummer;
import com.camerafilter.sdk.filter.ColorFilterSunset;
import com.camerafilter.sdk.filter.ColorFilterTender;
import com.camerafilter.sdk.filter.ColorFilterTexas;
import com.camerafilter.sdk.filter.ColorFilterTwilight;
import com.camerafilter.sdk.filter.ColorFilterWinter;
import com.camerafilter.sdk.filter.ColorFilterX400;
import com.camerafilter.sdk.filter.NoneImageFilter;
import com.camerafilter.sdk.tools.BrushTool;
import com.camerafilter.sdk.tools.ColorMatrixTool;
import com.camerafilter.sdk.tools.CropTool;
import com.camerafilter.sdk.tools.FilterTool;
import com.camerafilter.sdk.tools.FocusTool;
import com.camerafilter.sdk.tools.OrientationTool;
import com.camerafilter.sdk.tools.StickerTool;
import com.camerafilter.sdk.tools.TextTool;
import com.camerafilter.ui.utilities.OrientationSensor.SCREEN_ROTATION_MODE;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PhotoEditorSdkConfig {
    private static final String fontAssetsFolder = "fonts/";

    private static final ArrayList<AbstractConfig.ToolConfigInterface> tools = new ArrayList<>();
    private static final ArrayList<AbstractConfig.FontConfigInterface> fonts = new ArrayList<>();
    private static final ArrayList<AbstractConfig.ImageFilterInterface> filter = new ArrayList<>();
    private static final ArrayList<AbstractConfig.AspectConfigInterface> aspects = new ArrayList<>();
    private static final ArrayList<AbstractConfig.StickerConfigInterface> stickers = new ArrayList<>();
    private static final ArrayList<AbstractConfig.ColorConfigInterface> textColors = new ArrayList<>();
    private static final ArrayList<AbstractConfig.ColorConfigInterface> brushColors = new ArrayList<>();

    private static boolean isForceCropCaptureEnabled;

    private static SCREEN_ROTATION_MODE cameraScreenRotationMode = SCREEN_ROTATION_MODE.SENSOR_ALWAYS;
    private static SCREEN_ROTATION_MODE editorScreenRotationMode = SCREEN_ROTATION_MODE.SENSOR_WHEN_ROTATION_ENABLED;

    @Nullable
    private static CropAspectConfig forcePortraitCrop = null;
    @Nullable
    private static CropAspectConfig forceLandscapeCrop = null;

    static {
        aspects.add(new CropAspectConfig(R.string.imgly_crop_name_custom, R.drawable.imgly_icon_option_crop_custom, CropAspectConfig.CUSTOM_ASPECT));
        aspects.add(new CropAspectConfig(R.string.imgly_crop_name_square, R.drawable.imgly_icon_option_crop_square, 1));
        aspects.add(new CropAspectConfig(R.string.imgly_crop_name_16_9, R.drawable.imgly_icon_option_crop_16_9, 16 / 9f));
        aspects.add(new CropAspectConfig(R.string.imgly_crop_name_4_3, R.drawable.imgly_icon_option_crop_4_3, 4 / 3f));

        filter.add(new NoneImageFilter());
        filter.add(new ColorFilterAD1920());
        filter.add(new ColorFilterAncient());
        filter.add(new ColorFilterBleached());
        filter.add(new ColorFilterBleachedBlue());
        filter.add(new ColorFilterBlues());
        filter.add(new ColorFilterBlueShadows());
        filter.add(new ColorFilterBreeze());
        filter.add(new ColorFilterBW());
        filter.add(new ColorFilterCelsius());
        filter.add(new ColorFilterClassic());
        filter.add(new ColorFilterColorful());
        filter.add(new ColorFilterCool());
        filter.add(new ColorFilterCottonCandy());
        filter.add(new ColorFilterCreamy());
        filter.add(new ColorFilterEighties());
        filter.add(new ColorFilterElder());
        filter.add(new ColorFilterEvening());
        filter.add(new ColorFilterFall());
        filter.add(new ColorFilterFixie());
        filter.add(new ColorFilterFood());
        filter.add(new ColorFilterFridge());
        filter.add(new ColorFilterFront());
        filter.add(new ColorFilterGlam());
        filter.add(new ColorFilterHighCarb());
        filter.add(new ColorFilterHighContrast());
        filter.add(new ColorFilterK1());
        filter.add(new ColorFilterK2());
        filter.add(new ColorFilterK6());
        filter.add(new ColorFilterKDynamic());
        filter.add(new ColorFilterKeen());
        filter.add(new ColorFilterLenin());
        filter.add(new ColorFilterLitho());
        filter.add(new ColorFilterLomo());
        filter.add(new ColorFilterLomo100());
        filter.add(new ColorFilterLucid());
        filter.add(new ColorFilterMellow());
        filter.add(new ColorFilterNeat());
        filter.add(new ColorFilterNoGreen());
        filter.add(new ColorFilterOrchid());
        filter.add(new ColorFilterPale());
        filter.add(new ColorFilterPola669());
        filter.add(new ColorFilterPolaSx());
        filter.add(new ColorFilterPro400());
        filter.add(new ColorFilterQuozi());
        filter.add(new ColorFilterSepiahigh());
        filter.add(new ColorFilterSettled());
        filter.add(new ColorFilterSeventies());
        filter.add(new ColorFilterSin());
        filter.add(new ColorFilterSoft());
        filter.add(new ColorFilterSteel());
        filter.add(new ColorFilterSummer());
        filter.add(new ColorFilterSunset());
        filter.add(new ColorFilterTender());
        filter.add(new ColorFilterTexas());
        filter.add(new ColorFilterTwilight());
        filter.add(new ColorFilterWinter());
        filter.add(new ColorFilterX400());

        fonts.add(new FontConfig("Abramham Lincoln", fontAssetsFolder + "AbrahamLincoln.ttf"));
        fonts.add(new FontConfig("Blanch Condensed", fontAssetsFolder + "BLANCH_CONDENSED.otf"));
        fonts.add(new FontConfig("Geared Slab", fontAssetsFolder + "GearedSlab.ttf"));
        fonts.add(new FontConfig("Libarator", fontAssetsFolder + "Liberator.ttf"));
        fonts.add(new FontConfig("Sullivan", fontAssetsFolder + "Sullivan-Regular.otf"));
        fonts.add(new FontConfig("Cubano", fontAssetsFolder + "cubano-regular-webfont.ttf"));
        fonts.add(new FontConfig("Vevey", fontAssetsFolder + "vevey.ttf"));
        fonts.add(new FontConfig("Airship", fontAssetsFolder + "Airship 27-Regular.ttf"));
        fonts.add(new FontConfig("Bender", fontAssetsFolder + "Bender-Inline.otf"));
        fonts.add(new FontConfig("Haymaker", fontAssetsFolder + "Haymaker.ttf"));
        fonts.add(new FontConfig("Maven Pro", fontAssetsFolder + "MavenProLight-200.otf"));
        fonts.add(new FontConfig("Tommaso", fontAssetsFolder + "Tommaso.ttf"));
        fonts.add(new FontConfig("Governor", fontAssetsFolder + "governor.ttf"));
        fonts.add(new FontConfig("Arvil Sans", fontAssetsFolder + "Arvil_Sans.ttf"));
        fonts.add(new FontConfig("Franchise", fontAssetsFolder + "Franchise-Bold.ttf"));
        fonts.add(new FontConfig("Homestead", fontAssetsFolder + "Homestead-Regular.ttf"));
        fonts.add(new FontConfig("Muncie", fontAssetsFolder + "Muncie.ttf"));
        fonts.add(new FontConfig("Valencia", fontAssetsFolder + "ValenciaRegular.otf"));
        fonts.add(new FontConfig("Mensch", fontAssetsFolder + "mensch.ttf"));

        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_glasses_normal, R.drawable.imgly_sticker_preview_glasses_normal, R.raw.imgly_sticker_glasses_normal));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_glasses_nerd, R.drawable.imgly_sticker_preview_glasses_nerd, R.raw.imgly_sticker_glasses_nerd));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_glasses_shutter_green, R.drawable.imgly_sticker_preview_glasses_shutter_green, R.raw.imgly_sticker_glasses_shutter_green));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_glasses_shutter_yellow, R.drawable.imgly_sticker_preview_glasses_shutter_yellow, R.raw.imgly_sticker_glasses_shutter_yellow));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_glasses_sun, R.drawable.imgly_sticker_preview_glasses_sun, R.raw.imgly_sticker_glasses_sun));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_hat_cap, R.drawable.imgly_sticker_preview_hat_cap, R.raw.imgly_sticker_hat_cap));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_hat_sherrif, R.drawable.imgly_sticker_preview_hat_sherrif, R.raw.imgly_sticker_hat_sherrif));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_hat_party, R.drawable.imgly_sticker_preview_hat_party, R.raw.imgly_sticker_hat_party));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_hat_zylinder, R.drawable.imgly_sticker_preview_hat_zylinder, R.raw.imgly_sticker_hat_zylinder));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_mustache1, R.drawable.imgly_sticker_preview_mustache1, R.raw.imgly_sticker_mustache1));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_mustache2, R.drawable.imgly_sticker_preview_mustache2, R.raw.imgly_sticker_mustache2));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_mustache3, R.drawable.imgly_sticker_preview_mustache3, R.raw.imgly_sticker_mustache3));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_mustache_long, R.drawable.imgly_sticker_preview_mustache_long, R.raw.imgly_sticker_mustache_long));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_snowflake, R.drawable.imgly_sticker_preview_snowflake, R.raw.imgly_sticker_snowflake));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_heart, R.drawable.imgly_sticker_preview_heart, R.raw.imgly_sticker_heart));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_pipe, R.drawable.imgly_sticker_preview_pipe, R.raw.imgly_sticker_pipe));
        stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_star, R.drawable.imgly_sticker_preview_star, R.raw.imgly_sticker_star));

        tools.add(new CropTool(R.string.imgly_tool_name_crop, R.drawable.imgly_icon_tool_crop));
        tools.add(new OrientationTool(R.string.imgly_tool_name_orientation, R.drawable.imgly_icon_tool_orientation));
        tools.add(new Divider());
        tools.add(new FilterTool(R.string.imgly_tool_name_filter, R.drawable.imgly_icon_tool_filters));
        tools.add(new ColorMatrixTool(R.string.imgly_tool_name_adjust, R.drawable.imgly_icon_tool_adjust));
        tools.add(new Divider());
        tools.add(new TextTool(R.string.imgly_tool_name_text, R.drawable.imgly_icon_tool_text));
        tools.add(new StickerTool(R.string.imgly_tool_name_sticker, R.drawable.imgly_icon_tool_sticker));
        tools.add(new Divider());
        tools.add(new FocusTool(R.string.imgly_tool_name_focus, R.drawable.imgly_icon_tool_focus));
        tools.add(new Divider());
        //------------------
        tools.add(new BrushTool(R.string.imgly_tool_name_brush, R.drawable.imgly_icon_tool_focus));

        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xFFFFFF));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x7E7E7E));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x000000));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x6784FF));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x8B60FF));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xE161FF));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xFF64CE));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xFF6688));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xE74E49));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xF4744D));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xFFCD62));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0xC8FF5F));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x7EFF60));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x3FFF84));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x42FFDC));
        textColors.add(new ColorConfig(R.string.imgly_tool_name_text_color, 0x30E5E7));

        brushColors.addAll(textColors);
    }

    /**
     * Change Fontset
     * <p>
     * <pre>
     *  // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.FontConfigInterface&gt; fonts = PhotoEditorSdkConfig.getCropConfig();
     *
     * // Step2 optional clear it.
     * fonts.clear();
     *
     * // Step3 add the needed fonts
     * fonts.add(new FontConfig("Geared Slab", fontAssetsFolder + "GearedSlab.ttf"));
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.FontConfigInterface> getFontConfig() {
        return fonts;
    }

    /**
     * Change Filterset
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.ImageFilterInterface&gt; filter = PhotoEditorSdkConfig.getFilterConfig();
     *
     * // Step2 clear it.
     * filter.clear();
     *
     * // Step3 add needed filters
     * filter.add(new NoneImageFilter());
     * filter.add(new ColorFilterAD1920());
     * filter.add(new ColorFilterAncient());
     * filter.add(new ColorFilterBleached());
     * filter.add(new ColorFilterBleachedBlue());
     * filter.add(new ColorFilterBlues());
     * filter.add(new ColorFilterBlueShadows());
     * filter.add(new ColorFilterBreeze());
     * filter.add(new ColorFilterBW());
     * filter.add(new ColorFilterCelsius());
     * filter.add(new ColorFilterClassic());
     * filter.add(new ColorFilterColorful());
     * filter.add(new ColorFilterCool());
     * filter.add(new ColorFilterCottonCandy());
     * filter.add(new ColorFilterCreamy());
     * filter.add(new ColorFilterEighties());
     * filter.add(new ColorFilterElder());
     * filter.add(new ColorFilterEvening());
     * filter.add(new ColorFilterFall());
     * filter.add(new ColorFilterFixie());
     * filter.add(new ColorFilterFood());
     * filter.add(new ColorFilterFridge());
     * filter.add(new ColorFilterFront());
     * filter.add(new ColorFilterGlam());
     * filter.add(new ColorFilterHighCarb());
     * filter.add(new ColorFilterHighContrast());
     * filter.add(new ColorFilterK1());
     * filter.add(new ColorFilterK2());
     * filter.add(new ColorFilterK6());
     * filter.add(new ColorFilterKDynamic());
     * filter.add(new ColorFilterKeen());
     * filter.add(new ColorFilterLenin());
     * filter.add(new ColorFilterLitho());
     * filter.add(new ColorFilterLomo());
     * filter.add(new ColorFilterLomo100());
     * filter.add(new ColorFilterLucid());
     * filter.add(new ColorFilterMellow());
     * filter.add(new ColorFilterNeat());
     * filter.add(new ColorFilterNoGreen());
     * filter.add(new ColorFilterOrchid());
     * filter.add(new ColorFilterPale());
     * filter.add(new ColorFilterPola669());
     * filter.add(new ColorFilterPolaSx());
     * filter.add(new ColorFilterPro400());
     * filter.add(new ColorFilterQuozi());
     * filter.add(new ColorFilterSepiahigh());
     * filter.add(new ColorFilterSettled());
     * filter.add(new ColorFilterSeventies());
     * filter.add(new ColorFilterSin());
     * filter.add(new ColorFilterSoft());
     * filter.add(new ColorFilterSteel());
     * filter.add(new ColorFilterSummer());
     * filter.add(new ColorFilterSunset());
     * filter.add(new ColorFilterTender());
     * filter.add(new ColorFilterTexas());
     * filter.add(new ColorFilterTwilight());
     * filter.add(new ColorFilterWinter());
     * filter.add(new ColorFilterX400());
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.ImageFilterInterface> getFilterConfig() {
        if (filter.size() == 0) {
            filter.add(new NoneImageFilter());
        }

        return filter;
    }

    /**
     * Change Toolset
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.ToolConfigInterface&gt; tools = PhotoEditorSdkConfig.getTools()
     *
     * // Step2 clear it.
     *
     * tools.clear();
     *
     * // Step 3 add the Tools
     *
     * //Avalible Tools
     *
     * tools.add(new CropTool(R.string.imgly_tool_name_crop, R.drawable.imgly_icon_option_crop));
     * tools.add(new TextTool(R.string.imgly_tool_name_text, R.drawable.imgly_icon_option_text));
     * tools.add(new FilterTool(R.string.imgly_tool_name_filter, R.drawable.imgly_icon_option_filters));
     * tools.add(new OrientationTool(R.string.imgly_tool_name_orientation, R.drawable.imgly_icon_option_orientation));
     * tools.add(new StickerTool(R.string.imgly_tool_name_sticker, R.drawable.imgly_icon_option_sticker));
     * tools.add(new ColorMatrixTool.Contrast(R.string.imgly_tool_name_contrast, R.drawable.imgly_icon_option_contrast));
     * tools.add(new ColorMatrixTool.Brightness(R.string.imgly_tool_name_brightness, R.drawable.imgly_icon_option_brightness));
     * tools.add(new ColorMatrixTool.Saturation(R.string.imgly_tool_name_saturation, R.drawable.imgly_icon_option_saturation));
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.ToolConfigInterface> getTools() {
        return tools;
    }

    /**
     * Change Crop Config
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.AspectConfigInterface&gt; cropConfig = PhotoEditorSdkConfig.getCropConfig();
     *
     * // Step2 clear it.
     * cropConfig.clear();
     *
     * // Step3 add the needed crops
     * cropConfig.add(new CropAspectConfig(R.string.imgly_crop_name_custom, R.drawable.imgly_icon_crop_custom, -1));
     * cropConfig.add(new CropAspectConfig(R.string.imgly_crop_name_4_3, R.drawable.imgly_icon_crop_4_3, 4/3));
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.AspectConfigInterface> getCropConfig() {

        int addPos = aspects.size() != 0 ? aspects.get(0).getAspect() == CropAspectConfig.CUSTOM_ASPECT ? 1 : 0 : 0;

        if (forcePortraitCrop != null && !aspects.contains(forcePortraitCrop)) {
            aspects.add(addPos, forcePortraitCrop);
        }

        if (forceLandscapeCrop != null && !aspects.contains(forceLandscapeCrop)) {
            aspects.add(addPos, forceLandscapeCrop);
        }

        return aspects;
    }

    /**
     * Add a or remove Sticker
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.StickerConfigInterface&gt; stickers = PhotoEditorSdkConfig.getStickerConfig();
     *
     * // Step2 optional clear it.
     * stickers.clear();
     *
     * // Step3 add the needed fonts
     * stickers.add(new ImageStickerConfig(R.string.imgly_sticker_name_glasses_normal, R.drawable.imgly_sticker_preview_glasses_normal, R.raw.imgly_sticker_glasses_normal));​
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.StickerConfigInterface> getStickerConfig() {
        return stickers;
    }

    @SuppressWarnings("unused")
    public static void setForcedCropMode(boolean forceCropCapture, CropAspectConfig portraitCrop, CropAspectConfig landscapeCrop) {
        PhotoEditorSdkConfig.isForceCropCaptureEnabled = forceCropCapture;
        PhotoEditorSdkConfig.forcePortraitCrop = portraitCrop;
        PhotoEditorSdkConfig.forceLandscapeCrop = landscapeCrop;
    }

    @Nullable
    public static CropAspectConfig getForceLandscapeCrop() {
        return forceLandscapeCrop;
    }

    @Nullable
    public static CropAspectConfig getForcePortraitCrop() {
        return forcePortraitCrop;
    }

    public static boolean isForceCropCaptureEnabled() {
        return isForceCropCaptureEnabled;
    }


    /**
     * @deprecated Please use {@link #getTextColorConfig()} instead. Will be removed in the next big version update.
     */
    @NonNull
    @Deprecated
    public static ArrayList<AbstractConfig.ColorConfigInterface> getColorConfig() { // TODO: Remove in next version.
        Log.e("deprecated", "Deprecation warning getColorConfig() will be removed in the next version, please use getTextColorConfig()");
        return textColors;
    }

    /**
     * Add a or remove text colors
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.ColorConfigInterface&gt; stickers = PhotoEditorSdkConfig.getTextColorConfig();
     *
     * // Step2 optional clear it.
     * stickers.clear();
     *
     * // Step3 add the needed textColors
     * textColors.add(new ColorConfig(R.string.imgly_color_accessibility_name, 0xF4744D));
     * </pre>
     */
    @NonNull
    public static ArrayList<AbstractConfig.ColorConfigInterface> getTextColorConfig() {
        return textColors;
    }

    /**
     * Add a or remove  brush colors @see #getTextColorConfig()
     * <p>
     * <pre>
     * // Step1 get current configuration.
     * ArrayList&lt;AbstractConfig.ColorConfigInterface&gt; stickers = PhotoEditorSdkConfig.getTextColorConfig();
     *
     * // Step2 optional clear it.
     * stickers.clear();
     *
     * // Step3 add the needed textColors
     * textColors.add(new ColorConfig(R.string.imgly_color_accessibility_name, 0xF4744D));
     * </pre>
     */
    public static ArrayList<AbstractConfig.ColorConfigInterface> getBrushColors() {
        return brushColors;
    }

    /**
     * Set screen rotation mode in editor mode.
     * Default: #SCREEN_ROTATION_MODE.SENSOR_WHEN_ROTATION_ENABLED
     * @param mode desired screen mode
     */
    public static void setEditorScreenRotationMode(SCREEN_ROTATION_MODE mode) {
        PhotoEditorSdkConfig.editorScreenRotationMode = mode;
    }

    /**
     * Set screen rotation mode in camera mode.
     * Default: #SCREEN_ROTATION_MODE.SENSOR_ALWAYS
     * @param mode desired screen mode
     */
    public static void setCameraScreenRotationMode(SCREEN_ROTATION_MODE mode) {
        PhotoEditorSdkConfig.cameraScreenRotationMode = mode;
    }

    public static SCREEN_ROTATION_MODE getCameraScreenRotationMode() {
        return cameraScreenRotationMode;
    }

    public static SCREEN_ROTATION_MODE getEditorScreenRotationMode() {
        return editorScreenRotationMode;
    }
}
