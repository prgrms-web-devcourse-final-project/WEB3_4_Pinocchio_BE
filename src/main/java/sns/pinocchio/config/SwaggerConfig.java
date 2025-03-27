package sns.pinocchio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "Authorization";

		return new OpenAPI()
			.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes(securitySchemeName,
					new SecurityScheme()
						.name(securitySchemeName)
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)))
			.info(new Info().title("Pinocchio API").version("v1.0"));
	}
}