package com.devassist.service.ai;

public interface LLMProvider {

    String getName();

    String reviewCode(String code);
}