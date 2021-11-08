package com.backbase.accesscontrol.matchers;

import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import org.hamcrest.TypeSafeMatcher;

public class ForbiddenErrorMatcher extends TypeSafeMatcher<ForbiddenException> {

    private String code;
    private String message;

    public ForbiddenErrorMatcher(String message, String code) {
        this.message = message;
        this.code = code;
    }

    @Override
    protected boolean matchesSafely(ForbiddenException item) {
        boolean matches;
        if (code == null) {
            matches = item.getErrors().get(0).getMessage().equals(message);
        } else {
            matches = item.getErrors().get(0).getMessage().equals(message)
                && item.getErrors().get(0).getKey().equals(code);
        }
        return matches;
    }

    @Override
    public void describeTo(org.hamcrest.Description description) {
        description.appendText("expects message ")
            .appendValue(message);
        description.appendText("expects code ")
            .appendValue(code);
    }
}
