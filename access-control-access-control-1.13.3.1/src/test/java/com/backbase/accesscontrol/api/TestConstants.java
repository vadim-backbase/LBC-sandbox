package com.backbase.accesscontrol.api;

public class TestConstants {


    public static final String TEST_SERVICE_TOKEN
        = "Bearer eyJraWQiOiI1eFRhc09Hbko4TWVqMGxOQ2p2WmpEcEFoaFAyN0xqTGVFWlBZZlZKbEVBPSIsImFsZyI6I"
        + "khTMjU2In0.eyJzdWIiOiJteS1zZXJ2aWNlIiwic2NvcGUiOlsiYXBpOnNlcnZpY2UiXSwiZXhwIjoyMTQ3NDgzN"
        + "jQ3LCJpYXQiOjE0ODQ4MjAxOTZ9.CoAVba-NyHZ4NNn6-aw0GUQhZptmDNxBbQ2N7HpgSxQ";

    public static final String TOKEN_RESPONSE =
        "{\n"
            + "    \"access_token\": \"" + TEST_SERVICE_TOKEN.substring(7, TEST_SERVICE_TOKEN.length())
            + "\",\n"
            + "    \"token_type\": \"bearer\",\n"
            + "    \"expires_in\": 299,\n"
            + "    \"scope\": \"api:service\",\n"
            + "    \"sub\": \"bb-client\",\n"
            + "    \"iss\": \"token-converter\",\n"
            + "    \"jti\": \"611dded8-54eb-4dc2-abf0-60b26cf2eddc\"\n"
            + "}\n";

}
