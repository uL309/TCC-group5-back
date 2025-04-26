package puc.airtrack.airtrack.Login;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseDTO(@JsonProperty("name") String username, @JsonProperty("token") String JWT) {
}
