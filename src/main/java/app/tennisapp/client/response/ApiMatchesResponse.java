package app.tennisapp.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiMatchesResponse(
        Integer success,
        List<ApiMatchDto> result
) {}
