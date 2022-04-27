package by.mark.editorconfig.wrapper;

final class IdeaWorkSpaceXmlSettings {

    static final String OPTIMIZE_ON_SAVE_OPTIONS = "OptimizeOnSaveOptions";
    static final String FORMAT_ON_SAVE_OPTIONS = "FormatOnSaveOptions";

    // @formatter:off
    static final String OPTIMIZE_IMPORTS =
            "<component name=\"OptimizeOnSaveOptions\">\n" +
                "<option name=\"myRunOnSave\" value=\"true\" />\n" +
            "</component>\n";

    static final String REFORMAT_CODE =
            "<component name=\"FormatOnSaveOptions\">\n" +
                "<option name=\"myRunOnSave\" value=\"true\" />\n" +
            "</component>\n";
    // @formatter:on

    private IdeaWorkSpaceXmlSettings() {
    }
}
