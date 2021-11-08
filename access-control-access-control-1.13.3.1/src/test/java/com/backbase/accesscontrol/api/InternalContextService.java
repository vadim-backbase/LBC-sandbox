package com.backbase.accesscontrol.api;

import static java.lang.System.exit;

import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.properties.JsonWebTokenProperties;
import com.backbase.buildingblocks.jwt.core.token.JsonWebTokenClaimsSet;
import com.backbase.buildingblocks.jwt.core.type.JsonWebTokenTypeFactory;
import com.backbase.buildingblocks.jwt.internal.InternalJwtConsumerProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InternalContextService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApplicationContext applicationContext;

    public String createInternalContextToken(String userName, String serviceAgreementId, String legalEntityId,
        String userId, String tId)
        throws IOException, JSONException {

        Map<String, Object> claimSet = createClaims(userName);
        claimSet.put("said", serviceAgreementId);
        claimSet.put("leid", legalEntityId);
        claimSet.put("inuid", userId);
        if (Objects.nonNull(tId)) {
            claimSet.put("tid", tId);
        }

        return createAuthorisationTokenAndEncode(claimSet, JsonWebTokenTypeFactory.SIGNED_TOKEN_TYPE);
    }

    private Map<String, Object> createClaims(String user) throws IOException, JSONException {
        JSONObject item = new JSONObject();
        item.put("sub", user);
        item.put("naf", new Long(System.currentTimeMillis() / 1000).longValue() + 3600 * 24);
        item.put("cnexp", true);

        JSONArray grp = new JSONArray();
        grp.put("user(USER)");

        item.put("grp", grp);
        item.put("anloc", true);
        item.put("anexp", true);
        item.put("enbl", true);
        item.put("exp", 2533644487L);
        item.put("iat", 2533640487L);

        JSONArray role = new JSONArray();
        role.put("ROLE_group_user(USER)");
        role.put("ROLE_USER");
        item.put("rol", role);
        item.put("jti", "37cea40b-b1d9-486c-9522-834654f4b986");
        return createInitialClaimSet(item.toString());
    }

    private HashMap<String, Object> createInitialClaimSet(String loginJwt) throws IOException {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        return mapper.readValue(loginJwt, typeRef);
    }

    private String createAuthorisationTokenAndEncode(Map<String, Object> claims, String tokenType) {
        String token = "";

        try {

            JsonWebTokenProperties properties = createTokenProperties();

            @SuppressWarnings("unchecked")
            JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> producer = JsonWebTokenTypeFactory
                .getProducer(properties);

            JsonWebTokenClaimsSet claimsSet = new TestJsonWebTokenClaimsSet(claims);
            token = producer.createToken(claimsSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            exit(1);
        }
        return token;
    }

    private JsonWebTokenProperties createTokenProperties() {
        return InternalJwtConsumerProperties.create(applicationContext).orElse(null);
    }

    public class TestJsonWebTokenClaimsSet implements JsonWebTokenClaimsSet {

        private Map<String, Object> claims;

        public TestJsonWebTokenClaimsSet(Map<String, Object> claims) {
            this.claims = claims;
        }

        @Override
        public Map<String, Object> getClaims() {
            return claims;
        }

        @Override
        public Optional<Object> getClaim(String claimName) {
            return Optional.ofNullable(claims.get(claimName));
        }
    }
}
