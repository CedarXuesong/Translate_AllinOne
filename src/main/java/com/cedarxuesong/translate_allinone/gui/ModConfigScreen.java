package com.cedarxuesong.translate_allinone.gui;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.registration.ConfigManager;
import com.cedarxuesong.translate_allinone.gui.configui.controls.ActionBlock;
import com.cedarxuesong.translate_allinone.gui.configui.controls.ActionBlockRegistry;
import com.cedarxuesong.translate_allinone.gui.configui.controls.CheckboxBlock;
import com.cedarxuesong.translate_allinone.gui.configui.controls.GroupBox;
import com.cedarxuesong.translate_allinone.gui.configui.controls.IntSliderBlock;
import com.cedarxuesong.translate_allinone.gui.configui.interaction.ConfigUiInteractionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.interaction.ConfigUiModalInteractionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.model.ConfigSection;
import com.cedarxuesong.translate_allinone.gui.configui.model.FocusTarget;
import com.cedarxuesong.translate_allinone.gui.configui.model.RouteSlot;
import com.cedarxuesong.translate_allinone.gui.configui.model.UiRect;
import com.cedarxuesong.translate_allinone.gui.configui.modals.AddProviderModalSupport;
import com.cedarxuesong.translate_allinone.gui.configui.modals.CustomParametersModalSupport;
import com.cedarxuesong.translate_allinone.gui.configui.modals.ModelSettingsModalSupport;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiControlRenderer;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiDraw;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiModalSupport;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiScreenRenderSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.ConfigChromeActionsSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.ConfigSectionContentSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.ProviderDetailSectionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.ProviderListSectionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.ProviderManagerSectionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.RouteModelSectionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.sections.RouteModelSelectorSectionSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ConfigUiFocusSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ConfigUiRuntimeSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ConfigUiTextFieldSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.CustomParameterTreeSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ModelSettingsApplySupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ModelSettingsDraftSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ModelSettingsMutationSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderManagerMutationSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderProfileSupport;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;
import com.cedarxuesong.translate_allinone.utils.config.pojos.InputBindingConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ModConfigScreen extends Screen {
    private static final String I18N_PREFIX = "text.translate_allinone.configscreen.";

    private static final int COLOR_BG = 0xFF0C0C0C;
    private static final int COLOR_TOP_BAR = 0xFF151515;
    private static final int COLOR_LEFT_PANEL = 0xFF131313;
    private static final int COLOR_MAIN_PANEL = 0xFF101010;
    private static final int COLOR_BORDER = 0xFF3A3A3A;
    private static final int COLOR_BLOCK = 0xFF1A1A1A;
    private static final int COLOR_BLOCK_HOVER = 0xFF242424;
    private static final int COLOR_BLOCK_MUTED = 0xFF141414;
    private static final int COLOR_BLOCK_SELECTED = 0xFF214936;
    private static final int COLOR_BLOCK_SELECTED_HOVER = 0xFF295B43;
    private static final int COLOR_BLOCK_ACCENT = 0xFF2B6A4C;
    private static final int COLOR_BLOCK_ACCENT_HOVER = 0xFF337B59;
    private static final int COLOR_BLOCK_DANGER = 0xFF3C1C1C;
    private static final int COLOR_BLOCK_DANGER_HOVER = 0xFF4B2525;
    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_TEXT_MUTED = 0xFF9A9A9A;
    private static final int COLOR_TEXT_ACCENT = 0xFF9CF0C0;
    private static final int COLOR_STATUS_OK = 0xFF59D185;
    private static final int COLOR_STATUS_ERROR = 0xFFFF6A6A;
    private static final int COLOR_MODAL_OVERLAY = 0x98000000;
    private static final int COLOR_SLIDER_TRACK = 0xFF0E0E0E;
    private static final int COLOR_SLIDER_FILL = 0xFF276749;
    private static final int COLOR_SLIDER_FILL_ACTIVE = 0xFF359362;
    private static final int COLOR_SLIDER_KNOB = 0xFFE3E3E3;
    private static final int COLOR_SLIDER_KNOB_ACTIVE = 0xFFA6F4C7;
    private static final int COLOR_CHECKBOX_OFF = 0xFF0F0F0F;
    private static final int COLOR_CHECKBOX_ON = 0xFF2B6A4C;
    private static final int COLOR_CHECKBOX_BORDER_ON = 0xFF9CF0C0;
    private static final int COLOR_GROUP_BG = 0x66141414;
    private static final int COLOR_GROUP_BORDER = 0xFF2C2C2C;
    private static final int COLOR_GROUP_TITLE = 0xFFB5B5B5;
    private static final int COLOR_SCROLL_TRACK = 0xAA171717;
    private static final int COLOR_SCROLL_THUMB = 0xFF3A3A3A;
    private static final int COLOR_SCROLL_THUMB_HOVER = 0xFF4A4A4A;
    private static final int SLIDER_BLOCK_HEIGHT = 26;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_MIN_THUMB_HEIGHT = 26;
    private static final int SCROLL_STEP = 28;
    private static final double SCROLL_ANIMATION_SPEED = 15.0;
    private static final double OVER_SCROLL_RESISTANCE = 0.42;
    private static final double OVER_SCROLL_MAX = 56.0;
    private static final double OVER_SCROLL_SPRING = 58.0;
    private static final double OVER_SCROLL_DAMPING = 12.0;
    private static final double OVER_SCROLL_WHEEL_OFFSET_FACTOR = 0.2;
    private static final double OVER_SCROLL_WHEEL_VELOCITY_FACTOR = 9.0;
    private static final int CONTENT_CLIP_SIDE_PADDING = 6;
    private static final int CONTENT_CLIP_TOP_PADDING = 6;
    private static final int CONTENT_CLIP_BOTTOM_PADDING = 8;
    private static final IntSliderBlock.Style SLIDER_STYLE = new IntSliderBlock.Style(
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BORDER,
            COLOR_TEXT,
            COLOR_TEXT_ACCENT,
            COLOR_TEXT_MUTED,
            COLOR_BLOCK_ACCENT,
            COLOR_SLIDER_TRACK,
            COLOR_SLIDER_FILL,
            COLOR_SLIDER_FILL_ACTIVE,
            COLOR_SLIDER_KNOB,
            COLOR_SLIDER_KNOB_ACTIVE
    );
    private static final CheckboxBlock.Style CHECKBOX_STYLE = new CheckboxBlock.Style(
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_SELECTED,
            COLOR_BLOCK_SELECTED_HOVER,
            COLOR_BORDER,
            COLOR_TEXT,
            COLOR_TEXT_ACCENT,
            COLOR_CHECKBOX_OFF,
            COLOR_CHECKBOX_ON,
            COLOR_BORDER,
            COLOR_CHECKBOX_BORDER_ON
    );
    private static final GroupBox.Style GROUP_BOX_STYLE = new GroupBox.Style(
            COLOR_GROUP_BG,
            COLOR_GROUP_BORDER,
            COLOR_GROUP_TITLE,
            COLOR_MAIN_PANEL
    );
    private static final ProviderListSectionSupport.Style PROVIDER_LIST_STYLE = new ProviderListSectionSupport.Style(
            COLOR_BLOCK_MUTED,
            COLOR_TEXT,
            COLOR_TEXT_MUTED,
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_SELECTED,
            COLOR_BLOCK_SELECTED_HOVER,
            COLOR_TEXT_ACCENT,
            COLOR_BLOCK_ACCENT,
            COLOR_BLOCK_ACCENT_HOVER
    );
    private static final ProviderDetailSectionSupport.Style PROVIDER_DETAIL_STYLE = new ProviderDetailSectionSupport.Style(
            COLOR_BLOCK_MUTED,
            COLOR_TEXT,
            COLOR_TEXT_MUTED,
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_SELECTED,
            COLOR_BLOCK_SELECTED_HOVER,
            COLOR_TEXT_ACCENT,
            COLOR_BLOCK_DANGER,
            COLOR_BLOCK_DANGER_HOVER,
            COLOR_BLOCK_ACCENT,
            COLOR_BLOCK_ACCENT_HOVER
    );
    private static final AddProviderModalSupport.Style ADD_PROVIDER_MODAL_STYLE = new AddProviderModalSupport.Style(
            COLOR_BLOCK_MUTED,
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_ACCENT,
            COLOR_BLOCK_ACCENT_HOVER,
            COLOR_TEXT,
            COLOR_TEXT_ACCENT
    );
    private static final ModelSettingsModalSupport.Style MODEL_SETTINGS_MODAL_STYLE = new ModelSettingsModalSupport.Style(
            COLOR_BLOCK_MUTED,
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_SELECTED,
            COLOR_BLOCK_SELECTED_HOVER,
            COLOR_BLOCK_ACCENT,
            COLOR_BLOCK_ACCENT_HOVER,
            COLOR_TEXT
    );
    private static final CustomParametersModalSupport.Style CUSTOM_PARAMETERS_MODAL_STYLE = new CustomParametersModalSupport.Style(
            COLOR_BLOCK_MUTED,
            COLOR_TEXT,
            COLOR_TEXT_MUTED,
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_SELECTED,
            COLOR_BLOCK_SELECTED_HOVER,
            COLOR_TEXT_ACCENT,
            COLOR_BLOCK_ACCENT,
            COLOR_BLOCK_ACCENT_HOVER,
            COLOR_BLOCK_DANGER,
            COLOR_BLOCK_DANGER_HOVER
    );
    private static final RouteModelSelectorSectionSupport.Style ROUTE_MODEL_SELECTOR_STYLE = new RouteModelSelectorSectionSupport.Style(
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_BLOCK_ACCENT,
            COLOR_BLOCK_ACCENT_HOVER,
            COLOR_TEXT,
            COLOR_TEXT_ACCENT
    );
    private static final ConfigUiScreenRenderSupport.Style SCREEN_RENDER_STYLE = new ConfigUiScreenRenderSupport.Style(
            COLOR_BG,
            COLOR_TOP_BAR,
            COLOR_LEFT_PANEL,
            COLOR_MAIN_PANEL,
            COLOR_BORDER,
            COLOR_TEXT,
            COLOR_MODAL_OVERLAY
    );
    private static final ConfigChromeActionsSupport.Style CHROME_ACTIONS_STYLE = new ConfigChromeActionsSupport.Style(
            COLOR_BLOCK,
            COLOR_BLOCK_HOVER,
            COLOR_TEXT,
            COLOR_BLOCK_DANGER,
            COLOR_BLOCK_DANGER_HOVER
    );

    private static final int TOP_BAR_HEIGHT = 40;
    private static final int LEFT_PANEL_WIDTH = 200;

    private final Screen parent;
    private final ModConfig originalConfigSnapshot;
    private final List<ActionBlock> actionBlocks = new ArrayList<>();
    private final List<ActionBlock> contentActionBlocks = new ArrayList<>();
    private final List<ActionBlock> floatingActionBlocks = new ArrayList<>();
    private final List<GroupBox> groupBoxes = new ArrayList<>();
    private final List<CheckboxBlock> checkboxBlocks = new ArrayList<>();
    private final List<CheckboxBlock> floatingCheckboxBlocks = new ArrayList<>();
    private final ActionBlockRegistry actionBlockRegistry = new ActionBlockRegistry(actionBlocks, COLOR_BLOCK, COLOR_BLOCK_HOVER, COLOR_TEXT);
    private final ActionBlockRegistry contentActionBlockRegistry = new ActionBlockRegistry(contentActionBlocks, COLOR_BLOCK, COLOR_BLOCK_HOVER, COLOR_TEXT);
    private final ActionBlockRegistry floatingActionBlockRegistry = new ActionBlockRegistry(floatingActionBlocks, COLOR_BLOCK, COLOR_BLOCK_HOVER, COLOR_TEXT);
    private final List<IntSliderBlock> sliderBlocks = new ArrayList<>();
    private final List<TextFieldWidget> providerEditorFields = new ArrayList<>();
    private final List<TextFieldWidget> floatingEditorFields = new ArrayList<>();
    private IntSliderBlock draggingSlider;
    private long sliderAnimationLastNanos = System.nanoTime();

    private ConfigSection selectedSection;
    private int selectedProviderIndex;
    private String selectedProviderId = "";
    private boolean providerApiKeyVisible;
    private String providerSearchQuery = "";
    private RouteSlot routeDropdownSlot;

    private TextFieldWidget providerSearchField;
    private TextFieldWidget addProviderNameField;
    private TextFieldWidget modelSettingsField;

    private boolean addProviderModalOpen;
    private String addProviderNameDraft = "";
    private ApiProviderType addProviderTypeDraft = ApiProviderType.OPENAI_COMPAT;
    private boolean addProviderTypeDropdownOpen;

    private boolean modelSettingsModalOpen;
    private String modelSettingsProviderId = "";
    private String modelSettingsOriginalId = "";
    private String modelSettingsDraft = "";
    private String modelSettingsTemperatureDraft = "";
    private String modelSettingsKeepAliveDraft = "";
    private boolean modelSettingsSupportsSystemDraft;
    private boolean modelSettingsInjectPromptIntoUserDraft = true;
    private boolean modelSettingsStructuredOutputDraft;
    private String modelSettingsSystemPromptSuffixDraft = "";
    private List<CustomParameterEntry> modelSettingsCustomParametersDraft = new ArrayList<>();
    private List<CustomParameterEntry> customParametersBackup = new ArrayList<>();
    private boolean customParametersModalOpen;
    private boolean resetConfirmModalOpen;
    private String selectedCustomParameterPath = "";
    private TextFieldWidget customParameterNameField;
    private TextFieldWidget customParameterValueField;
    private boolean modelSettingsSetDefault;

    private FocusTarget pendingFocusTarget = FocusTarget.NONE;
    private ConfigSectionContentSupport.HotkeyTarget hotkeyCaptureTarget;
    private UiRect contentViewport = new UiRect(0, 0, 0, 0);
    private final int[] sectionScrollOffsets = new int[ConfigSection.values().length];
    private int contentScrollOffset;
    private double contentVisualOffset;
    private int contentScrollMaxOffset;
    private double contentElasticOffset;
    private double contentElasticVelocity;
    private boolean draggingContentScrollbar;
    private boolean draggingContentByMouse;
    private int contentDragStartOffset;
    private double contentDragStartMouseY;

    private Text statusMessage = Text.empty();
    private int statusColor = COLOR_STATUS_OK;
    private long statusExpireAtMillis;
    private boolean restoreSnapshotOnClose = true;

    public ModConfigScreen(Screen parent) {
        this(parent, ConfigSection.CHAT_OUTPUT);
    }

    private ModConfigScreen(Screen parent, ConfigSection selectedSection) {
        super(t("title"));
        this.parent = parent;
        this.originalConfigSnapshot = ConfigManager.copyCurrentConfig();
        this.selectedSection = selectedSection;
    }

    private static Text t(String key, Object... args) {
        return Text.translatable(I18N_PREFIX + key, args);
    }

    @Override
    protected void init() {
        rebuildActionBlocks();
    }

    private void rebuildActionBlocks() {
        rebuildActionBlocks(FocusTarget.NONE);
    }

    private void rebuildActionBlocks(FocusTarget focusTarget) {
        clearChildren();
        actionBlocks.clear();
        contentActionBlocks.clear();
        floatingActionBlocks.clear();
        groupBoxes.clear();
        checkboxBlocks.clear();
        floatingCheckboxBlocks.clear();
        sliderBlocks.clear();
        draggingSlider = null;
        sliderAnimationLastNanos = System.nanoTime();
        providerEditorFields.clear();
        floatingEditorFields.clear();
        providerSearchField = null;
        addProviderNameField = null;
        modelSettingsField = null;
        customParameterNameField = null;
        customParameterValueField = null;
        pendingFocusTarget = focusTarget;
        contentViewport = computeContentViewport();
        contentScrollOffset = Math.max(0, sectionScrollOffsets[selectedSection.ordinal()]);
        if (!Double.isFinite(contentVisualOffset)) {
            contentVisualOffset = contentScrollOffset;
        }

        ConfigChromeActionsSupport.renderTopBarActions(
                this.width,
                actionBlockRegistry::add,
                ModConfigScreen::t,
                this::openResetConfirmation,
                this::cancelAndClose,
                this::saveAndClose,
                CHROME_ACTIONS_STYLE
        );

        ConfigChromeActionsSupport.renderSectionActions(
                selectedSection,
                TOP_BAR_HEIGHT,
                LEFT_PANEL_WIDTH,
                actionBlockRegistry::add,
                ModConfigScreen::t,
                section -> {
                    if (selectedSection != section) {
                        sectionScrollOffsets[selectedSection.ordinal()] = contentScrollOffset;
                        hotkeyCaptureTarget = null;
                        selectedSection = section;
                        contentScrollOffset = Math.max(0, sectionScrollOffsets[selectedSection.ordinal()]);
                        contentVisualOffset = contentScrollOffset;
                        contentElasticOffset = 0.0;
                        contentElasticVelocity = 0.0;
                        addProviderTypeDropdownOpen = false;
                        routeDropdownSlot = null;
                        rebuildActionBlocks();
                    }
                },
                CHROME_ACTIONS_STYLE
        );

        addSectionSpecificActions();
        if (resetConfirmModalOpen) {
            addResetConfirmModal();
        }
        ConfigUiFocusSupport.applyPendingFocus(
                this,
                pendingFocusTarget,
                providerSearchField,
                addProviderNameField,
                modelSettingsField,
                customParameterNameField
        );
        pendingFocusTarget = FocusTarget.NONE;
    }

    private void addSectionSpecificActions() {
        ModConfig config = Translate_AllinOne.getConfig();
        config.providerManager.ensureDefaults();

        int x = LEFT_PANEL_WIDTH + 20;
        int y = TOP_BAR_HEIGHT + 28;
        int width = this.width - x - 16;
        int contentStartY = y - currentRenderScrollOffset();

        int contentBottomY = ConfigSectionContentSupport.render(
                selectedSection,
                config,
                x,
                contentStartY,
                width,
                contentViewport.height,
                ModConfigScreen::t,
                this::addGroupBox,
                this::addToggleAction,
                this::addIntSliderAction,
                this::addActionRow,
                this::addTextInputRow,
                this::hotkeyBindingLabel,
                this::startHotkeyCapture,
                this::clearHotkeyBinding,
                this::cycleHotkeyMode,
                this::addRouteModelSelector,
                this::addProviderManagerActions
        );

        updateScrollBounds(contentBottomY);
    }

    private UiRect computeContentViewport() {
        int x = LEFT_PANEL_WIDTH + 20;
        int y = TOP_BAR_HEIGHT + 28;
        int width = Math.max(0, this.width - x - 16);
        int height = Math.max(0, this.height - y - 12);
        return new UiRect(x, y, width, height);
    }

    private UiRect contentClipViewport() {
        if (contentViewport.width <= 0 || contentViewport.height <= 0) {
            return contentViewport;
        }

        int clipX = Math.max(LEFT_PANEL_WIDTH, contentViewport.x - CONTENT_CLIP_SIDE_PADDING);
        int clipY = Math.max(TOP_BAR_HEIGHT, contentViewport.y - CONTENT_CLIP_TOP_PADDING);
        int clipRight = Math.min(this.width, contentViewport.right() + CONTENT_CLIP_SIDE_PADDING);
        int clipBottom = Math.min(this.height, contentViewport.bottom() + CONTENT_CLIP_BOTTOM_PADDING);
        return new UiRect(
                clipX,
                clipY,
                Math.max(0, clipRight - clipX),
                Math.max(0, clipBottom - clipY)
        );
    }

    private int currentRenderScrollOffset() {
        return (int) Math.round(contentVisualOffset - contentElasticOffset);
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateScrollBounds(int contentBottomY) {
        double naturalBottomY = contentBottomY + currentRenderScrollOffset();
        int viewportBottom = contentViewport.bottom();
        contentScrollMaxOffset = Math.max(0, (int) Math.ceil(naturalBottomY - viewportBottom));
        int clamped = clampScrollOffset(contentScrollOffset);
        if (clamped != contentScrollOffset) {
            contentScrollOffset = clamped;
            sectionScrollOffsets[selectedSection.ordinal()] = clamped;
        }
    }

    private int clampScrollOffset(int value) {
        return Math.max(0, Math.min(contentScrollMaxOffset, value));
    }

    private boolean tryScrollBy(int delta) {
        if (delta == 0) {
            return false;
        }

        int rawNext = contentScrollOffset + delta;
        int next = clampScrollOffset(rawNext);
        if (next == contentScrollOffset && rawNext == next) {
            return false;
        }

        if (rawNext != next) {
            applyWheelOverscroll(delta);
        }

        applyScrollOffset(next, false);
        return true;
    }

    private void applyScrollOffset(int nextOffset, boolean immediateVisual) {
        contentScrollOffset = nextOffset;
        sectionScrollOffsets[selectedSection.ordinal()] = nextOffset;
        if (immediateVisual) {
            contentVisualOffset = nextOffset;
            if (!draggingContentByMouse) {
                contentElasticOffset = 0.0;
                contentElasticVelocity = 0.0;
            }
        }
        draggingSlider = null;
        routeDropdownSlot = null;
        addProviderTypeDropdownOpen = false;
        rebuildActionBlocks();
    }

    private void applyWheelOverscroll(int delta) {
        double nextElastic = contentElasticOffset + (-delta) * OVER_SCROLL_WHEEL_OFFSET_FACTOR;
        contentElasticOffset = clampDouble(nextElastic, -OVER_SCROLL_MAX, OVER_SCROLL_MAX);
        contentElasticVelocity += (-delta) * OVER_SCROLL_WHEEL_VELOCITY_FACTOR;
        contentElasticVelocity = clampDouble(contentElasticVelocity, -900.0, 900.0);
    }

    private UiRect scrollbarTrackRect() {
        if (contentViewport.width <= 0 || contentViewport.height <= 0 || contentScrollMaxOffset <= 0) {
            return null;
        }
        return new UiRect(
                contentViewport.right() - SCROLLBAR_WIDTH,
                contentViewport.y,
                SCROLLBAR_WIDTH,
                contentViewport.height
        );
    }

    private UiRect scrollbarThumbRect() {
        UiRect track = scrollbarTrackRect();
        if (track == null) {
            return null;
        }

        int thumbHeight = Math.max(
                SCROLLBAR_MIN_THUMB_HEIGHT,
                (int) Math.round(track.height * (track.height / (double) (track.height + contentScrollMaxOffset)))
        );
        thumbHeight = Math.min(track.height, thumbHeight);
        int travel = Math.max(0, track.height - thumbHeight);
        double thumbOffset = clampDouble(contentVisualOffset, 0.0, contentScrollMaxOffset);
        int thumbY = travel == 0
                ? track.y
                : track.y + (int) Math.round((thumbOffset / Math.max(1.0, contentScrollMaxOffset)) * travel);
        return new UiRect(track.x, thumbY, track.width, thumbHeight);
    }

    private int scrollOffsetFromThumbMouseY(double mouseY) {
        UiRect track = scrollbarTrackRect();
        UiRect thumb = scrollbarThumbRect();
        if (track == null || thumb == null) {
            return contentScrollOffset;
        }

        int travel = Math.max(1, track.height - thumb.height);
        double anchorY = mouseY - thumb.height / 2.0;
        double ratio = (anchorY - track.y) / travel;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        return clampScrollOffset((int) Math.round(ratio * contentScrollMaxOffset));
    }

    private void startScrollbarDrag() {
        draggingContentScrollbar = true;
        draggingContentByMouse = false;
        contentElasticOffset = 0.0;
        contentElasticVelocity = 0.0;
    }

    private void stopScrollingDrag() {
        draggingContentScrollbar = false;
        draggingContentByMouse = false;
    }

    private void drawContentScrollbar(DrawContext context, int mouseX, int mouseY) {
        UiRect track = scrollbarTrackRect();
        UiRect thumb = scrollbarThumbRect();
        if (track == null || thumb == null) {
            return;
        }

        context.fill(track.x, track.y, track.right(), track.bottom(), COLOR_SCROLL_TRACK);
        boolean hovered = thumb.contains(mouseX, mouseY);
        int thumbColor = draggingContentScrollbar || hovered ? COLOR_SCROLL_THUMB_HOVER : COLOR_SCROLL_THUMB;
        context.fill(thumb.x, thumb.y, thumb.right(), thumb.bottom(), thumbColor);
        ConfigUiDraw.drawOutline(context, track.x, track.y, track.width, track.height, COLOR_BORDER);
    }

    private Text providerTypeDisplayName(ApiProviderType providerType) {
        return t("provider_type." + providerType.name().toLowerCase(Locale.ROOT));
    }

    private void addRouteModelSelector(ProviderManagerConfig manager, RouteSlot routeSlot, int x, int y, int width) {
        RouteModelSelectorSectionSupport.render(
                manager,
                routeSlot,
                x,
                y,
                width,
                routeDropdownSlot == routeSlot,
                ModConfigScreen::t,
                contentActionBlockRegistry::add,
                floatingActionBlockRegistry::add,
                () -> {
                    routeDropdownSlot = routeDropdownSlot == routeSlot ? null : routeSlot;
                    rebuildActionBlocks();
                },
                (routeKey, displayLabel) -> {
                    RouteModelSectionSupport.setRouteKey(manager, routeSlot, routeKey);
                    routeDropdownSlot = null;
                    setStatus(t("status.assigned_route", t(routeSlot.translationKey()), displayLabel), COLOR_STATUS_OK);
                    rebuildActionBlocks();
                },
                ROUTE_MODEL_SELECTOR_STYLE
        );
    }

    private int addProviderManagerActions(ProviderManagerConfig providerManager, int x, int y, int width, int viewportHeight) {
        ProviderManagerSectionSupport.RenderResult result = ProviderManagerSectionSupport.render(
                providerManager,
                viewportHeight,
                x,
                y,
                width,
                providerSearchQuery,
                selectedProviderId,
                selectedProviderIndex,
                providerApiKeyVisible,
                PROVIDER_LIST_STYLE,
                PROVIDER_DETAIL_STYLE,
                ModConfigScreen::t,
                this::addGroupBox,
                this::providerTypeDisplayName,
                contentActionBlockRegistry::add,
                this::addTextField,
                value -> {
                    String next = ProviderProfileSupport.sanitizeText(value);
                    if (!providerSearchQuery.equals(next)) {
                        providerSearchQuery = next;
                        rebuildActionBlocks(FocusTarget.PROVIDER_SEARCH);
                    }
                },
                providerId -> {
                    selectedProviderId = providerId;
                    providerApiKeyVisible = false;
                    modelSettingsModalOpen = false;
                    rebuildActionBlocks();
                },
                () -> {
                    openAddProviderModal();
                    rebuildActionBlocks(FocusTarget.ADD_PROVIDER_NAME);
                },
                contentActionBlockRegistry::add,
                this::addTextField,
                targetProfile -> {
                    targetProfile.enabled = !targetProfile.enabled;
                    rebuildActionBlocks();
                },
                targetProfile -> deleteProvider(providerManager, targetProfile),
                () -> {
                    providerApiKeyVisible = !providerApiKeyVisible;
                    rebuildActionBlocks();
                },
                profile -> ConfigUiRuntimeSupport.testProviderConnection(
                        profile,
                        ModConfigScreen::t,
                        this::setStatus,
                        COLOR_STATUS_OK,
                        COLOR_STATUS_ERROR,
                        runnable -> {
                            if (this.client != null) {
                                this.client.execute(runnable);
                            } else {
                                runnable.run();
                            }
                        }
                ),
                (targetProfile, modelId) -> {
                    targetProfile.model_id = modelId;
                    targetProfile.ensureModelSettings();
                    setStatus(t("status.model_set_default", modelId), COLOR_STATUS_OK);
                    rebuildActionBlocks();
                },
                (targetProfile, modelId) -> {
                    openModelSettingsModal(targetProfile, modelId);
                    rebuildActionBlocks(FocusTarget.MODEL_NAME);
                },
                this::removeModel,
                targetProfile -> {
                    openModelSettingsModal(targetProfile, "");
                    rebuildActionBlocks(FocusTarget.MODEL_NAME);
                }
        );
        providerSearchField = result.providerSearchField();
        selectedProviderId = result.selectedProviderId();

        if (addProviderModalOpen) {
            addAddProviderModal(providerManager);
        }
        if (modelSettingsModalOpen && !customParametersModalOpen) {
            addModelSettingsModal(providerManager);
        }
        if (customParametersModalOpen) {
            addCustomParametersModal();
        }

        return result.contentBottomY();
    }

    private TextFieldWidget addTextField(
            int x,
            int y,
            int width,
            int maxLength,
            String initialValue,
            Text placeholder,
            Consumer<String> changed,
            boolean editable
    ) {
        return addTextField(x, y, width, maxLength, initialValue, placeholder, changed, editable, false);
    }

    private TextFieldWidget addTextField(
            int x,
            int y,
            int width,
            int maxLength,
            String initialValue,
            Text placeholder,
            Consumer<String> changed,
            boolean editable,
            boolean floating
    ) {
        return ConfigUiTextFieldSupport.create(
                this.textRenderer,
                this::addDrawableChild,
                providerEditorFields,
                floatingEditorFields,
                x,
                y,
                width,
                maxLength,
                initialValue,
                placeholder,
                changed,
                editable,
                floating,
                ConfigUiModalSupport.isAnyModalOpen(addProviderModalOpen, modelSettingsModalOpen, customParametersModalOpen, resetConfirmModalOpen)
        );
    }

    private void addAddProviderModal(ProviderManagerConfig providerManager) {
        addProviderNameField = AddProviderModalSupport.render(
                this.width,
                this.height,
                addProviderNameDraft,
                addProviderTypeDraft,
                addProviderTypeDropdownOpen,
                ModConfigScreen::t,
                this::providerTypeDisplayName,
                floatingActionBlockRegistry::add,
                (x, y, width, maxLength, initialValue, placeholder, changed, editable) ->
                        addTextField(x, y, width, maxLength, initialValue, placeholder, changed, editable, true),
                value -> addProviderNameDraft = ProviderProfileSupport.sanitizeText(value),
                () -> {
                    addProviderTypeDropdownOpen = !addProviderTypeDropdownOpen;
                    rebuildActionBlocks(FocusTarget.ADD_PROVIDER_NAME);
                },
                type -> {
                    addProviderTypeDraft = type;
                    addProviderTypeDropdownOpen = false;
                    rebuildActionBlocks(FocusTarget.ADD_PROVIDER_NAME);
                },
                () -> {
                    closeAddProviderModal();
                    rebuildActionBlocks();
                },
                () -> addProviderFromModal(providerManager),
                ADD_PROVIDER_MODAL_STYLE
        );
    }

    private void addProviderFromModal(ProviderManagerConfig providerManager) {
        ProviderManagerMutationSupport.AddProviderResult result = ProviderManagerMutationSupport.addProviderFromDraft(
                providerManager,
                addProviderNameDraft,
                addProviderTypeDraft
        );
        ApiProviderProfile profile = result.profile();
        selectedProviderId = result.selectedProviderId();
        selectedProviderIndex = result.selectedProviderIndex();
        providerApiKeyVisible = false;

        closeAddProviderModal();
        setStatus(t("status.added_provider", profile.name), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void openAddProviderModal() {
        addProviderModalOpen = true;
        addProviderTypeDropdownOpen = false;
        addProviderTypeDraft = ApiProviderType.OPENAI_COMPAT;
        addProviderNameDraft = "";
    }

    private void closeAddProviderModal() {
        addProviderModalOpen = false;
        addProviderTypeDropdownOpen = false;
        addProviderNameDraft = "";
    }

    private void openModelSettingsModal(ApiProviderProfile profile, String originalModelId) {
        modelSettingsModalOpen = true;
        ModelSettingsDraftSupport.Draft draft = ModelSettingsDraftSupport.fromProfile(profile, originalModelId);
        modelSettingsProviderId = draft.providerId();
        modelSettingsOriginalId = draft.originalModelId();
        modelSettingsDraft = draft.modelIdDraft();
        modelSettingsTemperatureDraft = draft.temperatureDraft();
        modelSettingsKeepAliveDraft = draft.keepAliveDraft();
        modelSettingsSupportsSystemDraft = draft.supportsSystem();
        modelSettingsInjectPromptIntoUserDraft = draft.injectPromptIntoUser();
        modelSettingsStructuredOutputDraft = draft.structuredOutput();
        modelSettingsSystemPromptSuffixDraft = draft.systemPromptSuffixDraft();
        modelSettingsCustomParametersDraft = draft.customParametersDraft();
        customParametersBackup = draft.customParametersBackup();
        customParametersModalOpen = false;
        selectedCustomParameterPath = "";
        modelSettingsSetDefault = draft.setDefault();
    }

    private void closeModelSettingsModal() {
        if (customParametersModalOpen) {
            closeCustomParametersModal(true);
        }

        ModelSettingsDraftSupport.Draft empty = ModelSettingsDraftSupport.empty();
        modelSettingsModalOpen = false;
        modelSettingsProviderId = empty.providerId();
        modelSettingsOriginalId = empty.originalModelId();
        modelSettingsDraft = empty.modelIdDraft();
        modelSettingsTemperatureDraft = empty.temperatureDraft();
        modelSettingsKeepAliveDraft = empty.keepAliveDraft();
        modelSettingsSupportsSystemDraft = empty.supportsSystem();
        modelSettingsInjectPromptIntoUserDraft = empty.injectPromptIntoUser();
        modelSettingsStructuredOutputDraft = empty.structuredOutput();
        modelSettingsSystemPromptSuffixDraft = empty.systemPromptSuffixDraft();
        modelSettingsCustomParametersDraft = empty.customParametersDraft();
        customParametersBackup = empty.customParametersBackup();
        selectedCustomParameterPath = "";
        modelSettingsSetDefault = empty.setDefault();
    }

    private void openCustomParametersModal() {
        customParametersModalOpen = true;
        customParametersBackup = CustomParameterEntry.deepCopyList(modelSettingsCustomParametersDraft);
        selectedCustomParameterPath = CustomParametersModalSupport.ensureSelection(modelSettingsCustomParametersDraft, selectedCustomParameterPath);
    }

    private void closeCustomParametersModal(boolean keepChanges) {
        if (!keepChanges) {
            modelSettingsCustomParametersDraft = CustomParameterEntry.deepCopyList(customParametersBackup);
        }
        customParametersModalOpen = false;
        selectedCustomParameterPath = "";
    }

    private void addCustomParametersModal() {
        CustomParametersModalSupport.Fields fields = CustomParametersModalSupport.render(
                this.width,
                this.height,
                modelSettingsCustomParametersDraft,
                () -> selectedCustomParameterPath,
                value -> selectedCustomParameterPath = ProviderProfileSupport.sanitizeText(value),
                ModConfigScreen::t,
                floatingActionBlockRegistry::add,
                (x, y, width, maxLength, initialValue, placeholder, changed, editable) ->
                        addTextField(x, y, width, maxLength, initialValue, placeholder, changed, editable, true),
                message -> setStatus(message, COLOR_STATUS_ERROR),
                () -> rebuildActionBlocks(FocusTarget.CUSTOM_PARAMETER_NAME),
                () -> {
                    closeCustomParametersModal(false);
                    rebuildActionBlocks(FocusTarget.MODEL_NAME);
                },
                () -> {
                    closeCustomParametersModal(true);
                    rebuildActionBlocks(FocusTarget.MODEL_NAME);
                },
                CUSTOM_PARAMETERS_MODAL_STYLE
        );
        customParameterNameField = fields.nameField();
        customParameterValueField = fields.valueField();
    }

    private void addModelSettingsModal(ProviderManagerConfig providerManager) {
        ApiProviderProfile profile = providerManager.findById(modelSettingsProviderId);
        if (profile == null) {
            closeModelSettingsModal();
            return;
        }

        modelSettingsField = ModelSettingsModalSupport.render(
                profile,
                this.width,
                this.height,
                modelSettingsDraft,
                modelSettingsTemperatureDraft,
                modelSettingsKeepAliveDraft,
                modelSettingsSystemPromptSuffixDraft,
                CustomParameterTreeSupport.countEntries(modelSettingsCustomParametersDraft),
                modelSettingsSupportsSystemDraft,
                modelSettingsInjectPromptIntoUserDraft,
                modelSettingsStructuredOutputDraft,
                modelSettingsSetDefault,
                ModConfigScreen::t,
                floatingActionBlockRegistry::add,
                this::addFloatingCheckbox,
                (x, y, width, maxLength, initialValue, placeholder, changed, editable) ->
                        addTextField(x, y, width, maxLength, initialValue, placeholder, changed, editable, true),
                value -> modelSettingsDraft = ProviderProfileSupport.sanitizeText(value),
                value -> modelSettingsTemperatureDraft = ProviderProfileSupport.sanitizeText(value),
                value -> modelSettingsKeepAliveDraft = ProviderProfileSupport.sanitizeText(value),
                value -> modelSettingsSystemPromptSuffixDraft = ProviderProfileSupport.sanitizeText(value),
                () -> {
                    openCustomParametersModal();
                    rebuildActionBlocks(FocusTarget.CUSTOM_PARAMETER_NAME);
                },
                value -> {
                    modelSettingsSupportsSystemDraft = value;
                    rebuildActionBlocks(FocusTarget.MODEL_NAME);
                },
                value -> modelSettingsInjectPromptIntoUserDraft = value,
                value -> modelSettingsStructuredOutputDraft = value,
                value -> modelSettingsSetDefault = value,
                () -> {
                    closeModelSettingsModal();
                    rebuildActionBlocks();
                },
                () -> applyModelSettings(profile),
                MODEL_SETTINGS_MODAL_STYLE,
                CHECKBOX_STYLE
        );
    }

    private void applyModelSettings(ApiProviderProfile profile) {
        ModelSettingsApplySupport.ApplyResult result = ModelSettingsApplySupport.apply(
                profile,
                modelSettingsOriginalId,
                modelSettingsDraft,
                modelSettingsTemperatureDraft,
                modelSettingsKeepAliveDraft,
                modelSettingsSupportsSystemDraft,
                modelSettingsInjectPromptIntoUserDraft,
                modelSettingsStructuredOutputDraft,
                modelSettingsSystemPromptSuffixDraft,
                modelSettingsCustomParametersDraft,
                modelSettingsSetDefault
        );
        if (!result.success()) {
            setStatus(
                    result.errorArg() == null ? t(result.errorKey()) : t(result.errorKey(), result.errorArg()),
                    COLOR_STATUS_ERROR
            );
            return;
        }

        closeModelSettingsModal();
        setStatus(result.creating() ? t("status.added_model", result.modelId()) : t("status.updated_model", result.modelId()), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void removeModel(ApiProviderProfile profile, String modelId) {
        boolean removed = ModelSettingsMutationSupport.removeModel(profile, modelId);
        if (!removed) {
            return;
        }

        setStatus(t("status.removed_model", modelId), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void deleteProvider(ProviderManagerConfig providerManager, ApiProviderProfile profile) {
        ProviderManagerMutationSupport.DeleteProviderResult result = ProviderManagerMutationSupport.deleteProvider(
                providerManager,
                selectedProviderId,
                profile
        );
        String removedId = result.removedProviderId();
        selectedProviderId = result.selectedProviderId();
        providerApiKeyVisible = false;

        setStatus(t("status.removed_profile", removedId), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void addToggleAction(int x, int y, int width, Text label, BooleanSupplier getter, Consumer<Boolean> setter) {
        checkboxBlocks.add(new CheckboxBlock(
                x,
                y,
                width,
                20,
                () -> label,
                getter.getAsBoolean(),
                setter,
                CHECKBOX_STYLE
        ));
    }

    private void addGroupBox(int x, int y, int width, int height, Text title) {
        groupBoxes.add(new GroupBox(x, y, width, height, title, GROUP_BOX_STYLE));
    }

    private void addActionRow(int x, int y, int width, Text label, Runnable action) {
        contentActionBlockRegistry.add(x, y, width, 20, label, action);
    }

    private void addTextInputRow(
            int x,
            int y,
            int width,
            Text label,
            int maxLength,
            String initialValue,
            Text placeholder,
            Consumer<String> changed,
            boolean editable
    ) {
        int labelWidth = Math.min(180, Math.max(120, width / 3));
        int fieldGap = 6;
        int fieldX = x + labelWidth + fieldGap;
        int fieldWidth = Math.max(120, width - labelWidth - fieldGap);

        contentActionBlockRegistry.add(x, y, labelWidth, 20, label, () -> {
        });
        addTextField(
                fieldX,
                y,
                fieldWidth,
                maxLength,
                initialValue == null ? "" : initialValue,
                placeholder,
                changed,
                editable
        );
    }

    private Text hotkeyBindingLabel(ConfigSectionContentSupport.HotkeyTarget target, InputBindingConfig binding) {
        if (hotkeyCaptureTarget == target) {
            return t("label.hotkey_binding", t("state.hotkey_listening"));
        }

        if (!KeybindingManager.isBound(binding)) {
            return t("label.hotkey_binding", t("state.hotkey_unbound"));
        }

        return t("label.hotkey_binding", Text.literal(KeybindingManager.displayName(binding)));
    }

    private void startHotkeyCapture(ConfigSectionContentSupport.HotkeyTarget target) {
        hotkeyCaptureTarget = target;
        setStatus(t("status.hotkey_listening", sectionLabel(target)), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void clearHotkeyBinding(ConfigSectionContentSupport.HotkeyTarget target) {
        InputBindingConfig binding = ensureBinding(target);
        if (binding == null) {
            return;
        }

        hotkeyCaptureTarget = hotkeyCaptureTarget == target ? null : hotkeyCaptureTarget;
        KeybindingManager.clear(binding);
        setStatus(t("status.hotkey_cleared", sectionLabel(target)), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void cycleHotkeyMode(ConfigSectionContentSupport.HotkeyTarget target) {
        ModConfig config = Translate_AllinOne.getConfig();
        switch (target) {
            case ITEM -> {
                ItemTranslateConfig.KeybindingConfig keybinding = ensureItemKeybinding(config);
                ItemTranslateConfig.KeybindingMode[] modes = ItemTranslateConfig.KeybindingMode.values();
                keybinding.mode = modes[(keybinding.mode.ordinal() + 1) % modes.length];
                setStatus(t("status.hotkey_mode_changed", sectionLabel(target), modeLabel(keybinding.mode.name())), COLOR_STATUS_OK);
            }
            case SCOREBOARD -> {
                ScoreboardConfig.KeybindingConfig keybinding = ensureScoreboardKeybinding(config);
                ScoreboardConfig.KeybindingMode[] modes = ScoreboardConfig.KeybindingMode.values();
                keybinding.mode = modes[(keybinding.mode.ordinal() + 1) % modes.length];
                setStatus(t("status.hotkey_mode_changed", sectionLabel(target), modeLabel(keybinding.mode.name())), COLOR_STATUS_OK);
            }
            default -> {
                return;
            }
        }
        rebuildActionBlocks();
    }

    private InputBindingConfig ensureBinding(ConfigSectionContentSupport.HotkeyTarget target) {
        ModConfig config = Translate_AllinOne.getConfig();
        return switch (target) {
            case CHAT_INPUT -> {
                if (config.chatTranslate.input.keybinding == null) {
                    config.chatTranslate.input.keybinding = new InputBindingConfig();
                }
                yield config.chatTranslate.input.keybinding;
            }
            case ITEM -> {
                ItemTranslateConfig.KeybindingConfig keybinding = ensureItemKeybinding(config);
                if (keybinding.binding == null) {
                    keybinding.binding = new InputBindingConfig();
                }
                yield keybinding.binding;
            }
            case SCOREBOARD -> {
                ScoreboardConfig.KeybindingConfig keybinding = ensureScoreboardKeybinding(config);
                if (keybinding.binding == null) {
                    keybinding.binding = new InputBindingConfig();
                }
                yield keybinding.binding;
            }
        };
    }

    private ItemTranslateConfig.KeybindingConfig ensureItemKeybinding(ModConfig config) {
        if (config.itemTranslate.keybinding == null) {
            config.itemTranslate.keybinding = new ItemTranslateConfig.KeybindingConfig();
        }
        return config.itemTranslate.keybinding;
    }

    private ScoreboardConfig.KeybindingConfig ensureScoreboardKeybinding(ModConfig config) {
        if (config.scoreboardTranslate.keybinding == null) {
            config.scoreboardTranslate.keybinding = new ScoreboardConfig.KeybindingConfig();
        }
        return config.scoreboardTranslate.keybinding;
    }

    private void applyCapturedBinding(InputBindingConfig captured) {
        if (hotkeyCaptureTarget == null || captured == null) {
            return;
        }

        ConfigSectionContentSupport.HotkeyTarget target = hotkeyCaptureTarget;
        InputBindingConfig binding = ensureBinding(target);
        KeybindingManager.apply(binding, captured);
        hotkeyCaptureTarget = null;
        setStatus(t("status.hotkey_bound", sectionLabel(target), Text.literal(KeybindingManager.displayName(binding))), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    private void cancelHotkeyCapture() {
        if (hotkeyCaptureTarget == null) {
            return;
        }
        hotkeyCaptureTarget = null;
        setStatus(t("status.hotkey_capture_cancelled"), COLOR_TEXT_MUTED);
        rebuildActionBlocks();
    }

    private Text sectionLabel(ConfigSectionContentSupport.HotkeyTarget target) {
        return switch (target) {
            case CHAT_INPUT -> t("section.chat_input");
            case ITEM -> t("section.item");
            case SCOREBOARD -> t("section.scoreboard");
        };
    }

    private Text modeLabel(String modeName) {
        return switch (modeName) {
            case "HOLD_TO_TRANSLATE" -> t("state.hold_to_translate");
            case "HOLD_TO_SEE_ORIGINAL" -> t("state.hold_to_see_original");
            default -> t("state.disabled");
        };
    }

    private void addFloatingCheckbox(
            int x,
            int y,
            int width,
            int height,
            Supplier<Text> labelSupplier,
            BooleanSupplier checked,
            Consumer<Boolean> changed,
            CheckboxBlock.Style style
    ) {
        floatingCheckboxBlocks.add(new CheckboxBlock(
                x,
                y,
                width,
                height,
                labelSupplier,
                checked.getAsBoolean(),
                changed,
                style
        ));
    }

    private void addIntSliderAction(
            int x,
            int y,
            int width,
            Text label,
            int min,
            int max,
            IntSupplier getter,
            IntConsumer setter
    ) {
        sliderBlocks.add(new IntSliderBlock(x, y, width, SLIDER_BLOCK_HEIGHT, label, min, max, getter, setter, textRenderer, SLIDER_STYLE));
    }

    private void setStatus(Text message, int color) {
        this.statusMessage = message;
        this.statusColor = color;
        this.statusExpireAtMillis = System.currentTimeMillis() + 4000;
    }

    private void saveAndClose() {
        boolean saved = ConfigUiRuntimeSupport.saveConfig(
                ModConfigScreen::t,
                this::setStatus,
                COLOR_STATUS_OK,
                COLOR_STATUS_ERROR,
                (message, throwable) -> Translate_AllinOne.LOGGER.error(message, throwable)
        );
        if (!saved) {
            return;
        }
        restoreSnapshotOnClose = false;
        close();
    }

    private void cancelAndClose() {
        close();
    }

    private void openResetConfirmation() {
        resetConfirmModalOpen = true;
        routeDropdownSlot = null;
        addProviderTypeDropdownOpen = false;
        rebuildActionBlocks();
    }

    private void closeResetConfirmModal() {
        resetConfirmModalOpen = false;
    }

    private void addResetConfirmModal() {
        UiRect modalRect = ConfigUiModalSupport.resetConfirmModalRect(this.width, this.height);
        int buttonWidth = 92;
        int buttonHeight = 20;
        int buttonGap = 10;
        int buttonY = modalRect.bottom() - 30;
        int resetX = modalRect.right() - 16 - buttonWidth;
        int cancelX = resetX - buttonGap - buttonWidth;

        floatingActionBlockRegistry.add(
                cancelX,
                buttonY,
                buttonWidth,
                buttonHeight,
                t("button.cancel"),
                () -> {
                    closeResetConfirmModal();
                    rebuildActionBlocks();
                },
                COLOR_BLOCK,
                COLOR_BLOCK_HOVER,
                COLOR_TEXT,
                true
        );

        floatingActionBlockRegistry.add(
                resetX,
                buttonY,
                buttonWidth,
                buttonHeight,
                t("button.reset"),
                this::applyResetToDefaults,
                COLOR_BLOCK_DANGER,
                COLOR_BLOCK_DANGER_HOVER,
                COLOR_TEXT,
                true
        );
    }

    private void renderResetConfirmModalMessage(DrawContext context) {
        UiRect modalRect = ConfigUiModalSupport.resetConfirmModalRect(this.width, this.height);
        int textX = modalRect.x + 16;
        int textY = modalRect.y + 48;
        int maxWidth = Math.max(10, modalRect.width - 32);
        List<OrderedText> lines = this.textRenderer.wrapLines(t("modal.reset_confirm.message"), maxWidth);
        int lineY = textY;
        for (OrderedText line : lines) {
            context.drawText(this.textRenderer, line, textX, lineY, COLOR_TEXT_MUTED, false);
            lineY += this.textRenderer.fontHeight + 2;
        }
    }

    private void applyResetToDefaults() {
        closeResetConfirmModal();
        ConfigManager.resetToDefaults();
        hotkeyCaptureTarget = null;
        selectedProviderIndex = 0;
        selectedProviderId = "";
        providerApiKeyVisible = false;
        providerSearchQuery = "";
        routeDropdownSlot = null;
        addProviderTypeDropdownOpen = false;
        stopScrollingDrag();
        contentElasticOffset = 0.0;
        contentElasticVelocity = 0.0;
        setStatus(t("status.config_reset_pending"), COLOR_STATUS_OK);
        rebuildActionBlocks();
    }

    @Override
    public void close() {
        hotkeyCaptureTarget = null;
        stopScrollingDrag();
        ConfigUiModalInteractionSupport.ModalCloseAction action = ConfigUiModalInteractionSupport.closeByPriority(
                addProviderModalOpen,
                modelSettingsModalOpen,
                customParametersModalOpen,
                resetConfirmModalOpen
        );

        switch (action) {
            case CLOSE_RESET_CONFIRM -> {
                closeResetConfirmModal();
                rebuildActionBlocks();
                return;
            }
            case CLOSE_ADD_PROVIDER -> {
                closeAddProviderModal();
                rebuildActionBlocks();
                return;
            }
            case CLOSE_CUSTOM_PARAMETERS -> {
                closeCustomParametersModal(true);
                rebuildActionBlocks(FocusTarget.MODEL_NAME);
                return;
            }
            case CLOSE_MODEL_SETTINGS -> {
                closeModelSettingsModal();
                rebuildActionBlocks();
                return;
            }
            default -> {
            }
        }

        if (restoreSnapshotOnClose) {
            ConfigManager.replaceConfig(originalConfigSnapshot);
        }
        restoreSnapshotOnClose = false;

        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (hotkeyCaptureTarget != null) {
            if (hotkeyCaptureTarget != ConfigSectionContentSupport.HotkeyTarget.CHAT_INPUT) {
                InputBindingConfig captured = KeybindingManager.captureMouseBinding(click.button());
                if (captured != null) {
                    applyCapturedBinding(captured);
                }
            }
            return true;
        }

        if (click.button() != 0) {
            return super.mouseClicked(click, doubled);
        }

        double mouseX = click.x();
        double mouseY = click.y();
        boolean modalOpen = ConfigUiModalSupport.isAnyModalOpen(addProviderModalOpen, modelSettingsModalOpen, customParametersModalOpen, resetConfirmModalOpen);

        if (modalOpen) {
            draggingSlider = null;
            if (!ConfigUiModalSupport.isInsideOpenModal(
                    mouseX,
                    mouseY,
                    this.width,
                    this.height,
                    addProviderModalOpen,
                    modelSettingsModalOpen,
                    customParametersModalOpen,
                    resetConfirmModalOpen
            )) {
                ConfigUiModalInteractionSupport.ModalCloseAction action = ConfigUiModalInteractionSupport.outsideModalClickAction(
                        addProviderModalOpen,
                        modelSettingsModalOpen,
                        customParametersModalOpen,
                        resetConfirmModalOpen
                );
                switch (action) {
                    case CLOSE_RESET_CONFIRM -> {
                        closeResetConfirmModal();
                        rebuildActionBlocks();
                        return true;
                    }
                    case CLOSE_CUSTOM_PARAMETERS -> {
                        closeCustomParametersModal(true);
                        rebuildActionBlocks(FocusTarget.MODEL_NAME);
                        return true;
                    }
                    case CLOSE_ADD_PROVIDER -> {
                        closeAddProviderModal();
                        rebuildActionBlocks();
                        return true;
                    }
                    case CLOSE_MODEL_SETTINGS -> {
                        closeModelSettingsModal();
                        rebuildActionBlocks();
                        return true;
                    }
                    default -> {
                    }
                }
            }

            if (ConfigUiInteractionSupport.dispatchActionBlocks(floatingActionBlocks, mouseX, mouseY)) {
                return true;
            }

            if (ConfigUiInteractionSupport.dispatchCheckboxBlocks(floatingCheckboxBlocks, mouseX, mouseY)) {
                return true;
            }

            if (super.mouseClicked(click, doubled)) {
                return true;
            }
            return true;
        }

        if (ConfigUiInteractionSupport.dispatchActionBlocks(floatingActionBlocks, mouseX, mouseY)) {
            return true;
        }

        if (ConfigUiInteractionSupport.dispatchCheckboxBlocks(floatingCheckboxBlocks, mouseX, mouseY)) {
            return true;
        }

        if (click.button() == 0) {
            UiRect thumb = scrollbarThumbRect();
            if (thumb != null && thumb.contains(mouseX, mouseY)) {
                startScrollbarDrag();
                return true;
            }

            UiRect track = scrollbarTrackRect();
            if (track != null && track.contains(mouseX, mouseY)) {
                int nextOffset = scrollOffsetFromThumbMouseY(mouseY);
                if (nextOffset != contentScrollOffset) {
                    applyScrollOffset(nextOffset, false);
                }
                startScrollbarDrag();
                return true;
            }
        }

        if (ConfigUiInteractionSupport.dispatchActionBlocks(actionBlocks, mouseX, mouseY)) {
            return true;
        }

        if (!contentViewport.contains(mouseX, mouseY)) {
            if (addProviderTypeDropdownOpen || routeDropdownSlot != null) {
                addProviderTypeDropdownOpen = false;
                routeDropdownSlot = null;
                rebuildActionBlocks();
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        IntSliderBlock selectedSlider = ConfigUiInteractionSupport.pickSlider(sliderBlocks, mouseX, mouseY);
        if (selectedSlider != null) {
            draggingSlider = selectedSlider;
            selectedSlider.startDrag(mouseX);
            return true;
        }

        if (super.mouseClicked(click, doubled)) {
            return true;
        }

        if (ConfigUiInteractionSupport.dispatchActionBlocks(contentActionBlocks, mouseX, mouseY)) {
            return true;
        }

        if (ConfigUiInteractionSupport.dispatchCheckboxBlocks(checkboxBlocks, mouseX, mouseY)) {
            return true;
        }

        if (contentScrollMaxOffset > 0) {
            draggingContentByMouse = true;
            contentDragStartMouseY = mouseY;
            contentDragStartOffset = contentScrollOffset;
            draggingContentScrollbar = false;
            return true;
        }

        if (addProviderTypeDropdownOpen || routeDropdownSlot != null) {
            addProviderTypeDropdownOpen = false;
            routeDropdownSlot = null;
            rebuildActionBlocks();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (draggingSlider != null && click.button() == 0) {
            draggingSlider.dragTo(click.x());
            return true;
        }

        if (draggingContentScrollbar && click.button() == 0) {
            int nextOffset = scrollOffsetFromThumbMouseY(click.y());
            if (nextOffset != contentScrollOffset) {
                applyScrollOffset(nextOffset, true);
            }
            return true;
        }

        if (draggingContentByMouse && click.button() == 0) {
            double delta = click.y() - contentDragStartMouseY;
            double rawOffset = contentDragStartOffset - delta;
            int nextOffset = clampScrollOffset((int) Math.round(rawOffset));
            if (nextOffset != contentScrollOffset) {
                applyScrollOffset(nextOffset, true);
            }

            double overflow = rawOffset - nextOffset;
            if (Math.abs(overflow) > 0.001) {
                contentElasticOffset = clampDouble(-overflow * OVER_SCROLL_RESISTANCE, -OVER_SCROLL_MAX, OVER_SCROLL_MAX);
                contentElasticVelocity = 0.0;
                rebuildActionBlocks();
            } else if (Math.abs(contentElasticOffset) > 0.001 || Math.abs(contentElasticVelocity) > 0.001) {
                contentElasticOffset = 0.0;
                contentElasticVelocity = 0.0;
                rebuildActionBlocks();
            }
            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (draggingSlider != null && click.button() == 0) {
            draggingSlider.release();
            draggingSlider = null;
            return true;
        }

        if ((draggingContentScrollbar || draggingContentByMouse) && click.button() == 0) {
            stopScrollingDrag();
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (ConfigUiModalSupport.isAnyModalOpen(addProviderModalOpen, modelSettingsModalOpen, customParametersModalOpen, resetConfirmModalOpen)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (!contentViewport.contains(mouseX, mouseY) || contentScrollMaxOffset <= 0 || verticalAmount == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        int delta = (int) Math.round(-verticalAmount * SCROLL_STEP);
        if (delta == 0) {
            delta = verticalAmount > 0 ? -SCROLL_STEP : SCROLL_STEP;
        }
        if (tryScrollBy(delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (hotkeyCaptureTarget != null) {
            if (KeybindingManager.isEscape(input)) {
                cancelHotkeyCapture();
                return true;
            }

            InputBindingConfig captured = KeybindingManager.captureKeyboardBinding(input);
            if (captured != null) {
                applyCapturedBinding(captured);
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (hotkeyCaptureTarget != null) {
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateControlAnimations();

        long nowMillis = System.currentTimeMillis();
        boolean modalOpen = ConfigUiModalSupport.isAnyModalOpen(addProviderModalOpen, modelSettingsModalOpen, customParametersModalOpen, resetConfirmModalOpen);
        ConfigUiScreenRenderSupport.renderChrome(
                context,
                this.textRenderer,
                this.width,
                this.height,
                TOP_BAR_HEIGHT,
                LEFT_PANEL_WIDTH,
                this.title,
                t("panel", t(selectedSection.translationKey())),
                statusMessage,
                statusColor,
                statusExpireAtMillis,
                nowMillis,
                selectedSection == ConfigSection.PROVIDERS,
                SCREEN_RENDER_STYLE
        );

        ConfigUiControlRenderer.drawActionBlocks(context, this.textRenderer, actionBlocks, mouseX, mouseY, COLOR_BORDER);

        ConfigUiDraw.withScissor(context, contentClipViewport(), () -> {
            ConfigUiControlRenderer.drawGroupBoxes(context, this.textRenderer, groupBoxes);
            ConfigUiControlRenderer.drawActionBlocks(context, this.textRenderer, contentActionBlocks, mouseX, mouseY, COLOR_BORDER);
            ConfigUiControlRenderer.drawSliderBlocks(context, sliderBlocks, mouseX, mouseY);
            ConfigUiControlRenderer.drawCheckboxBlocks(context, this.textRenderer, checkboxBlocks, mouseX, mouseY);
            if (!modalOpen) {
                super.render(context, mouseX, mouseY, delta);
            }
        });

        if (contentScrollMaxOffset > 0 && !modalOpen) {
            drawContentScrollbar(context, mouseX, mouseY);
        }

        ConfigUiScreenRenderSupport.renderModalOverlayAndShell(
                context,
                this.textRenderer,
                this.width,
                this.height,
                TOP_BAR_HEIGHT,
                addProviderModalOpen,
                modelSettingsModalOpen,
                customParametersModalOpen,
                resetConfirmModalOpen,
                t("modal.add_provider.title"),
                t("modal.model.title"),
                t("custom_params.title"),
                t("modal.reset_confirm.title"),
                SCREEN_RENDER_STYLE
        );

        if (resetConfirmModalOpen) {
            renderResetConfirmModalMessage(context);
        }

        if (modalOpen) {
            super.render(context, mouseX, mouseY, delta);
        }
        ConfigUiControlRenderer.drawCheckboxBlocks(context, this.textRenderer, floatingCheckboxBlocks, mouseX, mouseY);
        ConfigUiControlRenderer.drawActionBlocks(context, this.textRenderer, floatingActionBlocks, mouseX, mouseY, COLOR_BORDER);
    }

    private void updateControlAnimations() {
        long now = System.nanoTime();
        double deltaSeconds = (now - sliderAnimationLastNanos) / 1_000_000_000.0;
        sliderAnimationLastNanos = now;
        deltaSeconds = Math.max(0.0, Math.min(0.05, deltaSeconds));
        for (IntSliderBlock slider : sliderBlocks) {
            slider.update(deltaSeconds);
        }
        for (CheckboxBlock checkbox : checkboxBlocks) {
            checkbox.update(deltaSeconds);
        }
        for (CheckboxBlock checkbox : floatingCheckboxBlocks) {
            checkbox.update(deltaSeconds);
        }

        int beforeRenderOffset = currentRenderScrollOffset();

        if (draggingContentScrollbar || draggingContentByMouse) {
            contentVisualOffset = contentScrollOffset;
        } else {
            double blend = 1.0 - Math.exp(-deltaSeconds * SCROLL_ANIMATION_SPEED);
            contentVisualOffset += (contentScrollOffset - contentVisualOffset) * blend;

            double acceleration = -contentElasticOffset * OVER_SCROLL_SPRING - contentElasticVelocity * OVER_SCROLL_DAMPING;
            contentElasticVelocity += acceleration * deltaSeconds;
            contentElasticOffset += contentElasticVelocity * deltaSeconds;
            contentElasticOffset = clampDouble(contentElasticOffset, -OVER_SCROLL_MAX, OVER_SCROLL_MAX);

            if (Math.abs(contentElasticOffset) < 0.01 && Math.abs(contentElasticVelocity) < 0.01) {
                contentElasticOffset = 0.0;
                contentElasticVelocity = 0.0;
            }
        }

        int afterRenderOffset = currentRenderScrollOffset();
        if (afterRenderOffset != beforeRenderOffset) {
            rebuildActionBlocks();
        }
    }

}
