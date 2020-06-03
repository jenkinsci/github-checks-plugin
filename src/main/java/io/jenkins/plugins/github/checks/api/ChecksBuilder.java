package io.jenkins.plugins.github.checks.api;

public class ChecksBuilder {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName(String name) {
        return name;
    }
}
