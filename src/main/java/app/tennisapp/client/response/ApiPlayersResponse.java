package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiPlayersResponse(
        Integer success,
        List<ApiPlayerDto> result
) {}
