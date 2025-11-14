package dev.devrunner.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDocKeywordBoost {

    @JsonProperty("doc_id")
    private String docId;

    @JsonProperty("canonical")
    private String canonical;

    @JsonProperty("group")
    private String group;

    @JsonProperty("boost")
    private Double boost;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("text")
    private String text;

    @Override
    public String toString() {
        return "JobDocKeywordBoost{" +
                "docId='" + docId + '\'' +
                ", canonical='" + canonical + '\'' +
                ", group='" + group + '\'' +
                ", boost=" + boost +
                ", priority=" + priority +
                ", text='" + text + '\'' +
                '}';
    }
}
