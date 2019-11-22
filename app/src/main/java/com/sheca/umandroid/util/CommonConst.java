package com.sheca.umandroid.util;

public class CommonConst {
	public static final int SIGN_TYPE = 2;//签名

	public static final int SERT_TYPE = 1;//证书

	public static String PACKAGE_NAME="com.sheca.umandroid";

	public static final String TAG = "UniTrustMobile";

	public static final String PARAM_TOKEN = "token";

	public static final String PARAM_UID = "uid";

	public static final String PARAM_REALNAME = "realname";
	public static final String PARAM_IDCARD = "idcard";

	public static final String PARAM_HAS_AUTH = "faceAuth";

	public static final String PARAM_USERNAME = "username";
	
	public static final String PARAM_BIZSN = "bizSN";
	
	public static final String PARAM_RANDOM_NUMBER = "randomNumber";

	public static final String PARAM_MSGID = "msgid";

	public static final String PARAM_MESSAGE = "message";
	
	public static final String PARAM_MESSAGES = "messages";

	public static final String PARAM_CERTSN = "certsn";

	public static final String PARAM_TYPE = "type";

	public static final String PARAM_ACTIVE = "active";

	public static final String PARAM_ENVSN = "envsn";

	public static final String PARAM_AUTHCODE = "authcode";

	public static final String PARAM_SIGNDATE = "signdate";
	
	public static final String PARAM_MSGWRAPPER = "msgWrapper";
	
	public static final String PARAM_ENCRYPT_DATE = "encryptData";
	
	public static final String PARAM_ENCRYPT_CERTSN = "certSN";

	public static final String TYPE_APPLYCERT = "ApplyCert";

	public static final String TYPE_LOGIN = "Login";

	public static final String TYPE_SIGN = "Sign";
	
    public static final String TYPE_SIGNEX = "SignEx";
	
	public static final String TYPE_ENVELOP_DECRYPT = "EnvelopeDecrypt";
	
	public static final String QR_Login = "QRLogin";
	
	public static final String QR_Sign = "QRSign";
	
    public static final String QR_SignEx = "QRSignEx";
	
	public static final String QR_EnvelopeDecrypt = "QREnvelopeDecrypt";
	
	public static final String QR_SEAL = "QRSeal";
	
	public static final String QR_SERVICEURL = "serviceUrl";
	
	public static final String QR_ACTIONNAME = "actionName";

	public static final String CERT_FILENAME = "cert.cer";

	public static final String WEBSERVICE_UPLOADPKCS10 = "/UploadPkcs10";

	public static final String WEBSERVICE_DOWNLOADCERT = "/DownloadCert";

	public static final String PREFERENCES_NAME = "settings";
	
	public static final String SETTINGS_NOTIFICATION_ENABLED = "SETTINGS_NOTIFICATION_ENABLED";
	
	public static final String SETTINGS_ALL_APP_INFO = "SETTINGS_ALL_APP_INFO";
	
	public static final String SETTINGS_BLUEBOOTH_DEVICE = "SETTINGS_BLUEBOOTH_DEVICE";
	
	public static final String SETTINGS_BLUEBOOTH_SIM_DEVICE = "SETTINGS_BLUEBOOTH_SIM_DEVICE";
	
	public static final String SETTINGS_FINGER_ENABLED = "SETTINGS_FINGER_ENABLED";

	public static final String SETTINGS_IFAA_FACE_ENABLED = "SETTINGS_IFAA_FACE_ENABLED";

	public static final String SETTINGS_PWD_ERR_COUNT = "SETTINGS_PWD_ERR_COUNT";
	
	public static final String SETTINGS_FINGER_OPENED = "SETTINGS_FINGER_OPENED";
	
	public static final String SETTINGS_PREMISSION_ENABLED = "SETTINGS_PREMISSION_ENABLED";
	
	public static final String SETTINGS_LOGIN_ACT_NAME = "SETTINGS_LOGIN_ACT_NAME";
	
	public static final String SETTINGS_CERT_PWD = "SETTINGS_CERT_PWD";
	
	public static final String SETTINGS_MSG_WRAPPER = "SETTINGS_MSG_WRAPPER";
	
	public static final String SETTINGS_FACE_AUTH_SIGN = "SETTINGS_FACE_AUTH_SIGN";
	
	public static final String SP_SETTINGS_GESTURE = "LockPatternSP";
	
	public static final String SETTINGS_GESTURE_KEYNAME = "sppsd";
	
	public static final String SETTINGS_GESTURE_OPENED = "SETTINGS_GESTURE_OPENED";
	
	public static final String VERSION_CODE = "versioncode";
	
	public static final String SHOW_UPDATE = "showupdate";
	
	public static final String CARD_SCAN_PICTURE = "cardscan";
	
	public static final String FACE_REG_PICTURE = "faceregpicture";
	
