package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.json.simple.JSONObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "enabled",
    "id",
    "requests",
    "concurrency",
    "staging_url",
    "qa_url",
    "protocol",
    "method",
    "path",
    "port",
    "body",
    "query_params",
    "regions"
})
public class TestField {

    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("id")
    private String id;
    @JsonProperty("requests")
    private Integer requests;
    @JsonProperty("concurrency")
    private Integer concurrency;
    @JsonProperty("staging_url")
    private String stagingUrl;
    @JsonProperty("qa_url")
    private String qaUrl;
    @JsonProperty("protocol")
    private String protocol;
    @JsonProperty("method")
    private String method;
    @JsonProperty("path")
    private String path;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("body")
    private String body;
    @JsonProperty("query_params")
    private String queryParams;
    @JsonProperty("regions")
    private String regions;
    @JsonProperty("headers")
    private JSONObject headers;

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("requests")
    public Integer getRequests() {
        return requests;
    }

    @JsonProperty("requests")
    public void setRequests(Integer requests) {
        this.requests = requests;
    }

    @JsonProperty("concurrency")
    public Integer getConcurrency() {
        return concurrency;
    }

    @JsonProperty("concurrency")
    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    @JsonProperty("staging_url")
    public String getStagingUrl() {
        return stagingUrl;
    }

    @JsonProperty("staging_url")
    public void setStagingUrl(String stagingUrl) {
        this.stagingUrl = stagingUrl;
    }

    @JsonProperty("qa_url")
    public String getQaUrl() {
        return qaUrl;
    }

    @JsonProperty("qa_url")
    public void setQaUrl(String qaUrl) {
        this.qaUrl = qaUrl;
    }

    @JsonProperty("protocol")
    public String getProtocol() {
        return protocol;
    }

    @JsonProperty("protocol")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @JsonProperty("method")
    public String getMethod() {
        return method;
    }

    @JsonProperty("method")
    public void setMethod(String method) {
        this.method = method;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    @JsonProperty("query_params")
    public String getQueryParams() {
        return queryParams;
    }

    @JsonProperty("query_params")
    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    @JsonProperty("regions")
    public String getRegions() {
        return regions;
    }

    @JsonProperty("regions")
    public void setRegions(String regions) {
        this.regions = regions;
    }

    @JsonProperty("headers")
    public JSONObject getHeaders() {
        return headers;
    }

    @JsonProperty("headers")
    public void setHeaders(JSONObject headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("enabled", enabled).append("id", id).append("requests", requests).append("concurrency", concurrency).append("stagingUrl", stagingUrl).append("qaUrl", qaUrl).append("protocol", protocol).append("method", method).append("path", path).append("port", port).append("body", body).append("queryParams", queryParams).append("regions", regions).append("headers", headers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(stagingUrl).append(regions).append(method).append(queryParams).append(requests).append(body).append(enabled).append(concurrency).append(path).append(protocol).append(port).append(qaUrl).append(id).append(headers).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TestField) == false) {
            return false;
        }
        TestField rhs = ((TestField) other);
        return new EqualsBuilder().append(stagingUrl, rhs.stagingUrl).append(regions, rhs.regions).append(method, rhs.method).append(queryParams, rhs.queryParams).append(requests, rhs.requests).append(body, rhs.body).append(enabled, rhs.enabled).append(concurrency, rhs.concurrency).append(path, rhs.path).append(protocol, rhs.protocol).append(port, rhs.port).append(qaUrl, rhs.qaUrl).append(id, rhs.id).isEquals();
    }

}
