package com.team04.playlist.global.config;

import com.team04.playlist.auth.dto.TokenResponse;
import com.team04.playlist.global.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String AUTH_TAG = "인증";

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("api")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(jwtSecurity())
                .addOpenApiCustomizer(oauth2LoginPaths())
                .build();
    }

    // Swagger 우측 상단 Authorize 버튼으로 access 토큰을 넣고 API를 호출할 수 있게 한다.
    private OpenApiCustomizer jwtSecurity() {
        return openApi -> {
            components(openApi).addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
            openApi.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
        };
    }

    // 소셜 로그인 경로는 컨트롤러가 아니라 Security 필터(oauth2Login)가 처리해서
    // springdoc이 자동으로 찾지 못한다. 문서에 직접 추가한다.
    private OpenApiCustomizer oauth2LoginPaths() {
        return openApi -> {
            registerSchema(openApi, TokenResponse.class);
            registerSchema(openApi, ErrorResponse.class);

            Operation start = new Operation()
                    .addTagsItem(AUTH_TAG)
                    .summary("소셜 로그인 시작")
                    .description("""
                            카카오 로그인 페이지로 이동(302)합니다.
                            Swagger의 Try it out으로는 동작하지 않으니 브라우저 주소창에서 직접 열어 주세요.
                            로그인이 끝나면 콜백(/login/oauth2/code/{provider})에서 토큰이 응답됩니다.""")
                    .security(List.of())
                    .addParametersItem(providerParameter())
                    .responses(new ApiResponses()
                            .addApiResponse("302", new ApiResponse().description("카카오 로그인 페이지로 이동")));

            Operation callback = new Operation()
                    .addTagsItem(AUTH_TAG)
                    .summary("소셜 로그인 콜백")
                    .description("카카오가 로그인 완료 후 호출합니다. 클라이언트가 직접 호출하지 않습니다.")
                    .security(List.of())
                    .addParametersItem(providerParameter())
                    .addParametersItem(queryParameter("code", "카카오가 발급한 인가 코드"))
                    .addParametersItem(queryParameter("state", "위조 방지용 state 값"))
                    .responses(new ApiResponses()
                            .addApiResponse("200", jsonResponse("로그인 성공, 액세스 토큰은 바디로, 리프레시 토큰은 httpOnly 쿠키(refreshToken)로 발급", TokenResponse.class))
                            .addApiResponse("401", jsonResponse("소셜 로그인 실패 (OAUTH_LOGIN_FAILED)", ErrorResponse.class)));

            openApi.path("/oauth2/authorization/{provider}", new PathItem().get(start));
            openApi.path("/login/oauth2/code/{provider}", new PathItem().get(callback));
        };
    }

    private static Parameter providerParameter() {
        return new Parameter()
                .in("path")
                .name("provider")
                .required(true)
                .description("소셜 로그인 제공자")
                .schema(new StringSchema()._enum(List.of("kakao")));
    }

    private static Parameter queryParameter(String name, String description) {
        return new Parameter()
                .in("query")
                .name(name)
                .required(true)
                .description(description)
                .schema(new StringSchema());
    }

    private static ApiResponse jsonResponse(String description, Class<?> type) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + type.getSimpleName()))));
    }

    private static void registerSchema(OpenAPI openApi, Class<?> type) {
        ModelConverters.getInstance().read(type).forEach(components(openApi)::addSchemas);
    }

    private static Components components(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        return openApi.getComponents();
    }
}