	public static final String FACE_REG_AUTH = "faceregauth";

	public static final int CERT_OPERATOR_TYPE_DOWNLOAD= 1;
	
	public static final int CERT_OPERATOR_TYPE_VIEW = 2;

	public static final int CERT_OPERATOR_TYPE_CHANGEPWD = 3;

	public static final int CERT_OPERATOR_TYPE_IMPORT = 4;

	public static final int CERT_OPERATOR_TYPE_DELETE = 5;
	
	public static final int CERT_OPERATOR_TYPE_LOGIN = 6;
	
	public static final int CERT_OPERATOR_TYPE_SIGN = 7;
	
	public static final int CERT_OPERATOR_TYPE_APPLY = 8;
	
    public static final int CERT_OPERATOR_TYPE_SIGNEX = 9;
	
	public static final int CERT_OPERATOR_TYPE_ENVELOP_DECRYPT = 10;
	
	public static final int CERT_OPERATOR_TYPE_SEAL = 11;
	
	public static final String CREDIT_APP_NAMEEX = "上海诚信网";
	
	public static final String CREDIT_APP_NAME = "上海市公共信用信息服务平台";
	
	public static final String CREDIT_APP_ID = "a1d92bd5-9b24-4cd5-9586-5f1e8ae35fc9";
	
    public static final String UTEST_APP_NAME = "UTest";
	
	public static final String UTEST_APP_ID = "61a10ef1-7d74-4e9d-96f0-5e69edc0e1f7";
	
    public static final String NETHELPER_APP_NAME = "ShecaUM";
	
	public static final String NETHELPER_APP_ID = "539b8df7-2333-404b-a660-dbe5a41c7f45";
	
	public static final String NETHELPER_APP_NAMEEX = "ShecaNetAssistant";
	
	public static final String SCAN_SIGN_NAME = "扫码签名";
	
	public static final String SCAN_LOGIN_NAME = "扫码登录";
	
    public static final String SCAN_SIGNEX_NAME = "批量签名";
	
	public static final String SCAN_ENVELOP_DECRYPT_NAME = "扫码解密";
	
	public static final String SCAN_SEAL_NAME = "扫码签章";
	
    public static final String UM_SCAN_NAME = "扫一扫";
	
	public static final String UM_SCAN_ID = "e2f18056-0d27-4af3-b744-0883f14836e3";
	
	public static final String UM_APP_NAME = "移证通";

