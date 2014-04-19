package info.dyndns.pfitz.rabbitmq.topic;

import org.apache.commons.lang3.StringUtils;

public enum Facility {
    cron,
    launchd,
    kern,
    rabbitmq;

    public String getPrintValue() {
        return name();
    }

    public static Facility getFacility(String str) {
        for (final Facility facility : values()) {
            if (StringUtils.equalsIgnoreCase(facility.name(), str)) {
                return facility;
            }
        }

        return null;
    }
}
