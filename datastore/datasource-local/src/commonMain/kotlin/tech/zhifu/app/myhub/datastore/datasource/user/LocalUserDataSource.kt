package tech.zhifu.app.myhub.datastore.datasource.user

import tech.zhifu.app.myhub.datastore.operations.user.UserOperations
import tech.zhifu.app.myhub.datastore.operations.user.UserPreferencesOperations

interface LocalUserDataSource :
    UserOperations,
    UserPreferencesOperations