	public static final int RETURN_CODE_OK = 0;



//	public static final String UM_APPID = "fb9cd5a6-95a3-4821-8916-c9048b5b245e";//"49beb9e1-0a98-4f8d-bba3-832095b656f1";  //内网配置
//	public static final String UM_APPID_CONFIG = "fb9cd5a695a348218916c9048b5b245e-a1d92bd59b244cd595865f1e8ae35fc9-61a10ef17d744e9d96f05e69edc0e1f7-539b8df72333404ba660dbe5a41c7f45-e2f180560d274af3b7440883f14836e3-ae83058d82c84faabe3e25dafc61c9cd-8fd575995b2648a8a3f0e4d05f36639b-730181c23ea64608ba66fbc0dfe94f57-74b74f77771d4c1399fc3b81ca0003fc-be15791b96a14c3b832038364fff63da-0edd6bbc6d7e48ae8c4476e4b2555180-6c0e28275863490a8a3d60da5cd05183-f02882dbf5cd494ebc3ef681198341cb-cf147f5fdf0444ffb2540e45ff2efbb0-ed35cffd0fcc4a629acc4b59bcae3c8e-b73675321f53457dbf12a6f8d858e709-2a5e89e6a50e43b289cd6810abc65e3d";  //内网配置列表
//	public static final String UM_APP_LICENSE = "YdOEObRRTL1wjvSVTC/Ey3Lg7jg=";  //移证通内网应用license
//	public static final String UM_APP_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANq7o94U/CJmTfRFdc8O/2KGfxLd50g9+y9PDVLKJB30vcgTDZhBigZXuUSjW5G9sRkvTykVBfzelx3c2bRHTxZIb1RkOw/wLJn2hrynlDKdmR2yM01jIJi2brlCQxuxvFD9hhI/BE7w6aoDqbra6C1A01HkoQi1yCQ6f479YcozAgMBAAECgYB0lzI4rWzcFTYWL3QlY3Qjm1dewiOG8WrTg8qxR4hK42rJm8ZSUxT0XowllwG3RKqCpyIF3uvrJubcBx2QJ7ZySMROLiC8viywmkUmwM5CQFy/f82UjSDDfAWnskwx5ijOl4mICMb3WSCQWts0hR5WRqQFUujN005LudhlHtyRwQJBAPXT4qXZ82QRpwm4l+IGDXPOfs9UmqzRCeYiE38AY1oV0FQXkWYTkiC12CfSVpW9cS2eJyg7F5E5CJfNkNCskRMCQQDjyLvSikaNx8CQrOyZuXohaGwY0gR7znjykqoOtrfxrdcNhVdrD0QyJV9DSKQEThklEIpP8+/0bt7Wu5X8ciZhAkEA6hdDlTvfEkPdFNy7hXjOBqF1EGo4gKjvDD8W4bG06mwrXmizBMlONef0VrdtzFoCFLauAxmCb8An9qbCaHRsEQJBAMGbpMlXbpCj8DEISjJ/TCeJMhgGqnjxDixgLNtEwyRQtjFv3NXKrBRTX5046PAf0Y+Hd4htts70TcQGvX1NKQECQDE/XZXIoinZmjYiPJDl55G4mgXck6CiB4Ik+uUiHZwFZtJL72zW50Z6zqdgHu9H1PefS1LbCz9yJ4w8fjjAFi0=";  //移证通内网应用签名私钥
//	public static final String UM_APP_AUTH_KEY = "470BAEE9D98E07F855016CA4A683060E";     //移证通外网应用authkeyID
//
//	public static final String  WEB_DOMAIN_CONFIG =  "202.96.220.166";//"192.168.15.41";   //"192.168.15.156" ;     //内网配置    "192.168.2.133";
//
//    public static final String  RSA_CERT_CHAIN = "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";
//
//	public static final String  SM2_CERT_CHAIN = "MIIFCwYJKoZIhvcNAQcCoIIE/DCCBPgCAQExADALBgkqhkiG9w0BBwGgggTgMIICKTCCAc2gAwIBAgIQRrEGsdL91GIFwtWMqXwfxjAMBggqgRzPVQGDdQUAMDwxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDEaMBgGA1UEAwwRVUNBIFNNMiBUZXN0IFJvb3QwHhcNMTMwMTAxMDAwMDAwWhcNNDkxMjMxMDAwMDAwWjA8MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxGjAYBgNVBAMMEVVDQSBTTTIgVGVzdCBSb290MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEkedyH9SjOChr+JFPhBghoItFW/Zn4TSTwxry+0HD4Glf3nLO9f9ioqDMNOQkjDH6KnPOHGKlWBdeW88CTw2spqOBrjCBqzAOBgNVHQ8BAQAEBAMCAQYwDwYDVR0TAQEABAUwAwEB/zBmBgNVHSUBAQAEXDBaBggrBgEFBQcDAQYIKwYBBQUHAwIGCCsGAQUFBwMDBggrBgEFBQcDBAYIKwYBBQUHAwUGCCsGAQUFBwMGBggrBgEFBQcDBwYIKwYBBQUHAwgGCCsGAQUFBwMJMCAGA1UdDgEBAAQWBBTqAewgJKTPvU2/prCfWHFDsN6dlzAMBggqgRzPVQGDdQUAA0gAMEUCIC0HjkHrHSE71uslFWmr1ccjPt10BC8EPWveskz1G02YAiEAkZKGBSz8ohxf0D4Kau/h3M3OLZjlTDyjB0cFiuOMD4IwggKvMIICU6ADAgECAhBb3Kg/62uZAMUW9HQnCqZ+MAwGCCqBHM9VAYN1BQAwPDELMAkGA1UEBhMCQ04xETAPBgNVBAoMCFVuaVRydXN0MRowGAYDVQQDDBFVQ0EgU00yIFRlc3QgUm9vdDAeFw0xMzAxMDEwMDAwMDBaFw0zNzEyMzEwMDAwMDBaMDkxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDEXMBUGA1UEAwwOU0hFQ0EgU00yIFRlc3QwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAR6F/desbL7RuE9SnYS5L6rvk8DQxtKLGHtAPU+6D1o7xGYRNcNeheYC7O2ITORieJaDWxlH+qqrAKsOioO4gaKo4IBNjCCATIwRQYDVR0gAQEABDswOTA3BgkqgRwBhu86gRUwKjAoBggrBgEFBQcCARYcaHR0cDovL3d3dy5zaGVjYS5jb20vcG9saWN5LzAOBgNVHQ8BAQAEBAMCAQYwDwYDVR0TAQEABAUwAwEB/zA6BgNVHR8BAQAEMDAuMCygKqAohiZodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc3ViLmNybDAiBgNVHSMBAQAEGDAWgBTqAewgJKTPvU2/prCfWHFDsN6dlzAgBgNVHQ4BAQAEFgQUnuoEUuFRuK5Nt4WTZhUpJTplntIwRgYIKwYBBQUHAQEBAQAENzA1MDMGCCsGAQUFBzAChidodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhcm9vdC5kZXIwDAYIKoEcz1UBg3UFAANIADBFAiBB0t8gh9JJ1NZabZ+FDLY2dadrdR2ooBTA3MyI0lvRZwIhAKafrS9+dzA5PHtDr4lzUKN1xM+oDW0v6oKFxf54n2OrMQA=";
//

