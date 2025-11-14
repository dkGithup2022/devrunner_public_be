package dev.devrunner.elasticsearch.api;

import dev.devrunner.model.common.Popularity;

public interface DocPopularityManager {

    public void updatePopularity(String docId, Popularity popularity);

}
