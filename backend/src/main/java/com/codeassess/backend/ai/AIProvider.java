package com.codeassess.backend.ai;

public interface AIProvider {

    String reviewCode(String prompt);

    String getProviderName();
}