	public static final String OCR_KEY = "a58d19dac985608bebc611b80f0-funatunvpn";    //OCR KEY

    public static final String UM_APPID = "fb9cd5a6-95a3-4821-8916-c9048b5b245e";    //外网配置
	public static final String UM_APPID_CONFIG = "fb9cd5a695a348218916c9048b5b245e-a1d92bd59b244cd595865f1e8ae35fc9-61a10ef17d744e9d96f05e69edc0e1f7-539b8df72333404ba660dbe5a41c7f45-e2f180560d274af3b7440883f14836e3-ae83058d82c84faabe3e25dafc61c9cd-8fd575995b2648a8a3f0e4d05f36639b-730181c23ea64608ba66fbc0dfe94f57-4cde28128175414d92b9441d2b4c8a50-85831191344345e58d4a52143e2ca18f";  //外网配置列表
	public static final String UM_APP_LICENSE = "seKqFWz84oFShtkDwzbY5hRWWk83JI8oVGM7r/TTZIg=";//"qh0LFFoF0bYjVjbHPuprznOxbh0=";  //移证通外网应用license
	public static final String UM_APP_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANq7o94U/CJmTfRFdc8O/2KGfxLd50g9+y9PDVLKJB30vcgTDZhBigZXuUSjW5G9sRkvTykVBfzelx3c2bRHTxZIb1RkOw/wLJn2hrynlDKdmR2yM01jIJi2brlCQxuxvFD9hhI/BE7w6aoDqbra6C1A01HkoQi1yCQ6f479YcozAgMBAAECgYB0lzI4rWzcFTYWL3QlY3Qjm1dewiOG8WrTg8qxR4hK42rJm8ZSUxT0XowllwG3RKqCpyIF3uvrJubcBx2QJ7ZySMROLiC8viywmkUmwM5CQFy/f82UjSDDfAWnskwx5ijOl4mICMb3WSCQWts0hR5WRqQFUujN005LudhlHtyRwQJBAPXT4qXZ82QRpwm4l+IGDXPOfs9UmqzRCeYiE38AY1oV0FQXkWYTkiC12CfSVpW9cS2eJyg7F5E5CJfNkNCskRMCQQDjyLvSikaNx8CQrOyZuXohaGwY0gR7znjykqoOtrfxrdcNhVdrD0QyJV9DSKQEThklEIpP8+/0bt7Wu5X8ciZhAkEA6hdDlTvfEkPdFNy7hXjOBqF1EGo4gKjvDD8W4bG06mwrXmizBMlONef0VrdtzFoCFLauAxmCb8An9qbCaHRsEQJBAMGbpMlXbpCj8DEISjJ/TCeJMhgGqnjxDixgLNtEwyRQtjFv3NXKrBRTX5046PAf0Y+Hd4htts70TcQGvX1NKQECQDE/XZXIoinZmjYiPJDl55G4mgXck6CiB4Ik+uUiHZwFZtJL72zW50Z6zqdgHu9H1PefS1LbCz9yJ4w8fjjAFi0=";
	//"MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALfh6LpX68vQ33tRQq2TVo/DbHM6HBVk5QlNvSspk4IsyJPdQxh/4XULHz/FJ32v3XyFupOrZRTqGVS5JH+Vlp329/6zfp8NrrAu3BcfrDrJlBLWW3mMbLh2iAKrF+jbAPTMM93DzOC2BjtgLsJ5SVHdB4q2Q8oPxDoxX7k3uCLxAgMBAAECgYAZK7Bayaw6UxY0YDQ7ZF7GAR84Sq0LdKsxcdXCMcfCWvT94JzNnruy9NnLNeao+sjbwcZ/bkfA7CaZhVpsqIJcU0GD9bIYGjt/hXd6WRorug4IxF7R6zHUiOKcMvP+m7oUtynA4zc2Zppv+ybHFO3VuL6Gs2yJK+FfRvd/0i76UQJBAPgqinv1rzVviBAAhf9rxBWkCjrAHbCEaRXWc1SxQJEfgPuPms+cUWbYP927BrF2VSStDgEFIUFOoNEQ7+h3iGUCQQC9r+JVNhKtpzxpMexeavWRurRFcFFUI3H40SW1TrpU4EUQv+IheV35odLUErP/nX3NYLID8P6wbG834MjcrzmdAkADiG+CRkQhSZ4xRCCkOz+GY1h176g8jBYG5o+rw/48Uqt+aVP14q7R/QAjwvbcbefWZtRNNWp8yIbHkysnLxR1AkBIWZ7Fi6NiMuh64zzZ4ogZ9pGt7qJGqmJdWkC8v/OYThZpxhaVV/p9LbkBhMS9GbATNu+PS0uJtUJSmN1kNz2NAkAj5yC8+ZYMQysW9OkoLPhSWmFqlRP/gvZqQIeua/NW19m+CWg3qH8lu1jRrjxOWaa4eHFMYrhxk9/6swPu3Pht";  //移证通外网应用签名私钥
	public static final String UM_APP_AUTH_KEY = "470BAEE9D98E07F855016CA4A683060E";//"0846466DDE82AC4DA09571AB27DA845E";     //移证通外网应用authkeyID
	
