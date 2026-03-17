package tech.zhifu.app.myhub.feature.mixed.license

data class OpenSourceLicenseItem(
    val name: String,
    val license: String,
    val licenseText: String,
    val artifacts: List<String> = emptyList(),
    val notice: String? = null,
)

data class OpenSourceLicenseSection(
    val title: String,
    val items: List<OpenSourceLicenseItem>,
)

data class OpenSourceLicensesConfig(
    val sections: List<OpenSourceLicenseSection>,
)
