package dev.devrunner.openai.base;

public record GptParams(

        String userPrompt,
        String model
) {

    public static String NANO_MODEL="gpt-4.1-nano";
    public static String MINI_MODEL="gpt-4.1-mini";
    public static String NORMAL_MODEL="gpt-4.1-normal";

    public GptParams( String userPrompt, String model) {
        this.userPrompt = userPrompt;
        this.model = model;
    }

    public static GptParams of(String userPrompt) {
        return new GptParams(userPrompt, "gpt-4.1-nano");
    }

    public static GptParams ofMini(String userPrompt) {
        return new GptParams(userPrompt, "gpt-4.1-mini");
    }


    public static GptParams ofGood(String userPrompt) {
        return new GptParams(userPrompt, "gpt-4.1");
    }


}