	public static final String  WEB_DOMAIN_CONFIG = "umsp.sheca.com"; //"192.168.2.220"; //"192.168.2.178";   //外网配置
	
	public static final String RSA_CERT_CHAIN = "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";
	
	public static final String SM2_CERT_CHAIN =  "MIIEbgYJKoZIhvcNAQcCoIIEXzCCBFsCAQExADALBgkqhkiG9w0BBwGgggRDMIIBszCCAVegAwIBAgIIaeL+wBcKxnswDAYIKoEcz1UBg3UFADAuMQswCQYDVQQGEwJDTjEOMAwGA1UECgwFTlJDQUMxDzANBgNVBAMMBlJPT1RDQTAeFw0xMjA3MTQwMzExNTlaFw00MjA3MDcwMzExNTlaMC4xCzAJBgNVBAYTAkNOMQ4wDAYDVQQKDAVOUkNBQzEPMA0GA1UEAwwGUk9PVENBMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEMPCca6pmgcchsTf2UnBeL9rtp4nw+itk1Kzrmbnqo05lUwkwlWK+4OIrtFdAqnRTV7Q9v1htkv42TsIutzd126NdMFswHwYDVR0jBBgwFoAUTDKxl9kzG8SmBcHG5YtiW/CXdlgwDAYDVR0TBAUwAwEB/zALBgNVHQ8EBAMCAQYwHQYDVR0OBBYEFEwysZfZMxvEpgXBxuWLYlvwl3ZYMAwGCCqBHM9VAYN1BQADSAAwRQIgG1bSLeOXp3oB8H7b53W+CKOPl2PknmWEq/lMhtn25HkCIQDaHDgWxWFtnCrBjH16/W3Ezn7/U/Vjo5xIpDoiVhsLwjCCAogwggIsoAMCAQICEC2hpr0M52xiTmNLfWop0CowDAYIKoEcz1UBg3UFADAuMQswCQYDVQQGEwJDTjEOMAwGA1UECgwFTlJDQUMxDzANBgNVBAMMBlJPT1RDQTAeFw0xMzA5MTMwODEwMjVaFw0zMzA5MDgwODEwMjVaMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEfdEfkS0GSlQQ8ISEVSUdvKL7tcd3bsNssWlmmOhN5VCg1iLJgMDDqhO9TFt4EDsZuvECXz8uiU+BL4pddBcMgKOCASIwggEeMB8GA1UdIwQYMBaAFEwysZfZMxvEpgXBxuWLYlvwl3ZYMA8GA1UdEwEB/wQFMAMBAf8wgboGA1UdHwSBsjCBrzBBoD+gPaQ7MDkxCzAJBgNVBAYTAkNOMQ4wDAYDVQQKDAVOUkNBQzEMMAoGA1UECwwDQVJMMQwwCgYDVQQDDANhcmwwKqAooCaGJGh0dHA6Ly93d3cucm9vdGNhLmdvdi5jbi9hcmwvYXJsLmNybDA+oDygOoY4bGRhcDovL2xkYXAucm9vdGNhLmdvdi5jbjozODkvQ049YXJsLE9VPUFSTCxPPU5SQ0FDLEM9Q04wDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBSJMQSRe0Oqqpq/hB2bhu7wuHCZoDAMBggqgRzPVQGDdQUAA0gAMEUCIQCKe/9772vmcRXjynapM3RqFchrHxh4Yiy0HiqwmUNkOQIgJjDnX9H9G/Aopa1VnYvKX2cjukamH56XCet/Jeyh3zQxAA==";
	
	public static final String PARAM_ACCOUNT_NAME_PWD = "accountName";
	
	public static final String PARAM_ACCOUNT_NAME = "AccountName";
	
	public static final String PARAM_UM_APPID = "AppID";
	
	public static final String PARAM_PWD_BIZSN = "BizSN";
	
	public static final int FACE_RECOGNITION_FAIL_COUNT = 3;   //申请人工审核次数
	
	public static final String HTTPS_VALID_CERT_NAME = "umsp.sheca.com";
	
	public static final String JSHECACCISTD_APPID= "201508261832380565579";    //创元中间件APPID
	
