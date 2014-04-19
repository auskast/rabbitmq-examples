package info.dyndns.pfitz.rabbitmq.topic;

public class Binding {
    private final Facility facility;
    private final Severity severity;

    public Binding(Facility facility, Severity severity) {
        this.facility = facility;
        this.severity = severity;
    }

    public String getBinding() {
        final String facilityString = facility == null ? "*" : facility.name();
        final String severityString = severity == null ? "*" : severity.name();
        return facilityString + "." + severityString;
    }
}
