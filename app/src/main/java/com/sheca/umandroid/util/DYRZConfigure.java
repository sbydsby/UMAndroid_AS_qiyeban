package com.sheca.umandroid.util;

/**
 * @author xuchangqing
 * @time 2020/1/7 16:05
 * @descript 多源认证参数
 */
public class DYRZConfigure {

    private static DYRZConfigure config;

    public static DYRZConfigure getInstance(){
        if (config == null){
            config = new DYRZConfigure();
        }
        //根据某种条件判断是否需要动态修改各种属性值
//        config.host = "";
        return config;
    }

    private DYRZConfigure(){}

    //正式环境
    private final String appID = "3d012b7a-8e6a-4491-ac4b-995053dbd363";
    private final String priKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCd4qkif+yrnDCcOdVAHbF7xuZ9J0dQYGbmxa01r/o/NKi9CIuQ24+vKiABdvug9pluIWU0x1jNI3dVaR7BP8Qz4m71EIxWsI00K6Ttj29rn1uZGRumEICvv7FboQqf5WRchcz6X30U8YtoDuQIRjd1SJpPPd6aQQM/q8o5sAdBmvTLmlixx2IrLxuOgciq4QP2+5rncXnwFvBpH4koBHFWRAooKD035P4KfG8x7I5JSDKcZQCx5oE5ib/8BK19YGLLRQsbU/zLrYYDfpprdmX9YJHWpcVQmut+6X4W5aKUQMp1ya0132VvPwfsxOv19yicvwpqUSzhesgqdjT2/nXRAgMBAAECggEAUFYb360d/Pg9l9CpnLU/MQ81Cquo0tIqqfQdvXqsp+qgjYdClNPTmbqijHxBb1brHSmq+J8SpVcNio0xk/1hVVE7SkFAV4AAaXJnumVIuYddsVogQnCVOlgc49XetRug5J9EfIWH/NcnKyyghUFNSMVHl1iQ4wZ/o69C9/WEy7FCXWaVrXi3KELeM6YiS9nXVVF/g0mHDI1AGgaz2EOKuRlFv3bwjFgtfBkoK7SJhppfQdU2e0oxzCBKXMaEH7RwNB/hCgZuoBLzB6YxN8EiO6VQ9LxFQ8VcEt9/2UauMktXwjIT++KLpReII4CeZ81FxJxcdnS56osH3HJua5S9UQKBgQDgJTVAfshkNoBvMZsYfE7rXqvBC56JHMTxjwaPDyoixfIkIHAKcb4wqjZSDX4sUtzppQm8yCz11zlq6iLDPufUKN2llQ3eShScVxI5fXDZthPOXnfzT+9FH7uhlI9bE9OSM1WTusdZ0a5Mp6eH3xqw452yy3TVHFjezgGR7n+PvQKBgQC0Us1SktSz/U+XiBigZf8bIIn5SB0FD6CZ+dm8a+fZVVLGX3IAR/wPEJVOYfiYTlHgLuz2PBebt8mF5OWlkLGBN/a08TlkxMH3wYZ7H6RjyuB8DxiziGpd/CapoPDJpcU4GB/mAAmfygk3y8LP/+T//PounLmgEY5SK2fszMqlpQKBgQCK94akjfY/62qFhgPaknd7hh6GeWW5LJUODtbKK2S8Wm1d6J6OWybHlUBIbwUjBdzhHumCmbkGUoHOv4r98ipZZ1zEJD2M6/ELXRB3hEZLNI/jCr7jo1N9T8dMzoZyuUAbDKiqBstB7LZb4DYGD1TFBtnm2C1/UyvmN2LRz0RXIQKBgQCe/yuflWwLUF2vpQbh101649QEg3tjDQZ0/gLoytRho4Qa+emRKeeseNjNhkEmOlijLX0vGIlQelssvklnFRAOegQEQ3ZxzbOh+3fSdHIEs8wAV2dWVgBD4qNMimYFxy8AaPUnXf0ecYxzMC0ULfQDa1JFa1eZNiGhfjhiaah9KQKBgG8i80FikBAdCRO83YeU/inhh0lZQC/fcJkPrJwGtNswOpshp7Z9SMvxEwLEa96MCz8BoyQADL3L9SjnyUDGEK7o4S6DKRi07r9iv7dLH2K8xH5ZBWysvmPO551GoC+wgJiH655422us6PzHj/t7mvsJP36GmR3xxt2OZWxTD+fs";           // 需向多源平台申请

    //测试环境

    public String getAppID() {
        return appID;
    }

    public String getPriKey() {
        return priKey;
    }
}
