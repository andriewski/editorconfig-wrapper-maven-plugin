package by.mark.spotless.wrapper;

final class IdeaWorkSpaceXmlSettings {

    // @formatter:off
    static final String OPTIMIZE_IMPORTS =
            "<component name=\"OptimizeOnSaveOptions\">\n" +
                "<option name=\"myRunOnSave\" value=\"true\" />\n" +
            "</component>";

    static final String REFORMAT_CODE =
            "<component name=\"FormatOnSaveOptions\">\n" +
                "<option name=\"myRunOnSave\" value=\"true\" />\n" +
            "</component>";
    // @formatter:on

    private IdeaWorkSpaceXmlSettings() {
    }
}
