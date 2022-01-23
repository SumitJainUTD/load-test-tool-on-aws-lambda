package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "testFields"
})
public class TestData {

    @JsonProperty("testFields")
    private List<TestField> testFields = new ArrayList<TestField>();

    @JsonProperty("testFields")
    public List<TestField> getTestFields() {
        return testFields;
    }

    @JsonProperty("testFields")
    public void setTestFields(List<TestField> testFields) {
        this.testFields = testFields;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("testFields", testFields).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(testFields).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TestData) == false) {
            return false;
        }
        TestData rhs = ((TestData) other);
        return new EqualsBuilder().append(testFields, rhs.testFields).isEquals();
    }

}
