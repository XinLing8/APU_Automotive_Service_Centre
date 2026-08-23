package logic;

import java.time.LocalDate;

public enum Period {
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String displayName;

    Period(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDate getStartDate(LocalDate referenceDate) {
        LocalDate startDate;
        switch (this) {
            case MONTHLY:
                startDate = referenceDate.withDayOfMonth(1);
                break;
            case YEARLY:
                startDate = referenceDate.withDayOfYear(1);
                break;
            default:
                startDate = referenceDate;
        }
        System.out.println("[Period] getStartDate(" + this + ", " + referenceDate + ") = " + startDate);
        return startDate;
    }

    public LocalDate getEndDate(LocalDate referenceDate) {
        LocalDate endDate;
        switch (this) {
            case MONTHLY:
                endDate = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth());
                break;
            case YEARLY:
                endDate = referenceDate.withDayOfYear(referenceDate.lengthOfYear());
                break;
            default:
                endDate = referenceDate;
        }
        System.out.println("[Period] getEndDate(" + this + ", " + referenceDate + ") = " + endDate);
        return endDate;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
