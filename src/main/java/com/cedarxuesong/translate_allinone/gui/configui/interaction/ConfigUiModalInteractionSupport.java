package com.cedarxuesong.translate_allinone.gui.configui.interaction;

public final class ConfigUiModalInteractionSupport {
    private ConfigUiModalInteractionSupport() {
    }

    public static ModalCloseAction outsideModalClickAction(
            boolean addProviderModalOpen,
            boolean modelSettingsModalOpen,
            boolean customParametersModalOpen,
            boolean resetConfirmModalOpen,
            boolean updateNoticeModalOpen
    ) {
        if (updateNoticeModalOpen) {
            return ModalCloseAction.CLOSE_UPDATE_NOTICE;
        }
        if (resetConfirmModalOpen) {
            return ModalCloseAction.CLOSE_RESET_CONFIRM;
        }
        if (customParametersModalOpen) {
            return ModalCloseAction.CLOSE_CUSTOM_PARAMETERS;
        }
        if (addProviderModalOpen) {
            return ModalCloseAction.CLOSE_ADD_PROVIDER;
        }
        if (modelSettingsModalOpen) {
            return ModalCloseAction.CLOSE_MODEL_SETTINGS;
        }
        return ModalCloseAction.NONE;
    }

    public static ModalCloseAction closeByPriority(
            boolean addProviderModalOpen,
            boolean modelSettingsModalOpen,
            boolean customParametersModalOpen,
            boolean resetConfirmModalOpen,
            boolean updateNoticeModalOpen
    ) {
        if (updateNoticeModalOpen) {
            return ModalCloseAction.CLOSE_UPDATE_NOTICE;
        }
        if (resetConfirmModalOpen) {
            return ModalCloseAction.CLOSE_RESET_CONFIRM;
        }
        if (addProviderModalOpen) {
            return ModalCloseAction.CLOSE_ADD_PROVIDER;
        }
        if (customParametersModalOpen) {
            return ModalCloseAction.CLOSE_CUSTOM_PARAMETERS;
        }
        if (modelSettingsModalOpen) {
            return ModalCloseAction.CLOSE_MODEL_SETTINGS;
        }
        return ModalCloseAction.NONE;
    }

    public enum ModalCloseAction {
        NONE,
        CLOSE_UPDATE_NOTICE,
        CLOSE_RESET_CONFIRM,
        CLOSE_ADD_PROVIDER,
        CLOSE_MODEL_SETTINGS,
        CLOSE_CUSTOM_PARAMETERS
    }
}
