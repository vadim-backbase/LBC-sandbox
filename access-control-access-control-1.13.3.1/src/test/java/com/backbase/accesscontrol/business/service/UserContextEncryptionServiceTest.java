package com.backbase.accesscontrol.business.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.configuration.UserContextConfiguration;
import com.backbase.accesscontrol.dto.UserContextDto;
import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.jwt.internal.InternalJwtConsumer;
import com.backbase.buildingblocks.jwt.internal.exception.InternalJwtException;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserContextEncryptionServiceTest {

    public static final String SERVICE_AGREEMENT_ID = "SA07";
    public static final String EXTERNAL_USER_ID = "user";
    private static final String SERVICE_AGREEMENT_CLAIM_NAME = "saId";
    private static final String EXTERNAL_USER_ID_CLAIM_NAME = "sub";
    private static final String ENCRYPTED_TOKEN = "zaq1xsw2";
    @Mock
    private UserContextConfiguration userContextConfiguration;
    @Mock
    private JsonWebTokenProducerType producer;
    @Mock
    private InternalJwtConsumer consumer;
    @InjectMocks
    private UserContextEncryptionService userContextEncryptionService;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(userContextEncryptionService, "userContextExpiration", 300);
        setupProducer();
        setupConsumer();
        when(userContextConfiguration.getInternalJwtConsumer()).thenReturn(consumer);
        when(userContextConfiguration.getJsonWebTokenProducerType()).thenReturn(producer);

    }

    private void setupConsumer() throws InternalJwtException, JsonWebTokenException {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SERVICE_AGREEMENT_CLAIM_NAME, SERVICE_AGREEMENT_ID);
        claims.put(EXTERNAL_USER_ID_CLAIM_NAME, EXTERNAL_USER_ID);
        InternalJwtClaimsSet claimsSet = new InternalJwtClaimsSet(claims);

        when(consumer.parseToken(anyString())).thenReturn(new InternalJwt(ENCRYPTED_TOKEN, claimsSet));
    }

    private void setupProducer() throws JsonWebTokenException {
        when(producer.createToken(any())).thenReturn(ENCRYPTED_TOKEN);
    }

    @Test
    public void testEncryptCookieToJwtToken() throws JsonWebTokenException {

        UserContextDto userContextEncryptionDto = new UserContextDto(SERVICE_AGREEMENT_ID, EXTERNAL_USER_ID);

        String token = userContextEncryptionService.encryptUserContextToJwt(userContextEncryptionDto);

        assertEquals(ENCRYPTED_TOKEN, token);
    }

    @Test
    public void decryptUserContextToken() throws Exception {
        InternalJwt internJwt = userContextEncryptionService.decryptUserContextToken(ENCRYPTED_TOKEN);

        assertNotNull(internJwt);
        assertNotNull(internJwt.getClaimsSet());

        assertEquals(SERVICE_AGREEMENT_ID, internJwt.getClaimsSet().getClaim(SERVICE_AGREEMENT_CLAIM_NAME).get());
        assertEquals(EXTERNAL_USER_ID, internJwt.getClaimsSet().getClaim(EXTERNAL_USER_ID_CLAIM_NAME).get());
    }
}
