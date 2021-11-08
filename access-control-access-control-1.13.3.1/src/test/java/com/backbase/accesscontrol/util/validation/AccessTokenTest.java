package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_054;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.accesscontrol.util.helpers.AccessTokenGenerator;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenTest {

    private AccessToken accessToken;
    private AccessTokenGenerator accessTokenGenerator;

    @Before
    public void set() {
        accessToken = new AccessToken("300", "Bar12345Bar12345", "RandomInitVecto2");
        accessTokenGenerator = new AccessTokenGenerator("Bar12345Bar12345", "RandomInitVecto2", "1000");
    }

    @Test
    public void failForEmptyKey() {
        accessToken = new AccessToken("300", null, "RandomInitVecto2");
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());

    }

    @Test
    public void failForEmptyPhrase() {
        accessToken = new AccessToken("300", "123 ", null);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void failForKeyLength() {
        accessToken = new AccessToken("300", "1243", "RandomInitVecto2");
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void failForPhraseLength() {
        accessToken = new AccessToken("300", "RandomInitVecto2", "123");
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }


    @Test
    public void failForEmptyToken() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }


    @Test
    public void failForNotUUIDToken() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("123", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }


    @Test
    public void failForNotValidToken() {
        String token = accessTokenGenerator.generateValidToken();
        int firstNum = Integer.parseInt(token.substring(0, 1), 16) + 1;
        if (firstNum > 15) {
            firstNum = 0;
        }
        token = Integer.toHexString(firstNum) + token.substring(1);

        String finalToken = token;
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken(finalToken, null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }


    @Test
    public void failForNotRegularToken() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken("", null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldFailForExpiredToken() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken(accessTokenGenerator.generateExpiredToken(), null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldFailForTokenInFuture() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> accessToken.validateAccessToken(accessTokenGenerator.generateInFutureToken(), null));
        assertEquals(ERR_ACQ_054.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void successWithValidateToken() {

        accessToken.validateAccessToken(accessTokenGenerator.generateValidToken(), null);
    }
}
