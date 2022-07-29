package uk.gov.hmcts.reform.next.hearing.date.updater;

import java.util.ArrayList;
import java.util.List;

public class CaseReferencesContainer {
    private final List<String> caseReferences = new ArrayList<>();

    public List<String> getCaseReferences() {
        return caseReferences;
    }

    public void addCaseReference(final String caseReference) {
        this.caseReferences.add(caseReference);
    }
}
