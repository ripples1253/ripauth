package icu.ripley.fusionauthtest1minecraft.util;

import lombok.Getter;

@Getter
public enum Language {
    USER_NOT_AUTHENTICATED("&9&lAuth >&r&9 You're not authenticated. Click here to sign-in to Staff SSO."),
    USER_NOT_AUTHENTICATED_HOVER("&9&lClick me to authenticate."),
    USER_AUTHENTICATION_SUCCESS("&9&lAuth >&r&9 You've been authenticated!");

    private final String message;

    Language(String message) {
        this.message = CC.translate(message);
    }

}
