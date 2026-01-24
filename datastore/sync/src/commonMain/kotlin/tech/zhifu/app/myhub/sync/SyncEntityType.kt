package tech.zhifu.app.myhub.sync

enum class SyncEntityType(val value: String) {
    User("user"),
    UserPreferences("user_preferences"),
    Collection("collection"),
    Card("card"),
    Tag("tag"),
    Template("template"),
}
