package dev.devrunner.openai.base;

public interface SingleGptRunner<T> {

    T run(GptParams params);

}
