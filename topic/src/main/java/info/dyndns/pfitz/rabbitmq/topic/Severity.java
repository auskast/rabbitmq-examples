package info.dyndns.pfitz.rabbitmq.topic;

import org.apache.commons.lang3.StringUtils;

public enum Severity {
    info("I"),
    warn("W"),
    error("E");

    private String code;

    private Severity(String code) {
        this.code = code;
    }

    public String getPrintValue() {
        return StringUtils.upperCase(name());
    }

    public static Severity getSeverity(String code) {
        for (final Severity severity : values()) {
            if (StringUtils.equalsIgnoreCase(severity.code, code)) {
                return severity;
            }
        }

        return null;
    }
}
