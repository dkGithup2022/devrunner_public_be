package dev.devrunner.elasticsearch.vector;

import java.util.List;

public interface E5Vectorizer {

    List<Float> vectorize(String content);
}
