package uk.gov.hmcts.reform.next.hearing.date.updater.befta;

import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.util.BeftaUtils;

import java.util.Locale;

public class DataSetupApp extends DataLoaderToDefinitionStore {

    private static final String PROD = CcdEnvironment.PROD.name();

    public DataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
    }

    public static void main(String[] args) throws Throwable {
        // if we are not running against PROD, i.e. during a HighLevelDataSetup run: then ok to proceed
        if (!PROD.toLowerCase(Locale.ENGLISH).equals(args[0])) {
            main(DataSetupApp.class, args);
        }
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure() {
        return true;
    }

    @Override
    public void createRoleAssignments() {
        // Do not create role assignments.
        BeftaUtils.defaultLog("Will NOT create role assignments!");
    }

}