	public static final String JSHECACCISTD_SERVICE_URL= "https://umsp.sheca.com:8443/mshield-ca-inf/client/securityInfo";  //创元中间件安全服务平台链接地址   192.168.2.241
	
	public static final int JSHECACCISTD_TIMEOUT = 6000;   //创元中间件安全服务平台连接超时时间
	
	public static final String PARAM_CERT_VALIDITY = "validity";
	
	public static final int CERT_TYPE_SM2_VALIDITY = 12;   //sm2证书有效期
	
	public static final int CERT_TYPE_SM2_VALIDITY_ONE_YEAR = 12;   //sm2证书有效期(一年）
	
	public static final String JSHECACCISTD_PWD= "11111111";  //创元中间件默认pin	
	
	public static final String BT_DEVICE_DEFAULT_PWD= "12345678";  //蓝牙key初始pin	
	
	public static final int ACCOUNT_TYPE_PERSONAL = 1;   //个人账户
	
	public static final int ACCOUNT_TYPE_COMPANY = 2;   //企业账户

	public static final int CERT_TYPE_RSA_INT = 1; //rsa

	public static final int CERT_TYPE_SM2_INT = 2;//sm2

    public static final String CERT_TYPE_RSA= "个人移动证书_SHECA";  //个人rsa证书类型  int 1

//	public static final String CERT_TYPE_RSA= "个人证书申请RSA";

	public static final String CERT_TYPE_SM2= "个人移动证书_SHECA_SM2";  //个人sm2证书类型 int 2

//	public static final String CERT_TYPE_SM2= "个人证书申请SM2";

	public static final String CERT_TYPE_RSA_COMPANY = "单位移动证书_SHECA";  //企业rsa证书类型
	
	public static final String CERT_TYPE_SM2_COMPANY="单位移动证书_SHECA_SM2";  //企业sm2证书类型
	
	public static final String CERT_ALG_RSA = "SHA1withRSA"; // rsa证书签名摘要算法
	
    public static final String CERT_ALG_SM2="SM3withSM2";   //sm2证书签名摘要算法
    
    public static final String UM_APP_SIGN_ALG = "SHA1WithRSA"; // rsa证书签名摘要算法
	
	//public static final String USE_CERT_ALG_RSA = "SHA1withRSA"; // rsa证书签名摘要算法(证书使用)
	
	//public static final int  CERT_MOUDLE_SIZE = 1024;   
	
    public static final String USE_CERT_ALG_RSA = "SHA256withRSA"; // rsa证书签名摘要算法(证书使用)
    
    public static final String USE_CERT_ALG_SM2 = "SM3withSM2";   // sm2证书签名摘要算法(证书使用)
    
    public static final String USE_CERT_SCAN_ALG_RSA = "SHA256WithRSA"; // rsa证书扫码签名摘要算法(证书扫码使用)
    
    public static final String USE_CERT_SCAN_ALG_RSA_BLUETOOTH = "SHA1WithRSA"; //蓝牙key rsa证书扫码签名摘要算法(证书扫码使用)
    
    public static final String USE_CERT_SCAN_ALG_SM2 = "SM2WithSM3";   // sm2证书扫码签名摘要算法(证书扫码使用)
	
	public static final int  CERT_MOUDLE_SIZE = 2048;   
	
	public static final String CERT_RSA_NAME= "RSA";  //rsa证书
	
	public static final String CERT_SM2_NAME= "SM2";  //sm2证书
	
	public static final int CERT_ALG_TYPE_SIGN = 1;   //签名证书标识
	
	public static final int CERT_ALG_TYPE_ENC = 2;   //加密证书标识
	
    public static final int CERT_SIGN_ALG_TYPE_RSA = 1;   //证书签名摘要算法。（1：RSAWithSHA1；2：SM2WithSM3）
	
	public static final int CERT_SIGN_ALG_TYPE_SM2 = 2;   //证书签名摘要算法。（1：RSAWithSHA1；2：SM2WithSM3）
	
	
	public static final int  SIGNALG_TYPE = 3;      //1：SHA1WithRSA；2：SM2WithSM3 3：SHA256WithRSA
	
	public static final int  SIGNALG_TYPE_SM2 = 2;      //1：SHA1WithRSA；2：SM2WithSM3 3：SHA256WithRSA
	
	public static final int  UPLOAD_LOG_SIGNALG_TYPE = 2;    //1：SHA1WithRSA；2：SHA256WithRSA  3：SM2WithSM3
	
	public static final int  UPLOAD_LOG_SIGNALG_TYPE_SM2 = 3;    //1：SHA1WithRSA；2：SHA256WithRSA  3：SM2WithSM3
	
	public static final int  SAVE_CERT_TYPE_PHONE = 1;      //介质类别1.本地手机；2.蓝牙key 3.音频key 4.蓝牙sim卡 
	
