package timeboard.core.model;

public enum VacationRequestStatus {
    PENDING("En attente"),
    ACCEPTED("Acceptée"),
    REJECTED("Refusée");

    public final String label;

    private VacationRequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