	public static final int  SAVE_CERT_TYPE_BLUETOOTH = 2;  
	
	public static final int  SAVE_CERT_TYPE_AUDIO = 3;   
	
	public static final int  SAVE_CERT_TYPE_SIM = 4;  
	
	public static final int  USE_NO_FINGER_TYPE = 0;  
	
	public static final int  USE_FINGER_TYPE = 1;  
	
	public static final int  ACCOUNT_STATE_TYPE1 = 0;  
	
	public static final int  ACCOUNT_STATE_TYPE2 = 1;  
	
	public static final int  ACCOUNT_STATE_TYPE3 = 2;  
	
	public static final int  ACCOUNT_STATE_TYPE4 = 3;  
	
	public static final int  ACCOUNT_STATE_TYPE5 = 4;  
	
    public static final String SAVE_CERT_TYPE_PHONE_NAME= "移动设备";  
	
	public static final String SAVE_CERT_TYPE_BLUETOOTH_NAME= "蓝牙KEY"; 
	
	public static final String SAVE_CERT_TYPE_AUDIO_NAME= "音频KEY"; 
	
	public static final String SAVE_CERT_TYPE_SIM_NAME= "蓝牙SIM卡";  
	
	public static final int  SAVE_CERT_TYPE_RSA = 1;      //证书类别 1.RSA；2.SM2
	
	public static final int  SAVE_CERT_TYPE_SM2 = 2;  
	
    public static final String RETURN_CODE = "returnCode";
	
	public static final String RETURN_MSG = "returnMsg";
	
	public static final String RETURN_RESULT = "result";
	
	public static final String PARAM_APPID = "appID";
	
	public static final String PARAM_APP_NAME = "appName";
	
	public static final String PARAM_CCOUNT_UID = "accountUID";
	
	public static final String PARAM_CCOUNT_TYPE = "accountType";
	
	public static final String PARAM_REQUEST_NUMBER = "requestNumber";
	
	public static final String PARAM_COMMON_NAME = "commonName";
	
	public static final String PARAM_APPLY_NAME = "applyTime";
	
	public static final String PARAM_APPLY_STATUS = "applyStatus";

	public static final String PARAM_APPLY_STATUS_EX= "status";

	
	public static final String PARAM_PAY_STATUS = "payStatus";
	
	public static final String PARAM_CERT_TYPE = "certType";
	
	public static final String PARAM_SIGNALG_PLUS = "signAlg";
	
	public static final String PARAM_STATUS = "status";

	public static final String PARAM_V26_DBCHECK = "PV26DBCHECK";
	
    public static final String PARAM_NAME = "name";
	
	public static final String PARAM_IDENTITY_CODE = "identityCode";
	
	public static final String PARAM_TASK_GUID = "taskGUID";
	
	public static final String PARAM_VISIBILITY = "visibility";
	
    public static final String PARAM_CONTACT_PERSON = "contactPerson";
	
	public static final String PARAM_CONTACT_PHONE = "contactPhone";
	
	public static final String PARAM_CONTACT_EMAIL = "contactEmail";
	
	public static final String PARAM_ASSIGN_TIME = "assignTime";
	
	public static final String PARAM_COPY_IDPHOTO = "copyIDPhoto";
	
	public static final String PARAM_ORG_NAME = "orgName";
	
	public static final String PARAM_ISREAL = "isReal";
	
	public static final String PARAM_CODE = "code"; 
	
	public static final String PARAM_MOBILE = "mobile"; 
	
	public static final String PARAM_PWD_HASH = "pwdHash"; 
	
	public static final String RESULT_PARAM_REQUEST_NUMBER = "requestNumber";
	
    public static final String RESULT_PARAM_USER_CERT = "userCert";
	
	public static final String RESULT_PARAM_ENC_CERT = "encCert";
	
	public static final String RESULT_PARAM_ENC_KEYT = "encKey";
	
	public static final String RESULT_PARAM_CERT_CHAIN = "certChain";
	
	public static final String RESULT_PARAM_ENC_ALG = "encAlgorithm";
	
	public static final String RESULT_PARAM_VERSION = "version";
	
	public static final String RESULT_PARAM_DOWNLOADURL = "downloadURL";
	
	public static final String RESULT_PARAM_DESCRIPTION = "description";
	
	public static final String RESULT_PARAM_COMPULSION = "compulsion";
	
    public static final String RESULT_PARAM_BIZSN = "BizSN";
	
	public static final String RESULT_PARAM_PERSON_NAME = "PersonName";
	
	public static final String RESULT_PARAM_PERSON_ID = "PersonID";
	
	public static final String RESULT_PARAM_SIGN_ALG = "SignatureAlgorithm";
	
	public static final String RESULT_PARAM_SIGN_VALUE = "SignatureValue";
	
	public static final String RESULT_PARAM_REASON = "reason";
	
	public static final String RESULT_PARAM_SEALDATE = "sealData";
	
	public static final String RESULT_PARAM_SEALSN = "sealSn";
	
	public static final String RESULT_PARAM_VERIFY = "verify";
	
	public static final String RESULT_PARAM_VID = "vid";
	
	public static final String RESULT_PARAM_SEALNAME = "sealName";
	
	public static final String RESULT_PARAM_ISSUERCERT = "issuerCert";
	
	public static final String RESULT_PARAM_CERT = "cert";
	
	public static final String RESULT_PARAM_PICDATE = "picData";
	
	public static final String RESULT_PARAM_PICTYPE = "picType";
	
	public static final String RESULT_PARAM_PICWIDTH = "picWidth";
	
	public static final String RESULT_PARAM_PICHEIGHT = "picHeight";
	
	public static final String RESULT_PARAM_NOTBEFORE = "notBefore";
	
	public static final String RESULT_PARAM_NOTAFTER = "notAfter";
	
	public static final String RESULT_PARAM_SIGNAL = "signAlg";
	
	public static final String RESULT_PARAM_OID = "oid";
	
	public static final String RESULT_PARAM_VALUE = "value";
	
	public static final String RESULT_PARAM_EXTENSIONS = "extensions";
	
	
	public static final String INPUT_RSA_SIGN = "inputRSASign";
	
	public static final String INPUT_SM2_SIGN = "inputSM2Sign";
	
	public static final String INPUT_SM2_ENC = "inputSM2Enc";
	
	public static final String UDESK_DOMAIN = "sheca.udesk.cn"; 

	public static final String UDESK_SECRETKEY = "f0cd822197379c7bcd498fc73bbde88e"; 
	
	public static final String REVOKE_CERT_REASON = "撤销证书"; 
	
	public static final String UM_SPLIT_STR = ",";   //分隔字符
	
	public static final String SIGN_STR_CODE = "UTF-8";
	
	public static final String SIGN_STR_CODE_UTF8 = "UTF-8";
	
	public static final int KS_RSA_SIGN_ALG = 3;
	
	public static final String CERT_VALID_NAME_ONE_YEAR = "1年";
	
	public static final String CERT_VALID_NAME_THREE_MONTH = "1年";
	
    public static final String CERT_VALID_DESC_ONE_YEAR = "(30元)";
	
	public static final String CERT_VALID_DESC_THREE_MONTH = "";
	
	public static final String PAY_TYPE_BY_WX = "微信支付";
	
	public static final String PAY_TYPE_BY_ALIPAY= "支付宝";
	
	public static final int PAY_TYPE_USE_WX = 1;       //使用微信支付
	
	public static final int PAY_TYPE_USE_ALIPAY = 2;   //使用支付宝支付
	
	public static final int LOGIN_BY_PWD = 1;          //以帐号密码方式登录
	
	public static final int LOGIN_BY_MSG = 2;          //以短信验证码方式登录
	
	public static final String SEAL_PIC_TYPE = "PNG";
	
	public static final String SEAL_SCAN_APP_CODE = "GSYWXT";  //扫码签章正式appcode
		
    public static final String SEAL_SCAN_APP_PWD = "gs201809";   //扫码签章正式apppwd
	
	//public static final String YGT_APP_AUTH_KEY = "A0DA64BDCE495FC3EF4AC8615DA8773D";     //一窗通内网应用authkeyID
    
    public static final int RET_VERIFY_CERT_OK = 0;          //验证证书成功返回值
    
    public static final String NET_CONNECT_ERROR_CODE = "404";
	
	public static final String NET_CONNECT_ERROR_MSG = "网络连接异常或无法访问服务";

	public static final String STATE_TRUE = "true";
	public static final String STATE_FALSE = "false";

	//人脸免密键
	public static final String FACE_NOPASS="FACE_NOPASS";

	public static final int MAX_ERR_PASSWORD_COUNT = 6;

	public static final String FIRST_SMS_LOGIN = "firstSmsLogin";

	public final static String ESAND_DEV_SERVER_URL = "http://bizserver.dev.esandinfo.com:80/gateway";

	public final static String IFFA_OLD_APP_ID = "Tti2vVQIeg2w7TYjufoIu76150U";

	public final static String IFFA_NEW_APP_ID = "S/esvFBXNRhpsJNmGsmEmmpulj8";

	public final static int DEFAULT_CERT_VALIDITY = 12;

	public static final String RESULT_PARAM_ISUPDATE = "isUpdate";

	//app版本更新

	public final static String PARAM_CLIENT_NAME = "clientName";

	public final static String PARAM_CURENT_VERSION = "currentVersion";

	public final static  String   UM_APP_NAME_EX = "移证通";
}
